package com.hanun.hanunan.domain.disaster.controller;

import com.hanun.hanunan.domain.disaster.dto.DisasterResponse;
import com.hanun.hanunan.domain.disaster.service.DisasterMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/disaster")
@RequiredArgsConstructor
public class DisasterController {

    private final DisasterMessageService disasterMessageService;

//    @PostMapping("/extract")
//    public ResponseEntity<DisasterResponse> extract(@RequestBody Map<String, String> body) {
//        String message = body.get("message");
//        DisasterResponse response = disasterMessageService.extractInfo(message);
//        return ResponseEntity.ok(response);
//    }
//}

    @PostMapping(value = "/extract", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DisasterResponse> extract(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        DisasterResponse response = disasterMessageService.extractInfo(message);
        return ResponseEntity.ok(response);
    }
}