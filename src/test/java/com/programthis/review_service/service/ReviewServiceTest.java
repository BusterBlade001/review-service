package com.programthis.review_service.service;

import com.programthis.review_service.entity.Review;
import com.programthis.review_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReview_ValidRating() {
        Review review = new Review();
        review.setRating(4);
        review.setComment("Buen producto");
        when(reviewRepository.save(review)).thenReturn(review);

        Review result = reviewService.createReview(review);
        assertEquals(4, result.getRating());
        assertEquals("Buen producto", result.getComment());
    }

    @Test
    void testCreateReview_InvalidRatingTooHigh() {
        Review review = new Review();
        review.setRating(6); // inválido
        review.setComment("Comentario");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(review);
        });

        assertEquals("Rating must be between 1 and 5.", exception.getMessage());
    }

    @Test
    void testCreateReview_InvalidRatingTooLow() {
        Review review = new Review();
        review.setRating(0); // inválido
        review.setComment("Comentario");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(review);
        });

        assertEquals("Rating must be between 1 and 5.", exception.getMessage());
    }

    @Test
    void testCreateReview_NullRating() {
        Review review = new Review();
        review.setRating(null); // nulo
        review.setComment("Comentario");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(review);
        });

        assertEquals("Rating must be between 1 and 5.", exception.getMessage());
    }

    @Test
    void testCreateReview_NullComment() {
        Review review = new Review();
        review.setRating(4);
        review.setComment(null); // comentario nulo

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(review);
        });

        assertEquals("Comment cannot be empty.", exception.getMessage());
    }

    @Test
    void testCreateReview_EmptyComment() {
        Review review = new Review();
        review.setRating(4);
        review.setComment("  "); // comentario vacío

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(review);
        });

        assertEquals("Comment cannot be empty.", exception.getMessage());
    }

    @Test
    void testGetReviewsByProductId() {
        Review review = new Review();
        review.setProductId(1L);
        when(reviewRepository.findByProductId(1L)).thenReturn(Arrays.asList(review));

        List<Review> reviews = reviewService.getReviewsByProductId(1L);
        assertEquals(1, reviews.size());
    }

    @Test
    void testGetReviewsByUserId() {
        Review review = new Review();
        review.setUserId(1L);
        when(reviewRepository.findByUserId(1L)).thenReturn(Arrays.asList(review));

        List<Review> reviews = reviewService.getReviewsByUserId(1L);
        assertEquals(1, reviews.size());
    }

    @Test
    void testGetReviewById() {
        Review review = new Review();
        review.setId(1L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Optional<Review> result = reviewService.getReviewById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
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

        Review updated = new Review();
        updated.setComment("Nuevo");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenReturn(updated);

        Review result = reviewService.updateReview(1L, updated);
        assertEquals("Nuevo", result.getComment());
    }

    @Test
    void testUpdateReview_NotFound() {
        Review updated = new Review();
        updated.setComment("Nuevo");

        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.updateReview(1L, updated);
        });

        assertEquals("Review not found with id 1", exception.getMessage());
    }

}
