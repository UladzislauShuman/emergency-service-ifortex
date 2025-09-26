package ifortex.shuman.uladzislau.authservice.paramedic.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import ifortex.shuman.uladzislau.authservice.paramedic.properties.AzureStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AzureStorageConfig {

    private final AzureStorageProperties azureStorageProperties;

    @Bean
    public BlobContainerClient blobContainerClient() {
        if (!StringUtils.hasText(azureStorageProperties.getConnectionString()) ||
            !StringUtils.hasText(azureStorageProperties.getContainerName())) {
            log.error("Azure Storage connection string or container name is not configured.");
            throw new IllegalStateException("Azure Storage is not configured. Application cannot start.");
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(azureStorageProperties.getConnectionString())
                .buildClient();
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
                azureStorageProperties.getContainerName());

            if (!containerClient.exists()) {
                containerClient.create();
                log.info("Blob container '{}' created.", azureStorageProperties.getContainerName());
            } else {
                log.info("Connected to existing blob container '{}'.", azureStorageProperties.getContainerName());
            }
            return containerClient;
        } catch (Exception e) {
            log.error("Failed to initialize Azure Blob Storage client.", e);
            throw new IllegalStateException("Failed to initialize Azure Blob Storage client.", e);
        }
    }
}