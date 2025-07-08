package com.programthis.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String fullName; // Puede ser null si el user-service no lo tiene
    // No incluyas el email ni la contraseña a menos que sea estrictamente necesario para este servicio.
}