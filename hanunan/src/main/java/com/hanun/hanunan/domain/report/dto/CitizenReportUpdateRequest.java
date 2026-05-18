package com.hanun.hanunan.domain.report.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CitizenReportUpdateRequest {
    private String type;
    private String description;
}
