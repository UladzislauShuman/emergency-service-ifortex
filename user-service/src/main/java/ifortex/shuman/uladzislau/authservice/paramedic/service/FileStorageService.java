package ifortex.shuman.uladzislau.authservice.paramedic.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {

  String save(MultipartFile file) throws IOException;

  byte[] retrieveAsBytes(String blobName);

  void delete(String blobName);
}