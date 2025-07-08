package com.programthis.review_service.service;

import com.programthis.review_service.entity.Review;
import com.programthis.review_service.repository.ReviewRepository;
import com.programthis.review_service.client.UserServiceClient; // ¡NUEVA ADICIÓN!
import com.programthis.review_service.dto.ReviewResponseDto; // ¡NUEVA ADICIÓN!
import com.programthis.review_service.dto.UserDto; // ¡NUEVA ADICIÓN!
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime; // ¡NUEVA ADICIÓN!
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock // ¡NUEVA ADICIÓN! Mock para UserServiceClient
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ReviewService reviewService;

    // Datos de prueba comunes
    private Review testReview;
    private UserDto testUserDto;
    private ReviewResponseDto testReviewResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks

        // Configuración de datos de prueba
        testReview = new Review(1L, 101L, 1L, 5, "Great Product", "Loved it!", LocalDateTime.now());
        testUserDto = new UserDto(1L, "testuser", "Test User Full Name");
        testReviewResponseDto = new ReviewResponseDto(testReview, testUserDto);

        // Comportamiento de mock básico para UserServiceClient
        when(userServiceClient.getUserById(anyLong())).thenReturn(Optional.of(testUserDto));
    }

    @Test
    void testCreateReview_ValidRating() {
        Review review = new Review();
        review.setRating(4);
        review.setComment("Buen producto");
        review.setProductId(1L); // Asegúrate de tener productId y userId si createReview los espera
        review.setUserId(1L);

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.createReview(review);
        assertNotNull(result.getReviewDate()); // Verifica que la fecha se establece
        assertEquals(4, result.getRating());
        assertEquals("Buen producto", result.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReview_InvalidRatingTooHigh() {
        Review review = new Review();
        review.setRating(6);
        review.setComment("Comentario");
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(review), "Rating must be between 1 and 5.");
    }

    @Test
    void testCreateReview_InvalidRatingTooLow() {
        Review review = new Review();
        review.setRating(0);
        review.setComment("Comentario");
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(review), "Rating must be between 1 and 5.");
    }

    @Test
    void testCreateReview_NullRating() {
        Review review = new Review();
        review.setRating(null);
        review.setComment("Comentario");
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(review), "Rating must be between 1 and 5.");
    }

    @Test
    void testCreateReview_NullComment() {
        Review review = new Review();
        review.setRating(4);
        review.setComment(null);
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(review), "Comment cannot be empty.");
    }

    @Test
    void testCreateReview_EmptyComment() {
        Review review = new Review();
        review.setRating(4);
        review.setComment("  ");
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(review), "Comment cannot be empty.");
    }

    @Test
    // ¡MODIFICACIÓN CLAVE! El test ahora espera List<ReviewResponseDto>
    void testGetReviewsByProductId() {
        Review review = Review.builder().id(1L).productId(101L).userId(1L).build();
        when(reviewRepository.findByProductId(101L)).thenReturn(Arrays.asList(review));

        // Cuando getReviewsByProductId llama a userServiceClient, mockeamos la respuesta del usuario
        when(userServiceClient.getUserById(1L)).thenReturn(Optional.of(new UserDto(1L, "user1", "User One")));

        List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(101L); // Espera ReviewResponseDto
        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        assertEquals(101L, reviews.get(0).getProductId());
        assertEquals("user1", reviews.get(0).getUsername()); // Verifica el campo enriquecido
        verify(reviewRepository, times(1)).findByProductId(101L);
        verify(userServiceClient, times(1)).getUserById(1L); // Verifica que se llamó a UserServiceClient
    }

    @Test
    // ¡MODIFICACIÓN CLAVE! El test ahora espera List<ReviewResponseDto>
    void testGetReviewsByUserId() {
        Review review = Review.builder().id(1L).productId(101L).userId(1L).build();
        when(reviewRepository.findByUserId(1L)).thenReturn(Arrays.asList(review));

        // Cuando getReviewsByUserId llama a userServiceClient, mockeamos la respuesta del usuario
        when(userServiceClient.getUserById(1L)).thenReturn(Optional.of(new UserDto(1L, "user1", "User One")));

        List<ReviewResponseDto> reviews = reviewService.getReviewsByUserId(1L); // Espera ReviewResponseDto
        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        assertEquals(1L, reviews.get(0).getUserId());
        assertEquals("user1", reviews.get(0).getUsername()); // Verifica el campo enriquecido
        verify(reviewRepository, times(1)).findByUserId(1L);
        verify(userServiceClient, times(1)).getUserById(1L);
    }

    @Test
    // ¡MODIFICACIÓN CLAVE! El test ahora espera Optional<ReviewResponseDto>
    void testGetReviewById() {
        Review review = Review.builder().id(1L).productId(101L).userId(1L).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // Cuando getReviewById llama a userServiceClient, mockeamos la respuesta del usuario
        when(userServiceClient.getUserById(1L)).thenReturn(Optional.of(new UserDto(1L, "user1", "User One")));

        Optional<ReviewResponseDto> result = reviewService.getReviewById(1L); // Espera ReviewResponseDto
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("user1", result.get().getUsername()); // Verifica el campo enriquecido
        verify(reviewRepository, times(1)).findById(1L);
        verify(userServiceClient, times(1)).getUserById(1L);
    }

    @Test
    void testDeleteReview_WhenExists() {
        when(reviewRepository.existsById(1L)).thenReturn(true);
        boolean deleted = reviewService.deleteReview(1L);
        verify(reviewRepository, times(1)).deleteById(1L);
        assertTrue(deleted);
    }

    @Test
    void testDeleteReview_WhenNotExists() {
        when(reviewRepository.existsById(1L)).thenReturn(false);
        boolean deleted = reviewService.deleteReview(1L);
        verify(reviewRepository, never()).deleteById(anyLong());
        assertFalse(deleted);
    }

    @Test
    void testUpdateReview() {
        Review existing = new Review();
        existing.setId(1L);
        existing.setComment("Antiguo");
        existing.setRating(3);
        existing.setProductId(101L);
        existing.setUserId(1L);

        Review updated = new Review();
        updated.setId(1L); // El ID es importante para la actualización
        updated.setComment("Nuevo");
        updated.setRating(4);
        updated.setProductId(101L); // Mantener consistencia
        updated.setUserId(1L); // Mantener consistencia

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenReturn(updated);

        Review result = reviewService.updateReview(1L, updated);
        assertEquals("Nuevo", result.getComment());
        assertEquals(4, result.getRating());
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testUpdateReview_NotFound() {
        Review updated = new Review();
        updated.setId(99L);
        updated.setComment("Nuevo");
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.updateReview(99L, updated);
        });

        assertEquals("Review not found with id 99", exception.getMessage());
        verify(reviewRepository, times(1)).findById(99L);
        verify(reviewRepository, never()).save(any(Review.class)); // Asegura que no se guardó
    }
}