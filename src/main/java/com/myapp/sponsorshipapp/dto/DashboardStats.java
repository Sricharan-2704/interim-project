package com.myapp.sponsorshipapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalCampaigns;
    private long activeCampaigns;
    private long totalRequests;
    private long pendingRequests;
    private Double totalEarnings;
    private Double totalSpending;
    private Double averageRating;
    private long totalUsers;
    private long totalBrands;
    private long totalInfluencers;
}

