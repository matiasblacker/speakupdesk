package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

public class AlumnoService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "http://localhost:8080/api/alumnos";

    //crear alumnos

    //listar alumnos

    //editar alumnos

    //eliminar alumnos

}
