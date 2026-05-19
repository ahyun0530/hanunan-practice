package com.hanun.hanunan.domain.safety.repository;

import com.hanun.hanunan.domain.safety.entity.SafetyFacility;
import com.hanun.hanunan.domain.safety.entity.SafetyFacilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SafetyFacilityRepository extends JpaRepository<SafetyFacility, Long> {

    // CSV 적재 여부 확인 (타입별로 체크)
    long countByType(SafetyFacilityType type);

    // 바운딩박스 + 타입 필터 조회
    @Query("""
            SELECT f FROM SafetyFacility f
            WHERE f.latitude  BETWEEN :swLat AND :neLat
              AND f.longitude BETWEEN :swLng AND :neLng
              AND f.type IN :types
            """)
    List<SafetyFacility> findByBoundsAndTypes(
            @Param("swLat") double swLat,
            @Param("neLat") double neLat,
            @Param("swLng") double swLng,
            @Param("neLng") double neLng,
            @Param("types") List<SafetyFacilityType> types
    );
}
