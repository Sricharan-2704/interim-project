package com.myapp.sponsorshipapp.controller;

import com.myapp.sponsorshipapp.dto.ApiResponse;
import com.myapp.sponsorshipapp.dto.RatingRequest;
import com.myapp.sponsorshipapp.entity.Rating;
import com.myapp.sponsorshipapp.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "http://localhost:4200")
public class RatingController {
    
    @Autowired
    private RatingService ratingService;
    
    @PostMapping
    public ResponseEntity<?> addRating(@Valid @RequestBody RatingRequest request) {
        try {
            Rating rating = ratingService.addRating(request);
            return ResponseEntity.ok(new ApiResponse(true, "Rating added successfully", rating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rating>> getUserRatings(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getUserRatings(userId));
    }
    
    @GetMapping("/my-ratings")
    public ResponseEntity<List<Rating>> getMyRatings() {
        return ResponseEntity.ok(ratingService.getMyRatings());
    }
    
    @GetMapping("/average/{userId}")
    public ResponseEntity<?> getAverageRating(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getAverageRating(userId));
    }
}

