package com.hanun.hanunan.domain.safety.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ShelterApiResponse {

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("numOfRows")
    private int numOfRows;

    @JsonProperty("pageNo")
    private int pageNo;

    @JsonProperty("body")
    private List<ShelterItem> body;

    @Getter
    @NoArgsConstructor
    public static class ShelterItem {

        @JsonProperty("MNG_SN")
        private String mngSn;           // 관리일련번호

        @JsonProperty("REARE_NM")
        private String reareNm;         // 시설명

        @JsonProperty("RONA_DADDR")
        private String ronaDaddr;       // 도로명주소

        @JsonProperty("SHLT_SE_NM")
        private String shltSeNm;        // 쉼터구분명 (예: "한파쉼터,무더위쉼터")

        @JsonProperty("SHLT_SE_CD")
        private String shltSeCd;        // 쉼터구분코드

        @JsonProperty("LAT")
        private Double lat;

        @JsonProperty("LOT")
        private Double lot;
    }
}
