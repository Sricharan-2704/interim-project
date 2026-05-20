# Sponsorship App - Complete Project Structure Analysis

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Backend Structure](#backend-structure)
3. [Frontend Structure](#frontend-structure)
4. [Key Features & Flows](#key-features--flows)
5. [Security Implementation](#security-implementation)
6. [Data Models & Relationships](#data-models--relationships)
7. [Important Files Reference](#important-files-reference)

---

## Architecture Overview

### Technology Stack

**Backend:**
- **Framework**: Spring Boot 3.2
- **Security**: Spring Security + JWT (JSON Web Tokens)
- **ORM**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.0 (currently uses H2 in development)
- **Port**: 7070
- **Build Tool**: Maven

**Frontend:**
- **Framework**: Angular 17
- **UI Library**: Angular Material
- **State Management**: RxJS (Observables)
- **HTTP Client**: HttpClient
- **Port**: 4200

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER (Angular 17)               │
│    ├─ Components (UI Pages & Dialogs)                           │
│    ├─ Services (API Communication)                              │
│    ├─ Guards (Authorization & Authentication)                   │
│    ├─ Interceptors (JWT Token Injection)                        │
│    └─ Models (TypeScript Interfaces)                            │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP/REST (localhost:4200)
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│              API GATEWAY / SECURITY LAYER                        │
│    ├─ CORS Configuration                                        │
│    ├─ JWT Authentication Filter                                 │
│    └─ Role-Based Access Control (@PreAuthorize)                │
└──────────────────────────────┬───────────────────────────────────┘
                               │ HTTP/REST (localhost:7070)
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                   BACKEND LAYER (Spring Boot)                    │
│    ├─ Controllers (REST API Endpoints)                           │
│    ├─ Services (Business Logic)                                  │
│    ├─ Repositories (Data Access)                                 │
│    ├─ Entities (Database Models)                                 │
│    ├─ DTOs (Request/Response Objects)                            │
│    └─ Security Config (JWT, Authentication)                      │
└──────────────────────────────┬───────────────────────────────────┘
                               │ JDBC / JPA
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (MySQL)                        │
│    ├─ USERS (Admins, Brands, Influencers)                        │
│    ├─ CAMPAIGNS (Brand Marketing Projects)                       │
│    ├─ SPONSORSHIP_REQUESTS (Influencer Applications)             │
│    ├─ PAYMENTS (Transaction Records)                             │
│    ├─ RATINGS (Two-way Feedback)                                 │
│    ├─ NOTIFICATIONS (User Alerts)                                │
│    └─ Additional Enums & Status Tables                           │
└──────────────────────────────────────────────────────────────────┘
```

---

## Backend Structure

### 1️⃣ Controllers (REST API Endpoints)

#### **AuthController** - `/api/auth`
Handles user authentication and authorization
- `POST /register` - Register new user (ADMIN, BRAND, INFLUENCER)
- `POST /login` - Authenticate user and return JWT token
- `GET /me` - Get current authenticated user details
- **Security**: Public endpoints (no auth required)

#### **CampaignController** - `/api/campaigns`
Manages campaign lifecycle (CRUD operations)
- `POST /` - Create new campaign (BRAND role only)
- `GET /` - List all campaigns
- `GET /active` - Get active/ongoing campaigns
- `GET /my-campaigns` - Get campaigns created by current brand
- `GET /{id}` - Get campaign details
- `GET /search` - Search campaigns by name, platform, status
- `PUT /{id}` - Update campaign details (BRAND owner only)
- `DELETE /{id}` - Delete campaign (BRAND owner only)
- `PUT /{id}/status` - Update campaign status (ACTIVE → PAUSED → COMPLETED → EXPIRED)

#### **SponsorshipController** - `/api/sponsorship`
Manages sponsorship applications and requests
- `POST /apply` - Apply for a campaign (INFLUENCER role)
- `GET /my-applications` - Get influencer's applications
- `GET /brand-requests` - Get brand's received applications
- `GET /campaign/{campaignId}` - Get applications for specific campaign
- `GET /{id}` - Get specific application details
- `PUT /{id}/status` - Update application status (PENDING → ACCEPTED → REJECTED → COMPLETED)

#### **PaymentController** - `/api/payments`
Handles payment processing and earnings tracking
- `POST /` - Create new payment (BRAND pays INFLUENCER)
- `GET /influencer` - Get influencer's earnings/received payments
- `GET /brand` - Get brand's spending/sent payments
- `GET /earnings` - Get total earnings for influencer
- `GET /spending` - Get total spending for brand
- `PUT /{id}/complete` - Mark payment as completed
- `GET /{id}` - Get payment details

#### **RatingController** - `/api/ratings`
Two-way rating system between Brands and Influencers
- `POST /` - Submit rating and feedback (after payment)
- `GET /user/{userId}` - Get all ratings for a user
- `GET /my-ratings` - Get ratings received by current user
- `GET /average/{userId}` - Calculate average rating for user

#### **NotificationController** - `/api/notifications`
Real-time notifications for user actions
- `GET /` - Get all notifications for current user
- `GET /unread` - Get unread notifications
- `GET /unread-count` - Get count of unread notifications
- `PUT /{id}/read` - Mark notification as read
- `PUT /mark-all-read` - Mark all notifications as read

#### **AdminController** - `/api/admin` ⚠️ (ADMIN role only)
Administrative oversight and analytics
- `GET /stats` - Dashboard statistics (total users, campaigns, payments)
- `GET /users` - List all users with role filtering
- `GET /users/role/{role}` - Get users by role
- `DELETE /users/{id}` - Delete user account
- `GET /campaigns` - View all campaigns
- `GET /requests` - View all sponsorship requests
- `GET /payments` - View all payments
- `GET /ratings` - View all ratings

---

### 2️⃣ Services (Business Logic Layer)

#### **AuthService**
Handles user registration, login, and authentication
- **register()**: Validates duplicate email/username, encodes password, creates user, generates JWT
- **login()**: Authenticates using email/password, generates JWT token
- **getCurrentUser()**: Retrieves authenticated user from SecurityContext

#### **CampaignService**
Campaign management and lifecycle
- **createCampaign()**: Create campaign with brand ownership verification
- **updateCampaign()**: Only brand owner can update
- **deleteCampaign()**: Only brand owner can delete
- **getActiveCampaigns()**: Auto-updates expired campaigns
- **searchCampaigns()**: Filter by name, platform, status
- **updateExpiredCampaigns()**: Auto-transition expired campaigns to EXPIRED status

#### **SponsorshipService**
Sponsorship request workflow
- **applyForCampaign()**: Submit application, check for duplicates, trigger notification
- **updateRequestStatus()**: Brand accepts/rejects influencer applications
- **getInfluencerRequests()**: Get influencer's applications
- **getBrandRequests()**: Get received applications for brand
- **getCampaignRequests()**: Get all applications for specific campaign

#### **PaymentService**
Payment processing and financial tracking
- **createPayment()**: Create payment record, auto-mark as COMPLETED
- **completePayment()**: Transition payment to COMPLETED status
- **getInfluencerPayments()**: Get earnings/payments received
- **getBrandPayments()**: Get spending/payments sent
- **getInfluencerEarnings()**: Total earnings sum
- **getBrandSpending()**: Total spending sum

#### **RatingService**
Two-way feedback system
- **addRating()**: Submit rating (1-5 stars) with feedback
- **getUserRatings()**: Get all ratings received by user
- **getAverageRating()**: Calculate average star rating

#### **NotificationService**
Real-time user notifications
- **createNotification()**: Create and send notifications
- **getUserNotifications()**: Retrieve user's notification history
- **getUnreadNotifications()**: Get unread only
- **markAsRead()**: Mark single notification as read
- **markAllAsRead()**: Mark all notifications as read

#### **AdminService**
Administrative operations and reporting
- **getAdminStats()**: Dashboard statistics (total users, campaigns, payments)
- **getAllUsers()**: List all users with filtering by role
- **deleteUser()**: Remove user account and associated data
- **getAllCampaigns()**: View platform-wide campaigns
- **getAllRequests()**: View all sponsorship requests
- **getAllPayments()**: View all payments
- **getAllRatings()**: View all ratings

---

### 3️⃣ Entity Classes (Database Models)

#### **User Entity**
```java
@Entity @Table(name = "users")
- id: Long (Primary Key)
- name: String (Unique, Display Name)
- email: String (Unique, Email Address)
- password: String (BCrypt Encoded)
- role: Role (ENUM: ADMIN, BRAND, INFLUENCER)
- bio: String (Optional biography)
- profileImage: String (Optional profile picture URL)
```

#### **Campaign Entity**
```java
@Entity @Table(name = "campaigns")
- id: Long (Primary Key)
- name: String (Campaign Title)
- description: String (Max 2000 chars)
- platform: String (Instagram, YouTube, TikTok, etc.)
- budget: Double (Positive amount)
- startDate: LocalDate
- endDate: LocalDate (Auto-updates to EXPIRED if past)
- eligibility: String (Requirements for influencers)
- status: CampaignStatus (ENUM: ACTIVE, PAUSED, COMPLETED, CANCELLED, EXPIRED)
- brand: User (ManyToOne - Campaign Creator)
```

#### **SponsorshipRequest Entity**
```java
@Entity @Table(name = "sponsorship_requests")
- id: Long (Primary Key)
- influencer: User (ManyToOne - Applicant)
- campaign: Campaign (ManyToOne - Target Campaign)
- proposal: String (Influencer's pitch, max 2000 chars)
- status: RequestStatus (ENUM: PENDING, ACCEPTED, REJECTED, COMPLETED)
- createdAt: LocalDateTime (Auto-generated)
- updatedAt: LocalDateTime (Updated on status change)
```

#### **Payment Entity**
```java
@Entity @Table(name = "payments")
- id: Long (Primary Key)
- campaign: Campaign (ManyToOne)
- influencer: User (ManyToOne - Payment Recipient)
- brand: User (ManyToOne - Payment Sender)
- amount: Double (Positive amount)
- status: PaymentStatus (ENUM: PENDING, COMPLETED, FAILED, REFUNDED)
- createdAt: LocalDateTime (Auto-generated)
- paidAt: LocalDateTime (Set when marked complete)
- transactionId: String (UUID for tracking)
```

#### **Rating Entity**
```java
@Entity @Table(name = "ratings")
- id: Long (Primary Key)
- campaign: Campaign (ManyToOne - Context)
- rater: User (ManyToOne - Person giving rating)
- rated: User (ManyToOne - Person being rated)
- score: Integer (1-5 star scale)
- feedback: String (Max 1000 chars - Free text review)
- createdAt: LocalDateTime (Auto-generated)
```

#### **Notification Entity**
```java
@Entity @Table(name = "notifications")
- id: Long (Primary Key)
- user: User (ManyToOne - Recipient)
- title: String (Notification title)
- message: String (Notification message body)
- isRead: Boolean (Read status)
- createdAt: LocalDateTime (Auto-generated)
```

#### **Enums**
```java
Role { ADMIN, BRAND, INFLUENCER }
CampaignStatus { ACTIVE, PAUSED, COMPLETED, CANCELLED, EXPIRED }
RequestStatus { PENDING, ACCEPTED, REJECTED, COMPLETED }
PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
```

---

### 4️⃣ Security Implementation

#### **JWT Authentication Flow**

```
1. User Registers/Logs In
   ↓
2. Backend validates credentials
   ↓
3. Backend generates JWT token containing:
   - Subject (email)
   - Issued At (iat)
   - Expiration (exp: 24 hours)
   - Signed with secret key (HS256)
   ↓
4. Frontend stores JWT in localStorage
   ↓
5. For each API request:
   - AuthInterceptor adds: Authorization: Bearer <token>
   ↓
6. Backend ValidatesJWT:
   - JwtAuthenticationFilter extracts token
   - Validates signature and expiration
   - Creates Authentication object
```

#### **Key Security Classes**

**JwtTokenProvider** (`security/JwtTokenProvider.java`)
- Generates JWT tokens using JJWT library
- Validates token signatures using HMAC-SHA256
- Extracts username from token claims
- Configuration:
  - Secret: `app.jwt.secret` (64+ chars from application.properties)
  - Expiration: `app.jwt.expiration` (86400000 ms = 24 hours)

**JwtAuthenticationFilter** (`security/JwtAuthenticationFilter.java`)
- Intercepts all HTTP requests
- Extracts JWT from Authorization header
- Validates token and loads user details
- Sets authenticated principal in SecurityContext

**CustomUserDetailsService** (`security/CustomUserDetailsService.java`)
- Implements Spring's UserDetailsService interface
- Loads user by email from database
- Returns UserDetails for authentication

**SecurityConfig** (`config/SecurityConfig.java`)
- Enables HTTP Basic security and JWT filter
- CORS configuration (allows localhost:4200)
- Route-based authorization:
  - `/api/auth/**` - Public (no auth required)
  - `/api/admin/**` - ADMIN role only
  - `/api/brand/**` - BRAND role only
  - `/api/influencer/**` - INFLUENCER role only
  - Other endpoints - Require authentication
- Session management: STATELESS (no cookies)
- CSRF protection: Disabled (JWT is used instead)

---

## Frontend Structure

### 1️⃣ Components (UI Pages)

#### **Authentication Components** (`components/auth/`)

**LoginComponent** (`auth/login`)
- User email/password login form
- Calls AuthService.login()
- Redirects to appropriate dashboard on success
- Displays error messages for failed login

**SignupComponent** (`auth/signup`)
- Registration form for new users
- Role selection (ADMIN, BRAND, INFLUENCER)
- Email validation and uniqueness check
- Password confirmation
- Calls AuthService.register()

---

#### **Dashboard Components** (`components/dashboard/`)

**AdminDashboardComponent** (`dashboard/admin-dashboard`)
- Platform overview statistics
- User management (view, filter by role, delete)
- View all campaigns, requests, payments, ratings
- Admin controls and oversight

**BrandDashboardComponent** (`dashboard/brand-dashboard`)
- Quick stats: active campaigns, pending requests, earnings
- "Create New Campaign" button
- List of own campaigns with quick actions
- Recent sponsorship requests
- Financial summary

**InfluencerDashboardComponent** (`dashboard/influencer-dashboard`)
- Profile summary with average rating
- Available campaigns (active & matching)
- My applications (with status)
- Earnings and payment history
- Recent notifications

---

#### **Campaign Management** (`components/campaign/`)

**CampaignListComponent** (`campaign/campaign-list`)
- Lists campaigns based on user role:
  - **Brand**: Shows own campaigns (for editing/managing)
  - **Influencer**: Shows active campaigns (for browsing)
- Edit/Delete buttons for campaigns (brand only)
- Search by name
- Filter by platform, status
- "Apply" button for influencers
- Tracks which campaigns influencer has applied to

**CampaignFormComponent** (`campaign/campaign-form`)
- Create new campaign (brand only)
- Edit existing campaign (brand owner only)
- Form fields:
  - Campaign name, description, platform
  - Budget, start/end dates
  - Eligibility requirements
- Save/Cancel buttons
- Validation before submission

**CampaignDetailComponent** (`campaign/campaign-detail`)
- Displays full campaign information
- **For Brands**: Shows list of applications with acceptance/rejection buttons
- **For Influencers**: Shows application form to submit proposal
- Displays brand information
- Shows campaign deadline and status

---

#### **Sponsorship Request Component** (`components/sponsorship/`)

**SponsorshipRequestComponent** (`sponsorship/sponsorship-request`)
- Unified view for sponsor requests workflow
- **For Brands**: Shows received applications
  - Lists: campaign, influencer, proposal, status
  - Accept/Reject buttons
  - Mark as Completed button
  - "Make Payment" button (after Completed)
  - "Rate Influencer" button
- **For Influencers**: Shows own applications
  - Lists: campaign, status, proposal
  - "Rate Brand" button

---

#### **Payment Component** (`components/payment/`)

**PaymentComponent** (`payment/payment.component`)
- **For Brands**: Shows "My Payments" (money sent)
  - Tracks spending and pending payments
  - Complete Payment button for pending payments
- **For Influencers**: Shows "My Earnings" (money received)
  - Tracks total earnings
  - Payment status history

---

#### **Rating Component** (`components/rating/`)

**RatingComponent** (`rating/rating.component`)
- View ratings received from other users
- Average star rating display
- List of all ratings with feedback from raters
- Filter/search ratings

---

#### **Shared Components** (`components/shared/`)

**NavbarComponent** (`shared/navbar`)
- Navigation menu (appears on all pages)
- User profile dropdown
- Role-based menu items
- Notification bell with unread count
- Logout button

**PaymentDialogComponent** (`shared/payment-dialog`)
- Modal dialog for entering payment amount
- Inputs: campaign name, influencer name, amount
- Currency formatting
- Submit/Cancel buttons
- Triggered when brand clicks "Make Payment"

**RatingDialogComponent** (`shared/rating-dialog`)
- Modal dialog for rating submission
- Star rating selector (1-5)
- Feedback text area
- Submit/Cancel buttons
- Triggered after payment or sponsorship completion

**NotificationComponent** (`notification`)
- Displays notifications
- Mark as read functionality
- Notification list with timestamps
- Filter: all/unread

---

### 2️⃣ Services (API Communication)

All services use HttpClient to make REST calls to backend API and return RxJS Observables

**AuthService** (`services/auth.service`)
- `register(RegisterRequest)`: Register new user
- `login(LoginRequest)`: Login and store JWT
- `logout()`: Clear token and user data
- `getToken()`: Get stored JWT
- `getCurrentUser()`: Get logged-in user
- `isLoggedIn()`: Check authentication status
- `hasRole(role)`: Check if user has specific role
- `getDashboardRoute()`: Return appropriate dashboard path for user role

**CampaignService** (`services/campaign.service`)
- `getAllCampaigns()`: Get all campaigns
- `getActiveCampaigns()`: Get active only
- `getMyCampaigns()`: Get brand's own campaigns
- `getCampaignById(id)`: Get single campaign
- `createCampaign(CampaignRequest)`: Create new
- `updateCampaign(id, CampaignRequest)`: Update existing
- `deleteCampaign(id)`: Delete campaign
- `searchCampaigns(name?, platform?, status?)`: Advanced search

**SponsorshipService** (`services/sponsorship.service`)
- `applyForCampaign(SponsorshipApplicationRequest)`: Submit application
- `getMyApplications()`: Get influencer's applications
- `getBrandRequests()`: Get brand's received applications
- `getCampaignRequests(campaignId)`: Get applications for campaign
- `updateRequestStatus(id, status)`: Change application status

**PaymentService** (`services/payment.service`)
- `createPayment(PaymentRequest)`: Create payment
- `completePayment(id)`: Mark payment complete
- `getInfluencerPayments()`: Get influencer's earnings
- `getBrandPayments()`: Get brand's spendings
- `getEarnings()`: Total earnings for influencer
- `getSpending()`: Total spending for brand

**RatingService** (`services/rating.service`)
- `addRating(RatingRequest)`: Submit rating
- `getUserRatings(userId)`: Get ratings for user
- `getMyRatings()`: Get ratings received
- `getAverageRating(userId)`: Calculate average

**AdminService** (`services/admin.service`)
- `getStats()`: Dashboard statistics
- `getAllUsers()`: Get all users
- `getUsersByRole(role)`: Filter by role
- `deleteUser(id)`: Delete user

**NotificationService** (`services/notification.service`)
- `getNotifications()`: Get user's notifications
- `getUnreadNotifications()`: Get unread only
- `getUnreadCount()`: Count unread
- `markAsRead(id)`: Mark single as read
- `markAllAsRead()`: Mark all as read

---

### 3️⃣ Guards (Authorization)

**AuthGuard** (`guards/auth.guard.ts`)
- Implements CanActivate interface
- Checks if user is logged in (JWT token exists)
- Redirects to `/login` if not authenticated
- Used on all protected routes

**RoleGuard** (`guards/role.guard.ts`)
- Implements CanActivate interface
- Checks if user has required role
- Reads expected role from route data: `data: { role: 'BRAND' }`
- Redirects to appropriate dashboard if role mismatch
- Used on role-specific routes (admin, brand, influencer dashboards)

---

### 4️⃣ Interceptors (HTTP)

**AuthInterceptor** (`interceptors/auth.interceptor.ts`)
- Implements HttpInterceptor interface
- Intercepts all HTTP requests
- Adds JWT token to Authorization header: `Authorization: Bearer <token>`
- Handles 401 responses:
  - Clears storage
  - Logs out user
  - Redirects to login page
- Used globally via HTTP_INTERCEPTORS provider

---

### 5️⃣ Models (TypeScript Interfaces)

**User** (`models/user.model`)
- id, name, email, role, bio?, profileImage?
- AuthResponse: token, id, name, email, role
- LoginRequest: email, password
- RegisterRequest: name, email, password, role

**Campaign** (`models/campaign.model`)
- id, name, description, platform
- budget, startDate, endDate
- eligibility, status, brand (User)
- CampaignRequest: name, description, platform, budget, startDate, endDate, eligibility

**SponsorshipRequest** (`models/sponsorship.model`)
- id, influencer (User), campaign (Campaign)
- proposal, status, createdAt, updatedAt?
- SponsorshipApplicationRequest: campaignId, proposal

**Payment** (`models/payment.model`)
- id, campaign (Campaign), influencer (User), brand (User)
- amount, status, createdAt, paidAt?, transactionId
- PaymentRequest: campaignId, influencerId, amount

**Rating** (`models/rating.model`)
- id, campaign (Campaign), rater (User), rated (User), score, feedback, createdAt

**Notification** (`models/notification.model`)
- id, user, title, message, isRead, createdAt

**Common** (`models/common.model`)
- ApiResponse<T>: success, message, data

---

### 6️⃣ Routing

**App Routes** (`app-routing.module.ts`)

```
/login                           → LoginComponent (public)
/signup                          → SignupComponent (public)
/dashboard/admin                 → AdminDashboardComponent (AuthGuard + RoleGuard: ADMIN)
/dashboard/brand                 → BrandDashboardComponent (AuthGuard + RoleGuard: BRAND)
/dashboard/influencer            → InfluencerDashboardComponent (AuthGuard + RoleGuard: INFLUENCER)
/campaigns                        → CampaignListComponent (AuthGuard)
/campaigns/new                   → CampaignFormComponent (AuthGuard + RoleGuard: BRAND)
/campaigns/edit/:id              → CampaignFormComponent (AuthGuard + RoleGuard: BRAND)
/campaigns/:id                   → CampaignDetailComponent (AuthGuard)
/sponsorship-requests            → SponsorshipRequestComponent (AuthGuard)
/payments                         → PaymentComponent (AuthGuard)
/ratings                          → RatingComponent (AuthGuard)
/notifications                   → NotificationComponent (AuthGuard)
/                                → Redirect to /login (default)
**                               → Redirect to /login (catch all)
```

---

## Key Features & Flows

### 🔄 Complete Sponsorship Workflow

```
STEP 1: USER REGISTRATION & ROLES
┌─────────────────────────────────────┐
│ New User Registers                  │
│ - Email, Password, Role Selection   │
│ - Roles: ADMIN, BRAND, INFLUENCER   │
│ - Password encrypted with BCrypt    │
│ - JWT token generated               │
└──────────────┬──────────────────────┘

STEP 2: BRAND CREATES CAMPAIGN
┌──────────────────────────────────────┐
│ Brand (logged in)                    │
│ - Click "Create Campaign"            │
│ - Fill: name, desc, platform, budget │
│ - Set start/end dates                │
│ - Set eligibility requirements       │
│ - Campaign status: ACTIVE            │
│ - Notification created               │
└──────────────┬───────────────────────┘

STEP 3: INFLUENCER BROWSES
┌──────────────────────────────────────┐
│ Influencer (logged in)               │
│ - View active campaigns              │
│ - Filter by platform, budget, etc.   │
│ - View campaign details              │
│ - See brand information              │
└──────────────┬───────────────────────┘

STEP 4: INFLUENCER APPLIES
┌──────────────────────────────────────┐
│ Influencer applies                   │
│ - Click "Apply" on campaign          │
│ - Write proposal/pitch               │
│ - Submit application                 │
│ - Status: PENDING                    │
│ - Notification sent to brand         │
└──────────────┬───────────────────────┘

STEP 5: BRAND REVIEWS & DECIDES
┌──────────────────────────────────────┐
│ Brand reviews applications           │
│ - See influencer profile & proposal  │
│ - ACCEPT (status → ACCEPTED)         │
│ - or REJECT (status → REJECTED)      │
│ - Notification sent to influencer    │
└──────────────┬───────────────────────┘

STEP 6: WORK COMPLETES
┌──────────────────────────────────────┐
│ After influencer creates content     │
│ - Brand clicks "Mark as Completed"   │
│ - Status: ACCEPTED → COMPLETED       │
│ - "Make Payment" button now appears  │
│ - Notification sent to influencer    │
└──────────────┬───────────────────────┘

STEP 7: PAYMENT PROCESSING
┌──────────────────────────────────────┐
│ Brand makes payment                  │
│ - Click "Make Payment"               │
│ - Enter payment amount               │
│ - Create Payment (status: COMPLETED) │
│ - Transaction ID generated (UUID)    │
│ - Notification sent to influencer    │
│ - Campaign automatically → COMPLETED │
└──────────────┬───────────────────────┘

STEP 8: RATING & FEEDBACK
┌──────────────────────────────────────┐
│ Brand rates influencer               │
│ - Submit 1-5 stars                   │
│ - Add feedback/review                │
│ - Notification sent to influencer    │
│                                      │
│ Influencer rates brand               │
│ - Submit 1-5 stars                   │
│ - Add feedback/review                │
│ - Notification sent to brand         │
└──────────────────────────────────────┘
```

---

### 💰 Payment & Money Flow

```
PAYMENT LIFECYCLE:

1. PENDING STATE
   - Brand enters amount in dialog
   - PaymentService.createPayment() called
   - Payment record created
   - Status: PENDING (but immediately marked COMPLETED in current impl)

2. COMPLETED STATE
   - Payment.status = COMPLETED
   - Payment.paidAt = current timestamp
   - Payment.transactionId = UUID
   - Campaign status auto-updates to COMPLETED

3. NOTIFICATIONS
   - Influencer notified: "Payment Received: $X"
   - Payment appears in Influencer's earnings
   - Payment appears in Brand's spending

4. TRACKING
   - PaymentService.getInfluencerPayments() → All payments received
   - PaymentService.getBrandPayments() → All payments sent
   - PaymentService.getInfluencerEarnings() → Total sum (COMPLETED only)
   - PaymentService.getBrandSpending() → Total sum (COMPLETED only)
```

---

### ⭐ Rating System

```
RATING RULES:
- Rating allowed after sponsorship is COMPLETED
- Either party can rate the other
- 1-5 star scale (1 = poor, 5 = excellent)
- Optional feedback/review text (max 1000 chars)

TWO-WAY RATING:
1. Brand rates Influencer
   - Feedback on quality, responsiveness, professionalism
   
2. Influencer rates Brand
   - Feedback on communication, payment reliability, clarity

STATISTICS:
- Average rating calculated per user
- RatingService.getAverageRating(userId) → Double
- Displayed on user profile
- Helps build platform reputation
```

---

### 🔔 Notification System

```
NOTIFICATION TRIGGERS:

1. USER REGISTRATION
   - Trigger: New user joins
   - Target: N/A (system event)

2. CAMPAIGN APPLICATION
   - Trigger: Influencer applies
   - Target: Brand
   - Message: "{Influencer} applied for {Campaign}"

3. APPLICATION STATUS CHANGE
   - Trigger: Brand accepts/rejects
   - Target: Influencer
   - Message: "Application {ACCEPTED/REJECTED} for {Campaign}"

4. WORK COMPLETION
   - Trigger: Brand marks as completed
   - Target: Influencer
   - Message: "Work completed, ready for payment"

5. PAYMENT SENT
   - Trigger: Payment created
   - Target: Influencer
   - Message: "Payment of ${Amount} received"

6. RATING RECEIVED
   - Trigger: User rated
   - Target: Rated user
   - Message: "{User} gave you {Score} stars"

USER INTERFACE:
- Notification bell icon in navbar
- Shows unread count
- Click to view all notifications
- Mark as read individually or all at once
- Notifications ordered by recency
```

---

### 🔐 Three User Roles & Permissions

```
┌────────────────────────────────────────────────────────────────┐
│                        ADMIN ROLE                              │
├────────────────────────────────────────────────────────────────┤
│ Access: /dashboard/admin (/api/admin/**)                       │
│ Permissions:                                                   │
│ ✓ View all users (Admin, Brand, Influencer)                   │
│ ✓ Delete any user                                              │
│ ✓ View all campaigns                                           │
│ ✓ View all sponsorship requests                                │
│ ✓ View all payments                                            │
│ ✓ View all ratings                                             │
│ ✓ View platform statistics & analytics                         │
│ ✗ Cannot create campaigns, apply to campaigns, process payments│
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                        BRAND ROLE                              │
├────────────────────────────────────────────────────────────────┤
│ Access: /dashboard/brand                                       │
│ Permissions:                                                   │
│ ✓ Create new campaigns                                         │
│ ✓ Edit own campaigns                                           │
│ ✓ Delete own campaigns                                         │
│ ✓ View all active campaigns                                    │
│ ✓ View applications received from influencers                  │
│ ✓ Accept/Reject applications                                   │
│ ✓ Mark work as completed                                       │
│ ✓ Make payments to influencers                                 │
│ ✓ Rate influencers                                             │
│ ✓ View spending/payment history                                │
│ ✓ View received ratings                                        │
│ ✗ Cannot create/edit other brand's campaigns                   │
│ ✗ Cannot apply to campaigns                                    │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                     INFLUENCER ROLE                            │
├────────────────────────────────────────────────────────────────┤
│ Access: /dashboard/influencer                                 │
│ Permissions:                                                   │
│ ✓ View all active campaigns                                    │
│ ✓ Search and filter campaigns                                  │
│ ✓ Apply to campaigns with proposal                             │
│ ✓ View own applications & statuses                             │
│ ✓ View profile with average rating                             │
│ ✓ Rate brands (after payment)                                  │
│ ✓ View earnings/payment history                                │
│ ✓ View received ratings                                        │
│ ✗ Cannot create campaigns                                      │
│ ✗ Cannot make payments                                         │
│ ✗ Cannot delete campaigns                                      │
│ ✗ Cannot view other influencer's applications                  │
└────────────────────────────────────────────────────────────────┘
```

---

## Security Implementation

### 🔐 JWT Token Flow

**Backend Configuration** (`src/main/resources/application.properties`)
```properties
app.jwt.secret=mySecretKey123... (64+ characters)
app.jwt.expiration=86400000 (24 hours in milliseconds)
```

**Token Structure**
```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "user@email.com",       # Email (username)
  "iat": 1234567890,              # Issued At
  "exp": 1234654290               # Expiration (24h later)
}
Signature: HMACSHA256(header.payload, secret)
```

**Complete Auth Flow Diagram**
```
Frontend (Angular)          Backend (Spring Boot)

1. User types email/        
   password
        │
        ├─ POST /api/auth/login ────→ AuthController.login()
        │    {email, password}            │
        │                                 ├─ Validate email exists
        │                                 ├─ BCrypt compare password
        │                                 ├─ JwtTokenProvider.generateToken()
        │                                 ├─ Sign with secret key
        │                                 │
        │    ← AuthResponse {token} ────┤
        │
2. Store token in           
   localStorage
        │
3. For each API request,
   AuthInterceptor adds
   Authorization header
        │
        ├─ GET /api/campaigns ─────────→ JwtAuthenticationFilter
        │    Headers: {                    │
        │    Authorization: Bearer token   ├─ Extract token from header
        │    }                            ├─ JwtTokenProvider.validateToken()
        │                                 ├─ Verify signature
        │                                 ├─ Check expiration
        │                                 ├─ Load user details
        │                                 ├─ Set SecurityContext
        │                                 │
        │    ← Response ────────────────┤
```

---

### 🛡️ CORS Configuration

**Configured in** `config/CorsConfig.java`
```
Allowed Origins: http://localhost:4200
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Content-Type, Authorization
Credentials: true
```

---

### 🔑 Role-Based Access Control (RBAC)

**Implemented via Spring Security URL Patterns** (`config/SecurityConfig.java`)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()           // Public
    .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Admin only
    .requestMatchers("/api/brand/**").hasRole("BRAND")     // Brand only
    .requestMatchers("/api/influencer/**").hasRole("INFLUENCER") // Influencer only
    .anyRequest().authenticated()                          // All others need auth
)
```

**Method-level security** can be added via `@PreAuthorize`:
```java
@GetMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<DashboardStats> getStats() { ... }
```

---

## Data Models & Relationships

### Entity Relationship Diagram (ERD)

```
USERS
├─ id (PK)
├─ name (UNIQUE)
├─ email (UNIQUE)
├─ password (BCrypt)
├─ role (ENUM)
├─ bio (NULLABLE)
└─ profileImage (NULLABLE)

CAMPAIGNS (one-to-many with USERS)
├─ id (PK)
├─ name
├─ description
├─ platform
├─ budget
├─ startDate
├─ endDate
├─ eligibility
├─ status (ENUM)
└─ brand_id (FK → USERS)

SPONSORSHIP_REQUESTS (many-to-many through CAMPAIGNS)
├─ id (PK)
├─ influencer_id (FK → USERS)
├─ campaign_id (FK → CAMPAIGNS)
├─ proposal
├─ status (ENUM)
├─ createdAt
└─ updatedAt

PAYMENTS (one-to-many with CAMPAIGNS)
├─ id (PK)
├─ campaign_id (FK → CAMPAIGNS)
├─ influencer_id (FK → USERS)
├─ brand_id (FK → USERS)
├─ amount
├─ status (ENUM)
├─ createdAt
├─ paidAt
└─ transactionId (UUID)

RATINGS (many-to-many with USERS via CAMPAIGNS)
├─ id (PK)
├─ campaign_id (FK → CAMPAIGNS)
├─ rater_id (FK → USERS)
├─ rated_id (FK → USERS)
├─ score (1-5)
├─ feedback
└─ createdAt

NOTIFICATIONS (one-to-many with USERS)
├─ id (PK)
├─ user_id (FK → USERS)
├─ title
├─ message
├─ isRead
└─ createdAt
```

### Key Relationships

1. **USERS → CAMPAIGNS** (1 User can create many Campaigns)
   - Each Campaign has one Brand owner
   - Foreign Key: Campaign.brand_id → User.id

2. **USERS → SPONSORSHIP_REQUESTS** (1 User can make many applications)
   - Foreign Key: SponsorshipRequest.influencer_id → User.id

3. **CAMPAIGNS → SPONSORSHIP_REQUESTS** (1 Campaign receives many applications)
   - Foreign Key: SponsorshipRequest.campaign_id → Campaign.id

4. **USERS ↔ RATINGS** (Many-to-Many rating relationship)
   - Rater can be Brand or Influencer
   - Rated can be Brand or Influencer
   - Foreign Keys: Rating.rater_id, Rating.rated_id → User.id

5. **CAMPAIGNS → PAYMENTS** (1 Campaign can have multiple payments)
   - Foreign Key: Payment.campaign_id → Campaign.id

6. **USERS → NOTIFICATIONS** (1 User receives many notifications)
   - Foreign Key: Notification.user_id → User.id

---

## Important Files Reference

### 📁 Backend Key Files

#### Main Application
- [SponsorshipAppBackendApplication.java](src/main/java/com/myapp/sponsorshipapp/SponsorshipAppBackendApplication.java)
  - Spring Boot entry point
  - Starts embedded Tomcat server on port 7070

#### Configuration Files
- [application.properties](src/main/resources/application.properties)
  - Database connection (MySQL)
  - JWT secret and expiration
  - CORS allowed origins
  - Logging configuration
  - H2 console settings

- [SecurityConfig.java](src/main/java/com/myapp/sponsorshipapp/config/SecurityConfig.java)
  - JWT authentication setup
  - Role-based authorization rules
  - CORS configuration
  - Session management (STATELESS)
  - Password encoder (BCrypt)

- [CorsConfig.java](src/main/java/com/myapp/sponsorshipapp/config/CorsConfig.java)
  - Cross-Origin Resource Sharing setup
  - Allowed origins, methods, headers

- [DataInitializer.java](src/main/java/com/myapp/sponsorshipapp/config/DataInitializer.java)
  - Auto-seed demo data on startup
  - Creates demo users (admin, brand, influencer)

#### Controllers (REST API)
- [AuthController.java](src/main/java/com/myapp/sponsorshipapp/controller/AuthController.java) - `/api/auth`
- [CampaignController.java](src/main/java/com/myapp/sponsorshipapp/controller/CampaignController.java) - `/api/campaigns`
- [SponsorshipController.java](src/main/java/com/myapp/sponsorshipapp/controller/SponsorshipController.java) - `/api/sponsorship`
- [PaymentController.java](src/main/java/com/myapp/sponsorshipapp/controller/PaymentController.java) - `/api/payments`
- [RatingController.java](src/main/java/com/myapp/sponsorshipapp/controller/RatingController.java) - `/api/ratings`
- [NotificationController.java](src/main/java/com/myapp/sponsorshipapp/controller/NotificationController.java) - `/api/notifications`
- [AdminController.java](src/main/java/com/myapp/sponsorshipapp/controller/AdminController.java) - `/api/admin`

#### Services (Business Logic)
- [AuthService.java](src/main/java/com/myapp/sponsorshipapp/service/AuthService.java) - Authentication
- [CampaignService.java](src/main/java/com/myapp/sponsorshipapp/service/CampaignService.java) - Campaign management
- [SponsorshipService.java](src/main/java/com/myapp/sponsorshipapp/service/SponsorshipService.java) - Sponsorship workflow
- [PaymentService.java](src/main/java/com/myapp/sponsorshipapp/service/PaymentService.java) - Payment processing
- [RatingService.java](src/main/java/com/myapp/sponsorshipapp/service/RatingService.java) - Rating system
- [NotificationService.java](src/main/java/com/myapp/sponsorshipapp/service/NotificationService.java) - Notifications
- [AdminService.java](src/main/java/com/myapp/sponsorshipapp/service/AdminService.java) - Admin operations

#### Entities (Database Models)
- [User.java](src/main/java/com/myapp/sponsorshipapp/entity/User.java) - User with Role enum
- [Campaign.java](src/main/java/com/myapp/sponsorshipapp/entity/Campaign.java) - Campaign with CampaignStatus enum
- [SponsorshipRequest.java](src/main/java/com/myapp/sponsorshipapp/entity/SponsorshipRequest.java) - Application with RequestStatus enum
- [Payment.java](src/main/java/com/myapp/sponsorshipapp/entity/Payment.java) - Payment with PaymentStatus enum
- [Rating.java](src/main/java/com/myapp/sponsorshipapp/entity/Rating.java) - Rating system
- [Notification.java](src/main/java/com/myapp/sponsorshipapp/entity/Notification.java) - Notifications

#### Security Components
- [JwtTokenProvider.java](src/main/java/com/myapp/sponsorshipapp/security/JwtTokenProvider.java)
  - JWT token generation and validation
  - JJWT library for token management
  - HMAC-SHA256 signing

- [JwtAuthenticationFilter.java](src/main/java/com/myapp/sponsorshipapp/security/JwtAuthenticationFilter.java)
  - Interceptor for request processing
  - Extracts JWT from Authorization header
  - Validates and loads user authentication

- [CustomUserDetailsService.java](src/main/java/com/myapp/sponsorshipapp/security/CustomUserDetailsService.java)
  - Implements UserDetailsService
  - Loads user by email from database
  - Used by authentication provider

#### DTOs (Data Transfer Objects)
- `dto/` folder contains request/response models:
  - AuthResponse, LoginRequest, RegisterRequest
  - CampaignRequest
  - PaymentRequest
  - RatingRequest
  - SponsorshipApplicationRequest
  - ApiResponse (generic wrapper)
  - DashboardStats

#### Repositories (Data Access)
- `repository/` folder contains Spring Data JPA repositories:
  - UserRepository
  - CampaignRepository
  - SponsorshipRequestRepository
  - PaymentRepository
  - RatingRepository
  - NotificationRepository

#### Exception Handling
- `exception/` folder contains custom exceptions:
  - ResourceNotFoundException
  - UnauthorizedException
  - BadRequestException

#### Build Configuration
- [pom.xml](pom.xml) - Maven dependencies
  - Spring Boot 3.2
  - Spring Security
  - Spring Data JPA
  - Lombok
  - JJWT (JWT library)
  - MySQL driver
  - Validation APIs

---

### 📁 Frontend Key Files

#### Main Application Setup
- [app.component.ts](frontend/src/app/app.component.ts) - Root component with navbar + routing outlet
- [app-routing.module.ts](frontend/src/app/app-routing.module.ts) - All route definitions

#### Components
**Authentication**
- [login/login.component.ts](frontend/src/app/components/auth/login/login.component.ts) - Login form
- [signup/signup.component.ts](frontend/src/app/components/auth/signup/signup.component.ts) - Registration form

**Dashboards**
- [admin-dashboard/admin-dashboard.component.ts](frontend/src/app/components/dashboard/admin-dashboard/admin-dashboard.component.ts)
- [brand-dashboard/brand-dashboard.component.ts](frontend/src/app/components/dashboard/brand-dashboard/brand-dashboard.component.ts)
- [influencer-dashboard/influencer-dashboard.component.ts](frontend/src/app/components/dashboard/influencer-dashboard/influencer-dashboard.component.ts)

**Campaigns**
- [campaign-list/campaign-list.component.ts](frontend/src/app/components/campaign/campaign-list/campaign-list.component.ts)
- [campaign-detail/campaign-detail.component.ts](frontend/src/app/components/campaign/campaign-detail/campaign-detail.component.ts)
- [campaign-form/campaign-form.component.ts](frontend/src/app/components/campaign/campaign-form/campaign-form.component.ts)

**Sponsorship & Payments**
- [sponsorship-request/sponsorship-request.component.ts](frontend/src/app/components/sponsorship/sponsorship-request/sponsorship-request.component.ts)
- [payment/payment.component.ts](frontend/src/app/components/payment/payment.component.ts)

**Ratings & Notifications**
- [rating/rating.component.ts](frontend/src/app/components/rating/rating.component.ts)
- [notification/notification.component.ts](frontend/src/app/components/notification/notification.component.ts)

**Shared Components**
- [navbar/navbar.component.ts](frontend/src/app/components/shared/navbar/navbar.component.ts) - Navigation menu
- [payment-dialog/payment-dialog.component.ts](frontend/src/app/components/shared/payment-dialog/payment-dialog.component.ts)
- [rating-dialog/rating-dialog.component.ts](frontend/src/app/components/shared/rating-dialog/rating-dialog.component.ts)

#### Services
- [auth.service.ts](frontend/src/app/services/auth.service.ts) - Authentication & user management
- [campaign.service.ts](frontend/src/app/services/campaign.service.ts) - Campaign API calls
- [sponsorship.service.ts](frontend/src/app/services/sponsorship.service.ts) - Application API calls
- [payment.service.ts](frontend/src/app/services/payment.service.ts) - Payment API calls
- [rating.service.ts](frontend/src/app/services/rating.service.ts) - Rating API calls
- [notification.service.ts](frontend/src/app/services/notification.service.ts) - Notification API calls
- [admin.service.ts](frontend/src/app/services/admin.service.ts) - Admin API calls

#### Guards & Interceptors
- [guards/auth.guard.ts](frontend/src/app/guards/auth.guard.ts) - Check if logged in
- [guards/role.guard.ts](frontend/src/app/guards/role.guard.ts) - Check user role
- [interceptors/auth.interceptor.ts](frontend/src/app/interceptors/auth.interceptor.ts) - Add JWT to requests

#### Models (TypeScript Interfaces)
- [models/user.model.ts](frontend/src/app/models/user.model.ts)
- [models/campaign.model.ts](frontend/src/app/models/campaign.model.ts)
- [models/sponsorship.model.ts](frontend/src/app/models/sponsorship.model.ts)
- [models/payment.model.ts](frontend/src/app/models/payment.model.ts)
- [models/rating.model.ts](frontend/src/app/models/rating.model.ts)
- [models/notification.model.ts](frontend/src/app/models/notification.model.ts)
- [models/common.model.ts](frontend/src/app/models/common.model.ts) - ApiResponse

#### Configuration Files
- [angular.json](frontend/angular.json) - Angular CLI configuration
- [package.json](frontend/package.json) - Node dependencies
- [tsconfig.json](frontend/tsconfig.json) - TypeScript configuration
- [src/environments/environment.ts](frontend/src/environments/environment.ts) - API URL config
- [src/environments/environment.prod.ts](frontend/src/environments/environment.prod.ts) - Production config

#### Styling & Global
- [src/styles.scss](frontend/src/styles.scss) - Global styles
- [src/index.html](frontend/src/index.html) - HTML entry point

---

## Running the Application

### Backend Startup
```bash
cd sponsorship-app-backend
./mvnw spring-boot:run
# or on Windows:
mvnw.cmd spring-boot:run
```
- Starts on http://localhost:7070
- H2/MySQL database initialized automatically
- Demo data loaded if DataInitializer is enabled

### Frontend Startup
```bash
cd sponsorship-app-backend/frontend
npm install
ng serve
# or npm start
```
- Starts on http://localhost:4200
- Connects to backend at http://localhost:7070
- Hot-reload enabled during development

### Demo Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@sponsorship.com | admin123 |
| Brand | brand@example.com | brand123 |
| Influencer | influencer@example.com | influencer123 |

---

## Key Statistics

| Metric | Count |
|--------|-------|
| Controllers | 7 |
| Services | 7 |
| Entities | 6 |
| Components | 15+ |
| Routes | 12+ |
| API Endpoints | 40+ |
| Enums | 4 |
| Data Models | 6+ |
| Guards | 2 |
| Interceptors | 1 |

---

## Technologies Used by Component

| Component | Technologies |
|-----------|--------------|
| **Security** | Spring Security, JWT, BCrypt, OAuth-like token system |
| **Database** | MySQL, JPA/Hibernate, Spring Data |
| **Backend API** | Spring Boot, RESTful endpoints, Maven |
| **Frontend** | Angular 17, RxJS Observables, HttpClient |
| **UI** | Angular Material, SCSS, Responsive Design |
| **Deployment** | Embedded Tomcat (Backend), Node.js (Frontend) |
| **Code Utilities** | Lombok, DTOs, DTOs, Enums |

---

This comprehensive structure creates a full-featured sponsorship marketplace connecting brands and influencers with campaign management, payment processing, and mutual ratings.
