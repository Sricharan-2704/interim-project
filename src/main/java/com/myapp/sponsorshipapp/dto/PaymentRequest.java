package com.myapp.sponsorshipapp.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long campaignId;
    private Long influencerId;
    private Double amount;
}

