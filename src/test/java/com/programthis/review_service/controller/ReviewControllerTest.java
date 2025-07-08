package com.programthis.review_service.ControllerTest;

import com.programthis.review_service.controller.ReviewController;
import com.programthis.review_service.entity.Review;
import com.programthis.review_service.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Tests Corregidos ---

    @Test
    void testCreateReviewSuccess() {
        Review review = Review.builder().id(1L).productId(1L).userId(1L).comment("Test").rating(5).title("Title").build();
        when(reviewService.createReview(any(Review.class))).thenReturn(review);

        // El controlador ahora devuelve ResponseEntity<EntityModel<Review>>
        ResponseEntity<EntityModel<Review>> response = reviewController.createReview(new Review());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verificamos que el contenido del EntityModel es el objeto que esperamos
        assertEquals(review, response.getBody().getContent());
    }

    @Test
    void testGetReviewsByProductIdFound() {
        Long productId = 1L;
        Review review1 = Review.builder().id(1L).productId(productId).userId(1L).build();
        Review review2 = Review.builder().id(2L).productId(productId).userId(2L).build();
        List<Review> reviews = Arrays.asList(review1, review2);

        when(reviewService.getReviewsByProductId(productId)).thenReturn(reviews);

        // El controlador ahora devuelve ResponseEntity<CollectionModel<EntityModel<Review>>>
        ResponseEntity<CollectionModel<EntityModel<Review>>> response = reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verificamos que la colección dentro del CollectionModel tiene el tamaño esperado
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void testGetReviewsByUserIdFound() {
        Long userId = 1L;
        Review review1 = Review.builder().id(1L).productId(1L).userId(userId).build();
        Review review2 = Review.builder().id(2L).productId(2L).userId(userId).build();
        List<Review> reviews = Arrays.asList(review1, review2);

        when(reviewService.getReviewsByUserId(userId)).thenReturn(reviews);

        // El controlador ahora devuelve ResponseEntity<CollectionModel<EntityModel<Review>>>
        ResponseEntity<CollectionModel<EntityModel<Review>>> response = reviewController.getReviewsByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void testGetReviewByIdFound() {
        Long id = 1L;
        Review review = Review.builder().id(id).productId(1L).userId(1L).build();
        when(reviewService.getReviewById(id)).thenReturn(Optional.of(review));

        // El controlador ahora devuelve ResponseEntity<EntityModel<Review>>
        ResponseEntity<EntityModel<Review>> response = reviewController.getReviewById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verificamos el contenido del EntityModel
        assertEquals(review, response.getBody().getContent());
    }

    @Test
    void testUpdateReviewSuccess() {
        Long id = 1L;
        Review review = Review.builder().id(id).productId(1L).userId(1L).comment("Updated").build();
        when(reviewService.updateReview(eq(id), any(Review.class))).thenReturn(review);

        // El controlador ahora devuelve ResponseEntity<EntityModel<Review>>
        ResponseEntity<EntityModel<Review>> response = reviewController.updateReview(id, new Review());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verificamos el contenido del EntityModel
        assertEquals(review, response.getBody().getContent());
    }


    // --- Tests que ya funcionaban (no requieren cambios) ---

    @Test
    void testCreateReviewBadRequest() {
        when(reviewService.createReview(any(Review.class))).thenThrow(new IllegalArgumentException());
        // El tipo de respuesta no importa aquí, solo el status code
        ResponseEntity<?> response = reviewController.createReview(new Review());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetReviewsByProductIdNotFound() {
        Long productId = 1L;
        when(reviewService.getReviewsByProductId(productId)).thenReturn(Collections.emptyList());
        ResponseEntity<CollectionModel<EntityModel<Review>>> response = reviewController.getReviewsByProductId(productId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetReviewByIdNotFound() {
        Long id = 1L;
        when(reviewService.getReviewById(id)).thenReturn(Optional.empty());
        ResponseEntity<EntityModel<Review>> response = reviewController.getReviewById(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteReviewSuccess() {
        Long id = 1L;
        when(reviewService.deleteReview(id)).thenReturn(true);
        ResponseEntity<Void> response = reviewController.deleteReview(id);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteReviewNotFound() {
        Long id = 1L;
        when(reviewService.deleteReview(id)).thenReturn(false);
        ResponseEntity<Void> response = reviewController.deleteReview(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}