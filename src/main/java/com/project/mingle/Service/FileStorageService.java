package com.project.mingle.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // 이미지가 저장될 서버의 폴더 경로 (프로젝트 루트의 'uploads' 폴더)
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            // 'uploads' 폴더가 없으면 자동으로 생성합니다.
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // 파일 이름이 중복되지 않도록 고유한 이름을 생성합니다.
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try {
            // 파일을 서버에 저장합니다.
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            // 데이터베이스에 저장할 URL 경로를 반환합니다. (예: /uploads/filename.jpg)
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("파일을 저장할 수 없습니다. 파일명: " + fileName, ex);
        }
    }

    // --- ★★★ 신규 추가된 메서드 ★★★ ---
    public void deleteFile(String filePath) {
        // 데이터베이스에 저장된 경로(예: /uploads/filename.jpg)가 유효한지 확인합니다.
        if (filePath == null || filePath.isBlank() || !filePath.startsWith("/uploads/")) {
            return;
        }

        try {
            // URL 경로에서 실제 파일 이름만 추출합니다.
            String fileName = filePath.substring("/uploads/".length());
            Path fileToDelete = this.fileStorageLocation.resolve(fileName).normalize();

            // 서버에서 파일을 삭제합니다.
            Files.deleteIfExists(fileToDelete);
        } catch (IOException ex) {
            // 파일 삭제 실패 시, 에러 로그를 남기지만 전체 작업을 중단시키지는 않습니다.
            System.err.println("파일을 삭제할 수 없습니다: " + filePath + ". 원인: " + ex.getMessage());
        }
    }
}