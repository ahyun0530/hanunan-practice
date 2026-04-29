package com.hanun.hanunan.domain.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisasterResponse {
    private String time;
    private String location;
    private Double lat;
    private Double lng;
}