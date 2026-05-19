package com.hanun.hanunan.domain.safety.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "safety_facility", indexes = {
        @Index(name = "idx_safety_type", columnList = "type"),
        @Index(name = "idx_safety_location", columnList = "latitude, longitude")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SafetyFacilityType type;

    @Column(nullable = false)
    private String name;       // 안전센터명 / 설치상세장소

    private String address;    // 도로명주소 (없으면 지번주소)

    private Double latitude;
    private Double longitude;
}
