package com.programthis.review_service.service;

import com.programthis.review_service.entity.Review;
import com.programthis.review_service.repository.ReviewRepository;
import com.programthis.review_service.client.UserServiceClient; // ¡NUEVA ADICIÓN!
import com.programthis.review_service.dto.ReviewResponseDto; // ¡NUEVA ADICIÓN!
import com.programthis.review_service.dto.UserDto; // ¡NUEVA ADICIÓN!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // ¡NUEVA ADICIÓN!

@Service // Marca esta clase como un componente de servicio de Spring
public class ReviewService {

    private final ReviewRepository reviewRepository; // Hacerlo final
    private final UserServiceClient userServiceClient; // ¡NUEVA ADICIÓN! Hacerlo final

    // ¡MODIFICACIÓN CLAVE! Constructor para inyectar UserServiceClient
    @Autowired
    public ReviewService(ReviewRepository reviewRepository, UserServiceClient userServiceClient) {
        this.reviewRepository = reviewRepository;
        this.userServiceClient = userServiceClient; // ¡NUEVA ADICIÓN!
    }

    // Método para crear una nueva reseña
    public Review createReview(Review review) {
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        review.setReviewDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // Método para obtener todas las reseñas de un producto específico, enriquecidas con datos de usuario
    public List<ReviewResponseDto> getReviewsByProductId(Long productId) { // ¡MODIFICACIÓN CLAVE del tipo de retorno!
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::enrichReviewWithUserData) // ¡NUEVA ADICIÓN! Enriquecer cada reseña
                .collect(Collectors.toList());
    }

    // Método para obtener todas las reseñas hechas por un usuario específico, enriquecidas con datos de usuario
    public List<ReviewResponseDto> getReviewsByUserId(Long userId) { // ¡MODIFICACIÓN CLAVE del tipo de retorno!
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::enrichReviewWithUserData) // ¡NUEVA ADICIÓN! Enriquecer cada reseña
                .collect(Collectors.toList());
    }

    // Método para obtener una reseña por su ID, enriquecida con datos de usuario
    public Optional<ReviewResponseDto> getReviewById(Long id) { // ¡MODIFICACIÓN CLAVE del tipo de retorno!
        return reviewRepository.findById(id)
                .map(this::enrichReviewWithUserData); // ¡NUEVA ADICIÓN! Enriquecer la reseña si está presente
    }

    // Método auxiliar para enriquecer una Review con datos de UserDto
    private ReviewResponseDto enrichReviewWithUserData(Review review) { // ¡NUEVA ADICIÓN!
        Optional<UserDto> userDtoOptional = userServiceClient.getUserById(review.getUserId());
        UserDto userDto = userDtoOptional.orElse(null); // Si el usuario no se encuentra, pasamos null

        return new ReviewResponseDto(review, userDto);
    }

    // Método para eliminar una reseña
    public boolean deleteReview(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Opcional: para actualizar una reseña (si se permite)
    public Review updateReview(Long id, Review updatedReview) {
        return reviewRepository.findById(id)
                .map(review -> {
                    review.setRating(updatedReview.getRating());
                    review.setComment(updatedReview.getComment());
                    review.setTitle(updatedReview.getTitle());
                    return reviewRepository.save(review);
                })
                .orElseThrow(() -> new RuntimeException("Review not found with id " + id));
    }
}