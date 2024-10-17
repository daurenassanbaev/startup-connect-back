package com.daurenassanbaev.amazons3service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileServiceImpl {

    String saveFile(MultipartFile file);
    byte[] downloadFile(String fileName);
    String deleteFile(String fileName);
    List<String> listAllFiles();
}