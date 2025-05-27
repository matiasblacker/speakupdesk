package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.dto.response.ProfesorSimpleResponseDTO;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.model.Materia;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MateriaService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "http://localhost:8080/api/materias";

    //crear materias

    public static CompletableFuture<Materia> save(Materia materia){
        return CompletableFuture.supplyAsync(() ->{
            try{
                Map<String , Object> requestBody = new HashMap<>();
                requestBody.put("nombre", materia.getNombre());
                //el colegio lo toma desde el usuario
                String jsonbody = mapper.writeValueAsString(requestBody);
                RequestBody body = RequestBody.create(jsonbody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try(Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()&& response.body() != null){
                        return mapper.readValue(response.body().string(), Materia.class);
                    }else{
                        String errorBody = response.body().string();
                        throw new IOException("Error del servidor:" + errorBody);
                    }
                }

            } catch (Exception e) {
                throw new CompletionException("Error en la solicitud:" + e.getMessage(),e);
            }
        });
    }

    //actualizar materia
    public static CompletableFuture<Materia> update(Materia materia){
        return CompletableFuture.supplyAsync(() ->{
            try{
                Map<String , Object> requestBody = new HashMap<>();
                requestBody.put("nombre", materia.getNombre());
                //el colegio lo toma desde el usuario
                String jsonbody = mapper.writeValueAsString(requestBody);
                RequestBody body = RequestBody.create(jsonbody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL + "/" + materia.getId())
                        .put(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try(Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()&& response.body() != null){
                        return mapper.readValue(response.body().string(), Materia.class);
                    }else{
                        String errorBody = response.body() != null ? response.body().string() : "Sin respuesta";
                        throw new IOException("Error del servidor:" + response.code() + errorBody);
                    }
                }

            } catch (Exception e) {
                throw new CompletionException("Error en la solicitud:" + e.getMessage(),e);
            }
        });
    }

    //listar todas las materias
    public static CompletableFuture<List<Materia>> findAll(){
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try(Response response = client.newCall(request).execute()){
                if (!response.isSuccessful()){
                    throw  new IOException("Error HTTP: " + response.code());
                }
                String jsonResponse = response.body().string();
                return mapper.readValue(jsonResponse, new TypeReference<List<Materia>>() {});

            } catch (Exception e) {
                throw new CompletionException("Error al obtener materias", e);
            }
        });
    }
    // listar materias del colegio
    public static CompletableFuture<List<Materia>> findByColegioId(){
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(API_URL + "/colegio")  // Endpoint específico para cursos del colegio
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try(Response response = client.newCall(request).execute()){
                if (!response.isSuccessful()){
                    throw new IOException("Error HTTP: " + response.code());
                }
                String jsonResponse = response.body().string();
                return mapper.readValue(jsonResponse, new TypeReference<List<Materia>>() {});
            } catch (Exception e) {
                throw new CompletionException("Error al obtener materias del colegio", e);
            }
        });
    }

    //eliminar
    public static CompletableFuture<Void> delete(Long id){
        return CompletableFuture.supplyAsync(()-> {
            Request request = new Request.Builder()
                    .url(API_URL + "/"+ id)
                    .delete()
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error HTTP: " + response.code());
                }
                return null;
            } catch (IOException e) {
                throw new CompletionException("Error al eliminar materia", e);
            }
        });
    }

    //buscar usuarios profesores por curso
    public static CompletableFuture<List<ProfesorSimpleResponseDTO>>listarProfesoresDeLaMateria(Long materiaId){
        return CompletableFuture.supplyAsync(() ->{
            Request request = new Request.Builder()
                    .url(API_URL + "/"+ materiaId + "/profesores")
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try(Response response = client.newCall(request).execute()){
                if(!response.isSuccessful()){
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new IOException("Error HTTP: " + response.code() + " " + errorBody);
                }
                String jsonResponse = response.body().string();
                // Manejar posibles respuestas nulas o vacías
                if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                return mapper.readValue(jsonResponse, new TypeReference<List<ProfesorSimpleResponseDTO>>() {});

            } catch (Exception e) {
                //System.err.println("Error al obtener profesores del curso: " + e.getMessage());
                throw new CompletionException("Error al obtener profesores de la materia: " + e.getMessage(), e);
            }
        });
    }
}
