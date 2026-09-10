package com.example.spring_test_meteo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Dto per l'input del client con validazione
public record MeteoRequestDto (
        @NotBlank(message = "Il nome della città è obbligatorio")
        @Size(min=2, max=50, message= "La città deve avere tra i 2 e i 50 caratteri")
        String citta
){}
