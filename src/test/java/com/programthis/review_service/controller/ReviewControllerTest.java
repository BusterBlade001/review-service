package com.programthis.review_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper; // ¡NUEVA ADICIÓN!
import com.programthis.review_service.entity.Review;
import com.programthis.review_service.service.ReviewService;
import com.programthis.review_service.dto.ReviewResponseDto; // ¡NUEVA ADICIÓN!
import com.programthis.review_service.dto.UserDto; // ¡NUEVA ADICIÓN!
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime; // ¡NUEVA ADICIÓN!
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // ¡NUEVA ADICIÓN!

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    // Aunque ReviewController no inyecta directamente UserServiceClient,
    // su ReviewService sí lo hace. En los tests de controlador, es común mockear
    // el servicio y no sus dependencias internas a menos que sea necesario para un setup específico.
    // Aquí, al mockear ReviewService, no necesitamos mockear UserServiceClient directamente.

    @InjectMocks
    private ReviewController reviewController;

    // Datos de prueba comunes
    private Review testReview;
    private UserDto testUserDto;
    private ReviewResponseDto testReviewResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks

        // Configuración de datos de prueba
        testReview = Review.builder().id(1L).productId(101L).userId(1L).rating(5).title("Great Product").comment("Loved it!").reviewDate(LocalDateTime.now()).build();
        testUserDto = new UserDto(1L, "testuser", "Test User Full Name");
        testReviewResponseDto = new ReviewResponseDto(testReview, testUserDto);
    }

    @Test
    void testCreateReviewSuccess() {
        // Review de entrada para el POST
        Review reviewInput = Review.builder().productId(1L).userId(1L).comment("Test").rating(5).title("Title").build();
        // Review que retorna el servicio (con ID y reviewDate)
        Review savedReview = Review.builder().id(1L).productId(1L).userId(1L).comment("Test").rating(5).title("Title").reviewDate(LocalDateTime.now()).build();
        
        when(reviewService.createReview(any(Review.class))).thenReturn(savedReview);

        // El controlador ahora devuelve ResponseEntity<EntityModel<Review>>
        ResponseEntity<EntityModel<Review>> response = reviewController.createReview(reviewInput);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(savedReview.getId(), response.getBody().getContent().getId());
        assertEquals(savedReview.getTitle(), response.getBody().getContent().getTitle());
        verify(reviewService, times(1)).createReview(any(Review.class));
    }

    @Test
    void testGetReviewsByProductIdFound() {
        Long productId = 101L;
        // Mockeamos la respuesta del servicio para que devuelva una lista de ReviewResponseDto
        List<ReviewResponseDto> serviceResponse = Arrays.asList(
            new ReviewResponseDto(Review.builder().id(1L).productId(productId).userId(1L).build(), new UserDto(1L, "user1", "User One")),
            new ReviewResponseDto(Review.builder().id(2L).productId(productId).userId(2L).build(), new UserDto(2L, "user2", "User Two"))
        );

        when(reviewService.getReviewsByProductId(productId)).thenReturn(serviceResponse);

        // El controlador ahora devuelve ResponseEntity<CollectionModel<EntityModel<ReviewResponseDto>>>
        ResponseEntity<CollectionModel<EntityModel<ReviewResponseDto>>> response = reviewController.getReviewsByProductId(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        
        // Verificamos el contenido
        List<ReviewResponseDto> extractedContent = response.getBody().getContent().stream()
                                                    .map(EntityModel::getContent)
                                                    .collect(Collectors.toList());
        assertEquals("user1", extractedContent.get(0).getUsername());
        assertEquals("user2", extractedContent.get(1).getUsername());
        verify(reviewService, times(1)).getReviewsByProductId(productId);
    }

    @Test
    void testGetReviewsByUserIdFound() {
        Long userId = 1L;
        List<ReviewResponseDto> serviceResponse = Arrays.asList(
            new ReviewResponseDto(Review.builder().id(1L).productId(101L).userId(userId).build(), new UserDto(userId, "user1", "User One")),
            new ReviewResponseDto(Review.builder().id(2L).productId(102L).userId(userId).build(), new UserDto(userId, "user1", "User One"))
        );

        when(reviewService.getReviewsByUserId(userId)).thenReturn(serviceResponse);

        ResponseEntity<CollectionModel<EntityModel<ReviewResponseDto>>> response = reviewController.getReviewsByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        List<ReviewResponseDto> extractedContent = response.getBody().getContent().stream()
                                                    .map(EntityModel::getContent)
                                                    .collect(Collectors.toList());
        assertEquals("user1", extractedContent.get(0).getUsername());
        verify(reviewService, times(1)).getReviewsByUserId(userId);
    }

    @Test
    void testGetReviewByIdFound() {
        Long id = 1L;
        // Mockeamos la respuesta del servicio para que devuelva un ReviewResponseDto
        when(reviewService.getReviewById(id)).thenReturn(Optional.of(testReviewResponseDto));

        // El controlador ahora devuelve ResponseEntity<EntityModel<ReviewResponseDto>>
        ResponseEntity<EntityModel<ReviewResponseDto>> response = reviewController.getReviewById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verificamos el contenido del EntityModel
        assertEquals(testReviewResponseDto.getId(), response.getBody().getContent().getId());
        assertEquals(testReviewResponseDto.getUsername(), response.getBody().getContent().getUsername()); // Verifica el campo enriquecido
        verify(reviewService, times(1)).getReviewById(id);
    }

    @Test
    void testUpdateReviewSuccess() {
        Long id = 1L;
        Review reviewInput = Review.builder().comment("Updated").rating(4).title("Updated Title").build();
        // El servicio de actualización devuelve la entidad Review original
        Review updatedReviewEntity = Review.builder().id(id).productId(101L).userId(1L).comment("Updated").rating(4).title("Updated Title").reviewDate(LocalDateTime.now()).build();

        when(reviewService.updateReview(eq(id), any(Review.class))).thenReturn(updatedReviewEntity);

        ResponseEntity<EntityModel<Review>> response = reviewController.updateReview(id, reviewInput);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedReviewEntity.getId(), response.getBody().getContent().getId());
        assertEquals(updatedReviewEntity.getComment(), response.getBody().getContent().getComment());
        verify(reviewService, times(1)).updateReview(eq(id), any(Review.class));
    }


    // --- Tests que ya funcionaban (y se ajustan si es necesario) ---

    @Test
    void testCreateReviewBadRequest() {
        when(reviewService.createReview(any(Review.class))).thenThrow(new IllegalArgumentException());
        ResponseEntity<?> response = reviewController.createReview(new Review());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reviewService, times(1)).createReview(any(Review.class));
    }

    @Test
    void testGetReviewsByProductIdNotFound() {
        Long productId = 1L;
        when(reviewService.getReviewsByProductId(productId)).thenReturn(Collections.emptyList());
        // El tipo de respuesta del controlador ha cambiado, ajustamos el tipo esperado
        ResponseEntity<CollectionModel<EntityModel<ReviewResponseDto>>> response = reviewController.getReviewsByProductId(productId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reviewService, times(1)).getReviewsByProductId(productId);
    }

    @Test
    void testGetReviewByIdNotFound() {
        Long id = 1L;
        when(reviewService.getReviewById(id)).thenReturn(Optional.empty());
        // El tipo de respuesta del controlador ha cambiado, ajustamos el tipo esperado
        ResponseEntity<EntityModel<ReviewResponseDto>> response = reviewController.getReviewById(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reviewService, times(1)).getReviewById(id);
    }

    @Test
    void testDeleteReviewSuccess() {
        Long id = 1L;
        when(reviewService.deleteReview(id)).thenReturn(true);
        ResponseEntity<Void> response = reviewController.deleteReview(id);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reviewService, times(1)).deleteReview(id);
    }

    @Test
    void testDeleteReviewNotFound() {
        Long id = 1L;
        when(reviewService.deleteReview(id)).thenReturn(false);
        ResponseEntity<Void> response = reviewController.deleteReview(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reviewService, times(1)).deleteReview(id);
    }
}