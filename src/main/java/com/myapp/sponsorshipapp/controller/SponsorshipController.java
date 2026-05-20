package com.myapp.sponsorshipapp.controller;

import com.myapp.sponsorshipapp.dto.ApiResponse;
import com.myapp.sponsorshipapp.dto.SponsorshipApplicationRequest;
import com.myapp.sponsorshipapp.entity.SponsorshipRequest;
import com.myapp.sponsorshipapp.service.SponsorshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsorship")
@CrossOrigin(origins = "http://localhost:4200")
public class SponsorshipController {
    
    @Autowired
    private SponsorshipService sponsorshipService;
    
    @PostMapping("/apply")
    public ResponseEntity<?> applyForCampaign(@RequestBody SponsorshipApplicationRequest request) {
        try {
            SponsorshipRequest sponsorshipRequest = sponsorshipService.applyForCampaign(request);
            return ResponseEntity.ok(new ApiResponse(true, "Application submitted successfully", sponsorshipRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRequestStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            SponsorshipRequest request = sponsorshipService.updateRequestStatus(id, status);
            return ResponseEntity.ok(new ApiResponse(true, "Request status updated", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sponsorshipService.getRequestById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/my-applications")
    public ResponseEntity<List<SponsorshipRequest>> getMyApplications() {
        return ResponseEntity.ok(sponsorshipService.getInfluencerRequests());
    }
    
    @GetMapping("/brand-requests")
    public ResponseEntity<List<SponsorshipRequest>> getBrandRequests() {
        return ResponseEntity.ok(sponsorshipService.getBrandRequests());
    }
    
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<SponsorshipRequest>> getCampaignRequests(@PathVariable Long campaignId) {
        return ResponseEntity.ok(sponsorshipService.getCampaignRequests(campaignId));
    }
    
    @PostMapping("/{id}/submit-work")
    public ResponseEntity<?> submitWork(@PathVariable Long id, @RequestParam(required = false) String workDescription) {
        try {
            SponsorshipRequest request = sponsorshipService.submitWork(id, workDescription != null ? workDescription : "");
            return ResponseEntity.ok(new ApiResponse(true, "Work submitted successfully", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/mark-work-complete")
    public ResponseEntity<?> markWorkAsComplete(@PathVariable Long id) {
        try {
            SponsorshipRequest request = sponsorshipService.markWorkAsComplete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Work marked as complete", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}

