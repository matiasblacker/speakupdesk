package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.model.Alumno;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AlumnoService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "http://localhost:8080/api/alumnos";

    //crear alumnos
    public static CompletableFuture<Alumno> save(Alumno alumno){
        return CompletableFuture.supplyAsync(() ->{
            try{
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("nombre", alumno.getNombre());
                requestBody.put("apellido", alumno.getApellido());
                requestBody.put("numeroLista", alumno.getNumeroLista());
                requestBody.put("cursoId", alumno.getCursoId());

                String jsonBody = mapper.writeValueAsString(requestBody);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        return mapper.readValue(response.body().string(), Alumno.class);
                    } else {
                        String errorBody = response.body().string();
                        throw new IOException("Error del servidor: " + errorBody);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error en creación de alumno: " + e.getMessage());
                throw new CompletionException("Error en la solicitud: " + e.getMessage(), e);
            }
        });
    }
    //listar todos los alumnos
    public static CompletableFuture<List<Alumno>> findAll(){
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_URL) // Nuevo endpoint en el backend
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error HTTP: " + response.code());
                }
                String jsonResponse = response.body().string();
                return mapper.readValue(jsonResponse, new TypeReference<List<Alumno>>() {});
            } catch (IOException e) {
                throw new CompletionException("Error al obtener alumnos", e);
            }
        });
    }
    //listar los alumnos del curso seleccionado
    public static CompletableFuture<List<Alumno>> findByCursoId(Long cursoId) {
        return CompletableFuture.supplyAsync(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_URL + "/curso/" + cursoId)
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error HTTP: " + response.code());
                }

                String jsonResponse = response.body().string();
                List<Alumno> alumnos = mapper.readValue(jsonResponse, new TypeReference<List<Alumno>>() {});

                return alumnos;
            } catch (IOException e) {
                throw new CompletionException("Error al obtener alumnos", e);
            }
        });
    }
    //editar alumnos
    public static CompletableFuture<Alumno> update(Alumno alumno) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convertir usuario a JSON
                String jsonBody = mapper.writeValueAsString(crearRequestBody(alumno));
                RequestBody body = RequestBody.create(
                        jsonBody,
                        MediaType.parse("application/json")
                );
                Request request = new Request.Builder()
                        .url(API_URL + "/" + alumno.getId())
                        .put(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        return mapper.readValue(response.body().string(), Alumno.class);
                    } else {
                        String errorBody = response.body().string();
                        throw new IOException("Error al actualizar: " + errorBody);
                    }
                }
            } catch (IOException e) {
                throw new CompletionException("Error en la solicitud", e);
            }
        });
    }

    private static Map<String, Object> crearRequestBody(Alumno alumno) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", alumno.getNombre());
        requestBody.put("apellido", alumno.getApellido());
        requestBody.put("numeroLista", alumno.getNumeroLista());
        requestBody.put("cursoId", alumno.getCursoId());

        return requestBody;
    }
    //eliminar alumnos
    public static CompletableFuture<Void> delete(Long id) {
        return CompletableFuture.supplyAsync(() -> {
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
                throw new CompletionException("Error al eliminar alumno", e);
            }
        });
    }
}
