package com.myapp.sponsorshipapp.service;

import com.myapp.sponsorshipapp.dto.RatingRequest;
import com.myapp.sponsorshipapp.entity.Campaign;
import com.myapp.sponsorshipapp.entity.Rating;
import com.myapp.sponsorshipapp.entity.User;
import com.myapp.sponsorshipapp.repository.CampaignRepository;
import com.myapp.sponsorshipapp.repository.RatingRepository;
import com.myapp.sponsorshipapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private NotificationService notificationService;
    
    public Rating addRating(RatingRequest request) {
        User rater = authService.getCurrentUser();
        
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        User rated = userRepository.findById(request.getRatedUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Rating rating = new Rating();
        rating.setCampaign(campaign);
        rating.setRater(rater);
        rating.setRated(rated);
        rating.setScore(request.getScore());
        rating.setFeedback(request.getFeedback());
        rating.setCreatedAt(LocalDateTime.now());
        
        Rating saved = ratingRepository.save(rating);
        
        // Notify rated user
        notificationService.createNotification(
                rated,
                "New Rating",
                "You received a " + request.getScore() + "-star rating from " + rater.getName()
        );
        
        return saved;
    }
    
    public List<Rating> getUserRatings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ratingRepository.findByRated(user);
    }
    
    public List<Rating> getMyRatings() {
        User user = authService.getCurrentUser();
        return ratingRepository.findByRater(user);
    }
    
    public Double getAverageRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Double avg = ratingRepository.getAverageRatingForUser(user);
        return avg != null ? avg : 0.0;
    }
    
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
}

