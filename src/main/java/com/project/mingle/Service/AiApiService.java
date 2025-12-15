package com.project.mingle.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class AiApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    // --- ★★★ 수정된 부분 ★★★ ---
    // 1단계에서 얻은 ngrok 공개 주소와 Flask의 엔드포인트 경로('/analyze')를 조합합니다.
    private final String FLASK_API_URL = "https://8a2b-123-45-67-89.ngrok-free.app/analyze";

    public Map<String, Object> getAnimalFaceAnalysis(MultipartFile imageFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageFile.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Flask API에 POST 요청 보내고 응답 받기
        ResponseEntity<Map> response = restTemplate.postForEntity(FLASK_API_URL, requestEntity, Map.class);

        return response.getBody();
    }
}
