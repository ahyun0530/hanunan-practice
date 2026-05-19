package com.hanun.hanunan.domain.safety.entity;

public enum SafetyFacilityType {
    AED,        // 자동심장충격기 (외부 API)
    FIRE_WATER, // 소방용수시설 (CSV → DB)
    RESCUE_BOX, // 인명구조함   (CSV → DB)
    SHELTER     // 통합대피소   (외부 API, 추후 연동)
}
