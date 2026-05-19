package com.hanun.hanunan.domain.safety.service;

import com.hanun.hanunan.domain.safety.entity.SafetyFacility;
import com.hanun.hanunan.domain.safety.entity.SafetyFacilityType;
import com.hanun.hanunan.domain.safety.repository.SafetyFacilityRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyCsvLoaderService {

    private final SafetyFacilityRepository safetyFacilityRepository;

    @PostConstruct
    public void loadAll() {
        loadFireWater();
        loadRescueBox();
    }

    // ── 소방용수시설 ──────────────────────────────────────────────────
    private void loadFireWater() {
        if (safetyFacilityRepository.countByType(SafetyFacilityType.FIRE_WATER) > 0) {
            log.info("[CSV] 소방용수시설 데이터 이미 존재 → 적재 스킵");
            return;
        }
        log.info("[CSV] 소방용수시설 적재 시작");

        List<SafetyFacility> list = parseCsv(
                "data/fire_water.csv",
                row -> {
                    String lat  = col(row, "위도");
                    String lng  = col(row, "경도");
                    if (lat.isBlank() || lng.isBlank()) return null;

                    double latitude  = Double.parseDouble(lat);
                    double longitude = Double.parseDouble(lng);
                    if (latitude == 0 || longitude == 0) return null;

                    String address = col(row, "소재지도로명주소");
                    if (address.isBlank()) address = col(row, "소재지지번주소");

                    return SafetyFacility.builder()
                            .type(SafetyFacilityType.FIRE_WATER)
                            .name(blankOr(col(row, "안전센터명"), col(row, "시설번호")))
                            .address(address)
                            .latitude(latitude)
                            .longitude(longitude)
                            .build();
                }
        );

        safetyFacilityRepository.saveAll(list);
        log.info("[CSV] 소방용수시설 {}건 적재 완료", list.size());
    }

    // ── 인명구조함 ──────────────────────────────────────────────────
    private void loadRescueBox() {
        if (safetyFacilityRepository.countByType(SafetyFacilityType.RESCUE_BOX) > 0) {
            log.info("[CSV] 인명구조함 데이터 이미 존재 → 적재 스킵");
            return;
        }
        log.info("[CSV] 인명구조함 적재 시작");

        List<SafetyFacility> list = parseCsv(
                "data/rescue_box.csv",
                row -> {
                    // 위험표지판 제외
                    if (!"인명구조함".equals(col(row, "구분명"))) return null;

                    String lat = col(row, "위도");
                    String lng = col(row, "경도");
                    if (lat.isBlank() || lng.isBlank()) return null;

                    double latitude  = Double.parseDouble(lat);
                    double longitude = Double.parseDouble(lng);
                    if (latitude == 0 || longitude == 0) return null;

                    String address = col(row, "소재지도로명주소");
                    if (address.isBlank()) address = col(row, "소재지지번주소");

                    return SafetyFacility.builder()
                            .type(SafetyFacilityType.RESCUE_BOX)
                            .name(blankOr(col(row, "설치상세장소"), "인명구조함"))
                            .address(address)
                            .latitude(latitude)
                            .longitude(longitude)
                            .build();
                }
        );

        safetyFacilityRepository.saveAll(list);
        log.info("[CSV] 인명구조함 {}건 적재 완료", list.size());
    }

    // ── 공통 CSV 파서 (탭 구분자) ────────────────────────────────────
    private List<SafetyFacility> parseCsv(String classpathPath, RowMapper mapper) {
        List<SafetyFacility> results = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(classpathPath);
            // 공공데이터포털 파일은 EUC-KR인 경우가 많음
            Charset charset = Charset.forName("EUC-KR");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), charset))) {

                String headerLine = reader.readLine();
                if (headerLine == null) return results;

                // BOM 제거
                if (headerLine.startsWith("﻿")) {
                    headerLine = headerLine.substring(1);
                }

                // 구분자 자동 감지 (탭 vs 쉼표)
                String delimiter = headerLine.contains("\t") ? "\t" : ",";
                log.info("[CSV] {} 구분자: '{}', 헤더 앞 50자: [{}]",
                        classpathPath, delimiter.equals("\t") ? "TAB" : "COMMA",
                        headerLine.substring(0, Math.min(50, headerLine.length())));

                String[] headers = headerLine.split(delimiter, -1);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] cols = line.split(delimiter, -1);
                    CsvRow row = new CsvRow(headers, cols);
                    try {
                        SafetyFacility facility = mapper.map(row);
                        if (facility != null) results.add(facility);
                    } catch (NumberFormatException e) {
                        // 위경도 파싱 실패 행 스킵
                    }
                }
            }
        } catch (Exception e) {
            log.error("[CSV] {} 파싱 실패: {}", classpathPath, e.getMessage());
        }
        return results;
    }

    // ── 내부 유틸 ────────────────────────────────────────────────────
    private String col(CsvRow row, String header) {
        return row.get(header);
    }

    private String blankOr(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    @FunctionalInterface
    private interface RowMapper {
        SafetyFacility map(CsvRow row);
    }

    private record CsvRow(String[] headers, String[] cols) {
        String get(String header) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals(header)) {
                    return (i < cols.length) ? cols[i].trim() : "";
                }
            }
            return "";
        }
    }
}
