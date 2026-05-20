package com.myapp.sponsorshipapp.repository;

import com.myapp.sponsorshipapp.entity.Campaign;
import com.myapp.sponsorshipapp.entity.RequestStatus;
import com.myapp.sponsorshipapp.entity.SponsorshipRequest;
import com.myapp.sponsorshipapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SponsorshipRequestRepository extends JpaRepository<SponsorshipRequest, Long> {
    List<SponsorshipRequest> findByInfluencer(User influencer);
    List<SponsorshipRequest> findByCampaign(Campaign campaign);
    List<SponsorshipRequest> findByStatus(RequestStatus status);
    List<SponsorshipRequest> findByCampaignBrand(User brand);
    Optional<SponsorshipRequest> findByInfluencerAndCampaign(User influencer, Campaign campaign);
    boolean existsByInfluencerAndCampaign(User influencer, Campaign campaign);
    
    @Modifying
    @Query("DELETE FROM SponsorshipRequest r WHERE r.influencer.id = ?1")
    void deleteByInfluencerId(Long influencerId);
    
    @Modifying
    @Query("DELETE FROM SponsorshipRequest r WHERE r.campaign.id = ?1")
    void deleteByCampaignId(Long campaignId);
}

