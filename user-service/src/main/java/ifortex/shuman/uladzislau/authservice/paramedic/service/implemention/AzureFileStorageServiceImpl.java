package ifortex.shuman.uladzislau.authservice.paramedic.service.implemention;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import ifortex.shuman.uladzislau.authservice.paramedic.exception.FileNotFoundInStorageException;
import ifortex.shuman.uladzislau.authservice.paramedic.exception.StorageException;
import ifortex.shuman.uladzislau.authservice.paramedic.service.FileStorageService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class AzureFileStorageServiceImpl implements FileStorageService {

  private final BlobContainerClient containerClient;

  @Override
  public String save(MultipartFile file) {
    return Optional.ofNullable(file)
        .filter(f -> !f.isEmpty())
        .map(this::uploadValidatedFile)
        .orElseThrow(() -> new IllegalArgumentException("File must not be null or empty."));
  }

  @Override
  public byte[] retrieveAsBytes(String blobName) {
    String validBlobName = Optional.ofNullable(blobName)
        .filter(StringUtils::hasText)
        .orElseThrow(() -> new IllegalArgumentException("Blob name must not be blank."));

    log.debug("Attempting to download blob '{}'", validBlobName);
    BlobClient blobClient = getBlobClient(validBlobName);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      blobClient.downloadStream(outputStream);
      log.info("Successfully downloaded blob '{}'.", blobClient.getBlobName());
      return outputStream.toByteArray();
    } catch (BlobStorageException e) {
      log.error("Storage error while downloading blob '{}'", validBlobName, e);
      throw mapBlobStorageException(e, validBlobName);
    } catch (IOException e) {
      log.error("IO error during blob download for '{}'", blobClient.getBlobName(), e);
      throw new StorageException("Failed to process downloaded file stream", e);
    }
  }

  @Override
  public void delete(String blobName) {
    Optional.ofNullable(blobName)
        .filter(StringUtils::hasText)
        .ifPresent(this::deleteBlob);
  }

  private String uploadValidatedFile(MultipartFile file) {
    String blobName = createUniqueBlobName(file.getOriginalFilename());
    BlobClient blobClient = getBlobClient(blobName);
    try {
      blobClient.upload(file.getInputStream(), file.getSize(), true);
      log.info("File '{}' uploaded successfully as '{}'", file.getOriginalFilename(), blobName);
      return blobName;
    } catch (IOException | BlobStorageException e) {
      log.error("Failed to upload file '{}' as blob '{}'", file.getOriginalFilename(), blobName, e);
      throw new StorageException("Failed to upload file", e);
    }
  }

  private void deleteBlob(String blobName) {
    try {
      getBlobClient(blobName).deleteIfExists();
      log.info("Successfully deleted blob '{}'", blobName);
    } catch (BlobStorageException e) {
      log.error("Failed to delete blob '{}' from Azure storage. This operation will not be retried.", blobName, e);
    }
  }

  private StorageException mapBlobStorageException(BlobStorageException e, String blobName) {
    if (e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND) {
      return new FileNotFoundInStorageException("File not found in storage: " + blobName, e);
    }
    return new StorageException("Failed to download file from storage", e);
  }

  private BlobClient getBlobClient(String blobName) {
    return containerClient.getBlobClient(blobName);
  }

  private String createUniqueBlobName(String originalFilename) {
    String sanitizedFilename = StringUtils.cleanPath(Objects.toString(originalFilename, ""));
    String extension = StringUtils.getFilenameExtension(sanitizedFilename);
    String baseName = UUID.randomUUID().toString();
    return StringUtils.hasText(extension) ? baseName + "." + extension : baseName;
  }
}