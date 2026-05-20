package com.myapp.sponsorshipapp.service;

import com.myapp.sponsorshipapp.dto.PaymentRequest;
import com.myapp.sponsorshipapp.entity.*;
import com.myapp.sponsorshipapp.repository.CampaignRepository;
import com.myapp.sponsorshipapp.repository.PaymentRepository;
import com.myapp.sponsorshipapp.repository.SponsorshipRequestRepository;
import com.myapp.sponsorshipapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private SponsorshipRequestRepository sponsorshipRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private NotificationService notificationService;
    
    public Payment createPayment(PaymentRequest request) {
        User brand = authService.getCurrentUser();
        
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        User influencer = userRepository.findById(request.getInfluencerId())
                .orElseThrow(() -> new RuntimeException("Influencer not found"));
        
        // Check if sponsorship request exists and work has been marked as complete
        SponsorshipRequest sponsorshipRequest = sponsorshipRequestRepository.findByInfluencerAndCampaign(influencer, campaign)
                .orElseThrow(() -> new RuntimeException("No sponsorship request found for this influencer and campaign"));
        
        if (sponsorshipRequest.getWorkCompletedAt() == null) {
            throw new RuntimeException("Work must be completed before payment can be made");
        }
        
        // Check if payment already exists for this request
        if (paymentRepository.findByCampaignAndInfluencer(campaign, influencer).stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.COMPLETED)) {
            throw new RuntimeException("Payment has already been made for this sponsorship");
        }
        
        Payment payment = new Payment();
        payment.setCampaign(campaign);
        payment.setBrand(brand);
        payment.setInfluencer(influencer);
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.COMPLETED); // Mark as completed immediately
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaidAt(LocalDateTime.now()); // Set paid time
        payment.setTransactionId(UUID.randomUUID().toString());
        
        Payment saved = paymentRepository.save(payment);
        
        // Mark campaign as COMPLETED after payment
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);
        
        // Mark sponsorship request as COMPLETED
        sponsorshipRequest.setStatus(RequestStatus.COMPLETED);
        sponsorshipRequest.setUpdatedAt(LocalDateTime.now());
        sponsorshipRequestRepository.save(sponsorshipRequest);
        
        // Notify influencer
        notificationService.createNotification(
                influencer,
                "Payment Received!",
                "You have received a payment of $" + request.getAmount() + " for campaign: " + campaign.getName()
        );
        
        return saved;
    }
    
    public Payment completePayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        
        Payment completed = paymentRepository.save(payment);
        
        // Notify influencer
        notificationService.createNotification(
                payment.getInfluencer(),
                "Payment Completed",
                "Payment of $" + payment.getAmount() + " has been completed"
        );
        
        return completed;
    }
    
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    
    public List<Payment> getInfluencerPayments() {
        User influencer = authService.getCurrentUser();
        return paymentRepository.findByInfluencer(influencer);
    }
    
    public List<Payment> getBrandPayments() {
        User brand = authService.getCurrentUser();
        return paymentRepository.findByBrand(brand);
    }
    
    public Double getInfluencerEarnings() {
        User influencer = authService.getCurrentUser();
        Double earnings = paymentRepository.getTotalEarningsByInfluencer(influencer);
        return earnings != null ? earnings : 0.0;
    }
    
    public Double getBrandSpending() {
        User brand = authService.getCurrentUser();
        Double spending = paymentRepository.getTotalSpendingByBrand(brand);
        return spending != null ? spending : 0.0;
    }
    
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}

