package com.myapp.sponsorshipapp.service;

import com.myapp.sponsorshipapp.dto.CampaignRequest;
import com.myapp.sponsorshipapp.entity.Campaign;
import com.myapp.sponsorshipapp.entity.CampaignStatus;
import com.myapp.sponsorshipapp.entity.User;
import com.myapp.sponsorshipapp.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CampaignService {
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private AuthService authService;
    
    public Campaign createCampaign(CampaignRequest request) {
        User brand = authService.getCurrentUser();
        
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setPlatform(request.getPlatform());
        campaign.setBudget(request.getBudget());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setEligibility(request.getEligibility());
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setBrand(brand);
        
        return campaignRepository.save(campaign);
    }
    
    public Campaign updateCampaign(Long id, CampaignRequest request) {
        Campaign campaign = getCampaignById(id);
        User currentUser = authService.getCurrentUser();
        
        if (!campaign.getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own campaigns");
        }
        
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setPlatform(request.getPlatform());
        campaign.setBudget(request.getBudget());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setEligibility(request.getEligibility());
        
        return campaignRepository.save(campaign);
    }
    
    public void deleteCampaign(Long id) {
        Campaign campaign = getCampaignById(id);
        User currentUser = authService.getCurrentUser();
        
        if (!campaign.getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own campaigns");
        }
        
        campaignRepository.delete(campaign);
    }
    
    public Campaign getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        // Check if campaign has expired
        checkAndUpdateExpiredStatus(campaign);
        return campaign;
    }
    
    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = campaignRepository.findAll();
        // Update expired campaigns
        updateExpiredCampaigns();
        return campaignRepository.findAll();
    }
    
    public List<Campaign> getActiveCampaigns() {
        // First update any expired campaigns
        updateExpiredCampaigns();
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE);
    }
    
    public List<Campaign> getBrandCampaigns() {
        User brand = authService.getCurrentUser();
        List<Campaign> campaigns = campaignRepository.findByBrand(brand);
        // Check and update expired status for each campaign
        campaigns.forEach(this::checkAndUpdateExpiredStatus);
        return campaignRepository.findByBrand(brand);
    }
    
    public List<Campaign> searchCampaigns(String name, String platform, String status) {
        updateExpiredCampaigns();
        CampaignStatus campaignStatus = status != null ? CampaignStatus.valueOf(status.toUpperCase()) : null;
        return campaignRepository.searchCampaigns(name, platform, campaignStatus);
    }
    
    public Campaign updateCampaignStatus(Long id, String status) {
        Campaign campaign = getCampaignById(id);
        campaign.setStatus(CampaignStatus.valueOf(status.toUpperCase()));
        return campaignRepository.save(campaign);
    }
    
    // Helper method to check if a campaign has expired and update its status
    private void checkAndUpdateExpiredStatus(Campaign campaign) {
        if (campaign.getStatus() == CampaignStatus.ACTIVE && 
            campaign.getEndDate() != null && 
            campaign.getEndDate().isBefore(LocalDate.now())) {
            campaign.setStatus(CampaignStatus.EXPIRED);
            campaignRepository.save(campaign);
        }
    }
    
    // Update all expired campaigns
    public void updateExpiredCampaigns() {
        List<Campaign> expiredCampaigns = campaignRepository.findExpiredActiveCampaigns(LocalDate.now());
        for (Campaign campaign : expiredCampaigns) {
            campaign.setStatus(CampaignStatus.EXPIRED);
            campaignRepository.save(campaign);
        }
    }
}


