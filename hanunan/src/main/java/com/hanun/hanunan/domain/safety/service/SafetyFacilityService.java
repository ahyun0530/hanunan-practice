package com.hanun.hanunan.domain.safety.service;

import com.hanun.hanunan.domain.safety.dto.SafetyFacilityDto;
import com.hanun.hanunan.domain.safety.entity.SafetyFacility;
import com.hanun.hanunan.domain.safety.entity.SafetyFacilityType;
import com.hanun.hanunan.domain.safety.repository.SafetyFacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyFacilityService {

    private final SafetyFacilityRepository safetyFacilityRepository;
    private final AedApiService aedApiService;
    private final ShelterApiService shelterApiService;

    /**
     * 바운딩박스 범위 내 안전시설 조회
     *
     * @param swLat  남서쪽 위도
     * @param swLng  남서쪽 경도
     * @param neLat  북동쪽 위도
     * @param neLng  북동쪽 경도
     * @param types  조회할 타입 목록 (null 또는 빈 리스트면 전체 조회)
     */
    public List<SafetyFacilityDto> getFacilities(
            double swLat, double swLng,
            double neLat, double neLng,
            List<String> types) {

        boolean allTypes = (types == null || types.isEmpty());
        List<SafetyFacilityDto> results = new ArrayList<>();

        // ── AED (외부 API) ──────────────────────────────────────────
        if (allTypes || types.contains("AED")) {
            results.addAll(aedApiService.fetchAed(swLat, swLng, neLat, neLng));
        }

        // ── DB 조회 타입 목록 구성 ──────────────────────────────────
        List<SafetyFacilityType> dbTypes = new ArrayList<>();
        if (allTypes || types.contains("FIRE_WATER")) dbTypes.add(SafetyFacilityType.FIRE_WATER);
        if (allTypes || types.contains("RESCUE_BOX"))  dbTypes.add(SafetyFacilityType.RESCUE_BOX);
        // SHELTER 외부 API 연동 후 이 목록에서 제거 예정

        // ── DB 조회 (소방용수시설 · 인명구조함) ────────────────────────
        if (!dbTypes.isEmpty()) {
            List<SafetyFacility> dbFacilities = safetyFacilityRepository
                    .findByBoundsAndTypes(swLat, neLat, swLng, neLng, dbTypes);

            dbFacilities.stream()
                    .map(this::toDto)
                    .forEach(results::add);
        }

        // ── 통합대피소 (SHELTER) ─────────────────────────────────────
        if (allTypes || types.contains("SHELTER")) {
            results.addAll(shelterApiService.fetchShelter(swLat, swLng, neLat, neLng));
        }

        return results;
    }

    private SafetyFacilityDto toDto(SafetyFacility f) {
        return SafetyFacilityDto.builder()
                .id(String.valueOf(f.getId()))
                .type(f.getType().name())
                .name(f.getName())
                .address(f.getAddress())
                .detail(null)
                .latitude(f.getLatitude())
                .longitude(f.getLongitude())
                .phone(null)
                .build();
    }
}
