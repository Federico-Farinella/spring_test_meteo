package com.example.spring_test_meteo.dto;

import jakarta.validation.constraints.NotBlank;

public record MeteoResponseDto (
        String citta,
        double temperatura,
        String condizione,
        String consiglio
) {}
