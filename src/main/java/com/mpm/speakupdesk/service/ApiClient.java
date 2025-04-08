package com.mpm.speakupdesk.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/";
    private static final OkHttpClient client = new OkHttpClient();

    public static Response post(String endpoint, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();
        return client.newCall(request).execute();
    }

    public static Response get(String endpoint, String token) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return client.newCall(request).execute();
    }
}
