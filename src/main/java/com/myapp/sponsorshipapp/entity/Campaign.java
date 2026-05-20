package com.myapp.sponsorshipapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Campaign name is required")
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    private String platform; // Instagram, YouTube, TikTok, etc.
    
    @Positive(message = "Budget must be positive")
    private Double budget;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String eligibility; // Requirements for influencers
    
    @Enumerated(EnumType.STRING)
    private CampaignStatus status = CampaignStatus.ACTIVE;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private User brand;
}

