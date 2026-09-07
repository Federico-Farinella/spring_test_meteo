package com.example.spring_test_meteo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;


//Configuriamo centralmente l'HTTPClient per impostare i limiti di tempo
@Configuration
public class ClientConfig {
    @Value("${meteo.api.base-url}")
    private String baseUrl;



    @Bean
    public RestClient meteoRestClient() {
        //Timeout connessione 2s, lettura (5s)
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(5);

        return RestClient.builder().requestFactory(factory)
                .baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient geoRestClient(@Value("${geo.api.base-url}") String baseUrlGeo) {
        return RestClient.builder().baseUrl(baseUrlGeo).build();
    }

}