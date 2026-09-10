package com.example.spring_test_meteo.service;

import com.example.spring_test_meteo.dto.GeocodingResponse;
import com.example.spring_test_meteo.dto.MeteoRequestDto;
import com.example.spring_test_meteo.dto.MeteoResponseDto;
import com.example.spring_test_meteo.dto.OpenMeteoResponse;
import com.example.spring_test_meteo.exception.ExternalServiceException;
import com.example.spring_test_meteo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

@Service
public class MeteoService {
    private final RestClient meteoRestClient;
    private final RestClient geoRestClient;

    public MeteoService(@Qualifier("meteoRestClient") RestClient meteoRestClient,
                        @Qualifier("geoRestClient") RestClient geoRestClient) {
        this.meteoRestClient = meteoRestClient;
        this.geoRestClient = geoRestClient;
    }

    public MeteoResponseDto elaborateWeather(MeteoRequestDto request) {
        GeocodingResponse.GeocodingResult coordinate = searchCoordinate(request.citta());
        return searchWeatherByCoordinate(coordinate);
    }

    public GeocodingResponse.GeocodingResult searchCoordinate(String city) {
        try {
            GeocodingResponse geoResponse = geoRestClient.get()
                    .uri("/search?name={city}", city)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("Il server geografico di geocoding non risponde.");
                    })
                    .body(GeocodingResponse.class);
            if (geoResponse == null || geoResponse.results() == null || geoResponse.results().isEmpty())
                throw new ResourceNotFoundException("La città '" + city + "' non esiste sulle mappe geografiche.");

            return geoResponse.results().get(0);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new ResourceNotFoundException("La città '" + city + "' non esiste sulle mappe geografiche.");
        }
    }

    public MeteoResponseDto searchWeatherByCoordinate(GeocodingResponse.GeocodingResult coordinate) {
        try {
            OpenMeteoResponse weatherResponse
                    = meteoRestClient.get()
                    .uri("/forecast?latitude={lat}&longitude={lon}&current_weather=true", coordinate.latitude(), coordinate.longitude())
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("Il server meteo di Open-Meteo è offline.");
                    })
                    .body(OpenMeteoResponse.class);

            if (weatherResponse == null || weatherResponse.currentWeather() == null)
                throw new ExternalServiceException("I dati meteo restituiti per questa coordinata sono incompleti.");

            double temp = weatherResponse.currentWeather().temperature();
            String consiglio = temp < 15 ? "Portati una giacca pesante!" : "Meteo perfetto per una passeggiata.";
            String formattedName =
                    capitalizeFirstLetter(coordinate.name()) + ", " + coordinate.country();
            return new MeteoResponseDto(formattedName, temp, "Codice WMO: " + weatherResponse.currentWeather().weathercode(), consiglio);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("TIMEOUT di rete durante il recupero dei dati meteo.");
        }
    }

    // Vecchio metodo modificata con le precedenti
    public MeteoResponseDto searchWeatherByCity2(String city) {
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
                    //.body(Map.class);
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});  // ← tipo sicuro

            // Estraiamo i dati ipotetici dalla mappa ricevuta

            //double temp = (Double) externAnswer.get("temp");
            //Così evito ClassCastException: funzionerebbe anche se il valore temp fosse 22 (Integer)
            double temp = ((Number) externAnswer.get("temp")).doubleValue();


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