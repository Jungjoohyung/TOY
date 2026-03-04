package com.toy.api;

import com.toy.common.response.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.ok("서버 ON");
    }
}
