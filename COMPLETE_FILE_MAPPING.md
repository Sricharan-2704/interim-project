# 📋 Complete Backend File Mapping & Interconnections

## QUICK REFERENCE: All 47 Java Files Explained

---

## 🎯 PACKAGE: `entity/` (10 files)
**Purpose:** Define database table structures

### Files:

#### 1. `User.java`
- **What it is:** Entity representing a user (brand, influencer, or admin)
- **Database table:** `users`
- **Fields:** id, name, email, password, role, bio, profileImage
- **Used by:** 
  - UserRepository (for database queries)
  - AuthService (for registration/login)
  - AuthController (returns user data)
  - All other services (to get user info)
- **Connects to:**
  - Role.java (user's role type)
  - Campaign.java (User can be a brand who creates campaigns)

#### 2. `Campaign.java`
- **What it is:** Entity representing a marketing campaign
- **Database table:** `campaigns`
- **Fields:** id, name, description, platform, budget, startDate, endDate, eligibility, status, brand (User reference)
- **Used by:**
  - CampaignRepository
  - CampaignService
  - CampaignController
- **Connects to:**
  - User.java (campaign creator is a User/Brand)
  - CampaignStatus.java (campaign's current status)
  - SponsorshipRequest.java (influencers request to join campaigns)

#### 3. `SponsorshipRequest.java`
- **What it is:** Entity representing a request from influencer to join a campaign
- **Database table:** `sponsorship_requests`
- **Fields:** id, influencer, campaign, message, status, requestDate
- **Used by:**
  - SponsorshipRequestRepository
  - SponsorshipService
  - SponsorshipController
- **Connects to:**
  - User.java (influencer who requested)
  - Campaign.java (which campaign they're requesting)
  - RequestStatus.java (pending, approved, rejected)

#### 4. `Payment.java`
- **What it is:** Entity representing a payment transaction
- **Database table:** `payments`
- **Fields:** id, amount, paymentDate, paymentMethod, status
- **Used by:**
  - PaymentRepository
  - PaymentService
  - PaymentController
- **Connects to:**
  - PaymentStatus.java (pending, completed, failed)
  - SponsorshipRequest.java (payment for completed sponsorship)

#### 5. `Rating.java`
- **What it is:** Entity representing a review/rating
- **Database table:** `ratings`
- **Fields:** id, rater, ratee, score, comment, ratingDate
- **Used by:**
  - RatingRepository
  - RatingService
  - RatingController
- **Connects to:**
  - User.java (who rated, who got rated)

#### 6. `Notification.java`
- **What it is:** Entity representing a message/alert sent to a user
- **Database table:** `notifications`
- **Fields:** id, recipient, message, type, isRead, createdDate
- **Used by:**
  - NotificationRepository
  - NotificationService
  - NotificationController
- **Connects to:**
  - User.java (who receives notification)

#### 7. `Role.java`
- **What it is:** Enum (special type) for user roles
- **Values:** BRAND, INFLUENCER, ADMIN
- **Used by:**
  - User.java (defines what role a user has)
  - SecurityConfig.java (to restrict access by role)
  - AuthService (to set role during registration)

#### 8. `CampaignStatus.java`
- **What it is:** Enum for campaign states
- **Values:** ACTIVE, COMPLETED, CANCELLED
- **Used by:**
  - Campaign.java (to mark campaign status)
  - CampaignService (to update campaign status)

#### 9. `PaymentStatus.java`
- **What it is:** Enum for payment states
- **Values:** PENDING, COMPLETED, FAILED, REFUNDED
- **Used by:**
  - Payment.java (to mark payment status)
  - PaymentService (to update payment status)

#### 10. `RequestStatus.java`
- **What it is:** Enum for sponsorship request states
- **Values:** PENDING, APPROVED, REJECTED, WITHDRAWN
- **Used by:**
  - SponsorshipRequest.java (to mark request status)
  - SponsorshipService (to approve/reject requests)

---

## 🎯 PACKAGE: `repository/` (6 files)
**Purpose:** Handle database read/write operations

**How it works:**
- Each repository handles ONE entity
- Spring automatically creates database queries
- Repositories are used by Services

### Files:

#### 1. `UserRepository.java`
- **Extends:** JpaRepository<User, Long>
- **Custom methods:**
  - `findByEmail(String email)` - Get user by email
  - `existsByEmail(String email)` - Check if email exists
  - `existsByNameIgnoreCase(String name)` - Check if username exists
  - `findByRole(Role role)` - Get all users with specific role
- **Used by:** AuthService, AdminService, any service needing user data
- **Talks to:** MySQL database, users table

#### 2. `CampaignRepository.java`
- **Extends:** JpaRepository<Campaign, Long>
- **Purpose:** Get/save campaigns
- **Used by:** CampaignService, CampaignController
- **Talks to:** MySQL database, campaigns table

#### 3. `SponsorshipRequestRepository.java`
- **Extends:** JpaRepository<SponsorshipRequest, Long>
- **Purpose:** Get/save sponsorship requests
- **Used by:** SponsorshipService, SponsorshipController
- **Talks to:** MySQL database, sponsorship_requests table

#### 4. `PaymentRepository.java`
- **Extends:** JpaRepository<Payment, Long>
- **Purpose:** Get/save payments
- **Used by:** PaymentService, PaymentController
- **Talks to:** MySQL database, payments table

#### 5. `RatingRepository.java`
- **Extends:** JpaRepository<Rating, Long>
- **Purpose:** Get/save ratings
- **Used by:** RatingService, RatingController
- **Talks to:** MySQL database, ratings table

#### 6. `NotificationRepository.java`
- **Extends:** JpaRepository<Notification, Long>
- **Purpose:** Get/save notifications
- **Used by:** NotificationService, NotificationController
- **Talks to:** MySQL database, notifications table

---

## 🎯 PACKAGE: `service/` (7 files)
**Purpose:** Contains business logic and rules

**How it works:**
- Each service handles ONE feature area
- Services use repositories to get data
- Services are called by controllers
- Services are injected via @Autowired

### Files:

#### 1. `AuthService.java`
- **Responsibilities:**
  - Handle user registration
  - Handle user login
  - Get current logged-in user
  - Validate credentials
  - Create JWT tokens
- **Uses:** UserRepository, PasswordEncoder, AuthenticationManager, JwtTokenProvider
- **Returns:** AuthResponse DTO
- **Called by:** AuthController
- **Logic flow:**
  1. Receive registration/login request
  2. Validate email/password
  3. Query UserRepository for existing user
  4. If creating new: save to database
  5. If logging in: verify password
  6. Generate JWT token using JwtTokenProvider
  7. Return AuthResponse with token

#### 2. `CampaignService.java`
- **Responsibilities:**
  - Create new campaigns
  - List all campaigns
  - Get specific campaign
  - Update campaign
  - Delete campaign
- **Uses:** CampaignRepository, UserRepository
- **Called by:** CampaignController
- **Logic flow:**
  1. Validate campaign data
  2. Get current user (brand)
  3. Create Campaign entity
  4. Save via CampaignRepository
  5. Return CampaignResponse DTO

#### 3. `SponsorshipService.java`
- **Responsibilities:**
  - Create sponsorship requests
  - Approve/reject requests
  - Get requests for specific campaign
  - Get requests from specific influencer
- **Uses:** SponsorshipRequestRepository, UserRepository, CampaignRepository, NotificationService
- **Called by:** SponsorshipController
- **Logic flow:**
  1. Validate influencer & campaign exist
  2. Check for duplicate requests
  3. Create SponsorshipRequest entity
  4. Save via SponsorshipRequestRepository
  5. Send notification to brand
  6. Return response

#### 4. `PaymentService.java`
- **Responsibilities:**
  - Process payments
  - Track payment status
  - Get payment history
- **Uses:** PaymentRepository, SponsorshipRequestRepository
- **Called by:** PaymentController
- **Logic flow:**
  1. Validate payment amount
  2. Create Payment entity
  3. Process payment (simulate or call payment gateway)
  4. Save via PaymentRepository
  5. Update sponsorship request status
  6. Return payment response

#### 5. `RatingService.java`
- **Responsibilities:**
  - Submit ratings/reviews
  - Get ratings for a user
  - Calculate average rating
- **Uses:** RatingRepository, UserRepository
- **Called by:** RatingController
- **Logic flow:**
  1. Validate rater & ratee exist
  2. Create Rating entity
  3. Save via RatingRepository
  4. Calculate average rating
  5. Return rating response

#### 6. `NotificationService.java`
- **Responsibilities:**
  - Send notifications
  - Get notifications for user
  - Mark as read
- **Uses:** NotificationRepository, UserRepository
- **Called by:** NotificationController + internally by other services
- **Logic flow:**
  1. Validate recipient exists
  2. Create Notification entity
  3. Save via NotificationRepository
  4. Return notification response

#### 7. `AdminService.java`
- **Responsibilities:**
  - Get dashboard statistics
  - Get all users
  - Manage campaigns
  - View all payments
- **Uses:** All repositories
- **Called by:** AdminController
- **Logic flow:**
  1. Query multiple repositories
  2. Calculate statistics
  3. Return DashboardStats DTO

---

## 🎯 PACKAGE: `controller/` (7 files)
**Purpose:** Receive HTTP requests and send responses

**How it works:**
- Each controller handles one feature
- @RequestMapping defines base URL
- @PostMapping, @GetMapping, etc. define specific endpoints
- Controllers call services
- Controllers return responses as JSON

### Files:

#### 1. `AuthController.java`
- **Base URL:** `/api/auth`
- **Endpoints:**
  - `POST /api/auth/register` - Register new user
    - Receives: RegisterRequest DTO
    - Returns: AuthResponse DTO
  - `POST /api/auth/login` - Login user
    - Receives: LoginRequest DTO
    - Returns: AuthResponse DTO
  - `GET /api/auth/me` - Get current logged-in user
    - Requires: JWT token
    - Returns: User data
- **Uses:** AuthService
- **Request handling:**
  1. Receive HTTP request with JSON body
  2. Spring automatically converts JSON to DTO
  3. Call appropriate AuthService method
  4. Return response as JSON

#### 2. `CampaignController.java`
- **Base URL:** `/api/campaign`
- **Endpoints:**
  - `POST /api/campaign` - Create new campaign
  - `GET /api/campaign/all` - Get all campaigns
  - `GET /api/campaign/{id}` - Get specific campaign
  - `PUT /api/campaign/{id}` - Update campaign
  - `DELETE /api/campaign/{id}` - Delete campaign
- **Uses:** CampaignService
- **Requires:** JWT authentication (except GET /all)

#### 3. `SponsorshipController.java`
- **Base URL:** `/api/sponsorship`
- **Endpoints:**
  - `POST /api/sponsorship` - Submit sponsorship request
  - `GET /api/sponsorship/campaign/{id}` - Get requests for campaign
  - `GET /api/sponsorship/influencer/{id}` - Get requests from influencer
  - `PUT /api/sponsorship/{id}/approve` - Brand approves request
  - `PUT /api/sponsorship/{id}/reject` - Brand rejects request
- **Uses:** SponsorshipService
- **Requires:** JWT authentication

#### 4. `PaymentController.java`
- **Base URL:** `/api/payment`
- **Endpoints:**
  - `POST /api/payment` - Make payment
  - `GET /api/payment/{id}` - Get payment details
  - `GET /api/payment/user/{userId}` - Get user's payment history
- **Uses:** PaymentService
- **Requires:** JWT authentication

#### 5. `RatingController.java`
- **Base URL:** `/api/rating`
- **Endpoints:**
  - `POST /api/rating` - Submit rating
  - `GET /api/rating/user/{userId}` - Get ratings for user
  - `GET /api/rating/{id}` - Get specific rating
- **Uses:** RatingService
- **Requires:** JWT authentication

#### 6. `NotificationController.java`
- **Base URL:** `/api/notification`
- **Endpoints:**
  - `GET /api/notification` - Get all notifications for current user
  - `PUT /api/notification/{id}/read` - Mark notification as read
  - `DELETE /api/notification/{id}` - Delete notification
- **Uses:** NotificationService
- **Requires:** JWT authentication

#### 7. `AdminController.java`
- **Base URL:** `/api/admin`
- **Endpoints:**
  - `GET /api/admin/dashboard` - Get dashboard statistics
  - `GET /api/admin/users` - Get all users
  - `GET /api/admin/campaigns` - Get all campaigns
  - `GET /api/admin/payments` - Get all payments
- **Uses:** AdminService
- **Requires:** JWT authentication + ADMIN role

---

## 🎯 PACKAGE: `dto/` (9 files)
**Purpose:** Define JSON format for communication

**What is DTO?**
- Data Transfer Object
- Simpler version of entity
- Only includes data frontend needs
- Hides sensitive data

### Files:

#### 1. `AuthResponse.java`
- **When sent:** After login or registration
- **Fields:** token, type, id, name, email, role
- **NOT included:** password (for security)
- **Sent by:** AuthController
- **Frontend receives and stores token**

#### 2. `LoginRequest.java`
- **When received:** When user tries to login
- **Fields:** email, password
- **Sent by:** Angular frontend to AuthController

#### 3. `RegisterRequest.java`
- **When received:** When user tries to sign up
- **Fields:** name, email, password, role
- **Sent by:** Angular frontend to AuthController

#### 4. `ApiResponse.java`
- **When sent:** Standard response for all endpoints
- **Fields:** success (boolean), message (String)
- **Example:** { "success": false, "message": "Email already registered" }

#### 5. `CampaignRequest.java`
- **When received:** When creating/updating campaign
- **Fields:** name, description, platform, budget, startDate, endDate, eligibility
- **Sent by:** Angular frontend to CampaignController

#### 6. `SponsorshipApplicationRequest.java`
- **When received:** When influencer applies to campaign
- **Fields:** campaignId, message
- **Sent by:** Angular frontend to SponsorshipController

#### 7. `PaymentRequest.java`
- **When received:** When making payment
- **Fields:** amount, paymentMethod, sponsorshipRequestId
- **Sent by:** Angular frontend to PaymentController

#### 8. `RatingRequest.java`
- **When received:** When submitting rating
- **Fields:** rateeId, score, comment
- **Sent by:** Angular frontend to RatingController

#### 9. `DashboardStats.java`
- **When sent:** Admin dashboard data
- **Fields:** totalUsers, totalCampaigns, totalPayments, totalRevenue
- **Sent by:** AdminController to frontend

---

## 🎯 PACKAGE: `security/` (3 files)
**Purpose:** Authentication and authorization

### Files:

#### 1. `JwtTokenProvider.java`
- **What it does:**
  - Generate JWT tokens after login
  - Validate JWT tokens on each request
  - Extract username from token
- **Key methods:**
  - `generateToken(Authentication)` - Create JWT
  - `validateToken(String)` - Check if token is valid
  - `getUsernameFromToken(String)` - Get email from token
- **Token format:** eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (contains encoded data)
- **Expires:** After configured time (e.g., 24 hours)

#### 2. `JwtAuthenticationFilter.java`
- **What it does:**
  - Intercepts every request
  - Extracts JWT from Authorization header
  - Validates JWT using JwtTokenProvider
  - Sets authenticated user in SecurityContext
  - If invalid token, request is rejected
- **Runs on:** Every HTTP request
- **Order:** Before controller receives request

#### 3. `CustomUserDetailsService.java`
- **What it does:**
  - Loads user details from database
  - Provides user info to Spring Security
  - Used during authentication process
- **Called by:** Spring Security framework
- **Returns:** UserDetails object with user info and authorities (permissions)

---

## 🎯 PACKAGE: `config/` (3 files)
**Purpose:** Application configuration and setup

### Files:

#### 1. `SecurityConfig.java`
- **What it configures:**
  - Which URLs are public (don't need JWT)
    - /api/auth/login
    - /api/auth/register
  - Which URLs need JWT protection
    - /api/campaign/*
    - /api/sponsorship/*
    - /api/payment/*
    - /api/admin/*
  - Password encryption
  - JWT filter setup
  - CORS configuration
- **No changes needed:** Usually setup once and left alone

#### 2. `CorsConfig.java`
- **What it does:**
  - Allows Angular frontend to communicate with Java backend
  - Prevents browser "CORS error"
  - Specifies which origins (domains) are allowed
- **Configured for:** localhost:4200 (Angular dev server)
- **In production:** Change to actual frontend domain

#### 3. `DataInitializer.java`
- **What it does:**
  - Loads sample data into database on startup
  - Useful for testing
  - Examples: Create sample users, campaigns, etc.
- **When runs:** Every time backend starts
- **Can be disabled:** If data already loaded

---

## 🎯 PACKAGE: `exception/` (1 file)
**Purpose:** Centralized error handling

### Files:

#### 1. `GlobalExceptionHandler.java`
- **What it does:**
  - Catches all exceptions thrown in the application
  - Converts exceptions to user-friendly JSON responses
  - Sets appropriate HTTP status codes
- **Examples:**
  - If UserRepository throws "User not found" → Return 404 NOT_FOUND
  - If validation fails → Return 400 BAD_REQUEST
  - If JWT invalid → Return 401 UNAUTHORIZED
- **Benefits:**
  - No ugly stack traces sent to frontend
  - Consistent error response format
  - Better user experience

---

## 🎯 MAIN APPLICATION FILE: (1 file)

#### `SponsorshipAppBackendApplication.java`
- **What it is:** Entry point for the entire application
- **What it does:**
  - Starts Spring Boot
  - Initializes all beans
  - Sets up all packages (controller, service, repository, etc.)
  - Starts the server on port 8080
- **How to run:**
  - `mvn spring-boot:run`
  - Or run directly from IDE
- **When it starts:**
  - All @Repository @Service @Controller classes are created
  - All @Autowired dependencies are injected
  - DataInitializer.java runs and loads sample data
  - Server is ready to receive requests

---

## 📊 COMPLETE INTERCONNECTION DIAGRAM

```
User Registration Flow:
┌─────────────────────────────────────────────────────┐
│ 1. FRONTEND (Angular)                               │
│    Sends: POST /api/auth/register                    │
│    With: RegisterRequest { name, email, pwd, role }  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ 2. AuthController.register()                        │
│    ├─ Receives RegisterRequest (DTO)                │
│    └─ Calls authService.register()                  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ 3. AuthService.register()                           │
│    ├─ Validates email                              │
│    ├─ Calls userRepository.existsByEmail()          │
│    ├─ Creates User entity                           │
│    ├─ Calls userRepository.save()                   │
│    ├─ Generates JWT via jwtTokenProvider           │
│    └─ Returns AuthResponse (DTO)                    │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ 4. UserRepository.save()                            │
│    ├─ Converts User entity to SQL                   │
│    ├─ Executes: INSERT INTO users (...)             │
│    └─ Returns saved User with ID                    │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ 5. MySQL Database                                   │
│    ├─ Inserts row in 'users' table                  │
│    └─ Generates ID (1, 2, 3, ...)                   │
└──────────────────┬──────────────────────────────────┘
                   │
              (Data returns up)
                   │
┌──────────────────▼──────────────────────────────────┐
│ 6. AuthController.register() returns:              │
│    HTTP 200 OK + AuthResponse (JSON)               │
│    {                                                │
│      "token": "eyJhbGciOiJIUzI1NiIs...",           │
│      "id": 1,                                       │
│      "name": "John",                                │
│      "email": "john@example.com",                   │
│      "role": "INFLUENCER"                           │
│    }                                                │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ 7. FRONTEND (Angular)                               │
│    ├─ Receives response                             │
│    ├─ Stores token in localStorage                  │
│    ├─ Stores user info                              │
│    └─ Redirects to /dashboard                       │
└─────────────────────────────────────────────────────┘
```

---

## 🔗 Dependency Graph

```
SponsorshipAppBackendApplication
└─ Initializes Spring Boot
   ├─ Creates all @Repository beans
   │  ├─ UserRepository (for entity User)
   │  ├─ CampaignRepository (for entity Campaign)
   │  ├─ SponsorshipRequestRepository
   │  ├─ PaymentRepository
   │  ├─ RatingRepository
   │  └─ NotificationRepository
   │
   ├─ Creates all @Service beans
   │  ├─ AuthService
   │  │  └─ @Autowired UserRepository, JwtTokenProvider, etc.
   │  ├─ CampaignService
   │  │  └─ @Autowired CampaignRepository, UserRepository
   │  ├─ SponsorshipService
   │  │  └─ @Autowired SponsorshipRequestRepository, NotificationService
   │  ├─ PaymentService
   │  │  └─ @Autowired PaymentRepository
   │  ├─ RatingService
   │  │  └─ @Autowired RatingRepository
   │  ├─ NotificationService
   │  │  └─ @Autowired NotificationRepository
   │  └─ AdminService
   │     └─ @Autowired All repositories
   │
   ├─ Creates all @Controller beans
   │  ├─ AuthController
   │  │  └─ @Autowired AuthService
   │  ├─ CampaignController
   │  │  └─ @Autowired CampaignService
   │  ├─ SponsorshipController
   │  │  └─ @Autowired SponsorshipService
   │  ├─ PaymentController
   │  │  └─ @Autowired PaymentService
   │  ├─ RatingController
   │  │  └─ @Autowired RatingService
   │  ├─ NotificationController
   │  │  └─ @Autowired NotificationService
   │  └─ AdminController
   │     └─ @Autowired AdminService
   │
   ├─ Creates Security beans
   │  ├─ JwtTokenProvider
   │  ├─ JwtAuthenticationFilter
   │  └─ CustomUserDetailsService
   │
   └─ Runs DataInitializer
      └─ Loads sample data
```

---

## ✅ Summary

**Total Files:** 47 Java files across 8 packages

**Data Flow Path:**
Frontend → Controller → Service → Repository → Entity → Database

**Every feature follows this pattern:**
1. Click something in Angular frontend
2. Frontend sends HTTP request to controller
3. Controller calls service
4. Service calls repository
5. Repository queries database
6. Response comes back: Database → Repository → Service → Controller → Frontend (as JSON)

**Every file exists for a reason:**
- **Entity:** Define data structure
- **Repository:** Access data
- **Service:** Process data with logic
- **Controller:** Expose via HTTP
- **DTO:** Communicate with frontend
- **Security:** Verify users
- **Config:** Setup application

You now understand how all 47 files work together! 🎉
