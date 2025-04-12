package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.model.Curso;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CursoService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String API_URL = "http://localhost:8080/api/cursos";

    //Crear cursos
    public static CompletableFuture<Curso> save(Curso curso){
        return CompletableFuture.supplyAsync(() ->{
            OkHttpClient client = new OkHttpClient();
            try{
                Map<String , Object> requestBody = new HashMap<>();
                requestBody.put("nombre", curso.getNombre());
                requestBody.put("colegio", curso.getColegioId());

                String jsonBody = mapper.writeValueAsString(requestBody);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();

                try(Response response = client.newCall(request).execute()){
                    if(response.isSuccessful()){
                        return mapper.readValue(response.body().string(), Curso.class);
                    }else{
                        String errorBody = response.body().string();
                        throw new IOException("Error del servidor:" + errorBody);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en la creacion del curso: " + e.getMessage());
                throw new CompletionException("Error en la solicitud:" + e.getMessage(),e);
            }
        });
    }

    //Actualizar curso
    public static CompletableFuture<Curso> update(Curso curso){
        return CompletableFuture.supplyAsync(() ->{
            OkHttpClient client = new OkHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            try{
                String jsonBody = mapper.writeValueAsString(crearRequestBody(curso));
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL + "/" + curso.getId())
                        .put(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();

                try(Response response = client.newCall(request).execute()){
                    if(response.isSuccessful()){
                        return mapper.readValue(response.body().string(), Curso.class);
                    }else{
                        String errorBody = response.body().string();
                        throw new IOException("Error del servidor:" + errorBody);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en la creacion del curso: " + e.getMessage());
                throw new CompletionException("Error en la solicitud:" + e.getMessage(),e);
            }
        });
    }

    private static Map<String, Object> crearRequestBody(Curso curso) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", curso.getNombre());
        requestBody.put("colegio", curso.getColegioId());
        return requestBody;
    }

    public static CompletableFuture<List<Curso>> findAll(){
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try(Response response = client.newCall(request).execute()){
                if (!response.isSuccessful()){
                    throw  new IOException("Error HTTP: " + response.code());
                }
                String jsonResponse = response.body().string();
                return mapper.readValue(jsonResponse, new TypeReference<List<Curso>>() {});

            } catch (Exception e) {
                throw new CompletionException("Error al obtener cursos", e);
            }
        });
    }
}
