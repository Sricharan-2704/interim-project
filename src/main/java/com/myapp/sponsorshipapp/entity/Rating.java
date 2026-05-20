package com.myapp.sponsorshipapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rater_id")
    private User rater; // Person giving the rating
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rated_id")
    private User rated; // Person being rated
    
    @Min(1)
    @Max(5)
    private Integer score;
    
    @Column(length = 1000)
    private String feedback;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}

