package com.daurenassanbaev.amazons3service.service;

import com.daurenassanbaev.amazons3service.exceptions.FileNotFoundException;
import com.daurenassanbaev.amazons3service.exceptions.S3ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service implements FileServiceImpl {

    private final S3Client s3;

    @Value("${bucketName}")
    private String bucketName;

    @Value("${region}")
    private String region;

    @Override
    public String saveFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new S3ServiceException("File name cannot be null", null);
        }

        try {
            log.info("Saving file: {}", originalFileName);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(originalFileName)
                    .build();

            s3.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("File {} successfully saved", originalFileName);
            return getFileUrl(originalFileName);
        } catch (IOException e) {
            log.error("Error saving file to S3", e);
            throw new S3ServiceException("Error saving file to S3", e);
        } catch (S3Exception e) {
            log.error("S3 error occurred while saving file", e);
            throw new S3ServiceException("S3 error occurred while saving file", e);
        }
    }

    public String getFileUrl(String fileName) {
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        log.info("Generated file URL: {}", fileUrl);
        return fileUrl;
    }

    @Override
    public byte[] downloadFile(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        try {
            log.info("Downloading file: {}", fileName);
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);
            log.info("File {} successfully downloaded", fileName);
            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            log.error("Error downloading file from S3", e);
            throw new S3ServiceException("Error downloading file from S3", e);
        } catch (Exception e) {
            log.error("File not found: {}", fileName, e);
            throw new FileNotFoundException("File not found: " + fileName);
        }
    }

    @Override
    public String deleteFile(String fileName) {
        try {
            log.info("Deleting file: {}", fileName);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3.deleteObject(deleteObjectRequest);
            log.info("File {} successfully deleted", fileName);
            return "File deleted";
        } catch (S3Exception e) {
            log.error("Error deleting file from S3", e);
            throw new S3ServiceException("Error deleting file from S3", e);
        }
    }

    @Override
    public List<String> listAllFiles() {
        try {
            log.info("Listing all files in bucket: {}", bucketName);
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
            List<String> fileList = listObjectsV2Response.contents()
                    .stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
            log.info("Files listed: {}", fileList);
            return fileList;
        } catch (S3Exception e) {
            log.error("Error listing files from S3", e);
            throw new S3ServiceException("Error listing files from S3", e);
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        log.info("Multipart file {} converted to File object", file.getOriginalFilename());
        return convFile;
    }
}
