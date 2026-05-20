package com.myapp.sponsorshipapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RatingRequest {
    private Long campaignId;
    private Long ratedUserId;
    
    @Min(1)
    @Max(5)
    private Integer score;
    
    private String feedback;
}

