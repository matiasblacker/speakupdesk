package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.model.Usuario;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class UsuarioService {
    private static final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "http://localhost:8080/api/usuarios";
    // Crear usuario
    public static CompletableFuture<Usuario> crearUsuario(Usuario usuario, List<Long> cursosIds, List<Long> materiasIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("nombre", usuario.getNombre());
                requestBody.put("apellido", usuario.getApellido());
                requestBody.put("email", usuario.getEmail());
                requestBody.put("password", usuario.getPassword());
                requestBody.put("rol", usuario.getRol());

                if (usuario.getColegioId() != null) {
                    requestBody.put("colegioId", usuario.getColegioId());
                }
                // Añadir los IDs de cursos si están disponibles
                if (cursosIds != null && !cursosIds.isEmpty()) {
                    requestBody.put("cursosIds", cursosIds);
                }
                //añadir los IDs de materias si estan disponibles
                if(materiasIds != null && !materiasIds.isEmpty()){
                    requestBody.put("materiasIds", materiasIds);
                }
                String jsonBody = mapper.writeValueAsString(requestBody);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        return mapper.readValue(response.body().string(), Usuario.class);
                    } else {
                        String errorBody = response.body().string();
                        throw new IOException("Error del servidor: " + errorBody);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error en creación de usuario: " + e.getMessage());
                throw new CompletionException("Error en la solicitud: " + e.getMessage(), e);
            }
        });
    }
    //otener usuarios por colegio para el usuario admin_colegio
    public static CompletableFuture<List<Usuario>> getUsuariosByColegio(int colegioId) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(API_URL + "/colegio/" + colegioId)
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error HTTP: " + response.code());
                }

                String jsonResponse = response.body().string();
                List<Usuario> usuarios = mapper.readValue(jsonResponse, new TypeReference<List<Usuario>>() {});

                return usuarios;
            } catch (IOException e) {
                throw new CompletionException("Error al obtener usuarios", e);
            }
        });
    }
    //actualizar usuario
    public static CompletableFuture<Usuario> update(Usuario usuario) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Log antes de crear el JSON
                System.out.println("Actualizando usuario ID: " + usuario.getId());
                System.out.println("¿Incluye contraseña? " + (usuario.getPassword() != null && !usuario.getPassword().isEmpty()));
                // Convertir usuario a JSON
                String jsonBody = mapper.writeValueAsString(crearRequestBody(usuario));
                RequestBody body = RequestBody.create(
                        jsonBody,
                        MediaType.parse("application/json")
                );
                Request request = new Request.Builder()
                        .url(API_URL + "/" + usuario.getId())
                        .put(body)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        return mapper.readValue(response.body().string(), Usuario.class);
                    } else {
                        String errorBody = response.body().string();
                        System.out.println(errorBody);
                        throw new IOException("Error al actualizar: " + errorBody);
                    }
                }
            } catch (IOException e) {
                throw new CompletionException("Error en la solicitud", e);
            }
        });
    }

    private static Map<String, Object> crearRequestBody(Usuario usuario) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nombre", usuario.getNombre());
        requestBody.put("apellido", usuario.getApellido());
        requestBody.put("email", usuario.getEmail());
        requestBody.put("enabled", usuario.isEnabled());
        // Solo incluir password si no está vacío
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            requestBody.put("password", usuario.getPassword());
        }
        if (usuario.getCursosIds() != null) {
            requestBody.put("cursosIds", usuario.getCursosIds());
        }

        if (usuario.getMateriasIds() != null) {
            requestBody.put("materiasIds", usuario.getMateriasIds());
        }
        return requestBody;
    }

    //buscar todos los usuarios
    public static CompletableFuture<List<Usuario>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(API_URL) // Nuevo endpoint en el backend
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error HTTP: " + response.code());
                }
                String jsonResponse = response.body().string();
                return mapper.readValue(jsonResponse, new TypeReference<List<Usuario>>() {});
            } catch (IOException e) {
                throw new CompletionException("Error al obtener usuarios", e);
            }
        });
    }

    public static CompletableFuture<Usuario> findById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(API_URL + "/" + id)
                    .addHeader("Authorization", "Bearer " + AuthService.getToken())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // Mapear directamente a Usuario (sin DTO intermedio)
                    return mapper.readValue(response.body().string(), Usuario.class);
                } else {
                    throw new IOException("Error al obtener usuario: " + response.code());
                }
            } catch (IOException e) {
                throw new CompletionException("Error en la solicitud", e);
            }
        });
    }
}