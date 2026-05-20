package com.myapp.sponsorshipapp.controller;

import com.myapp.sponsorshipapp.dto.ApiResponse;
import com.myapp.sponsorshipapp.dto.CampaignRequest;
import com.myapp.sponsorshipapp.entity.Campaign;
import com.myapp.sponsorshipapp.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@CrossOrigin(origins = "http://localhost:4200")
public class CampaignController {
    
    @Autowired
    private CampaignService campaignService;
    
    @PostMapping
    public ResponseEntity<?> createCampaign(@Valid @RequestBody CampaignRequest request) {
        try {
            Campaign campaign = campaignService.createCampaign(request);
            return ResponseEntity.ok(new ApiResponse(true, "Campaign created successfully", campaign));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(@PathVariable Long id, @Valid @RequestBody CampaignRequest request) {
        try {
            Campaign campaign = campaignService.updateCampaign(id, request);
            return ResponseEntity.ok(new ApiResponse(true, "Campaign updated successfully", campaign));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        try {
            campaignService.deleteCampaign(id);
            return ResponseEntity.ok(new ApiResponse(true, "Campaign deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCampaign(@PathVariable Long id) {
        try {
            Campaign campaign = campaignService.getCampaignById(id);
            return ResponseEntity.ok(campaign);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Campaign>> getActiveCampaigns() {
        return ResponseEntity.ok(campaignService.getActiveCampaigns());
    }
    
    @GetMapping("/my-campaigns")
    public ResponseEntity<List<Campaign>> getMyCampaigns() {
        return ResponseEntity.ok(campaignService.getBrandCampaigns());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Campaign>> searchCampaigns(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(campaignService.searchCampaigns(name, platform, status));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCampaignStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Campaign campaign = campaignService.updateCampaignStatus(id, status);
            return ResponseEntity.ok(new ApiResponse(true, "Campaign status updated", campaign));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}

