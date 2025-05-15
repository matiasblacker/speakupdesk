package com.mpm.speakupdesk.offline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOffline {
    private String email;
    private String encryptedPassword; // Contraseña cifrada
    private String token;
    private LocalDateTime lastLogin;
}
