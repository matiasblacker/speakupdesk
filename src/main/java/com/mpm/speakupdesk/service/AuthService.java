package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.dto.request.LoginRequest;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import okhttp3.*;

import java.io.IOException;

public class AuthService {
    private static LoginResponse usuarioLogueado;
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private static final String DB_PATH = "speakuplocal.db";

    public static boolean login(String email, String password) {
        try {
            LoginRequest request = new LoginRequest(email, password);
            String json = mapper.writeValueAsString(request); // Serialización dentro del try

            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            Request httpRequest = new Request.Builder()
                    .url(BASE_URL + "/login")
                    .post(body)
                    .build();
            try (Response response = client.newCall(httpRequest).execute()) {
                System.out.println("Código de respuesta: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string(); // ✅ Guardar la respuesta una sola vez
                    System.out.println("Respuesta del servidor: " + responseBody);

                    usuarioLogueado = mapper.readValue(responseBody, LoginResponse.class); // ✅ Usar la variable guardada
                    return true;
                }
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void logout() {
        usuarioLogueado = null;
    }
    // Obtener datos del usuario logueado
    public static LoginResponse getUsuarioLogueado() {
        return usuarioLogueado;
    }
    // Obtener token JWT
    public static String getToken() {
        return (usuarioLogueado != null) ? usuarioLogueado.getToken() : null;
    }

    //metodos offline

}
