package com.toy.api; // 패키지명 확인!

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController {

    @GetMapping("/")
    public String healthCheck() {

        


        return "서버 ON";
    }
}