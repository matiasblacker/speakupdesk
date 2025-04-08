package com.mpm.speakupdesk.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.model.Colegio;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ColegioService {
    private static final String API_URL = "http://localhost:8080/api/colegios";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper(); // Jackson

    public static CompletableFuture<List<Colegio>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error HTTP: " + response.code());
                }

                String json = response.body().string();
                // Deserializar con Jackson
                return mapper.readValue(json, new TypeReference<List<Colegio>>() {});

            } catch (IOException e) {
                throw new CompletionException("Error al obtener intitutos", e);
            }
        });
    }
    //Crear un instituto
    public static CompletableFuture<Colegio> save(Colegio colegio) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Serializar el objeto Colegio a JSON
                String json = mapper.writeValueAsString(colegio);
                RequestBody body = RequestBody.create(
                        json,
                        MediaType.parse("application/json; charset=utf-8")
                );
                Request request = new Request.Builder()
                        .url(API_URL + "/nuevocolegio")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Error HTTP: " + response.code()
                                + " - " + response.message());
                    }
                    // Deserializar la respuesta
                    String responseJson = response.body().string();
                    return mapper.readValue(responseJson, Colegio.class);

                } catch (IOException e) {
                    throw new CompletionException("Error en la solicitud: " + e.getMessage(), e);
                }

            } catch (JsonProcessingException e) {
                throw new CompletionException("Error al serializar el colegio", e);
            }
        });
    }
}