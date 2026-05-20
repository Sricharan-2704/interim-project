package com.myapp.sponsorshipapp.dto;

import lombok.Data;

@Data
public class SponsorshipApplicationRequest {
    private Long campaignId;
    private String proposal;
}

