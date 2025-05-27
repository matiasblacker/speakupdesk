package com.mpm.speakupdesk.service;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class PasswordResetTokenService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "http://localhost:8080/api/auth";

    public static CompletableFuture<Void> sendPasswordResetCode(String email) {
        return CompletableFuture.runAsync(() -> {
            try {
                RequestBody body = new FormBody.Builder()
                        .add("email", email)
                        .build();

                Request request = new Request.Builder()
                        .url(BASE_URL + "/forgot-password")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Request failed with code: " + response.code());
                    }
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }
    public static CompletableFuture<String> validateCodeAndResetPassword(String email, String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RequestBody body = new FormBody.Builder()
                        .add("email", email)
                        .add("code", code)
                        .build();

                Request request = new Request.Builder()
                        .url(BASE_URL + "/reset-password")
                        .post(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Request failed with code: " + response.code());
                    }
                    return response.body().string();
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }
}

