package com.myapp.sponsorshipapp.repository;

import com.myapp.sponsorshipapp.entity.Rating;
import com.myapp.sponsorshipapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRated(User rated);
    List<Rating> findByRater(User rater);
    
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.rated = :user")
    Double getAverageRatingForUser(@Param("user") User user);
    
    @Modifying
    @Query("DELETE FROM Rating r WHERE r.rater.id = ?1 OR r.rated.id = ?2")
    void deleteByRaterIdOrRatedId(Long raterId, Long ratedId);
    
    @Modifying
    @Query("DELETE FROM Rating r WHERE r.campaign.id = ?1")
    void deleteByCampaignId(Long campaignId);
}

