package ifortex.shuman.uladzislau.authservice.paramedic.service.implemention;

import ifortex.shuman.uladzislau.authservice.exception.EntityNotFoundException;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.DocumentDataDto;
import ifortex.shuman.uladzislau.authservice.paramedic.model.DocumentType;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplication;
import ifortex.shuman.uladzislau.authservice.paramedic.repository.ParamedicApplicationRepository;
import ifortex.shuman.uladzislau.authservice.paramedic.service.FileStorageService;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycDocumentService;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycDocumentServiceImpl implements KycDocumentService {

  private final ParamedicApplicationRepository applicationRepository;
  private final FileStorageService fileStorageService;

  private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE_MAP = Map.of(
      "png", "image/png",
      "jpg", "image/jpeg",
      "jpeg", "image/jpeg",
      "pdf", "application/pdf"
  );
  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  private static final Map<DocumentType, Function<ParamedicApplication, String>>
      DOCUMENT_TYPE_TO_PATH_EXTRACTOR = Map.of(
      DocumentType.IDENTITY, ParamedicApplication::getIdentityDocumentPath,
      DocumentType.SELFIE, ParamedicApplication::getSelfieWithDocumentPath,
      DocumentType.CERTIFICATE, ParamedicApplication::getMedicalCertificatePath
  );

  @Override
  public DocumentDataDto getDocument(Long applicationId, DocumentType documentType) {
    log.info("Requesting document type {} for application ID: {}", documentType, applicationId);

    return applicationRepository.findById(applicationId)
        .map(application -> getBlobName(application, documentType))
        .filter(StringUtils::hasText)
        .map(this::buildDocumentData)
        .orElseThrow(() -> {
          log.warn("Application or associated document not found for ID: {} and type: {}",
              applicationId, documentType);
          return new EntityNotFoundException("Application or associated document not found.");
        });
  }

  private DocumentDataDto buildDocumentData(String blobName) {
    byte[] documentBytes = fileStorageService.retrieveAsBytes(blobName);
    String contentType = determineContentType(blobName);
    return new DocumentDataDto(documentBytes, blobName, contentType);
  }

  private String getBlobName(ParamedicApplication application, DocumentType documentType) {
    Function<ParamedicApplication, String> extractor = DOCUMENT_TYPE_TO_PATH_EXTRACTOR.get(documentType);
    if (extractor != null) {
      return extractor.apply(application);
    }
    log.warn("No path extractor defined for document type: {}", documentType);
    return null;
  }

  private String determineContentType(String blobName) {
    String extension = StringUtils.getFilenameExtension(
        Objects.requireNonNullElse(blobName, "").toLowerCase()
    );
    return EXTENSION_TO_CONTENT_TYPE_MAP.getOrDefault(extension, DEFAULT_CONTENT_TYPE);
  }
}