package com.hanun.hanunan.domain.safety.service;

import com.hanun.hanunan.domain.safety.dto.SafetyFacilityDto;
import com.hanun.hanunan.domain.safety.dto.ShelterApiResponse;
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
public class ShelterApiService {

    private static final int NUM_OF_ROWS = 100;

    @Value("${safety.shelter.key}")
    private String shelterApiKey;

    @Value("${safety.shelter.url}")
    private String shelterApiUrl;

    private final RestTemplate restTemplate;

    public List<SafetyFacilityDto> fetchShelter(double swLat, double swLng, double neLat, double neLng) {
        List<SafetyFacilityDto> results = new ArrayList<>();

        try {
            // 1페이지 먼저 조회해서 totalCount 확인
            ShelterApiResponse first = callApi(swLat, swLng, neLat, neLng, 1);
            if (first == null || first.getBody() == null) return results;

            addAll(results, first.getBody());

            int totalCount = first.getTotalCount();
            int totalPages = (int) Math.ceil((double) totalCount / NUM_OF_ROWS);

            // 나머지 페이지 순차 조회
            for (int page = 2; page <= totalPages; page++) {
                ShelterApiResponse response = callApi(swLat, swLng, neLat, neLng, page);
                if (response == null || response.getBody() == null) break;
                addAll(results, response.getBody());
            }

            log.info("[SHELTER API] 바운딩박스 조회 완료 - {}건", results.size());
        } catch (Exception e) {
            log.error("[SHELTER API] 호출 실패: {}", e.getMessage());
        }

        return results;
    }

    private ShelterApiResponse callApi(double swLat, double swLng, double neLat, double neLng, int pageNo) {
        String url = UriComponentsBuilder.fromHttpUrl(shelterApiUrl)
                .queryParam("serviceKey", shelterApiKey)
                .queryParam("numRows", NUM_OF_ROWS)
                .queryParam("pageNo", pageNo)
                .queryParam("returnType", "json")
                .queryParam("startLat", swLat)
                .queryParam("endLat", neLat)
                .queryParam("startLot", swLng)
                .queryParam("endLot", neLng)
                .build(true)
                .toUriString();

        return restTemplate.getForObject(url, ShelterApiResponse.class);
    }

    private void addAll(List<SafetyFacilityDto> results, List<ShelterApiResponse.ShelterItem> items) {
        for (ShelterApiResponse.ShelterItem item : items) {
            if (item.getLat() == null || item.getLot() == null) continue;
            results.add(SafetyFacilityDto.builder()
                    .id(item.getMngSn())
                    .type("SHELTER")
                    .name(item.getReareNm())
                    .address(item.getRonaDaddr())
                    .detail(item.getShltSeNm())   // 예: "한파쉼터,무더위쉼터"
                    .latitude(item.getLat())
                    .longitude(item.getLot())
                    .phone(null)
                    .build());
        }
    }
}
