package com.hanun.hanunan.domain.safety.controller;

import com.hanun.hanunan.domain.safety.dto.SafetyFacilityDto;
import com.hanun.hanunan.domain.safety.service.SafetyFacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class SafetyFacilityController {

    private final SafetyFacilityService safetyFacilityService;

    /**
     * 지도 바운딩박스 기준 안전시설 조회
     *
     * GET /api/safety/facilities?swLat=37.5&swLng=126.9&neLat=37.6&neLng=127.0
     * GET /api/safety/facilities?swLat=37.5&swLng=126.9&neLat=37.6&neLng=127.0&types=AED
     * GET /api/safety/facilities?swLat=37.5&swLng=126.9&neLat=37.6&neLng=127.0&types=AED,RESCUE_BOX
     */
    @GetMapping("/facilities")
    public ResponseEntity<List<SafetyFacilityDto>> getFacilities(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            @RequestParam(required = false) List<String> types
    ) {
        List<SafetyFacilityDto> result = safetyFacilityService.getFacilities(
                swLat, swLng, neLat, neLng, types);
        return ResponseEntity.ok(result);
    }
}
