package com.hanun.hanunan.domain.report.repository;

import com.hanun.hanunan.domain.report.entity.CitizenReportLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CitizenReportLikeRepository extends JpaRepository<CitizenReportLike, Long> {
    Optional<CitizenReportLike> findByMemberIdAndReportId(Long memberId, Long reportId);
    boolean existsByMemberIdAndReportId(Long memberId, Long reportId);
    List<CitizenReportLike> findAllByMemberId(Long memberId);
}
