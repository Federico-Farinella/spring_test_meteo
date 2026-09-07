package com.example.spring_test_meteo.service;

import com.example.spring_test_meteo.dto.MeteoResponseDto;
import com.example.spring_test_meteo.exception.ExternalServiceException;
import com.example.spring_test_meteo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class MeteoService {
    private final RestClient meteoRestClient;
    private final RestClient geoRestClient;

    public MeteoService(RestClient meteoRestClient, RestClient geoRestClient) {
        this.meteoRestClient = meteoRestClient;
        this.geoRestClient = geoRestClient;
    }

    public MeteoResponseDto elaborateWeather(String city) {
        try {
            // Chiamata HTTP esterna usando la fluent API di RestClient
            Map<?, ?> externAnswer = meteoRestClient.get()
                    .uri("/previsioni?citta={citta}", city)
                    .retrieve()
                    // Gestione errore se la città non esiste sul server esterno
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode() == HttpStatus.NOT_FOUND)
                            throw new ResourceNotFoundException("City " + city + " not found");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("The external weather provider is temporarily offline");
                    })
                    .body(Map.class);
            // Estraiamo i dati ipotetici dalla mappa ricevuta
            double temp = (Double) externAnswer.get("temp");
            String condition = (String) externAnswer.get("condition");
            // Logica di business interna
            String consiglio = temp < 15 ? "Portati una giacca pesante!" : "Meteo perfetto per una passeggiata.";

            return new MeteoResponseDto(city, temp, condition, consiglio);
        } catch (ResourceAccessException e) {
            // Questo blocco cattura il timeout configurato nella factory!
            throw new ExternalServiceException("La richiesta al servizio meteo è andata in TIMEOUT.");
        }

    }


}