package ifortex.shuman.uladzislau.authservice.paramedic.service;

import ifortex.shuman.uladzislau.authservice.paramedic.dto.DocumentDataDto;
import ifortex.shuman.uladzislau.authservice.paramedic.model.DocumentType;

public interface KycDocumentService {
    DocumentDataDto getDocument(Long applicationId, DocumentType documentType);
}