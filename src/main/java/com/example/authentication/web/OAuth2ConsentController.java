package com.example.authentication.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class OAuth2ConsentController {

    @GetMapping("/consent")
    public ResponseEntity<Map<String, Object>> consent(@RequestParam String email) {
        return ResponseEntity.ok(Map.of(
                "email", email,
                "message", "동일 이메일의 기존 계정이 있습니다. 연결하려면 consent=link 로 다시 시도하세요.")
        );
    }
}


