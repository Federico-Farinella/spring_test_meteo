package com.example.spring_test_meteo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenMeteoResponse(@JsonProperty("current_weather") CurrentWeather currentWeather) {
    public record CurrentWeather(double temperature, @JsonProperty("weathercode") int weathercode) {}
}
