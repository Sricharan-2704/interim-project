# Sponsorship App Backend - Complete Java Architecture Guide

## 📋 Executive Summary

This is a **Spring Boot-based REST API** for managing influencer sponsorship campaigns. The backend follows a **layered architecture** with clear separation of concerns:
- **Controllers** - Handle HTTP requests/responses
- **Services** - Contain business logic
- **Repositories** - Handle data persistence
- **Entities** - Define database models
- **DTOs** - Data transfer objects for API contracts
- **Security** - JWT-based authentication
- **Config** - Application configuration

---

## 🏗️ Complete Package Structure

```
com.myapp.sponsorshipapp/
├── SponsorshipAppBackendApplication.java    # Main Spring Boot entry point
├── entity/                                   # Database entities/models
├── service/                                  # Business logic layer
├── controller/                               # REST API endpoints
├── repository/                               # Data access layer
├── dto/                                      # Data transfer objects
├── security/                                 # JWT authentication
├── config/                                   # Application configuration
└── exception/                                # Global exception handling
```

---

## 🗄️ Database Entities (JPA Models)

### 1. **User** Entity
**Purpose**: Represents app users (Admin, Brand, Influencer)

```java
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank @Column(unique = true)
    private String name;                    // Unique username
    
    @Email @NotBlank @Column(unique = true)
    private String email;                   // Unique email
    
    @NotBlank
    private String password;                // Hashed password (BCrypt)
    
    @Enumerated(EnumType.STRING)
    private Role role;                      // ADMIN, BRAND, INFLUENCER
    
    private String bio;                     // User biography
    private String profileImage;            // Profile picture URL
}

// Role Enum
public enum Role {
    ADMIN,                                  // System administrator
    BRAND,                                  // Campaign creator/sponsor
    INFLUENCER                              // Content creator/applicant
}
```

**Relationships**:
- One-to-Many: User → Campaign (as brand)
- One-to-Many: User → SponsorshipRequest (as influencer)
- One-to-Many: User → Payment (influencer/brand)
- One-to-Many: User → Notification
- One-to-Many: User → Rating (rater/rated)

---

### 2. **Campaign** Entity
**Purpose**: Represents sponsorship campaigns created by brands

```java
@Entity @Table(name = "campaigns")
public class Campaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;                    // Campaign name
    
    @Column(length = 2000)
    private String description;             // Detailed description
    
    private String platform;                // Instagram, YouTube, TikTok, etc.
    
    @Positive
    private Double budget;                  // Budget allocation
    
    private LocalDate startDate;            // Campaign start date
    private LocalDate endDate;              // Campaign end date
    
    private String eligibility;             // Requirements for influencers
    
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;          // ACTIVE, PAUSED, COMPLETED, CANCELLED, EXPIRED
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private User brand;                     // Campaign creator (Brand)
}

// Status Enum
public enum CampaignStatus {
    ACTIVE,                                 // Open for applications
    PAUSED,                                 // Temporarily paused
    COMPLETED,                              // Campaign finished
    CANCELLED,                              // Cancelled by brand
    EXPIRED                                 // End date has passed
}
```

---

### 3. **SponsorshipRequest** Entity
**Purpose**: Represents influencer applications for campaigns

```java
@Entity @Table(name = "sponsorship_requests")
public class SponsorshipRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "influencer_id")
    private User influencer;                // Applying influencer
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;              // Target campaign
    
    @Column(length = 2000)
    private String proposal;                // Influencer's pitch/proposal
    
    @Enumerated(EnumType.STRING)
    private RequestStatus status;           // Application status
    
    @Column(length = 2000)
    private String workDescription;         // Description of work done
    
    private LocalDateTime workSubmittedAt;  // When work was submitted
    private LocalDateTime workCompletedAt;  // When brand approved work
    
    private LocalDateTime createdAt;        // Application timestamp
    private LocalDateTime updatedAt;        // Last update timestamp
}

// Status Enum
public enum RequestStatus {
    PENDING,                                // Awaiting brand decision
    ACCEPTED,                               // Approved by brand
    REJECTED,                               // Rejected by brand
    COMPLETED                               // Work done & approved, payment processed
}
```

---

### 4. **Payment** Entity
**Purpose**: Tracks payments from brands to influencers

```java
@Entity @Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;              // Associated campaign
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "influencer_id")
    private User influencer;                // Receiving payments
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private User brand;                     // Paying brand
    
    @Positive
    private Double amount;                  // Payment amount in USD
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;           // PENDING, COMPLETED, FAILED, REFUNDED
    
    private LocalDateTime createdAt;        // Payment creation time
    private LocalDateTime paidAt;           // Actual payment time
    
    private String transactionId;           // Unique transaction ID (UUID)
}

// Status Enum
public enum PaymentStatus {
    PENDING,                                // Awaiting payment processing
    COMPLETED,                              // Payment successfully processed
    FAILED,                                 // Payment failed
    REFUNDED                                // Payment refunded
}
```

---

### 5. **Rating** Entity
**Purpose**: User ratings/reviews after campaign completion

```java
@Entity @Table(name = "ratings")
public class Rating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;              // Campaign context
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rater_id")
    private User rater;                     // Person giving rating
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rated_id")
    private User rated;                     // Person being rated
    
    @Min(1) @Max(5)
    private Integer score;                  // Rating 1-5 stars
    
    @Column(length = 1000)
    private String feedback;                // Review comment
    
    private LocalDateTime createdAt;        // Rating timestamp
}
```

---

### 6. **Notification** Entity
**Purpose**: Real-time notifications to users

```java
@Entity @Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;                      // Recipient
    
    private String title;                   // Notification title
    private String message;                 // Notification message
    
    private boolean isRead;                 // Read status
    
    private LocalDateTime createdAt;        // Creation timestamp
}
```

---

## 🔐 Security Architecture

### JWT Token Flow

```
1. User Registration/Login
   └─→ JwtTokenProvider generates JWT token
   
2. Client stores token in localStorage
   └─→ Includes "Authorization: Bearer {token}" in requests
   
3. JwtAuthenticationFilter intercepts requests
   └─→ Validates token
   └─→ Extracts username from token
   └─→ Loads UserDetails via CustomUserDetailsService
   └─→ Sets Spring Security context
   
4. @PreAuthorize checks role-based access control (RBAC)
   └─→ Routes to appropriate controller
```

### Security Classes

#### **JwtTokenProvider**
```java
@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;               // 64-char secret key
    
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;             // 86400000 ms = 24 hours
    
    // Generates JWT token from Authentication
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userDetails.getUsername())  // email
                .issuedAt(new Date())
                .expiration(new Date(now + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    // Extracts email from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
    
    // Validates token signature and expiration
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

#### **JwtAuthenticationFilter**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Extract JWT from "Authorization: Bearer {token}"
        String jwt = getJwtFromRequest(request);  // substring after "Bearer "
        
        // 2. Validate token
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            // 3. Extract username (email)
            String username = tokenProvider.getUsernameFromToken(jwt);
            
            // 4. Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 5. Create authentication token
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // 6. Set in Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " = 7 chars
        }
        return null;
    }
}
```

#### **CustomUserDetailsService**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Convert to Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            )
        );
    }
}
```

#### **SecurityConfig**
```java
@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Stateless session management (JWT doesn't need sessions)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // URL-based authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()           // Public auth endpoints
                .requestMatchers("/h2-console/**").permitAll()         // H2 console
                .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Admin-only
                .requestMatchers("/api/brand/**").hasRole("BRAND")     // Brand-only
                .requestMatchers("/api/influencer/**").hasRole("INFLUENCER")
                .anyRequest().authenticated()                          // All others require auth
            )
            
            // Add JWT filter before standard auth filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt hash with salt
    }
}
```

---

## 📡 REST API Endpoints

### Auth Controller `/api/auth`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user (Brand/Influencer) |
| POST | `/login` | Authenticate user, return JWT |
| GET | `/me` | Get current authenticated user |
| POST | `/change-password` | Change user password |

**Example Request/Response**:
```bash
POST /api/auth/register
{
  "name": "John Influencer",
  "email": "john@example.com",
  "password": "Pass@123",
  "role": "INFLUENCER"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "name": "John Influencer",
  "email": "john@example.com",
  "role": "INFLUENCER"
}
```

---

### Campaign Controller `/api/campaigns` (Brand)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create campaign (Brand) |
| PUT | `/{id}` | Update campaign (Brand owner) |
| DELETE | `/{id}` | Delete campaign (Brand owner) |
| GET | `/{id}` | Get campaign details |
| GET | `/` | Get all campaigns |
| GET | `/active` | Get only active campaigns |
| GET | `/my-campaigns` | Get signed-in brand's campaigns |
| GET | `/search` | Search campaigns (name, platform, status) |
| PUT | `/{id}/status` | Update campaign status |

**Campaign Creation Flow**:
```
1. Brand sends POST /api/campaigns with CampaignRequest
2. CampaignController.createCampaign() validates request
3. CampaignService.createCampaign():
   - Gets current authenticated brand (Brand user)
   - Creates Campaign entity
   - Sets brand, status=ACTIVE, dates
   - Saves to DB
4. Returns Campaign with ID
```

---

### Sponsorship Controller `/api/sponsorship` (Influencer Applications)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/apply` | Submit application to campaign |
| PUT | `/{id}/status` | Update status (Accept/Reject) - Brand |
| GET | `/{id}` | Get request details |
| GET | `/my-applications` | Get influencer's applications |
| GET | `/brand-requests` | Get brand's received applications |
| GET | `/campaign/{campaignId}` | Get all applications for campaign |
| POST | `/{id}/submit-work` | Influencer submits work |
| PUT | `/{id}/mark-work-complete` | Brand approves work |

**Application Workflow**:
```
1. Influencer: POST /api/sponsorship/apply
   └─→ SponsorshipService creates SponsorshipRequest (status=PENDING)
   └─→ Notifies brand
   
2. Brand: PUT /api/sponsorship/{id}/status?status=ACCEPTED
   └─→ Updates request status
   └─→ Notifies influencer
   
3. Influencer: POST /api/sponsorship/{id}/submit-work
   └─→ Sets workDescription + workSubmittedAt
   └─→ Notifies brand
   
4. Brand: PUT /api/sponsorship/{id}/mark-work-complete
   └─→ Sets workCompletedAt
   └─→ Notifies influencer
   └─→ Ready for payment
```

---

### Payment Controller `/api/payments`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create/process payment (Brand) |
| PUT | `/{id}/complete` | Mark payment complete |
| GET | `/{id}` | Get payment details |
| GET | `/influencer` | Get influencer's payments |
| GET | `/brand` | Get brand's payments |
| GET | `/earnings` | Get influencer earnings |
| GET | `/spending` | Get brand spending |

**Payment Flow**:
```
1. Brand: POST /api/payments
   PaymentRequest: { campaignId, influencerId, amount }
   
2. PaymentService.createPayment():
   - Validates work is completed (workCompletedAt != null)
   - Creates Payment with transactionId (UUID)
   - Sets status=COMPLETED, paidAt=now
   - Campaign status → COMPLETED
   - SponsorshipRequest status → COMPLETED
   - Notifies influencer
```

---

### Rating Controller `/api/ratings`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Submit rating |
| GET | `/user/{userId}` | Get user's received ratings |
| GET | `/my-ratings` | Get ratings given by current user |
| GET | `/average/{userId}` | Get user's average rating |

---

### Notification Controller `/api/notifications`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all notifications (latest first) |
| GET | `/unread` | Get unread notifications |
| GET | `/unread-count` | Get count of unread |
| PUT | `/{id}/read` | Mark notification as read |
| PUT | `/mark-all-read` | Mark all as read |

---

### Admin Controller `/api/admin` (Admin-only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/stats` | Get dashboard statistics |
| GET | `/users` | List all users |
| GET | `/users/role/{role}` | Get users by role |
| DELETE | `/users/{id}` | Delete user (cascading) |
| GET | `/campaigns` | List all campaigns |
| GET | `/requests` | List all sponsorship requests |
| GET | `/payments` | List all payments |
| GET | `/ratings` | List all ratings |

---

## 🧠 Service Layer (Business Logic)

### AuthService
```java
@Service
public class AuthService {
    
    public AuthResponse register(RegisterRequest request) {
        // Normalize inputs
        String normalizedName = request.getName().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        
        // Validate uniqueness
        if (userRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create user
        User user = new User();
        user.setName(normalizedName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // BCrypt
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        userRepository.save(user);
        
        // Generate JWT
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );
        String token = tokenProvider.generateToken(authentication);
        
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
    
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().trim().toLowerCase(), 
                request.getPassword()
            )
        );
        
        // Generate token and fetch user
        String token = tokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
    
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
```

### CampaignService
```java
@Service
public class CampaignService {
    
    public Campaign createCampaign(CampaignRequest request) {
        User brand = authService.getCurrentUser();
        
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setPlatform(request.getPlatform());
        campaign.setBudget(request.getBudget());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setEligibility(request.getEligibility());
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setBrand(brand);
        
        return campaignRepository.save(campaign);
    }
    
    public List<Campaign> getActiveCampaigns() {
        updateExpiredCampaigns();  // Auto-expire campaigns past endDate
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE);
    }
    
    private void updateExpiredCampaigns() {
        List<Campaign> expiredCampaigns = campaignRepository
                .findExpiredActiveCampaigns(LocalDate.now());
        for (Campaign campaign : expiredCampaigns) {
            campaign.setStatus(CampaignStatus.EXPIRED);
            campaignRepository.save(campaign);
        }
    }
}
```

### SponsorshipService
```java
@Service
public class SponsorshipService {
    
    public SponsorshipRequest applyForCampaign(SponsorshipApplicationRequest request) {
        User influencer = authService.getCurrentUser();
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // Prevent duplicate applications
        if (requestRepository.existsByInfluencerAndCampaign(influencer, campaign)) {
            throw new RuntimeException("You have already applied for this campaign");
        }
        
        SponsorshipRequest sponsorshipRequest = new SponsorshipRequest();
        sponsorshipRequest.setInfluencer(influencer);
        sponsorshipRequest.setCampaign(campaign);
        sponsorshipRequest.setProposal(request.getProposal());
        sponsorshipRequest.setStatus(RequestStatus.PENDING);
        sponsorshipRequest.setCreatedAt(LocalDateTime.now());
        
        SponsorshipRequest saved = requestRepository.save(sponsorshipRequest);
        
        // Notify brand of new application
        notificationService.createNotification(
            campaign.getBrand(),
            "New Application",
            influencer.getName() + " applied for campaign: " + campaign.getName()
        );
        
        return saved;
    }
    
    public SponsorshipRequest submitWork(Long requestId, String workDescription) {
        SponsorshipRequest request = getRequestById(requestId);
        User currentUser = authService.getCurrentUser();
        
        // Only influencer can submit work
        if (!request.getInfluencer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the influencer can submit work");
        }
        
        // Only for ACCEPTED requests
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new RuntimeException("Work can only be submitted for accepted sponsorships");
        }
        
        // No duplicate submissions
        if (request.getWorkSubmittedAt() != null) {
            throw new RuntimeException("Work has already been submitted");
        }
        
        request.setWorkDescription(workDescription);
        request.setWorkSubmittedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        SponsorshipRequest updated = requestRepository.save(request);
        
        // Notify brand about submission
        notificationService.createNotification(
            request.getCampaign().getBrand(),
            "Work Submitted",
            request.getInfluencer().getName() + " submitted work for: " + request.getCampaign().getName()
        );
        
        return updated;
    }
    
    public SponsorshipRequest markWorkAsComplete(Long requestId) {
        SponsorshipRequest request = getRequestById(requestId);
        User currentUser = authService.getCurrentUser();
        
        // Only brand can complete
        if (!request.getCampaign().getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only campaign owner can complete work");
        }
        
        request.setWorkCompletedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        SponsorshipRequest updated = requestRepository.save(request);
        
        // Notify influencer - ready for payment!
        notificationService.createNotification(
            request.getInfluencer(),
            "Work Approved",
            "Your work approved! Payment can now be processed."
        );
        
        return updated;
    }
}
```

### PaymentService
```java
@Service
public class PaymentService {
    
    public Payment createPayment(PaymentRequest request) {
        User brand = authService.getCurrentUser();
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        User influencer = userRepository.findById(request.getInfluencerId())
                .orElseThrow(() -> new RuntimeException("Influencer not found"));
        
        // Must have completed work first
        SponsorshipRequest sponsorshipRequest = sponsorshipRequestRepository
                .findByInfluencerAndCampaign(influencer, campaign)
                .orElseThrow(() -> new RuntimeException("No sponsorship request found"));
        
        if (sponsorshipRequest.getWorkCompletedAt() == null) {
            throw new RuntimeException("Work must be completed before payment");
        }
        
        // Prevent duplicate payments
        if (paymentRepository.findByCampaignAndInfluencer(campaign, influencer).stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.COMPLETED)) {
            throw new RuntimeException("Payment already made for this sponsorship");
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.setCampaign(campaign);
        payment.setBrand(brand);
        payment.setInfluencer(influencer);
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId(UUID.randomUUID().toString());
        
        Payment saved = paymentRepository.save(payment);
        
        // Mark campaign as COMPLETED
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);
        
        // Mark sponsorship request as COMPLETED
        sponsorshipRequest.setStatus(RequestStatus.COMPLETED);
        sponsorshipRequest.setUpdatedAt(LocalDateTime.now());
        sponsorshipRequestRepository.save(sponsorshipRequest);
        
        // Notify influencer of payment
        notificationService.createNotification(
            influencer,
            "Payment Received!",
            "You received $" + request.getAmount() + " for: " + campaign.getName()
        );
        
        return saved;
    }
    
    public Double getInfluencerEarnings() {
        User influencer = authService.getCurrentUser();
        Double earnings = paymentRepository.getTotalEarningsByInfluencer(influencer);
        return earnings != null ? earnings : 0.0;
    }
}
```

### RatingService
```java
@Service
public class RatingService {
    
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
        rating.setScore(request.getScore());  // 1-5 stars
        rating.setFeedback(request.getFeedback());
        rating.setCreatedAt(LocalDateTime.now());
        
        Rating saved = ratingRepository.save(rating);
        
        // Notify rated user
        notificationService.createNotification(
            rated,
            "New Rating",
            "You received " + request.getScore() + " stars from " + rater.getName()
        );
        
        return saved;
    }
    
    public Double getAverageRating(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Double avg = ratingRepository.getAverageRatingForUser(user);
        return avg != null ? avg : 0.0;
    }
}
```

### NotificationService
```java
@Service
public class NotificationService {
    
    public Notification createNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getUserNotifications() {
        User user = authService.getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);  // Latest first
    }
    
    public long getUnreadCount() {
        User user = authService.getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
```

### AdminService
```java
@Service
public class AdminService {
    
    public DashboardStats getAdminStats() {
        DashboardStats stats = new DashboardStats();
        
        stats.setTotalUsers(userRepository.count());
        stats.setTotalBrands(userRepository.findByRole(Role.BRAND).size());
        stats.setTotalInfluencers(userRepository.findByRole(Role.INFLUENCER).size());
        stats.setTotalCampaigns(campaignRepository.count());
        stats.setActiveCampaigns(campaignRepository.findByStatus(CampaignStatus.ACTIVE).size());
        stats.setTotalRequests(requestRepository.count());
        stats.setPendingRequests(requestRepository.findByStatus(RequestStatus.PENDING).size());
        
        // Sum all completed payments
        Double totalPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
        stats.setTotalEarnings(totalPayments);
        
        return stats;
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete cascade: notifications → ratings → payments → requests → campaigns
        notificationRepository.deleteByUserId(userId);
        ratingRepository.deleteByRaterIdOrRatedId(userId, userId);
        paymentRepository.deleteByInfluencerIdOrBrandId(userId, userId);
        requestRepository.deleteByInfluencerId(userId);
        
        if (user.getRole() == Role.BRAND) {
            List<Campaign> campaigns = campaignRepository.findByBrandId(userId);
            for (Campaign campaign : campaigns) {
                requestRepository.deleteByCampaignId(campaign.getId());
                paymentRepository.deleteByCampaignId(campaign.getId());
                ratingRepository.deleteByCampaignId(campaign.getId());
            }
            campaignRepository.deleteByBrandId(userId);
        }
        
        userRepository.deleteById(userId);
    }
}
```

---

## 📊 Repository Layer (Data Access)

```java
// UserRepository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNameIgnoreCase(String name);
    List<User> findByRole(Role role);
}

// CampaignRepository
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByBrand(User brand);
    List<Campaign> findByStatus(CampaignStatus status);
    
    @Query("SELECT c FROM Campaign c WHERE c.endDate < ?1 AND c.status = 'ACTIVE'")
    List<Campaign> findExpiredActiveCampaigns(LocalDate date);
    
    @Query("SELECT c FROM Campaign c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:platform IS NULL OR c.platform = :platform) AND " +
           "(:status IS NULL OR c.status = :status)")
    List<Campaign> searchCampaigns(@Param("name") String name, 
                                   @Param("platform") String platform, 
                                   @Param("status") CampaignStatus status);
}

// SponsorshipRequestRepository
@Repository
public interface SponsorshipRequestRepository extends JpaRepository<SponsorshipRequest, Long> {
    List<SponsorshipRequest> findByInfluencer(User influencer);
    List<SponsorshipRequest> findByCampaign(Campaign campaign);
    List<SponsorshipRequest> findByCampaignBrand(User brand);
    Optional<SponsorshipRequest> findByInfluencerAndCampaign(User influencer, Campaign campaign);
    boolean existsByInfluencerAndCampaign(User influencer, Campaign campaign);
}

// PaymentRepository
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInfluencer(User influencer);
    List<Payment> findByBrand(User brand);
    List<Payment> findByCampaignAndInfluencer(Campaign campaign, User influencer);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.influencer = :influencer AND p.status = 'COMPLETED'")
    Double getTotalEarningsByInfluencer(@Param("influencer") User influencer);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.brand = :brand AND p.status = 'COMPLETED'")
    Double getTotalSpendingByBrand(@Param("brand") User brand);
}

// RatingRepository
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRated(User rated);
    List<Rating> findByRater(User rater);
    
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.rated = :user")
    Double getAverageRatingForUser(@Param("user") User user);
}

// NotificationRepository
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    long countByUserAndIsReadFalse(User user);
}
```

---

## 📝 Data Transfer Objects (DTOs)

```java
// Request DTOs

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3)
    private String name;
    
    @Email @NotBlank
    private String email;
    
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*@).{6,}$",
             message = "Must contain letter, digit, @, min 6 chars")
    private String password;
    
    @NotBlank
    private String role;  // BRAND, INFLUENCER
}

@Data
public class LoginRequest {
    @Email @NotBlank
    private String email;
    
    @NotBlank
    private String password;
}

@Data
public class CampaignRequest {
    @NotBlank
    private String name;
    
    private String description;
    private String platform;
    
    @Positive
    private Double budget;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String eligibility;
}

@Data
public class SponsorshipApplicationRequest {
    private Long campaignId;
    private String proposal;       // Influencer's pitch
}

@Data
public class PaymentRequest {
    private Long campaignId;
    private Long influencerId;
    
    @Positive
    private Double amount;
}

@Data
public class RatingRequest {
    private Long campaignId;
    private Long ratedUserId;
    
    @Min(1) @Max(5)
    private Integer score;
    
    private String feedback;
}

// Response DTOs

@Data @AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;      // Can be campaign, user, list, etc.
}

@Data @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String role;
}

@Data @AllArgsConstructor
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
```

---

## ⚙️ Configuration

### application.properties
```properties
# Server
server.port=7070

# Database (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/sponsorshipdb?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update      # Auto-create/update tables

# JWT
app.jwt.secret=mySecretKey123456789...    # 64+ char key
app.jwt.expiration=86400000                # 24 hours in milliseconds

# CORS
app.cors.allowed-origins=http://localhost:4200
```

### DataInitializer (Sample Data)
On startup, creates:
- Admin user: `admin@sponsorship.com` / `admin123`
- Sample Brand: `brand@example.com` with 2 campaigns
- Sample Influencer: `influencer@example.com`

---

## 🔄 Complete Request Flow Example

### Campaign Application to Payment Flow

```
1. BRAND CREATES CAMPAIGN
   └─→ POST /api/campaigns
   └─→ CampaignController → CampaignService
   └─→ Campaign saved (status=ACTIVE, brand=current user)

2. INFLUENCER APPLIES
   └─→ POST /api/sponsorship/apply
   └─→ SponsorshipController → SponsorshipService
   └─→ SponsorshipRequest saved (status=PENDING)
   └─→ Brand receives notification

3. BRAND REVIEWS & ACCEPTS
   └─→ PUT /api/sponsorship/{id}/status?status=ACCEPTED
   └─→ Request status → ACCEPTED
   └─→ Influencer receives notification

4. INFLUENCER SUBMITS WORK
   └─→ POST /api/sponsorship/{id}/submit-work
   └─→ workDescription + workSubmittedAt set
   └─→ Brand receives notification

5. BRAND APPROVES WORK
   └─→ PUT /api/sponsorship/{id}/mark-work-complete
   └─→ workCompletedAt set
   └─→ Influencer notified (ready for payment)

6. BRAND PROCESSES PAYMENT
   └─→ POST /api/payments
   └─→ PaymentService validates work is complete
   └─→ Payment created (status=COMPLETED)
   └─→ Campaign status → COMPLETED
   └─→ Request status → COMPLETED
   └─→ Influencer receives payment notification
   └─→ Brand can see in spending, influencer in earnings
```

---

## 🛡️ Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
```

---

## 📦 Dependencies (Spring Boot 3.2.5)

```xml
<!-- Web & REST -->
spring-boot-starter-web

<!-- Data Access -->
spring-boot-starter-data-jpa
mysql-connector-j

<!-- Security -->
spring-boot-starter-security
jjwt-api (JWT tokens)

<!-- Validation -->
spring-boot-starter-validation

<!-- Utilities -->
lombok (boilerplate reduction)

<!-- Testing -->
spring-boot-starter-test
spring-security-test
```

---

## 🚀 To Run the Application

```bash
# Prerequisites
# - MySQL running on localhost:3306
# - Database credentials in application.properties

# Build & Run
mvn clean install
mvn spring-boot:run

# Or
java -jar target/sponsorship-app-backend-0.0.1-SNAPSHOT.jar

# Server runs on http://localhost:7070
# Frontend (Angular) connects from http://localhost:4200
```

---

## 🎯 Key Design Patterns

| Pattern | Usage |
|---------|-------|
| **Layered Architecture** | Controllers → Services → Repositories |
| **Repository Pattern** | Data abstraction via JpaRepository |
| **DTO Pattern** | Request/Response validation & transformation |
| **JWT Bearer Authentication** | Stateless security token |
| **Role-Based Access Control (RBAC)** | @PreAuthorize for authorization |
| **Service Layer** | Business logic encapsulation |
| **Global Exception Handling** | Centralized error management |
| **Cascade Deletion** | Related data cleanup on user delete |

---

## 📈 Database Schema Relationships

```
User (1) ------- (Many) Campaign
         │
         ├─── (Many) SponsorshipRequest
         │
         ├─── (Many) Payment
         │
         ├─── (Many) Notification
         │
         └─── (Many) Rating

Campaign (1) ------- (Many) SponsorshipRequest
        │             
        ├─── (Many) Payment
        │
        └─── (Many) Rating

SponsorshipRequest (Many-to-1) User
                   (Many-to-1) Campaign

Payment (Many-to-1) Campaign
        (Many-to-1) User (influencer)
        (Many-to-1) User (brand)

Rating (Many-to-1) Campaign
       (Many-to-1) User (rater)
       (Many-to-1) User (rated)

Notification (Many-to-1) User
```

---

This architecture ensures:
✅ **Separation of Concerns** - Each layer has single responsibility
✅ **Security** - JWT + role-based endpoint protection
✅ **Scalability** - Stateless API, easy to deploy horizontally
✅ **Maintainability** - Clear structure and well-defined interactions
✅ **Data Integrity** - Cascading deletes, transaction management
✅ **User Experience** - Notifications for important events
