package com.programthis.review_service.repository;

import com.programthis.review_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // Importar Optional si lo usas

public interface ReviewRepository extends JpaRepository<Review, Long> {


    /**
     * Busca todas las reseñas asociadas a un producto específico.
     * @param productId El ID del producto.
     * @return Una lista de reseñas para el producto dado.
     */
    List<Review> findByProductId(Long productId);

    /**
     * Busca todas las reseñas escritas por un usuario específico.
     * @param userId El ID del usuario.
     * @return Una lista de reseñas escritas por el usuario dado.
     */
    List<Review> findByUserId(Long userId);

    /**
     * Busca una reseña específica por ID de producto y ID de usuario.
     * Útil si quieres asegurar que un usuario solo puede dejar una reseña por producto.
     * @param productId El ID del producto.
     * @param userId El ID del usuario.
     * @return Un Optional que contiene la reseña si se encuentra, o vacío si no.
     */
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    /**
     * Busca reseñas con una puntuación igual o superior a un valor dado.
     * @param rating El valor mínimo de puntuación.
     * @return Una lista de reseñas con puntuación igual o superior.
     */
    List<Review> findByRatingGreaterThanEqual(Integer rating);

    /**
     * Busca reseñas con un comentario que contiene una palabra clave (ignorando mayúsculas/minúsculas).
     * @param keyword La palabra clave a buscar en el comentario.
     * @return Una lista de reseñas que contienen la palabra clave.
     */
    
    List<Review> findByCommentContainingIgnoreCase(String keyword);

    List<Review> findTop5ByProductIdOrderByReviewDateDesc(Long productId);
}