package com.karoldm.k_board_api.services;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Configuration;
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

    public FileStorageService(AmazonS3 amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public String upload(MultipartFile fileUpload) {
        if(fileUpload == null || fileUpload.isEmpty()) {
            return "";
        }

        try {
            String key = UUID.randomUUID().toString();

            String[] splittedFileName = fileUpload.getOriginalFilename().split("\\.");
            if(splittedFileName.length == 0) {
                throw new RuntimeException("Invalid file name or extension");
            }
            String fileKey = splittedFileName[0]+"-"+key+"."+splittedFileName[1];

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(fileUpload.getSize());
            objectMetadata.setContentType(fileUpload.getContentType());

            PutObjectRequest request = new PutObjectRequest(
                    "k-board-images",
                    fileKey,
                    fileUpload.getInputStream(),
                    objectMetadata);

            amazonS3Client.putObject(request);

            return bucketUrl + fileKey;
        } catch (IOException ex) {
            throw new RuntimeException("Error uploading file", ex);
        }
    }
}
