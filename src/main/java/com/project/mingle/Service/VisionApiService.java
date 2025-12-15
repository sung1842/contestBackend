package com.project.mingle.Service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisionApiService {

    private final ProductSearchClient productSearchClient;
    private final ImageAnnotatorClient imageAnnotatorClient;

    // API 응답을 담을 DTO (데이터 전송 객체)
    @Getter
    @RequiredArgsConstructor
    public static class ProductSearchResult {
        private final String productId;
        private final String productName;
        private final float score;
    }

    public VisionApiService() throws IOException {
        // --- 인증 정보 설정 (기존과 동일) ---
        String credentialsPath = ".credentials/google-vision-key.json";
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        // --- Product Search 클라이언트 초기화 ---
        ProductSearchSettings productSearchSettings = ProductSearchSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        this.productSearchClient = ProductSearchClient.create(productSearchSettings);

        // --- Image Annotator 클라이언트 초기화 ---
        ImageAnnotatorSettings imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        this.imageAnnotatorClient = ImageAnnotatorClient.create(imageAnnotatorSettings);

        System.out.println("✅ Google Vision API 클라이언트가 모두 초기화되었습니다.");
    }

    // --- Product Search를 호출하는 새로운 메서드 ---
    public List<ProductSearchResult> searchFashionProducts(MultipartFile file) throws IOException {

        ByteString imgBytes = ByteString.copyFrom(file.getBytes());
        Image img = Image.newBuilder().setContent(imgBytes).build();

        // --- 중요! ---
        // 아래 값들은 나중에 Google Cloud Console에서 생성한 후 실제 값으로 변경해야 합니다.
        String projectId = "your-gcp-project-id";      // 예: mingle-project-12345
        String location = "asia-east1";                // 예: 상품 세트를 만든 위치
        String productSetId = "your-product-set-id";   // 예: "sunglasses_set"

        ProductSetName productSetName = ProductSetName.of(projectId, location, productSetId);

        // Product Search 요청 생성
        ImageContext imageContext = ImageContext.newBuilder()
                .setProductSearchParams(
                        ProductSearchParams.newBuilder()
                                .setProductSet(productSetName.toString())
                ).build();

        Feature feature = Feature.newBuilder().setType(Feature.Type.PRODUCT_SEARCH).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(img)
                .setImageContext(imageContext)
                .build();

        // API 호출 및 결과 처리
        BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(List.of(request));
        AnnotateImageResponse res = response.getResponses(0);

        if (res.hasError()) {
            throw new RuntimeException("Google Vision API Error: " + res.getError().getMessage());
        }

        ProductSearchResults productSearchResults = res.getProductSearchResults();

        // 검색 결과를 우리가 만든 DTO 리스트로 변환하여 반환
        return productSearchResults.getResultsList().stream()
                .map(result -> new ProductSearchResult(
                        result.getProduct().getName(),
                        result.getProduct().getDisplayName(),
                        result.getScore()
                ))
                .collect(Collectors.toList());
    }
}
