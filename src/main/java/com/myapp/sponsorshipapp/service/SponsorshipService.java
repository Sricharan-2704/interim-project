package com.myapp.sponsorshipapp.service;

import com.myapp.sponsorshipapp.dto.SponsorshipApplicationRequest;
import com.myapp.sponsorshipapp.dto.WorkSubmissionRequest;
import com.myapp.sponsorshipapp.entity.*;
import com.myapp.sponsorshipapp.repository.CampaignRepository;
import com.myapp.sponsorshipapp.repository.SponsorshipRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SponsorshipService {
    
    @Autowired
    private SponsorshipRequestRepository requestRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private NotificationService notificationService;
    
    public SponsorshipRequest applyForCampaign(SponsorshipApplicationRequest request) {
        User influencer = authService.getCurrentUser();
        
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        if (requestRepository.existsByInfluencerAndCampaign(influencer, campaign)) {
            throw new RuntimeException("You have already applied for this campaign");
        }
        
        SponsorshipRequest sponsorshipRequest = new SponsorshipRequest();
        sponsorshipRequest.setInfluencer(influencer);
        sponsorshipRequest.setCampaign(campaign);
        sponsorshipRequest.setProposal(request.getProposal());
        sponsorshipRequest.setStatus(RequestStatus.PENDING);
        sponsorshipRequest.setCreatedAt(LocalDateTime.now());
        
        SponsorshipRequest saved = requestRepository.save(sponsorshipRequest);
        
        // Notify brand
        notificationService.createNotification(
                campaign.getBrand(),
                "New Application",
                influencer.getName() + " applied for your campaign: " + campaign.getName()
        );
        
        return saved;
    }
    
    public SponsorshipRequest updateRequestStatus(Long requestId, String status) {
        SponsorshipRequest request = getRequestById(requestId);
        User currentUser = authService.getCurrentUser();
        
        // Only brand can update request status
        if (!request.getCampaign().getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only campaign owner can update request status");
        }
        
        request.setStatus(RequestStatus.valueOf(status.toUpperCase()));
        request.setUpdatedAt(LocalDateTime.now());
        
        SponsorshipRequest updated = requestRepository.save(request);
        
        // Notify influencer
        notificationService.createNotification(
                request.getInfluencer(),
                "Application " + status,
                "Your application for " + request.getCampaign().getName() + " has been " + status.toLowerCase()
        );
        
        return updated;
    }
    
    public SponsorshipRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }
    
    public List<SponsorshipRequest> getInfluencerRequests() {
        User influencer = authService.getCurrentUser();
        return requestRepository.findByInfluencer(influencer);
    }
    
    public List<SponsorshipRequest> getBrandRequests() {
        User brand = authService.getCurrentUser();
        return requestRepository.findByCampaignBrand(brand);
    }
    
    public List<SponsorshipRequest> getCampaignRequests(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        return requestRepository.findByCampaign(campaign);
    }
    
    public List<SponsorshipRequest> getAllRequests() {
        return requestRepository.findAll();
    }
    
    public SponsorshipRequest submitWork(Long sponsorshipRequestId, String workDescription) {
        SponsorshipRequest request = getRequestById(sponsorshipRequestId);
        User currentUser = authService.getCurrentUser();
        
        // Only influencer who submitted the request can submit work
        if (!request.getInfluencer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the influencer can submit work for this request");
        }
        
        // Can only submit work if request is ACCEPTED
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new RuntimeException("Work can only be submitted for accepted sponsorship requests");
        }
        
        // Check if work is already submitted
        if (request.getWorkSubmittedAt() != null) {
            throw new RuntimeException("Work has already been submitted for this request");
        }
        
        request.setWorkDescription(workDescription);
        request.setWorkSubmittedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        SponsorshipRequest updated = requestRepository.save(request);
        
        // Notify brand about work submission
        notificationService.createNotification(
                request.getCampaign().getBrand(),
                "Work Submitted",
                request.getInfluencer().getName() + " has submitted their work for campaign: " + request.getCampaign().getName()
        );
        
        return updated;
    }
    
    public SponsorshipRequest markWorkAsComplete(Long sponsorshipRequestId) {
        SponsorshipRequest request = getRequestById(sponsorshipRequestId);
        User currentUser = authService.getCurrentUser();
        
        // Only brand can mark work as complete
        if (!request.getCampaign().getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only campaign owner can mark work as complete");
        }
        
        // Work must be submitted first
        if (request.getWorkSubmittedAt() == null) {
            throw new RuntimeException("Work has not been submitted for this request");
        }
        
        // Work cannot be marked as complete if already completed
        if (request.getWorkCompletedAt() != null) {
            throw new RuntimeException("Work has already been marked as complete");
        }
        
        request.setWorkCompletedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        SponsorshipRequest updated = requestRepository.save(request);
        
        // Notify influencer that work has been approved
        notificationService.createNotification(
                request.getInfluencer(),
                "Work Approved",
                "Your work for campaign " + request.getCampaign().getName() + " has been approved. Payment can now be processed."
        );
        
        return updated;
    }
}

