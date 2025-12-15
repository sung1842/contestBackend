package com.project.mingle.Controller;

import com.project.mingle.Service.AiIntegrationService;
import com.project.mingle.Service.GeminiStyleAnalysisService;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/analyze")
public class CelebrityAnalysisController {

    private final AiIntegrationService aiIntegrationService;

    @PostMapping("/celebrity")
    public ResponseEntity<?> analyzeCelebrityFace(@RequestParam("image") MultipartFile imageFile) {

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing File"));
        }

        try {
            // AiIntegrationService의 메서드를 호출하여 Flask API로 이미지를 전송합니다.
            Map<String, Object> aiResult = aiIntegrationService.getAiPrediction(imageFile); // 타입 변경
            return ResponseEntity.ok(aiResult);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "file error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            // WebClient 통신 오류 등
            return ResponseEntity.internalServerError().body(Map.of("error", "AI server error: " + e.getMessage()));
        }
    }

    // [NEW] 새로 추가하는 부분: 연예인 아이템 추천 API
    // 프론트엔드에서 { "name": "이민혁" } 형태로 요청을 보냅니다.
    @PostMapping("/recommend-items")
    public ResponseEntity<?> recommendFashionItems(@RequestBody Map<String, String> request) {
        String celebrityName = request.get("name");

        // 이름 유효성 검사
        if (celebrityName == null || celebrityName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "연예인 이름이 필요합니다."));
        }

        try {
            // 서비스 호출 (Gemini 구글 검색 기능 수행)
            String jsonResult = aiIntegrationService.getFashionRecommendations(celebrityName);

            // 결과 반환 (JSON 문자열 그대로 전송, 프론트에서 파싱)
            return ResponseEntity.ok(jsonResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "추천 시스템 오류: " + e.getMessage()));
        }
    }
}