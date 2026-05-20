package com.myapp.sponsorshipapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CampaignRequest {
    @NotBlank(message = "Campaign name is required")
    private String name;
    
    private String description;
    
    private String platform;
    
    @Positive(message = "Budget must be positive")
    private Double budget;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String eligibility;
}

