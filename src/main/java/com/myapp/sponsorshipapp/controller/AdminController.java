package com.myapp.sponsorshipapp.controller;

import com.myapp.sponsorshipapp.dto.ApiResponse;
import com.myapp.sponsorshipapp.dto.DashboardStats;
import com.myapp.sponsorshipapp.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:4200","https://gregarious-naiad-7592c9.netlify.app"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }
    
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/campaigns")
    public ResponseEntity<?> getAllCampaigns() {
        return ResponseEntity.ok(adminService.getAllCampaigns());
    }
    
    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        return ResponseEntity.ok(adminService.getAllRequests());
    }
    
    @GetMapping("/payments")
    public ResponseEntity<?> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }
    
    @GetMapping("/ratings")
    public ResponseEntity<?> getAllRatings() {
        return ResponseEntity.ok(adminService.getAllRatings());
    }
}

