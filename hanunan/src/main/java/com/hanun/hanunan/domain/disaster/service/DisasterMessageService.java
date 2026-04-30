package com.hanun.hanunan.domain.disaster.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanun.hanunan.domain.disaster.dto.DisasterResponse;
import com.hanun.hanunan.domain.disaster.dto.GroqRequest;
import com.hanun.hanunan.domain.disaster.dto.GroqResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisasterMessageService {

    private static final Logger log = LoggerFactory.getLogger(DisasterMessageService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final GeocodingService geocodingService;
    private final ObjectMapper objectMapper;

    public DisasterResponse extractInfo(String disasterMessage) {
        log.info("입력 메시지: {}", disasterMessage);


        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String prompt = """
            다음 재난문자에서 시간과 장소를 추출하세요.
            현재 시각은 %s 입니다.
            
            규칙:
            - time: 시각 또는 날짜 표현 (예: 15:00, 오전 9시, 11월 15일)
              * "현재", "지금" 등의 표현이 있으면 현재 시각(%s)을 그대로 사용
            - location: 주소와 건물명/시설명을 최대한 상세하게 추출
              * 시/군/구 + 읍/면/동 + 번지까지 포함
              * 번지 없이 건물명, 공장명, 시설명이 있으면 반드시 포함 (예: 울주군 온산읍 에쓰오일 공장)
              * 번지와 건물명이 모두 있으면 둘 다 포함 (예: 오산시 누읍동 45-1 OO공장)
            - 반드시 JSON 형식으로만 응답
            - 값이 없으면 null
            
            출력 형식: {"time": "...", "location": "..."}
            
            재난문자: "%s"
        """.formatted(now, now, disasterMessage);


        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        GroqRequest request = new GroqRequest();
        request.setMessages(List.of(
                new GroqRequest.Message("user", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<GroqRequest> entity = new HttpEntity<>(request, headers);

        GroqResponse response = restTemplate.postForObject(
                apiUrl,
                entity,
                GroqResponse.class
        );

        String result = response.getChoices().get(0).getMessage().getContent();
        log.info("Groq 응답: {}", result);

        try {
            DisasterResponse disasterResponse = objectMapper.readValue(result, DisasterResponse.class);

            // 주소 → 좌표 변환
            if (disasterResponse.getLocation() != null) {
                double[] coords = geocodingService.getCoordinates(disasterResponse.getLocation());
                if (coords != null) {
                    disasterResponse.setLat(coords[0]);
                    disasterResponse.setLng(coords[1]);
                }
            }

            return disasterResponse;
        } catch (Exception e) {
            log.error("파싱 오류: {}", e.getMessage());
            return new DisasterResponse();
        }
    }
}