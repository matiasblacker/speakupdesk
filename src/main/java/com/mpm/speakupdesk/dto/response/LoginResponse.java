package com.mpm.speakupdesk.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mpm.speakupdesk.model.Rol;
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private String token;
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Rol rol;
    private Long colegioId;
    private String nombreColegio;
    private boolean enabled;
    public LoginResponse() {
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Long getColegioId() {
        return colegioId;
    }

    public void setColegioId(Long colegioId) {
        this.colegioId = colegioId;
    }

    public String getNombreColegio() {
        return nombreColegio;
    }
    public void setNombreColegio(String nombreColegio) {
        this.nombreColegio = nombreColegio;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
