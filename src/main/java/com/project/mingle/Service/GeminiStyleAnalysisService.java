package com.project.mingle.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiStyleAnalysisService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public GeminiStyleAnalysisService() {
        this.restTemplate = new RestTemplate();
    }

    public String analyzeFashionStyle(String celebrityName) {
        String prompt = String.format(
                "유명인 '%s'의 최근 패션 스타일을 분석해줘. 다음과 같은 정보를 통계 자료 형태로 정리해줘: " +
                        "1. 주로 선호하는 스타일 카테고리 (예: 미니멀, 캐주얼, 스트리트, 포멀)와 그 비율. " +
                        "2. 자주 사용하는 핵심 색상 팔레트와 그 비율. " +
                        "3. 가장 특징적인 패션 아이템 3가지 (예: 오버사이즈 재킷, 볼캡, 체인 목걸이). " +
                        "4. 모든 답변은 영어로 대답해줘" +
                        "답변은 분석 통계 데이터만 JSON 형식의 텍스트로 제공해줘. (마크다운 없이 순수 JSON만)",
                celebrityName
        );

        Map<String, Object> requestBody = Map.of(
                "contents", Collections.singletonList(
                        Map.of("parts", Collections.singletonList(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // v1beta와 gemini-2.5 모델 사용 (성공했던 설정)
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey.trim();
            System.out.println("Gemini 요청 시작 (Model: 2.5): " + celebrityName);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            String rawText = extractAnalysisText(response.getBody());
            // [중요] 여기서 JSON을 깨끗하게 청소합니다.
            return cleanJsonOutput(rawText);

        } catch (Exception e) {
            // ... 에러 처리 유지 ...
            System.err.println("============ Gemini API 호출 오류 ============");
            e.printStackTrace();
            // 프론트가 깨지지 않게 에러도 JSON 형식으로 반환
            return "{\"error\": \"AI 분석에 실패했습니다.\", \"details\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String cleanJsonOutput(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
        }
        text = text.trim();
        // 정규식으로 ```json 또는 ``` 로 시작하고 ``` 로 끝나는 부분에서 내용만 추출
        Pattern pattern = Pattern.compile("```(?:json)?(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // 마크다운이 없으면 그냥 반환 (이미 순수 JSON일 경우)
        return text;
    }

    private String extractAnalysisText(Map response) {
        try {
            if (response == null || !response.containsKey("candidates")) {
                return "{\"error\": \"Gemini 응답이 비어있습니다.\"}";
            }

            Object candidatesObj = response.get("candidates");
            if (candidatesObj instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) candidatesObj;
                if (!list.isEmpty()) {
                    Map candidate = (Map) list.get(0);
                    Map content = (Map) candidate.get("content");
                    Map part = (Map) ((java.util.List) content.get("parts")).get(0);
                    return (String) part.get("text");
                }
            }
            return "{\"error\": \"분석 결과 후보가 없습니다.\"}";

        } catch (Exception e) {
            System.err.println("응답 파싱 오류: " + e.getMessage());
            return "{\"error\": \"분석 결과를 추출하지 못했습니다.\"}";
        }
    }

    public String recommendItemsWithSearch(String celebrityName) {
        // 1. 프롬프트: 검색 증거를 요구하고, 구체적인 아이템을 지시
        String prompt = String.format(
                "지금부터 구글 검색 도구를 사용하여 2024년, 2025년 '%s'의 실제 공항 패션, 인스타그램 사복을 검색해줘. " +
                        "그가 실제로 착용해서 화제가 된 '구체적인 패션 아이템 3가지'를 찾아줘. (예: 단순히 '니트'가 아니라 '앙고라 니트', '바시티 재킷' 등 구체적으로)\n" +
                        "각 아이템에 대해 아래 JSON 형식으로 답변해줘. 설명에는 그가 언제/어디서 입었는지 내용을 포함해서 작성해.\n\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"item\": \"아이템 명칭 (예: 크롭 기장 바시티 재킷)\",\n" +
                        "    \"desc\": \"착용 정보 포함한 설명 (예: 24년 10월 공항패션에서 착용한...)\",\n" +
                        "    \"keyword\": \"쇼핑 검색 키워드 (예: 남자 크롭 바시티 재킷)\"\n" +
                        "  }\n" +
                        "]\n\n" +
                        "응답은 마크다운 없이 순수 JSON 텍스트만 줘. 한국어로 작성해.",
                celebrityName
        );

        // 2. 도구 설정 (Google Search Grounding)
        Map<String, Object> requestBody = Map.of(
                "contents", Collections.singletonList(
                        Map.of("parts", Collections.singletonList(
                                Map.of("text", prompt)
                        ))
                ),
                "tools", Collections.singletonList(
                        Map.of("googleSearch", Collections.emptyMap())
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Pro 모델이 검색 도구(Tool use)를 훨씬 더 잘 다룹니다.
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey.trim();

            System.out.println("Gemini 검색 요청 시작 (Pro): " + celebrityName);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // [디버깅] 실제 응답 내용을 서버 로그에 출력해서 확인
            System.out.println("Gemini 응답(Raw): " + response.getBody());

            String rawText = extractAnalysisText(response.getBody());

            // JSON 정제
            String cleanResult = cleanJsonOutput(rawText);
            System.out.println("Gemini 응답(Clean): " + cleanResult);

            return cleanResult;

        } catch (Exception e) {
            System.err.println("============ Gemini 검색 API 오류 ============");
            e.printStackTrace();
            // 오류 발생 시에도 JSON 형태로 에러 메시지 반환
            return String.format("[{\"item\": \"분석 실패\", \"desc\": \"서버 연결 상태를 확인해주세요.\", \"keyword\": \"%s 패션\"}]", celebrityName);
        }
    }
}