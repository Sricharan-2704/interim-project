package com.myapp.sponsorshipapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkSubmissionRequest {
    @NotBlank(message = "Sponsorship request ID is required")
    private Long sponsorshipRequestId;
    
    private String workDescription;
}
