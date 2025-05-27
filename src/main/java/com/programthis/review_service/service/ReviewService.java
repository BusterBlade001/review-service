package com.programthis.review_service.service;

import com.programthis.review_service.entity.Review;
import com.programthis.review_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service // Marca esta clase como un componente de servicio de Spring
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // Método para crear una nueva reseña
    public Review createReview(Review review) {
        // Aquí puedes añadir validaciones adicionales antes de guardar
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            // Lanzar una excepción o manejar el error si la puntuación no es válida
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        // Establece la fecha de la reseña antes de guardar
        review.setReviewDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // Método para obtener todas las reseñas de un producto específico
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    // Método para obtener todas las reseñas hechas por un usuario específico
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    // Método para obtener una reseña por su ID
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
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
                    // No actualizar reviewDate ni productId/userId normalmente
                    return reviewRepository.save(review);
                })
                .orElseThrow(() -> new RuntimeException("Review not found with id " + id));
    }
}