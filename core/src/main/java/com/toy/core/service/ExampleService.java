package com.toy.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExampleService {

    public String getMessage() {
        return "Hello from Core Service";
    }
}
