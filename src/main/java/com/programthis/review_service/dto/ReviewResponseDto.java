package com.programthis.review_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.programthis.review_service.entity.Review;

import java.time.LocalDateTime;

// Este DTO representa la reseña ENRIQUECIDA con información del usuario
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Long productId;
    private Long userId;
    private Integer rating;
    private String title;
    private String comment;
    private LocalDateTime reviewDate;

    // Campos del usuario que hizo la reseña (obtenidos del user-service)
    private String username; // Nombre de usuario
    private String fullName; // Nombre completo (puede ser null si el user-service no lo proporciona)

    // Constructor que toma un Review y datos de UserDto para construir el DTO de respuesta
    public ReviewResponseDto(Review review, UserDto userDto) {
        this.id = review.getId();
        this.productId = review.getProductId();
        this.userId = review.getUserId();
        this.rating = review.getRating();
        this.title = review.getTitle();
        this.comment = review.getComment();
        this.reviewDate = review.getReviewDate();
        
        // Asignar datos del usuario si UserDto no es null
        if (userDto != null) {
            this.username = userDto.getUsername();
            this.fullName = userDto.getFullName();
        } else {
            // Si el usuario no se encuentra, podemos dejar estos campos como null o usar un default
            this.username = "Usuario Desconocido"; 
            this.fullName = "N/A";
        }
    }
}