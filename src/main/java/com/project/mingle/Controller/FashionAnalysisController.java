package com.project.mingle.Controller;

import com.project.mingle.Service.VisionApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fashion")
@RequiredArgsConstructor
public class FashionAnalysisController {

    private final VisionApiService visionApiService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeFashionItems(@RequestParam("image") MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일이 비어있습니다."));
        }

        try {
            // VisionApiService의 Product Search 메서드를 호출합니다.
            List<VisionApiService.ProductSearchResult> detectedProducts = visionApiService.searchFashionProducts(imageFile);

            // 구조화된 상품 데이터를 JSON 형태로 프론트엔드에 반환합니다.
            // 예: { "products": [ { "productName": "...", "score": 0.89 }, ... ] }
            return ResponseEntity.ok(Map.of("products", detectedProducts));

        } catch (Exception e) {
            // 오류 발생 시, 전체 오류 내용을 서버 콘솔에 출력하여 디버깅을 돕습니다.
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "AI 분석 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
