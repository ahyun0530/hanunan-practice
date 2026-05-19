package com.hanun.hanunan.domain.safety.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AedApiResponse {

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("numOfRows")
    private int numOfRows;

    @JsonProperty("pageNo")
    private int pageNo;

    @JsonProperty("body")
    private List<AedItem> body;

    @Getter
    @NoArgsConstructor
    public static class AedItem {

        @JsonProperty("SN")
        private Long sn;

        @JsonProperty("MNG_INST_NM")
        private String mngInstNm;      // 관리기관명

        @JsonProperty("ADDR")
        private String addr;           // 전체 주소

        @JsonProperty("INSTL_PSTN")
        private String instlPstn;      // 설치 위치 (예: 1층 로비)

        @JsonProperty("LAT")
        private Double lat;

        @JsonProperty("LOT")
        private Double lot;

        @JsonProperty("MNGR_TELNO")
        private String mngrTelno;      // 관리자 연락처

        @JsonProperty("MKR_NM")
        private String mkrNm;          // 제조사명

        @JsonProperty("MDL_NM")
        private String mdlNm;          // 모델명
    }
}
