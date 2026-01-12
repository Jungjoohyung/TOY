package com.toy.core.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ExampleService.class)
class ExampleServiceTest {

    @Autowired
    private ExampleService exampleService;

    @Test
    void getMessage() {
        String message = exampleService.getMessage();
        assertThat(message).isEqualTo("Hello from Core Service");
    }
}
