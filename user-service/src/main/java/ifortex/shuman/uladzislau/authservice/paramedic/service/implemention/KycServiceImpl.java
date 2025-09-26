package ifortex.shuman.uladzislau.authservice.paramedic.service.implemention;

import ifortex.shuman.uladzislau.authservice.dto.ActionResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.CreateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.CreateUserResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import ifortex.shuman.uladzislau.authservice.exception.EntityNotFoundException;
import ifortex.shuman.uladzislau.authservice.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.authservice.model.OtpType;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.*;
import ifortex.shuman.uladzislau.authservice.paramedic.exception.FileStorageOperationException;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplication;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplicationStatus;
import ifortex.shuman.uladzislau.authservice.paramedic.repository.ParamedicApplicationRepository;
import ifortex.shuman.uladzislau.authservice.paramedic.service.FileStorageService;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycService;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import ifortex.shuman.uladzislau.authservice.service.OtpService;
import ifortex.shuman.uladzislau.authservice.service.UserCreationService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements KycService {

  private final ParamedicApplicationRepository applicationRepository;
  private final FileStorageService fileStorageService;
  private final OtpService otpService;
  private final NotificationService notificationService;
  private final UserCreationService userCreationService;

  @Override
  @Transactional
  public MessageResponseDto submitApplication(KycSubmissionRequestDto request) {
    if (applicationRepository.existsByEmailAndStatus(request.getEmail(),
        ParamedicApplicationStatus.APPROVED)) {
      throw new ResourceConflictException("An approved application for this email already exists.");
    }
    applicationRepository.findByEmailAndStatusIn(request.getEmail(),
            Set.of(ParamedicApplicationStatus.PENDING_EMAIL_VERIFICATION,
                ParamedicApplicationStatus.PENDING_REVIEW))
        .ifPresent(app -> {
          throw new ResourceConflictException(
              "You already have an active application. Please check your email for a verification code or wait for admin review.");
        });

    ParamedicApplication newApplication = buildNewApplication(request);
    applicationRepository.save(newApplication);

    otpService.generateAndSendOtp(request.getEmail(), OtpType.EMAIL_VERIFICATION);

    return new MessageResponseDto(
        "Application submitted. Please check your email for a verification code.");
  }

  @Override
  @Transactional
  public MessageResponseDto verifyApplicationEmail(VerificationRequestDto request) {
    otpService.validateOtp(request.getEmail(), request.getOtpCode(), OtpType.EMAIL_VERIFICATION);

    ParamedicApplication application = applicationRepository
        .findByEmailAndStatus(request.getEmail(),
            ParamedicApplicationStatus.PENDING_EMAIL_VERIFICATION)
        .orElseThrow(
            () -> new EntityNotFoundException("Application not found or email already verified."));

    application.setStatus(ParamedicApplicationStatus.PENDING_REVIEW);
    applicationRepository.save(application);
    log.info("Email for paramedic application {} verified. Status changed to PENDING_REVIEW.",
        application.getId());
    return new MessageResponseDto(
        "Email verified successfully. Your application is now pending review.");
  }

  @Override
  public KycStatusResponseDto getKycStatus(String email) {
    return applicationRepository.findTopByEmailOrderBySubmittedAtDesc(email)
        .map(app -> KycStatusResponseDto.builder()
            .applicationStatus(app.getStatus())
            .rejectionReason(app.getRejectionReason())
            .build())
        .orElseThrow(() -> new EntityNotFoundException("No application found for this email."));
  }

  @Override
  public Page<KycApplicationDetailsDto> getPendingApplications(Pageable pageable) {
    return applicationRepository.findAllByStatus(ParamedicApplicationStatus.PENDING_REVIEW,
            pageable)
        .map(this::mapToApplicationDetailsDto);
  }

  @Override
  public KycApplicationDetailsDto getApplicationDetails(Long applicationId) {
    return applicationRepository.findById(applicationId)
        .map(this::mapToApplicationDetailsDto)
        .orElseThrow(
            () -> new EntityNotFoundException("Application not found with ID: " + applicationId));
  }

  @Override
  @Transactional
  public ActionResponseDto updateApplicationStatus(Long applicationId,
      UpdateKycStatusRequestDto request) {
    ParamedicApplication application = applicationRepository.findById(applicationId)
        .orElseThrow(
            () -> new EntityNotFoundException("Application not found with ID: " + applicationId));

    if (application.getStatus() != ParamedicApplicationStatus.PENDING_REVIEW) {
      throw new ResourceConflictException("This application has already been reviewed.");
    }

    String message;
    switch (request.getStatus()) {
      case APPROVED ->
        message = handleApproval(application, request.getApprovalData());
      case REJECTED ->
        message = handleRejection(application, request.getReason());
      default -> throw new IllegalStateException("Unexpected value: " + request.getStatus());
    }

    deleteKycDocuments(application);

    return ActionResponseDto.builder()
        .message(message)
        .needTokenRefresh(false)
        .build();
  }

  private String handleApproval(ParamedicApplication application,
      KycApprovalRequestDto approvalData) {
    if (approvalData == null || !StringUtils.hasText(approvalData.getWorkEmail())) {
      throw new IllegalArgumentException(
          "Approval data (workEmail) is mandatory when approving an application.");
    }

    CreateUserByAdminRequestDto createUserDto = new CreateUserByAdminRequestDto();
    createUserDto.setEmail(approvalData.getWorkEmail());
    createUserDto.setFullName(application.getFullName());
    createUserDto.setPhone(application.getPhone());

    CreateUserResponseDto creationResponse = userCreationService.createUser(createUserDto,
        UserRole.ROLE_PARAMEDIC);

    application.setStatus(ParamedicApplicationStatus.APPROVED);
    application.setReviewedAt(Instant.now());
    applicationRepository.save(application);

    notificationService.sendParamedicApprovalNotification(
        application.getEmail(),
        application.getFullName(),
        approvalData.getWorkEmail(),
        approvalData.getWorkEmailPassword()
    );
    notificationService.sendTemporaryPasswordEmail(
        creationResponse.getUser().getEmail(),
        creationResponse.getTemporaryPassword()
    );

    log.info("Application {} for {} approved. New user created with email {}",
        application.getId(), application.getEmail(), creationResponse.getUser().getEmail());

    return "Application approved. New user created with email: " + creationResponse.getUser()
        .getEmail();
  }

  private String handleRejection(ParamedicApplication application, String reason) {
    if (!StringUtils.hasText(reason)) {
      throw new IllegalArgumentException(
          "Rejection reason is mandatory when rejecting an application.");
    }

    application.setStatus(ParamedicApplicationStatus.REJECTED);
    application.setRejectionReason(reason);
    application.setReviewedAt(Instant.now());
    applicationRepository.save(application);

    notificationService.sendKycRejectionEmail(application.getEmail(), application.getFullName(),
        reason);
    log.info("Application {} for {} was rejected. Reason: {}", application.getId(),
        application.getEmail(), reason);

    return "Application has been rejected.";
  }

  private void deleteKycDocuments(ParamedicApplication application) {
    log.info("Deleting KYC documents for application {}", application.getId());
    try {
      fileStorageService.delete(application.getIdentityDocumentPath());
      fileStorageService.delete(application.getSelfieWithDocumentPath());
      fileStorageService.delete(application.getMedicalCertificatePath());
    } catch (Exception e) {
      log.error(
          "CRITICAL: Failed to delete KYC documents for application {}. Manual cleanup required.",
          application.getId(), e);
    }
  }

  private ParamedicApplication buildNewApplication(KycSubmissionRequestDto request) {
    try {
      String identityDocName = fileStorageService.save(request.getIdentityDocument());
      String selfieName = fileStorageService.save(request.getSelfieWithDocument());
      String certificateName = fileStorageService.save(request.getMedicalCertificate());

      return ParamedicApplication.builder()
          .fullName(request.getFullName())
          .email(request.getEmail())
          .phone(request.getPhone())
          .status(ParamedicApplicationStatus.PENDING_EMAIL_VERIFICATION)
          .identityDocumentPath(identityDocName)
          .selfieWithDocumentPath(selfieName)
          .medicalCertificatePath(certificateName)
          .submittedAt(Instant.now())
          .build();
    } catch (IOException e) {
      log.error("Failed to upload KYC documents for email {}", request.getEmail(), e);
      throw new FileStorageOperationException("Could not proc`ess file upload.", e);
    }
  }

  private KycApplicationDetailsDto mapToApplicationDetailsDto(ParamedicApplication app) {
    UserDto applicantData = UserDto.builder()
        .fullName(app.getFullName())
        .email(app.getEmail())
        .phone(app.getPhone())
        .build();

    return KycApplicationDetailsDto.builder()
        .applicationId(app.getId())
        .user(applicantData)
        .identityDocumentPath(app.getIdentityDocumentPath())
        .selfieWithDocumentPath(app.getSelfieWithDocumentPath())
        .medicalCertificatePath(app.getMedicalCertificatePath())
        .submittedAt(app.getSubmittedAt())
        .build();
  }
}