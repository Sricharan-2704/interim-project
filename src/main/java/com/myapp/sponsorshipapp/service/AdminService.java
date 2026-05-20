package com.myapp.sponsorshipapp.service;

import com.myapp.sponsorshipapp.dto.DashboardStats;
import com.myapp.sponsorshipapp.entity.*;
import com.myapp.sponsorshipapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private SponsorshipRequestRepository requestRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public DashboardStats getAdminStats() {
        DashboardStats stats = new DashboardStats();
        
        stats.setTotalUsers(userRepository.count());
        stats.setTotalBrands(userRepository.findByRole(Role.BRAND).size());
        stats.setTotalInfluencers(userRepository.findByRole(Role.INFLUENCER).size());
        stats.setTotalCampaigns(campaignRepository.count());
        stats.setActiveCampaigns(campaignRepository.findByStatus(CampaignStatus.ACTIVE).size());
        stats.setTotalRequests(requestRepository.count());
        stats.setPendingRequests(requestRepository.findByStatus(RequestStatus.PENDING).size());
        
        // Calculate total payments
        Double totalPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
        stats.setTotalEarnings(totalPayments);
        
        return stats;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase()));
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete related notifications
        notificationRepository.deleteByUserId(userId);
        
        // Delete related ratings (where user is rater or rated)
        ratingRepository.deleteByRaterIdOrRatedId(userId, userId);
        
        // Delete related payments
        paymentRepository.deleteByInfluencerIdOrBrandId(userId, userId);
        
        // Delete related sponsorship requests
        requestRepository.deleteByInfluencerId(userId);
        
        // If user is a brand, delete their campaigns and related requests
        if (user.getRole() == Role.BRAND) {
            List<Campaign> campaigns = campaignRepository.findByBrandId(userId);
            for (Campaign campaign : campaigns) {
                requestRepository.deleteByCampaignId(campaign.getId());
                paymentRepository.deleteByCampaignId(campaign.getId());
                ratingRepository.deleteByCampaignId(campaign.getId());
            }
            campaignRepository.deleteByBrandId(userId);
        }
        
        userRepository.deleteById(userId);
    }
    
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }
    
    public List<SponsorshipRequest> getAllRequests() {
        return requestRepository.findAll();
    }
    
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
}

