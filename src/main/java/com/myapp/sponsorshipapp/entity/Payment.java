package com.myapp.sponsorshipapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "influencer_id")
    private User influencer;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private User brand;
    
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime paidAt;
    
    private String transactionId;
}

