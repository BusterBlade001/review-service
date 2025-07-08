package com.programthis.review_service.controller;

import com.programthis.review_service.entity.Review;
import com.programthis.review_service.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<EntityModel<Review>> createReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.createReview(review);
            addLinks(savedReview);
            return new ResponseEntity<>(EntityModel.of(savedReview), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>((EntityModel<Review>) null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<CollectionModel<EntityModel<Review>>> getReviewsByProductId(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<EntityModel<Review>> reviewModels = reviews.stream()
                .map(this::addLinks)
                .map(EntityModel::of)
                .collect(Collectors.toList());

        WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).getReviewsByProductId(productId));
        return ResponseEntity.ok(CollectionModel.of(reviewModels, link.withSelfRel()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CollectionModel<EntityModel<Review>>> getReviewsByUserId(@PathVariable Long userId) {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<EntityModel<Review>> reviewModels = reviews.stream()
                .map(this::addLinks)
                .map(EntityModel::of)
                .collect(Collectors.toList());
        
        WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).getReviewsByUserId(userId));
        return ResponseEntity.ok(CollectionModel.of(reviewModels, link.withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Review>> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(this::addLinks)
                .map(review -> EntityModel.of(review,
                        linkTo(methodOn(ReviewController.class).getReviewById(id)).withSelfRel(),
                        linkTo(methodOn(ReviewController.class).getReviewsByProductId(review.getProductId())).withRel("product-reviews"),
                        linkTo(methodOn(ReviewController.class).getReviewsByUserId(review.getUserId())).withRel("user-reviews")))
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

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Review>> updateReview(@PathVariable Long id, @RequestBody Review review) {
        try {
            Review updated = reviewService.updateReview(id, review);
            addLinks(updated);
            return new ResponseEntity<>(EntityModel.of(updated), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>((EntityModel<Review>) null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Review addLinks(Review review) {
        review.add(linkTo(methodOn(ReviewController.class).getReviewById(review.getId())).withSelfRel());
        review.add(linkTo(methodOn(ReviewController.class).getReviewsByProductId(review.getProductId())).withRel("product-reviews"));
        review.add(linkTo(methodOn(ReviewController.class).getReviewsByUserId(review.getUserId())).withRel("user-reviews"));
        return review;
    }
}