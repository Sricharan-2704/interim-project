package com.myapp.sponsorshipapp.repository;

import com.myapp.sponsorshipapp.entity.Campaign;
import com.myapp.sponsorshipapp.entity.Payment;
import com.myapp.sponsorshipapp.entity.PaymentStatus;
import com.myapp.sponsorshipapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInfluencer(User influencer);
    List<Payment> findByBrand(User brand);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCampaignAndInfluencer(Campaign campaign, User influencer);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.influencer = :influencer AND p.status = 'COMPLETED'")
    Double getTotalEarningsByInfluencer(@Param("influencer") User influencer);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.brand = :brand AND p.status = 'COMPLETED'")
    Double getTotalSpendingByBrand(@Param("brand") User brand);
    
    @Modifying
    @Query("DELETE FROM Payment p WHERE p.influencer.id = ?1 OR p.brand.id = ?2")
    void deleteByInfluencerIdOrBrandId(Long influencerId, Long brandId);
    
    @Modifying
    @Query("DELETE FROM Payment p WHERE p.campaign.id = ?1")
    void deleteByCampaignId(Long campaignId);
}

