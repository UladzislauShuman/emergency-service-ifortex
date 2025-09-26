package ifortex.shuman.uladzislau.authservice.paramedic.controller;

import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.DocumentDataDto;
import ifortex.shuman.uladzislau.authservice.paramedic.model.DocumentType;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/admin/kyc/applications/{applicationId}/documents")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('" + Permissions.ADMIN_KYC_READ + "')")
public class AdminKycDocumentController {

  private final KycDocumentService kycDocumentService;

  @GetMapping("/identity")
  public ResponseEntity<byte[]> getIdentityDocument(@PathVariable Long applicationId) {
    return buildResponse(kycDocumentService.getDocument(applicationId, DocumentType.IDENTITY));
  }

  @GetMapping("/selfie")
  public ResponseEntity<byte[]> getSelfieWithDocument(@PathVariable Long applicationId) {
    return buildResponse(kycDocumentService.getDocument(applicationId, DocumentType.SELFIE));
  }

  @GetMapping("/certificate")
  public ResponseEntity<byte[]> getMedicalCertificate(@PathVariable Long applicationId) {
    return buildResponse(kycDocumentService.getDocument(applicationId, DocumentType.CERTIFICATE));
  }

  private ResponseEntity<byte[]> buildResponse(DocumentDataDto documentData) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, documentData.contentType())
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=\"" + documentData.filename() + "\"")
        .body(documentData.content());
  }
}