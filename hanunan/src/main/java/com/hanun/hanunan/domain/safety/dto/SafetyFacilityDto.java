package com.hanun.hanunan.domain.safety.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SafetyFacilityDto {

    private String id;         // 고유 식별자
    private String type;       // AED / FIRE_WATER / RESCUE_BOX / SHELTER
    private String name;       // 시설명 (관리기관명, 안전센터명, 설치상세장소 등)
    private String address;    // 전체 주소
    private String detail;     // 세부 설치 위치 (AED 설치위치 등)
    private Double latitude;
    private Double longitude;
    private String phone;      // 관리자 연락처 (있는 경우)
}
