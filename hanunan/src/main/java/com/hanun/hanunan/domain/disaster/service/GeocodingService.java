package com.hanun.hanunan.domain.disaster.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class GeocodingService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public double[] getCoordinates(String address) {
        try {
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query="
                    + java.net.URLEncoder.encode(address, "UTF-8");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode documents = root.path("documents");

            if (documents.isArray() && documents.size() > 0) {
                double lat = documents.get(0).path("y").asDouble();
                double lng = documents.get(0).path("x").asDouble();
                log.info("좌표 변환 성공: {} → lat={}, lng={}", address, lat, lng);
                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            log.error("좌표 변환 실패: {}", e.getMessage());
        }
        return null;
    }
}