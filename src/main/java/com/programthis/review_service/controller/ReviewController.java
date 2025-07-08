package com.programthis.review_service.controller;

import com.programthis.review_service.entity.Review; // Mantener para createReview y updateReview
import com.programthis.review_service.service.ReviewService;
import com.programthis.review_service.dto.ReviewResponseDto; // ¡NUEVA ADICIÓN! Importar el DTO de respuesta

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Asegurarse de que esté importado
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping // Este método crea un Review, no un ReviewResponseDto
    public ResponseEntity<EntityModel<Review>> createReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.createReview(review);
            // addLinks(savedReview); // Si deseas que la respuesta de creación tenga enlaces HATEOAS basados en Review
            // Para HATEOAS de la entidad original Review, puedes usar esto:
            EntityModel<Review> resource = EntityModel.of(savedReview,
                    linkTo(methodOn(ReviewController.class).getReviewById(savedReview.getId())).withSelfRel());
            return new ResponseEntity<>(resource, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Manejo de errores simplificado
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/product/{productId}")
    // ¡MODIFICACIÓN CLAVE! Cambiar el tipo de retorno a CollectionModel<EntityModel<ReviewResponseDto>>
    public ResponseEntity<CollectionModel<EntityModel<ReviewResponseDto>>> getReviewsByProductId(@PathVariable Long productId) {
        // ¡MODIFICACIÓN CLAVE! Llamar al servicio que devuelve List<ReviewResponseDto>
        List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(productId);
        
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Devuelve 404 si no hay reseñas
        }
        
        // ¡MODIFICACIÓN CLAVE! Mapear a EntityModel<ReviewResponseDto> y añadir enlaces
        List<EntityModel<ReviewResponseDto>> reviewModels = reviews.stream()
                .map(this::toReviewResponseModel) // Usamos el nuevo método auxiliar
                .collect(Collectors.toList());

        WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).getReviewsByProductId(productId));
        return ResponseEntity.ok(CollectionModel.of(reviewModels, link.withSelfRel()));
    }

    @GetMapping("/user/{userId}")
    // ¡MODIFICACIÓN CLAVE! Cambiar el tipo de retorno a CollectionModel<EntityModel<ReviewResponseDto>>
    public ResponseEntity<CollectionModel<EntityModel<ReviewResponseDto>>> getReviewsByUserId(@PathVariable Long userId) {
        // ¡MODIFICACIÓN CLAVE! Llamar al servicio que devuelve List<ReviewResponseDto>
        List<ReviewResponseDto> reviews = reviewService.getReviewsByUserId(userId);
        
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // ¡MODIFICACIÓN CLAVE! Mapear a EntityModel<ReviewResponseDto> y añadir enlaces
        List<EntityModel<ReviewResponseDto>> reviewModels = reviews.stream()
                .map(this::toReviewResponseModel) // Usamos el nuevo método auxiliar
                .collect(Collectors.toList());
        
        WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).getReviewsByUserId(userId));
        return ResponseEntity.ok(CollectionModel.of(reviewModels, link.withSelfRel()));
    }

    @GetMapping("/{id}")
    // ¡MODIFICACIÓN CLAVE! Cambiar el tipo de retorno a EntityModel<ReviewResponseDto>
    public ResponseEntity<EntityModel<ReviewResponseDto>> getReviewById(@PathVariable Long id) {
        // ¡MODIFICACIÓN CLAVE! Llamar al servicio que devuelve Optional<ReviewResponseDto>
        return reviewService.getReviewById(id)
                .map(this::toReviewResponseModel) // Usamos el nuevo método auxiliar
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (reviewService.deleteReview(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}") // Este método actualiza un Review, no un ReviewResponseDto
    public ResponseEntity<EntityModel<Review>> updateReview(@PathVariable Long id, @RequestBody Review review) {
        try {
            Review updated = reviewService.updateReview(id, review);
            // addLinks(updated); // Si deseas que la respuesta de actualización tenga enlaces HATEOAS basados en Review
            // Para HATEOAS de la entidad original Review, puedes usar esto:
            EntityModel<Review> resource = EntityModel.of(updated,
                    linkTo(methodOn(ReviewController.class).getReviewById(updated.getId())).withSelfRel());
            return new ResponseEntity<>(resource, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Método auxiliar para añadir enlaces HATEOAS a ReviewResponseDto
    private EntityModel<ReviewResponseDto> toReviewResponseModel(ReviewResponseDto reviewResponseDto) { // ¡NUEVO MÉTODO!
        return EntityModel.of(reviewResponseDto,
                linkTo(methodOn(ReviewController.class).getReviewById(reviewResponseDto.getId())).withSelfRel(),
                linkTo(methodOn(ReviewController.class).getReviewsByProductId(reviewResponseDto.getProductId())).withRel("product-reviews"),
                linkTo(methodOn(ReviewController.class).getReviewsByUserId(reviewResponseDto.getUserId())).withRel("user-reviews"));
    }
    // NOTA: El método 'private Review addLinks(Review review)' original ya no se usa para los GETs enriquecidos,
    // puedes mantenerlo si lo usas en otros lados, o eliminarlo si no.
    // Lo he dejado comentado en los POST/PUT que devuelven Review original.
}