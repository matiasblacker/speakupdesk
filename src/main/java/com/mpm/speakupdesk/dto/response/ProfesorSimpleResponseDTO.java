package com.mpm.speakupdesk.dto.response;


public class ProfesorSimpleResponseDTO {
    private Long id;
    private String nombreCompleto;
    private String email;

    public ProfesorSimpleResponseDTO() {
    }

    public ProfesorSimpleResponseDTO(Long id, String nombreCompleto, String email) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ProfesorSimpleResponseDTO{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

