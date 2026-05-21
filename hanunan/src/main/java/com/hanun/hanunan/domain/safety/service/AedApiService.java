package com.hanun.hanunan.domain.safety.service;

import com.hanun.hanunan.domain.safety.dto.AedApiResponse;
import com.hanun.hanunan.domain.safety.dto.SafetyFacilityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AedApiService {

    private static final int NUM_OF_ROWS = 100;

    @Value("${safety.aed.key}")
    private String aedApiKey;

    @Value("${safety.aed.url}")
    private String aedApiUrl;

    private final RestTemplate restTemplate;

    public List<SafetyFacilityDto> fetchAed(double swLat, double swLng, double neLat, double neLng) {
        List<SafetyFacilityDto> results = new ArrayList<>();

        try {
            // 1페이지 먼저 조회해서 totalCount 확인
            AedApiResponse first = callApi(swLat, swLng, neLat, neLng, 1);
            if (first == null || first.getBody() == null) return results;

            addAll(results, first.getBody());

            int totalCount = first.getTotalCount();
            int totalPages = (int) Math.ceil((double) totalCount / NUM_OF_ROWS);

            // 나머지 페이지 순차 조회
            for (int page = 2; page <= totalPages; page++) {
                AedApiResponse response = callApi(swLat, swLng, neLat, neLng, page);
                if (response == null || response.getBody() == null) break;
                addAll(results, response.getBody());
            }

            log.info("[AED API] 바운딩박스 조회 완료 - {}건", results.size());
        } catch (Exception e) {
            log.error("[AED API] 호출 실패: {}", e.getMessage());
        }

        return results;
    }

    private AedApiResponse callApi(double swLat, double swLng, double neLat, double neLng, int pageNo) {
        String url = UriComponentsBuilder.fromHttpUrl(aedApiUrl)
                .queryParam("serviceKey", aedApiKey)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("pageNo", pageNo)
                .queryParam("returnType", "json")
                .queryParam("startLat", swLat)
                .queryParam("endLat", neLat)
                .queryParam("startLot", swLng)
                .queryParam("endLot", neLng)
                .build(true)
                .toUriString();

        return restTemplate.getForObject(url, AedApiResponse.class);
    }

    private void addAll(List<SafetyFacilityDto> results, List<AedApiResponse.AedItem> items) {
        for (AedApiResponse.AedItem item : items) {
            if (item.getLat() == null || item.getLot() == null) continue;
            results.add(SafetyFacilityDto.builder()
                    .id(item.getSn() != null ? String.valueOf(item.getSn()) : null)
                    .type("AED")
                    .name(item.getMngInstNm())
                    .address(item.getAddr())
                    .detail(item.getInstlPstn())
                    .latitude(item.getLat())
                    .longitude(item.getLot())
                    .phone(item.getMngrTelno())
                    .build());
        }
    }
}
