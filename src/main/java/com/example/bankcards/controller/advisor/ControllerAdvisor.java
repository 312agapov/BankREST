package com.example.bankcards.controller.advisor;

import com.example.bankcards.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handle(Exception e) {
        return ResponseEntity.badRequest()
                .body(ErrorDto.builder()
                        .message(e.getMessage())
                        .build());
    }
}