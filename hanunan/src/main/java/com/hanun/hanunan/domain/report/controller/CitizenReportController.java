package com.hanun.hanunan.domain.report.controller;

import com.hanun.hanunan.domain.report.dto.CitizenReportCreateRequest;
import com.hanun.hanunan.domain.report.dto.CitizenReportResponse;
import com.hanun.hanunan.domain.report.dto.CitizenReportUpdateRequest;
import com.hanun.hanunan.domain.report.service.CitizenReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class CitizenReportController {
    private final CitizenReportService citizenReportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CitizenReportResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute CitizenReportCreateRequest request
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        CitizenReportResponse response = CitizenReportResponse.from(
                citizenReportService.create(userDetails.getUsername(), request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CitizenReportResponse> findAll() {
        return citizenReportService.findAll().stream()
                .map(CitizenReportResponse::from)
                .toList();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CitizenReportResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CitizenReportUpdateRequest request
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        CitizenReportResponse response = CitizenReportResponse.from(
                citizenReportService.update(userDetails.getUsername(), id, request)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        citizenReportService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        boolean liked = citizenReportService.toggleLike(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/{id}/flag")
    public ResponseEntity<CitizenReportResponse> flag(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        CitizenReportResponse response = CitizenReportResponse.from(
                citizenReportService.flag(id)
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
}
