package com.myapp.sponsorshipapp.config;

import com.myapp.sponsorshipapp.entity.*;
import com.myapp.sponsorshipapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        if (!userRepository.existsByEmail("admin@sponsorship.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@sponsorship.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user created: admin@sponsorship.com / admin123");
        }
        
        // Create sample brand
        if (!userRepository.existsByEmail("brand@example.com")) {
            User brand = new User();
            brand.setName("Sample Brand");
            brand.setEmail("brand@example.com");
            brand.setPassword(passwordEncoder.encode("brand123"));
            brand.setRole(Role.BRAND);
            brand.setBio("A leading fashion brand looking for influencers");
            userRepository.save(brand);
            System.out.println("Brand user created: brand@example.com / brand123");
            
            // Create sample campaigns
            Campaign campaign1 = new Campaign();
            campaign1.setName("Summer Fashion Campaign");
            campaign1.setDescription("Promote our new summer collection on social media");
            campaign1.setPlatform("Instagram");
            campaign1.setBudget(5000.0);
            campaign1.setStartDate(LocalDate.now());
            campaign1.setEndDate(LocalDate.now().plusMonths(2));
            campaign1.setEligibility("Minimum 10K followers");
            campaign1.setStatus(CampaignStatus.ACTIVE);
            campaign1.setBrand(brand);
            campaignRepository.save(campaign1);
            
            Campaign campaign2 = new Campaign();
            campaign2.setName("Product Review Campaign");
            campaign2.setDescription("Review and showcase our latest tech gadgets");
            campaign2.setPlatform("YouTube");
            campaign2.setBudget(10000.0);
            campaign2.setStartDate(LocalDate.now());
            campaign2.setEndDate(LocalDate.now().plusMonths(3));
            campaign2.setEligibility("Tech-focused channel with 50K+ subscribers");
            campaign2.setStatus(CampaignStatus.ACTIVE);
            campaign2.setBrand(brand);
            campaignRepository.save(campaign2);
            
            System.out.println("Sample campaigns created");
        }
        
        // Create sample influencer
        if (!userRepository.existsByEmail("influencer@example.com")) {
            User influencer = new User();
            influencer.setName("Sample Influencer");
            influencer.setEmail("influencer@example.com");
            influencer.setPassword(passwordEncoder.encode("influencer123"));
            influencer.setRole(Role.INFLUENCER);
            influencer.setBio("Fashion and lifestyle content creator with 100K followers");
            userRepository.save(influencer);
            System.out.println("Influencer user created: influencer@example.com / influencer123");
        }
    }
}

