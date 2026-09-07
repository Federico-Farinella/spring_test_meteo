package com.example.spring_test_meteo.controller;

import com.example.spring_test_meteo.dto.MeteoRequestDto;
import com.example.spring_test_meteo.dto.MeteoResponseDto;
import com.example.spring_test_meteo.service.MeteoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
public class MeteoController {
    private final MeteoService service;

    // Constructor Injection automatica in Spring Boot 3
    public MeteoController(MeteoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MeteoResponseDto> requestWeather (@Valid @RequestBody MeteoRequestDto meteoRequestDto) {
        MeteoResponseDto response = service.elaborateWeather(meteoRequestDto.città());
        return ResponseEntity.ok(response);
    }
}
