package com.project.mingle.Service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AiIntegrationService {
    private final WebClient webClient;
    private final GeminiStyleAnalysisService geminiService;

    public AiIntegrationService(WebClient webClient, GeminiStyleAnalysisService geminiService) {
        this.webClient = webClient;
        this.geminiService = geminiService;
    }

    public Map<String, Object> getAiPrediction(MultipartFile imageFile) throws IOException {

        // 1. Flask AI API 호출 (동기 블록)
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // [핵심 수정] ByteArrayResource를 익명 클래스로 확장하여 getFilename() 오버라이드
        // 이렇게 해야 WebClient가 multipart 전송 시 파일 이름을 누락하지 않아 Flask 500 오류를 방지합니다.
        builder.part("image", new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        // 원본 파일 이름이 있으면 사용하고, 없으면 기본값 설정
                        return imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename() : "upload.jpg";
                    }
                })
                .contentType(MediaType.IMAGE_JPEG); // Content-Type 명시

        Map<String, Object> flaskApiResponse = webClient.post()
                .uri("/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        // 2. Flask 응답에서 연예인 이름 추출 (안전한 파싱 로직)
        List<String> celebrityNames = new ArrayList<>();
        Object celebritiesObj = flaskApiResponse.get("celebrities");

        if (celebritiesObj instanceof List<?>) {
            List<?> list = (List<?>) celebritiesObj;
            for (Object item : list) {
                if (item instanceof String) {
                    // ["아이유", "제니"] 형태
                    celebrityNames.add((String) item);
                } else if (item instanceof Map) {
                    // [{"name": "아이유"}, ...] 형태
                    Map<?, ?> map = (Map<?, ?>) item;
                    Object nameVal = map.get("name"); // Flask 키 확인 필요 (name, label 등)
                    if (nameVal != null) {
                        celebrityNames.add(nameVal.toString());
                    } else if (!map.isEmpty()) {
                        // 키를 모를 경우 첫 번째 값을 사용
                        celebrityNames.add(map.values().iterator().next().toString());
                    }
                }
            }
        }

        if (celebrityNames.isEmpty()) {
            return flaskApiResponse; // 연예인 정보가 없으면 그대로 반환
        }

        // 3. Gemini 분석 Mono 생성 및 병렬 처리
        Mono<String> mono1 = createGeminiMono(celebrityNames, 0);
        Mono<String> mono2 = createGeminiMono(celebrityNames, 1);

        // Mono.zip을 사용하여 두 개의 비동기 작업을 병렬로 실행하고 결과를 합침
        List<Map<String, Object>> analysisResults = Mono.zip(mono1, mono2)
                .map(tuple -> {
                    List<Map<String, Object>> results = new ArrayList<>();

                    // 첫 번째 연예인 결과
                    results.add(Map.of(
                            "name", celebrityNames.get(0),
                            "style_analysis", tuple.getT1()
                    ));

                    // 두 번째 연예인 결과 (존재할 경우)
                    if (celebrityNames.size() > 1) {
                        results.add(Map.of(
                                "name", celebrityNames.get(1),
                                "style_analysis", tuple.getT2()
                        ));
                    }

                    return results;
                })
                .onErrorResume(throwable -> {
                    System.err.println("Gemini 분석 중 오류 발생: " + throwable.getMessage());
                    return Mono.just(Collections.singletonList(
                            Map.of("error", "패션 분석 서버 오류가 발생했습니다.")
                    ));
                })
                .block();

        // 4. 최종 결과 구조화
        flaskApiResponse.put("gemini_fashion_analysis", analysisResults);

        return flaskApiResponse;
    }

    // 특정 연예인 이름에 대해 Gemini 분석을 요청하는 Mono 생성
    private Mono<String> createGeminiMono(List<String> names, int index) {
        if (names.size() > index) {
            String name = names.get(index);
            return Mono.fromCallable(() -> geminiService.analyzeFashionStyle(name))
                    .onErrorReturn("{\"error\": \""+ name +"님의 분석에 실패했습니다.\"}");
        }
        // 연예인 정보가 부족할 경우
        return Mono.just("{\"name\":\"N/A\", \"style_analysis\":\"연예인 정보 부족\"}");
    }

    public String getFashionRecommendations(String celebrityName) {
        // GeminiService에 있는 검색 로직을 호출하여 결과를 반환
        return geminiService.recommendItemsWithSearch(celebrityName);
    }
}