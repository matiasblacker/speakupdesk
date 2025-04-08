package com.mpm.speakupdesk.model;

public enum Rol {
    ADMIN_GLOBAL, //Gestiona todos los colegios
    ADMIN_COLEGIO, //Gestiona solo su colegio designado
    PROFESOR //puede acceder a los cursos y alumnos asignados a su curso
}