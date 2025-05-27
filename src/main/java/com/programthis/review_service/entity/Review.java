package com.programthis.review_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity 
@Data 
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class Review {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    private Long productId;

    private Long userId;

    private Integer rating;

    private String title;

    private String comment;

    private LocalDateTime reviewDate;

}