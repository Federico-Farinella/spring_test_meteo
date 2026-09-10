package com.example.spring_test_meteo.dto;

import java.util.List;

public record GeocodingResponse(List<GeocodingResult> results) {
    public record GeocodingResult(String name, double latitude, double longitude, String country) {}
}
