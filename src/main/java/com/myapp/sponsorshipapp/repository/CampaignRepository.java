package com.myapp.sponsorshipapp.repository;

import com.myapp.sponsorshipapp.entity.Campaign;
import com.myapp.sponsorshipapp.entity.CampaignStatus;
import com.myapp.sponsorshipapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByBrand(User brand);
    List<Campaign> findByStatus(CampaignStatus status);
    
    @Query("SELECT c FROM Campaign c WHERE c.brand.id = ?1")
    List<Campaign> findByBrandId(Long brandId);
    
    @Modifying
    @Query("DELETE FROM Campaign c WHERE c.brand.id = ?1")
    void deleteByBrandId(Long brandId);
    
    @Query("SELECT c FROM Campaign c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:platform IS NULL OR c.platform = :platform) AND " +
           "(:status IS NULL OR c.status = :status)")
    List<Campaign> searchCampaigns(@Param("name") String name, 
                                   @Param("platform") String platform, 
                                   @Param("status") CampaignStatus status);
    
    // Find campaigns that have expired (end date passed but still marked as active)
    @Query("SELECT c FROM Campaign c WHERE c.endDate < ?1 AND c.status = 'ACTIVE'")
    List<Campaign> findExpiredActiveCampaigns(LocalDate date);
}

