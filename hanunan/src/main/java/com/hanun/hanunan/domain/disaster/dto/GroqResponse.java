package com.hanun.hanunan.domain.disaster.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroqResponse {

    private List<Choice> choices;

    @Data
    public static class Choice {

        private Message message;

        @Data
        public static class Message {
            private String content;
        }
    }
}