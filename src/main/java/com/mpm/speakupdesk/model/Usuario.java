package com.mpm.speakupdesk.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private Rol rol;
    private Long colegioId;;
    @JsonProperty("enabled")  // Mapea el campo "enabled" del JSON
    private boolean enabled;
    // Campo "estado" (no existe en el JSON, se calcula desde "enabled")
    @JsonIgnore  // No se serializa/deserializa desde/hacia JSON
    private String estado;

    public Usuario() {
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
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = Rol.valueOf(rol);
    }

    public Long getColegioId() {
        return colegioId;
    }

    public void setColegioId(Long colegioId) {
        this.colegioId = colegioId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEstado() {
        return this.enabled ?  "Activo" : "Inactivo";
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }


}
