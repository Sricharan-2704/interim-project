package com.myapp.sponsorshipapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sponsorship_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "influencer_id")
    private User influencer;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
    
    @Column(length = 2000)
    private String proposal;
    
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(length = 2000)
    private String workDescription;
    
    private LocalDateTime workSubmittedAt;
    
    private LocalDateTime workCompletedAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
}

