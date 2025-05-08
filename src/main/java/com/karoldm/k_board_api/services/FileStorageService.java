package com.karoldm.k_board_api.services;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.karoldm.k_board_api.exceptions.AmazonS3Exception;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Service
public class FileStorageService {
    private final AmazonS3 amazonS3Client;

    @Value("${api.aws.bucket-url}")
    private String bucketUrl;

    final private String bucketName = "k-board-images";

    public FileStorageService(AmazonS3 amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public String uploadFile(MultipartFile fileUpload) {
        if (fileUpload == null || fileUpload.isEmpty()) {
            return "";
        }

        try {
            String originalFilename = fileUpload.getOriginalFilename();
            String extension = "";
            String fileName = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                int lastDotIndex = originalFilename.lastIndexOf('.');
                fileName = originalFilename.substring(0, lastDotIndex);
                extension = originalFilename.substring(lastDotIndex);
            }

            String fileKey = String.format("%s-%s%s",
                    fileName,
                    UUID.randomUUID(),
                    extension);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileUpload.getSize());
            metadata.setContentType(fileUpload.getContentType());

            amazonS3Client.putObject(
                    new PutObjectRequest(bucketName, fileKey, fileUpload.getInputStream(), metadata)
            );

            return bucketUrl + fileKey;
        } catch (IOException ex) {
            throw new AmazonS3Exception("Error uploading file: " + ex.getMessage());
        }
    }

    public void removeFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String fileKey = fileUrl.replace(bucketUrl, "");

            if (amazonS3Client.doesObjectExist(bucketName, fileKey)) {
                amazonS3Client.deleteObject(bucketName, fileKey);
            }
        } catch (Exception ex) {
            throw new AmazonS3Exception("Error deleting file: " + ex.getMessage());
        }
    }
}
