# 🎓 Senior Engineer's Complete Project Guide: Sponsorship App

> **Table of Contents**
> 1. [Architecture Overview](#architecture-overview)
> 2. [Backend Deep Dive](#backend-deep-dive)
> 3. [Frontend Deep Dive](#frontend-deep-dive)
> 4. [Data Flow & Control Flow](#data-flow--control-flow)
> 5. [Design Patterns & Best Practices](#design-patterns--best-practices)
> 6. [Complete Execution Flow](#complete-execution-flow)
> 7. [Breakpoint Analysis](#breakpoint-analysis)
> 8. [Interview Analogies](#interview-analogies)
> 9. [Strengths & Improvements](#strengths--improvements)

---

# ARCHITECTURE OVERVIEW

## The Big Picture

Your sponsorship app is a **three-tier distributed system**:

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│                   Angular 17 (Port 4200)                        │
│  - Components, Services, Guards, Interceptors, Models           │
│  - HTTP Client for API communication                            │
└──────────────────────┬──────────────────────────────────────────┘
                       │ HTTP/REST API (JSON)
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                    APPLICATION LAYER                            │
│              Spring Boot 3.2 (Port 7070)                        │
│  - Controllers, Services, Repositories, Security, DTO Layer    │
│  - JWT Authentication, Role-based Authorization                │
│  - Business Logic & Validation                                  │
└──────────────────────┬──────────────────────────────────────────┘
                       │ JDBC/Hibernate ORM
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                      DATA LAYER                                 │
│            MySQL Database (Port 3306)                           │
│  - 6 Core Tables: Users, Campaigns, Sponsorships,               │
│    Payments, Ratings, Notifications                            │
└──────────────────────────────────────────────────────────────────┘
```

### Key Architectural Principles

- **Separation of Concerns**: Each layer has distinct responsibilities
- **Stateless Backend**: REST API with JWT tokens (not session-based)
- **Role-Based Access Control (RBAC)**: Different endpoints for ADMIN, BRAND, INFLUENCER
- **Asynchronous Communication**: Frontend uses RxJS Observables, backend is request-response
- **Single Source of Truth**: Database is the canonical state

---

# BACKEND DEEP DIVE

## 1. Entry Point: Application Startup

### File: `SponsorshipAppBackendApplication.java`

```java
@SpringBootApplication
public class SponsorshipAppBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SponsorshipAppBackendApplication.class, args);
    }
}
```

**What happens when you run `mvnw.cmd spring-boot:run`:**

1. Spring discovers `@SpringBootApplication` annotation
2. Auto-configuration kicks in (detects Spring Data JPA, Spring Security, etc.)
3. Tomcat servlet container starts on port 7070
4. Database connection pool initialized (HikariCP)
5. Hibernate ORM scans entities and creates/updates tables
6. Security filters registered
7. All beans instantiated and wired via dependency injection

**Analogy:** Like turning on an assembly line → all machines boot up, connect to each other, and stand ready to process work orders.

---

## 2. Configuration Layer

### File: `application.properties`

```properties
# Database Setup
spring.datasource.url=jdbc:mysql://localhost:3306/sponsorshipdb?createDatabaseIfNotExist=true
spring.jpa.hibernate.ddl-auto=update  # Auto-creates/updates tables

# JWT Tokens (Authentication)
app.jwt.secret=mySecretKey...  # Signing key for tokens
app.jwt.expiration=86400000     # 24 hours in milliseconds

# CORS (Cross-Origin)
app.cors.allowed-origins=http://localhost:4200  # Only Angular app can call us
```

**Why each setting matters:**

| Setting | Purpose | Impact |
|---------|---------|--------|
| `ddl-auto=update` | Auto-sync Java entities with DB | First run: creates tables; later runs: adds columns |
| `jwt.secret` | Signs tokens cryptographically | If someone has this key, they can forge tokens |
| `cors.allowed-origins` | Security gate | Prevents unauthorized domains from making API calls |

---

## 3. Security Architecture

### Three-Layer Security

#### Layer 1: JWT Token Provider
**File:** `security/JwtTokenProvider.java`

```
User Login → Generate JWT Token → Token contains (email, role, expiration)
                                     ↓
            Token signed with SECRET KEY (HMAC-SHA256)
                                     ↓
        Send token to frontend, stored in localStorage
```

**Token Structure:**
```
Header.Payload.Signature

Header: {"alg": "HS256", "typ": "JWT"}
Payload: {"email": "brand@example.com", "roles": ["BRAND"], "exp": 1234567890}
Signature: HMACSHA256(secret_key, Header.Payload)
```

#### Layer 2: JWT Filter
**File:** `security/JwtAuthenticationFilter.java`

**What happens on every request:**

```
Request arrives with header: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                     │
                                                     ▼
                                    Extract token from "Bearer " prefix
                                                     │
                                                     ▼
                                    Validate signature (JWT hasn't been tampered)
                                                     │
                                                     ▼
                                    Extract email from token payload
                                                     │
                                                     ▼
                                    Load user details from database
                                                     │
                                                     ▼
                                    Set in SecurityContext (available via magic)
                                                     │
                                                     ▼
                                    Request proceeds to controller
```

**Security Context Magic:** After filter runs, you can access current user anywhere via:
```java
@Autowired private SecurityContext context;
String email = context.getAuthentication().getName();  // You're logged in as this email
```

#### Layer 3: Authorization (Role Checking)
**File:** `controller/CampaignController.java` (Example)

```java
@PostMapping
@PreAuthorize("hasRole('BRAND')")  // Only BRAND users can create campaigns
public ResponseEntity<?> createCampaign(@RequestBody CampaignRequest req) {
    // This method only runs if user has BRAND role
    // Spring Security automatically rejects non-BRAND users with 403 Forbidden
}

@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can view all users
public ResponseEntity<?> getAllUsers() { }
```

---

## 4. Data Layer: Entities & Relationships

### The 6 Core Entities

**Entity 1: User** (Base for all three roles)
```java
@Entity
public class User {
    @Id long id;              // Primary key
    String name;              // Username
    String email;             // Login email (unique)
    String password;          // BCrypt encrypted
    @Enumerated UserRole role; // ADMIN, BRAND, INFLUENCER
    String bio;               // Profile bio
    String profileImage;      // Avatar URL
}
```

**Entity 2: Campaign** (Job posting from Brand)
```java
@Entity
public class Campaign {
    @Id long id;
    String name;              // "Nike Summer Promotion"
    String description;
    String platform;          // "Instagram"
    Double budget;            // Marketing spend: $5000
    LocalDate startDate;
    LocalDate endDate;
    String eligibility;       // Requirements
    @Enumerated CampaignStatus status;  // ACTIVE, PAUSED, COMPLETED
    
    @ManyToOne
    @JoinColumn(name = "brand_id")
    User brand;               // Who created it (must be BRAND user)
    
    @OneToMany(mappedBy = "campaign")
    List<SponsorshipRequest> requests;  // Influencers who applied
}
```

**Entity 3: SponsorshipRequest** (Application from Influencer to Campaign)
```java
@Entity
public class SponsorshipRequest {
    @Id long id;
    
    @ManyToOne
    @JoinColumn(name = "influencer_id")
    User influencer;          // Who applied
    
    @ManyToOne
    @JoinColumn(name = "campaign_id")
    Campaign campaign;        // Applied to what campaign
    
    String proposal;          // "I can reach 100K followers on Instagram"
    
    @Enumerated RequestStatus status;  // PENDING → ACCEPTED → COMPLETED → PAID
}
```

**Entity 4: Payment** (Money transfer)
```java
@Entity
public class Payment {
    @Id long id;
    
    @ManyToOne Campaign campaign;
    @ManyToOne User influencer;    // Getting paid
    @ManyToOne User brand;         // Paying
    
    Double amount;            // How much
    @Enumerated PaymentStatus status;  // PENDING → COMPLETED
    LocalDateTime paidAt;     // When it was completed
}
```

**Entity 5: Rating** (Feedback system)
```java
@Entity
public class Rating {
    @Id long id;
    
    @ManyToOne User raterId;       // Who gave the rating
    @ManyToOne User ratedId;       // Who got rated
    @ManyToOne Campaign campaign;
    
    Integer score;            // 1-5 stars
    String feedback;          // Text review
}
```

**Entity 6: Notification** (Alert system)
```java
@Entity
public class Notification {
    @Id long id;
    @ManyToOne User user;     // Who gets notified
    String title;
    String message;
    Boolean isRead;
    LocalDateTime createdAt;
}
```

### Relationships Visualized

```
                    User (Brand)
                        │
                        ├─ creates ─► Campaign ◄── applies ─ User (Influencer)
                        │                │
                        │                ├─ has many ─► SponsorshipRequest
                        │                │                      │
                        │                │                      └─ status: PENDING/ACCEPTED/COMPLETED
                        │                │
                        ├─ pays ──── Payment
                        │                │
                        └─ rates ─► Rating ◄┬─ gives (Influencer rates Brand)
                                       └─ receives (Brand rates Influencer)

Notification ────► User (any role)
```

---

## 5. Service Layer: Business Logic

### Example: SponsorshipService (Core Business Logic)

**File:** `service/SponsorshipService.java`

```java
@Service
public class SponsorshipService {
    
    // ============ APPLY FOR CAMPAIGN ============
    // When influencer clicks "Apply" on a campaign
    public SponsorshipRequest applyForCampaign(Long campaignId, 
                                               String proposal,
                                               User influencer) {
        // Step 1: Find the campaign
        Campaign campaign = campaignRepo.findById(campaignId)
            .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));
            
        // Step 2: Validate (can influencer apply?)
        if (campaign.getStatus() == COMPLETED) {
            throw new InvalidOperationException("Campaign finished");
        }
        
        // Step 3: Check if already applied (prevent duplicates)
        boolean alreadyApplied = request exists for (influencer + campaign);
        if (alreadyApplied) {
            throw new InvalidOperationException("Already applied");
        }
        
        // Step 4: Create sponsorship request
        SponsorshipRequest request = new SponsorshipRequest();
        request.setInfluencer(influencer);
        request.setCampaign(campaign);
        request.setProposal(proposal);
        request.setStatus(PENDING);
        
        // Step 5: Save to database
        sponsorshipRepo.save(request);
        
        // Step 6: Notify the brand
        notificationService.notifyBrand(campaign.getBrand(),
            "New application: " + influencer.getName() + " applied to your campaign");
            
        return request;
    }
    
    // ============ ACCEPT/REJECT APPLICATION ============
    public void updateRequestStatus(Long requestId, String newStatus) {
        SponsorshipRequest request = repo.findById(requestId).orElseThrow();
        
        // Validate state transition
        if (request.getStatus() == COMPLETED) {
            throw new InvalidOperationException("Can't change completed request");
        }
        
        request.setStatus(newStatus);
        repo.save(request);
        
        // Notify influencer
        notificationService.notifyInfluencer(request.getInfluencer(),
            "Your application to " + request.getCampaign().getName() + 
            " was " + newStatus.toLowerCase());
    }
}
```

### Why This Pattern?

| Layer | What | Why |
|-------|------|-----|
| Controller | HTTP handling | Takes data from web, validates format |
| Service | Business logic | All decision-making lives here |
| Repository | Database access | Abstraction → can change DB without changing business logic |

**Analogy:** Restaurant → Waiter (Controller) takes order → Chef (Service) decides how to cook → Warehouse (Repository) gets ingredients.

---

## 6. Controller Layer: HTTP Endpoints

### Example: AuthController

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // SIGNUP: Create new user
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        // 1. Validate input
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email already registered");
        }
        
        // 2. Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // Encrypt!
        user.setRole(request.getRole());  // ADMIN, BRAND, or INFLUENCER
        
        // 3. Save to database
        userRepo.save(user);
        
        // 4. Return success message
        return ResponseEntity.ok("User created successfully");
    }
    
    // LOGIN: Authenticate user & generate JWT token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. Find user by email
        User user = userRepo.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email/password"));
            
        // 2. Compare provided password with stored password (both encrypted)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/password");
        }
        
        // 3. Generate JWT token (lasts 24 hours)
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole());
        
        // 4. Return token to frontend
        return ResponseEntity.ok(new LoginResponse(
            token: token,
            userEmail: user.getEmail(),
            userRole: user.getRole()
        ));
    }
}
```

**Request/Response Flow:**

```
Frontend (Angular):
  POST http://localhost:7070/api/auth/login
  Body: { "email": "brand@example.com", "password": "brand123" }
  
  ↓ (Network)
  
Backend (Spring Boot):
  1. AuthController receives request
  2. Validates password
  3. Generates JWT: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  4. Returns: { "token": "eyJ...", "userRole": "BRAND" }
  
  ↓ (Network, JSON)
  
Frontend (Angular):
  1. Receives response
  2. localStorage.setItem("token", "eyJ...")
  3. Redirects to /dashboard
```

---

## 7. Flow Execution: Complete Request-Response Cycle

### Scenario: Brand Creates a Campaign

```
[1] USER CLICKS "CREATE CAMPAIGN" BUTTON (Frontend)
    ↓
    Angular: Route: /create-campaign → CampaignFormComponent

[2] USER FILLS FORM & CLICKS SUBMIT (Frontend)
    ↓
    CampaignComponent calls:
    this.campaignService.createCampaign(formData)
    
[3] FRONTEND MAKES HTTP REQUEST (Network)
    POST http://localhost:7070/api/campaigns
    Headers: {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
    Body: {
        "name": "Nike Summer Promo",
        "budget": 5000,
        "platform": "Instagram",
        "description": "..."
    }

[4] REQUEST ARRIVES AT BACKEND (Spring Boot)
    ↓
    CORS Filter checks:
    - Is request from http://localhost:4200? YES ✓ Continue
    
    ↓
    JwtAuthenticationFilter intercepts:
    - Extract token from "Bearer ..." header
    - Validate signature (hasn't been tampered)
    - Load user email from token: "brand@example.com"
    - Set in SecurityContext (available everywhere now)
    
    ↓
    Spring DispatcherServlet routes to:
    CampaignController.createCampaign()
    
    ↓
    Check @PreAuthorize("hasRole('BRAND')")
    - Current user role is BRAND? YES ✓ Continue
    
[5] CONTROLLER PROCESSES REQUEST
    CampaignController.createCampaign(CampaignRequest req):
    {
        campaignService.createCampaign(req, currentUser);
    }
    
    ↓
    CampaignService.createCampaign():
    {
        // Validate: campaign name not empty
        // Validate: budget > 0
        // Get current user (is this a BRAND user?)
        
        Campaign campaign = new Campaign();
        campaign.setName(req.getName());
        campaign.setBrand(currentUser);  // Link to the BRAND who created it
        campaign.setStatus(ACTIVE);
        
        campaignRepository.save(campaign);  // Goes to database
        
        // Trigger notification to admin
        notificationService.notify(adminUsers, 
            "New campaign created: Nike Summer Promo");
    }
    
[6] DATABASE WRITE
    Hibernate translates to SQL:
    INSERT INTO campaigns (name, budget, brand_id, status, created_at)
    VALUES ('Nike Summer Promo', 5000, 42, 'ACTIVE', NOW());
    
    MySQL executes, returns inserted ID: 1001
    
[7] RESPONSE SENT BACK
    HTTP 201 Created
    Body: {
        "id": 1001,
        "name": "Nike Summer Promo",
        "budget": 5000,
        "status": "ACTIVE",
        "brand": { "id": 42, "email": "brand@example.com" }
    }

[8] FRONTEND RECEIVES RESPONSE
    CampaignService.createCampaign() returns Observable
    Component subscribes:
    {
        .subscribe(
            (campaign) => {
                // Success!
                this.campaigns.push(campaign);
                this.snackBar.open("Campaign created!");
                this.router.navigate(['/campaigns', campaign.id]);
            },
            (error) => {
                // Network error or validation failed
                this.snackBar.open("Failed: " + error.message);
            }
        )
    }

[9] USER SEE RESULT
    ✓ Page redirects to campaign detail view
    ✓ New campaign appears in campaigns list
```

---

# FRONTEND DEEP DIVE

## 1. Angular Architecture

### File Structure

```
frontend/src/app/
├── components/
│   ├── auth/
│   │   ├── login/                 [User enters email/password]
│   │   └── signup/                [User registers account]
│   ├── campaign/
│   │   ├── campaign-list/         [Browse all campaigns]
│   │   ├── campaign-detail/       [View single campaign]
│   │   └── campaign-form/         [Create/Edit campaign]
│   ├── sponsorship/
│   │   └── sponsorship-request/   [Manage applications]
│   ├── payment/                   [View payments/earnings]
│   ├── rating/                    [View & submit ratings]
│   ├── dashboard/
│   │   ├── admin-dashboard/       [Admin overview]
│   │   ├── brand-dashboard/       [Brand's campaigns & stats]
│   │   └── influencer-dashboard/  [Influencer's earnings]
│   └── shared/
│       ├── navbar/                [Header with navigation]
│       ├── payment-dialog/        [Modal for making payment]
│       └── rating-dialog/         [Modal for submitting rating]
├── services/
│   ├── auth.service.ts            [Handles login/signup HTTP calls]
│   ├── campaign.service.ts
│   ├── sponsorship.service.ts
│   ├── payment.service.ts
│   ├── rating.service.ts
│   ├── notification.service.ts
│   └── admin.service.ts
├── models/
│   ├── user.model.ts              [TypeScript interfaces]
│   ├── campaign.model.ts
│   ├── payment.model.ts
│   └── ...
├── guards/
│   ├── auth.guard.ts              [Check: Are you logged in?]
│   └── role.guard.ts              [Check: Do you have required role?]
├── interceptors/
│   └── auth.interceptor.ts        [Auto-add JWT to all requests]
└── app.module.ts                  [Main module, imports all]
```

## 2. Authentication Flow (Frontend Side)

### File: `auth.service.ts`

```typescript
@Injectable()
export class AuthService {
    
    login(email: string, password: string): Observable<any> {
        return this.http.post('/api/auth/login', { email, password })
            .pipe(
                tap(response => {
                    // Store token in browser's localStorage (persists across page reloads)
                    localStorage.setItem('token', response.token);
                    localStorage.setItem('userRole', response.userRole);
                    localStorage.setItem('userEmail', response.userEmail);
                    
                    // Emit event: "User logged in!"
                    this.isLoggedIn$.next(true);
                    this.currentRole$.next(response.userRole);
                }),
                catchError(error => {
                    // Network error or bad credentials
                    return throwError(() => new Error('Login failed'));
                })
            );
    }
    
    getToken(): string | null {
        return localStorage.getItem('token');
    }
    
    isLoggedIn(): boolean {
        return !!this.getToken();
    }
}
```

**What localStorage does:** Browser's hard drive for small data
```
When you login:
    localStorage: {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "userRole": "BRAND",
        "userEmail": "brand@example.com"
    }

When user closes browser and comes back next day:
    Token still exists! They're still logged in.
    
When user logs out:
    localStorage.clear()  → token gone
```

## 3. HTTP Interceptor: Auto-Inject JWT

### File: `auth.interceptor.ts`

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.authService.getToken();
        
        // If we have a token, add it to EVERY request
        if (token) {
            req = req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }
        
        return next.handle(req).pipe(
            catchError(error => {
                // If 401 Unauthorized (token expired), force logout
                if (error.status === 401) {
                    this.authService.logout();
                    this.router.navigate(['/login']);
                }
                return throwError(() => error);
            })
        );
    }
}
```

**What this does:** Saves repetition
```
Without interceptor:
    this.http.post('/api/campaigns', data, {
        headers: {'Authorization': 'Bearer eyJ...'}
    })

With interceptor:
    this.http.post('/api/campaigns', data)  // Token added automatically!
```

## 4. Route Guards: Authorization

### File: `auth.guard.ts`

```typescript
@Injectable()
export class AuthGuard implements CanActivate {
    
    canActivate(route: ActivatedRouteSnapshot): boolean {
        const isLoggedIn = this.authService.isLoggedIn();
        
        if (isLoggedIn) {
            return true;  // ✓ Allow navigation
        } else {
            this.router.navigate(['/login']);  // ✗ Redirect to login
            return false;
        }
    }
}
```

### File: `role.guard.ts`

```typescript
@Injectable()
export class RoleGuard implements CanActivate {
    
    canActivate(route: ActivatedRouteSnapshot): boolean {
        const requiredRole = route.data['role'];  // What role is required?
        const userRole = this.authService.getCurrentRole();
        
        if (userRole === requiredRole) {
            return true;  // ✓ User has required role
        } else {
            this.router.navigate(['/unauthorized']);  // ✗ Not allowed
            return false;
        }
    }
}
```

### Usage in Routing

```typescript
const routes: Routes = [
    { 
        path: 'dashboard/brand',
        component: BrandDashboardComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: { role: 'BRAND' }
    },
    {
        path: 'dashboard/admin',
        component: AdminDashboardComponent,
        canActivate: [AuthGuard, RoleGuard],
        data: { role: 'ADMIN' }
    }
];
```

**What happens when Influencer tries to access /dashboard/brand:**

```
User navigates to /dashboard/brand
    ↓
Angular Router checks canActivate guards
    ↓
AuthGuard: Is user logged in? YES ✓
    ↓
RoleGuard: User role = INFLUENCER, required = BRAND? NO ✗
    ↓
RoleGuard returns false
    ↓
Router redirects to /unauthorized
    ↓
User sees: "You don't have permission"
```

## 5. Component Example: Sponsorship Request List

### File: `sponsorship-request.component.ts`

```typescript
@Component({
    selector: 'app-sponsorship-request',
    templateUrl: './sponsorship-request.component.html'
})
export class SponsorshipRequestComponent implements OnInit {
    requests: SponsorshipRequest[] = [];
    isBrand = false;
    isInfluencer = false;
    paymentsByRequestId: Map<number, any> = new Map();  // NEW: Track payments
    
    constructor(
        private sponsorshipService: SponsorshipService,
        private paymentService: PaymentService,
        private authService: AuthService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar
    ) {
        // Determine what role user is
        this.isBrand = this.authService.hasRole('BRAND');
        this.isInfluencer = this.authService.hasRole('INFLUENCER');
    }
    
    ngOnInit() {
        this.loadRequests();
    }
    
    loadRequests() {
        // Load different data based on role
        const source = this.isBrand
            ? this.sponsorshipService.getBrandRequests()  // Brand sees applications
            : this.sponsorshipService.getMyApplications();  // Influencer sees their applications
            
        source.subscribe({
            next: (requests) => {
                this.requests = requests;
                this.loadPaymentStatus();  // NEW: Check which have payments
            },
            error: () => this.isLoading = false
        });
    }
    
    loadPaymentStatus() {
        if (this.isBrand) {
            this.paymentService.getBrandPayments().subscribe({
                next: (payments) => {
                    // Map each payment to its associated sponsorship request
                    this.paymentsByRequestId = new Map();
                    payments.forEach(payment => {
                        this.requests.forEach(request => {
                            // Same campaign + same influencer = same sponsorship
                            if (request.campaign?.id === payment.campaign?.id &&
                                request.influencer?.id === payment.influencer?.id) {
                                this.paymentsByRequestId.set(request.id, payment);
                            }
                        });
                    });
                }
            });
        }
    }
    
    // Check if payment already made for this request
    hasPaymentCompleted(request: SponsorshipRequest): boolean {
        const payment = this.paymentsByRequestId.get(request.id);
        return payment && payment.status === 'COMPLETED';
    }
    
    // Check if payment pending (user already clicked "Make Payment")
    hasPaymentPending(request: SponsorshipRequest): boolean {
        const payment = this.paymentsByRequestId.get(request.id);
        return payment && payment.status === 'PENDING';
    }
    
    // Brand clicks "Make Payment" button
    openPaymentDialog(request: SponsorshipRequest) {
        const dialogRef = this.dialog.open(PaymentDialogComponent, {
            data: {
                campaignName: request.campaign?.name,
                influencerName: request.influencer?.name,
                campaignId: request.campaign?.id,
                influencerId: request.influencer?.id
            }
        });
        
        // After dialog closes, if user submitted payment...
        dialogRef.afterClosed().subscribe(result => {
            if (result) {  // result = payment amount user entered
                // Create payment record
                this.paymentService.createPayment(result).subscribe({
                    next: () => {
                        this.snackBar.open('Payment created!');
                        this.loadPaymentStatus();  // Refresh UI
                    }
                });
            }
        });
    }
}
```

### File: `sponsorship-request.component.html` (Template)

```html
<!-- BRAND VIEW: Shows what they see when status = COMPLETED -->
<ng-container *ngIf="isBrand && request.status === 'COMPLETED'">
    <!-- Button state machine:
         1. No payment yet: "Make Payment" enabled
         2. Payment created (PENDING): "Make Payment" disabled
         3. Payment completed: "Payment Completed" disabled
    -->
    <button mat-raised-button
            color="primary"
            [disabled]="hasPaymentPending(request) || hasPaymentCompleted(request)"
            (click)="openPaymentDialog(request)">
        <mat-icon>{{ hasPaymentCompleted(request) ? 'check_circle' : 'payment' }}</mat-icon>
        {{ hasPaymentCompleted(request) ? 'Payment Completed' : 'Make Payment' }}
    </button>
</ng-container>

<!-- INFLUENCER VIEW: Shows when status = ACCEPTED what they need to do -->
<ng-container *ngIf="isInfluencer && request.status === 'ACCEPTED'">
    <span class="success-text">
        <mat-icon>work</mat-icon> Work in progress
    </span>
    <span class="info-text">
        <mat-icon>info</mat-icon> Complete work to get payment
    </span>
</ng-container>
```

---

# DATA FLOW & CONTROL FLOW

## End-to-End Scenario: Influencer Gets Paid

### Step-by-Step Journey

```
SCENARIO: Influencer "John" completes work and gets paid $500

╔═════════════════════════════════════════════════════════════════════════════╗
║ STEP 1: Influencer Completes Work (Frontend)                                ║
╚═════════════════════════════════════════════════════════════════════════════╝

UI: John sees campaign "Nike Summer Promo" with status "ACCEPTED"
    Sees button: "Mark Work Complete"
    
John clicks button → sponsorshipComponent.markAsCompleted(requestId: 1001)
    ↓
HTTP Request:
    PUT /api/sponsorship/1001/status
    Body: { status: "COMPLETED" }
    
Backend receives → SponsorshipController.updateStatus()
    ↓
SponsorshipService.updateRequestStatus():
    - Finds SponsorshipRequest #1001
    - Changes status: PENDING → ACCEPTED → COMPLETED ✓
    - Saves to database
    - Creates notification: "Brand: John completed the work! Review and pay."
    
Frontend receives 200 OK
    ↓
UI updates: Button changes to "Work Complete ✓"


╔═════════════════════════════════════════════════════════════════════════════╗
║ STEP 2: Brand Reviews & Initiates Payment (Frontend)                        ║
╚═════════════════════════════════════════════════════════════════════════════╝

Brand logs in, sees sponsorship request status = COMPLETED
    ↓
UI shows button: "Make Payment" (enabled)
    
Brand clicks → openPaymentDialog(request)
    ↓
Material Dialog opens, modal appears
    User sees: "Campaign: Nike Summer Promo"
              "Paying: John (influencer)"
              "Amount: [text field - editable]"
              "Suggested: $500"
    
Brand enters: $500 (or different amount)
Brand clicks: "Process Payment"
    ↓
HTTP Request:
    POST /api/payments
    Body: {
        campaignId: 42,
        influencerId: 1001,
        amount: 500
    }
    
Backend receives → PaymentController.createPayment()
    ↓
PaymentService.createPayment():
    - Validates: amount > 0 ✓
    - Validates: campaign exists ✓
    - Validates: influencer exists ✓
    - Creates Payment object:
        {
            id: 5000,
            campaign: Campaign #42,
            influencer: User #1001 (John),
            brand: User #43 (Nike),
            amount: 500,
            status: "PENDING",
            createdAt: NOW()
        }
    - Saves to database
    - Creates notification: "John: You earned $500 for Nike campaign! Check payments."
    
Frontend receives:
    { id: 5000, status: "PENDING", amount: 500 }
    ↓
UI updates:
    - Button changes to "Payment Completed" (disabled)
    - Success message: "Payment created successfully!"


╔═════════════════════════════════════════════════════════════════════════════╗
║ STEP 3: Complete Payment in Payment Dashboard (Frontend)                    ║
╚═════════════════════════════════════════════════════════════════════════════╝

Brand navigates to: /payments (Payment component)
    ↓
PaymentComponent.ngOnInit():
    - Calls: paymentService.getBrandPayments()
    - Shows table of all payments brand created
    - Column: Status
    - For PENDING payments, shows "Complete" button
    
Brand sees payment: "Nike Summer Promo | $500 | PENDING | [Complete button]"
    
Brand clicks: [Complete button] → completePayment(paymentId: 5000)
    ↓
HTTP Request:
    PUT /api/payments/5000/complete
    
Backend receives → PaymentController.completePayment()
    ↓
PaymentService.completePayment():
    - Finds Payment #5000
    - Changes status: PENDING → COMPLETED ✓
    - Sets: paidAt: NOW()
    - Saves to database
    - Creates notification: "John: Payment of $500 completed!"
    
Frontend receives 200 OK
    ↓
UI updates:
    - Payment status changes to "COMPLETED"
    - Button disappears
    - Influencer's earnings increase


╔═════════════════════════════════════════════════════════════════════════════╗
║ STEP 4: Influencer Sees Earnings (Frontend)                                 ║
╚═════════════════════════════════════════════════════════════════════════════╝

John (Influencer) logs in, navigates to /payments
    ↓
PaymentComponent.ngOnInit():
    - Calls: paymentService.getInfluencerPayments()
    - Shows all payments John received
    
John sees:
    "My Earnings: $500 total"
    [Table]
    Campaign          | Amount | Status     | Date
    Nike Summer Promo | $500   | COMPLETED  | May 20, 2026
    
UI also loads notification:
    ✓ "Payment of $500 completed!"


╔═════════════════════════════════════════════════════════════════════════════╗
║ DATABASE STATE AFTER ALL STEPS                                              ║
╚═════════════════════════════════════════════════════════════════════════════╝

users table:
    id=43  name=Nike       role=BRAND
    id=1001 name=John      role=INFLUENCER

campaigns table:
    id=42  name=Nike Summer Promo  brand_id=43

sponsorship_requests table:
    id=1001 campaign_id=42 influencer_id=1001 status=COMPLETED

payments table:
    id=5000 campaign_id=42 influencer_id=1001 brand_id=43
            amount=500 status=COMPLETED paidAt=2026-05-20 11:30:00

notifications table:
    id=N1   user_id=43   message=John completed work
    id=N2   user_id=1001 message=You earned $500
    id=N3   user_id=1001 message=Payment of $500 completed
```

---

# DESIGN PATTERNS & BEST PRACTICES

## 1. MVC Pattern (Model-View-Controller)

Your app strictly follows MVC:

| Layer | Files | Responsibility |
|-------|-------|-----------------|
| **Model** | `entity/*.java` | Data structure (what data we store) |
| **View** | `*.component.html` | User interface (what user sees) |
| **Controller** | `controller/*.java` | Handles HTTP requests, calls service |
| **Service** | `service/*.java` | Business logic (how things work) |
| **Repository** | `repository/*.java` | Database access (CRUD operations) |

## 2. Dependency Injection (DI)

```java
@Service
public class PaymentService {
    
    // Instead of: PaymentRepository repo = new PaymentRepository();
    // Spring automatically provides instances (injects dependencies)
    
    @Autowired
    private PaymentRepository paymentRepo;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentMapper paymentMapper;
}
```

**Why?** Loose coupling → easy to test, easy to change implementations

## 3. DTO (Data Transfer Object) Pattern

```java
// Entity (Database schema)
@Entity
public class User {
    Long id;
    String email;
    String password;
    String role;
    String profileImage;
}

// DTO (What we send over HTTP)
public class UserDTO {
    Long id;
    String email;
    String role;
    String profileImage;
    // NOTE: No password! Never send passwords over HTTP
}

// Mapping
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    User user = userRepo.findByEmail(req.getEmail());
    UserDTO userDTO = new UserDTO();
    userDTO.setId(user.getId());
    userDTO.setEmail(user.getEmail());
    // Don't set password!
    return ResponseEntity.ok(userDTO);
}
```

**Why?** Security (never expose password), separation (DB ≠ API contract)

## 4. Repository Pattern

```java
// Abstraction layer between service and database
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom query methods
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    boolean existsByEmail(String email);
}

// In service:
@Service
public class AuthService {
    
    @Autowired private UserRepository userRepo;
    
    public void signup(String email) {
        // We don't care HOW it's saved (SQL, MongoDB, etc.)
        // Repository abstraction handles it
        userRepo.save(user);
    }
}
```

**Why?** If you switch databases, only repository changes, service stays same

## 5. Interceptor Pattern (Frontend)

```typescript
// Automatically modify EVERY HTTP request
export class AuthInterceptor implements HttpInterceptor {
    intercept(req, next): Observable {
        // Before request
        const newReq = req.clone({
            setHeaders: { Authorization: 'Bearer ' + token }
        });
        
        return next.handle(newReq).pipe(
            tap(response => { /* After success */ }),
            catchError(error => { /* After error */ })
        );
    }
}
```

**Why?** DRY principle - don't repeat "add token" in 50 places

## 6. Observable Pattern (Reactive Programming)

```typescript
// Frontend is reactive, not imperative
// Instead of: var campaigns = getCampaigns(); (blocking)
// We use: getCampaigns().subscribe(campaigns => { ... }) (async)

campaigns$ = this.campaignService.getCampaigns().pipe(
    // Operators transform the data stream
    map(campaigns => campaigns.filter(c => c.status === 'ACTIVE')),
    shareReplay(1),  // Cache result, share among subscribers
    tap(campaigns => console.log('Campaigns loaded:', campaigns))
);

// In template (automatic unsubscribe via OnDestroy)
<div *ngFor="let campaign of campaigns$ | async">
    {{ campaign.name }}
</div>
```

## 7. Role-Based Access Control (RBAC)

```java
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    
    // Only ADMIN can see all campaigns
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Campaign> getAllCampaigns() { }
    
    // Any authenticated user can view active campaigns
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Campaign> getCampaigns() { }
    
    // Only BRAND can create campaigns
    @PostMapping
    @PreAuthorize("hasRole('BRAND')")
    public Campaign create(@RequestBody CampaignRequest req) { }
    
    // Only the BRAND who owns this campaign can edit it
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BRAND')")
    public Campaign update(@PathVariable Long id, @RequestBody CampaignRequest req) {
        Campaign campaign = repo.findById(id).orElseThrow();
        
        // Extra check: Is current user the brand who created it?
        if (!campaign.getBrand().getId().equals(getCurrentUserId())) {
            throw new UnauthorizedException("Not your campaign");
        }
        
        return campaignService.update(campaign, req);
    }
}
```

---

# COMPLETE EXECUTION FLOW

## From Startup to User Login

```
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 1: SYSTEM STARTUP                                                  ║
╚═══════════════════════════════════════════════════════════════════════════╝

USER RUNS: mvnw.cmd spring-boot:run (or java -jar sponsorship-app.jar)

┌─ [Backend Startup]
│
├─ Java Virtual Machine starts
├─ Spring Boot detects @SpringBootApplication
├─ Auto-configuration kicks in
├─ Database connected:
│   ├─ Tries connection to jdbc:mysql://localhost:3306/sponsorshipdb
│   ├─ If DB doesn't exist → creates it (createDatabaseIfNotExist=true)
│   ├─ HikariCP connection pool initialized (10 connections default)
│   └─ Ready to accept queries
├─ Hibernate ORM processes entities
│   ├─ Scans @Entity classes
│   ├─ Reads @OneToMany, @ManyToOne relationships
│   ├─ Runs DDL: CREATE/ALTER tables based on ddl-auto=update
│   └─ Demo data inserted (admin@, brand@, influencer@ users)
├─ Security filters registered:
│   ├─ CORS filter (allow localhost:4200)
│   ├─ JWT filter (process tokens)
│   ├─ Authorization filter (role checks)
│   └─ Session management disabled (stateless)
├─ All @Component, @Service, @Repository beans instantiated
├─ Dependency injection wired:
│   ├─ Services get repositories
│   ├─ Controllers get services
│   └─ Everything connected
├─ Tomcat servlet container starts on port 7070
└─ Startup complete! Ready to receive HTTP requests

┌─ [Frontend Startup]
│
├─ npm start triggers ng serve
├─ Webpack bundles Angular code into ~3 JS files
├─ Dev server starts on port 4200
├─ Angular detects app.module.ts
├─ All component directives registered
├─ All services instantiated
├─ HttpClient dependencies resolved
└─ App loads in browser!


╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 2: USER VISITS http://localhost:4200                               ║
╚═══════════════════════════════════════════════════════════════════════════╝

Browser receives index.html
    ↓
First thing: <script src="main.js"></script>
    ↓
main.ts runs:
    platformBrowserDynamic()
        .bootstrapModule(AppModule)
        .catch(err => console.error(err));
    ↓
AppComponent loads (root component)
    ↓
AppComponent template includes <router-outlet></router-outlet>
    ↓
Angular Router checks URL: http://localhost:4200
    ↓
Routes:
    { path: '', redirectTo: '/login' }  ← matches!
    { path: 'login', component: LoginComponent }
    ↓
Router redirects to /login
    ↓
LoginComponent loads:
    - Shows email input
    - Shows password input
    - Shows "Sign up" link
    - Shows "Login" button


╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 3: USER ENTERS CREDENTIALS                                         ║
╚═══════════════════════════════════════════════════════════════════════════╝

User enters:
    Email: brand@example.com
    Password: brand123
    
Clicks: [Login]
    ↓
LoginComponent calls:
    this.authService.login(email, password)
    ↓
AuthService makes HTTP call:
    POST http://localhost:7070/api/auth/login
    Body: { email: "brand@example.com", password: "brand123" }
    Interceptor adds nothing (no token yet)
    
Request arrives at Backend:
    ↓
AuthController.login(LoginRequest req)
    ├─ Find user by email: brand@example.com
    ├─ Load hashed password from DB: $2a$10$dxJ3SW6G...
    ├─ BCrypt compare: does "brand123" hash to stored value? YES ✓
    ├─ Generate JWT:
    │   Header: { "alg": "HS256", "typ": "JWT" }
    │   Payload: { "email": "brand@example.com", "roles": ["BRAND"], "exp": 1234567890 }
    │   Signature: HMAC-SHA256("mySecretKey", header.payload)
    │   Result: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImJyYW5kQGV4YW1wbGUuY29t..."
    └─ Return response:
        {
            token: "eyJ...",
            userRole: "BRAND",
            userEmail: "brand@example.com"
        }
    
Frontend receives response:
    ↓
AuthService.login() tap operator:
    ├─ localStorage.setItem("token", "eyJ...")
    ├─ localStorage.setItem("userRole", "BRAND")
    ├─ this.isLoggedIn$.next(true)  // Notify observers
    └─ this.currentRole$.next("BRAND")
    
LoginComponent subscribes:
    ├─ Success: Token saved!
    ├─ Navigate to: /dashboard/brand
    └─ Component destroys
    
Angular Router loads new route:
    ├─ canActivate: [AuthGuard, RoleGuard]
    ├─ AuthGuard: isLoggedIn()? localStorage has token? YES ✓
    ├─ RoleGuard: User role = BRAND, required = BRAND? YES ✓
    └─ Navigation allowed!
    
BrandDashboardComponent loads:
    ├─ Shows welcome: "Welcome, brand@example.com"
    ├─ Calls: campaignService.getBrandCampaigns()
    ├─ HTTP GET /api/campaigns?filter=mine
    ├─ AuthInterceptor adds: Authorization: Bearer eyJ...
    ├─ Backend processes JWT: verifies signature, loads user
    ├─ Backend returns brand's campaigns
    └─ Dashboard displays campaigns list


╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 4: USER TERMINATES                                                 ║
╚═══════════════════════════════════════════════════════════════════════════╝

User clicks: "Logout"
    ↓
AuthService.logout():
    ├─ localStorage.removeItem("token")
    ├─ localStorage.clear()
    ├─ this.isLoggedIn$.next(false)
    └─ router.navigate(['/login'])
    
Current component destroyed
    ↓
AuthGuard blocks navigation if they try to go back
    ↓
LoginComponent shown again


╔═══════════════════════════════════════════════════════════════════════════╗
║ SYSTEM SHUTDOWN                                                          ║
╚═══════════════════════════════════════════════════════════════════════════╝

User closes browser or stops server:

Backend:
    ├─ Tomcat shuts down (closes port 7070)
    ├─ HikariCP connection pool closes DB connections
    ├─ Spring context destroyed
    └─ JVM terminates

Frontend:
    ├─ Angular stopped (no more ng serve)
    ├─ Dev server closes (port 4200 released)
    └─ Webpack stops watching files
```

---

# BREAKPOINT ANALYSIS

## Breakpoint 1: In `AuthController.login()`

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    
    User user = userRepo.findByEmail(request.getEmail());
    // ← BREAKPOINT: Register here
    
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new UnAuthenticatedException("Invalid credentials");
    }
    
    String token = jwtProvider.generateToken(user.getEmail(), user.getRole());
    return ResponseEntity.ok(new LoginResponse(token, user.getRole()));
}
```

**When breakpoint hits:**

```
What you see:
    request: LoginRequest { email: "brand@example.com", password: "brand123" }
    user: User { id: 2, name: "Nike Brand", email: "brand@example.com", ... }
    
What's happening:
    1. HTTP POST just arrived from frontend
    2. Spring deserialized JSON into LoginRequest object
    3. @RequestBody intercepted the body and mapped it
    4. Repository executed SQL: SELECT * FROM users WHERE email = '....'
    5. Found 1 row, created User object from DB data
    
Next steps:
    - Spring will compare plaintext password with BCrypt hash
    - Generate JWT token
    - Return 200 OK with token
    
To understand flow:
    - Look at user object: What fields are populated?
    - Look at request: Did it deserialize correctly?
    - Step over passwordEncoder.matches() to see if auth succeeds
```

## Breakpoint 2: In `JwtAuthenticationFilter.doFilterInternal()`

```java
protected void doFilterInternal(HttpServletRequest request, 
                               HttpServletResponse response, 
                               FilterChain filterChain) throws ServletException, IOException {
    try {
        String jwt = getJwtFromRequest(request);
        
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            // ← BREAKPOINT: Register here
            
            String username = tokenProvider.getUsernameFromToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception ex) {
        logger.error("Could not set user authentication", ex);
    }
    
    filterChain.doFilter(request, response);
}
```

**When breakpoint hits:**

```
What you see:
    jwt: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImJyYW5kQGV4YW1wbGUuY29tIn0..."
    username: "brand@example.com"
    userDetails: User { username: "brand@example.com", authorities: [BRAND], ... }
    
What's happening:
    1. Request came in with header: "Authorization: Bearer eyJ..."
    2. Filter extracted token (removed "Bearer " prefix)
    3. JWT signature was validated (hasn't been tampered)
    4. Payload extracted: got email "brand@example.com"
    5. Loaded user from DB using email
    6. About to set user in SecurityContext
    
Key insight:
    After this line: SecurityContextHolder.getContext().setAuthentication(...)
    
    The current user is now available EVERYWHERE:
    
    @GetMapping
    public ResponseEntity<?> myProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();  // "brand@example.com"
        Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
        
        // Now you know who's making the request!
    }
```

## Breakpoint 3: In `SponsorshipService.applyForCampaign()`

```java
public SponsorshipRequest applyForCampaign(Long campaignId, 
                                          String proposal,
                                          User influencer) {
    Campaign campaign = campaignRepo.findById(campaignId).orElseThrow();
    
    boolean alreadyApplied = sponsorshipRepo.existsByCampaignAndInfluencer(campaignId, influencer.getId());
    
    if (alreadyApplied) {
        // ← BREAKPOINT: Register here
        throw new InvalidOperationException("Already applied");
    }
    
    SponsorshipRequest request = new SponsorshipRequest();
    request.setInfluencer(influencer);
    request.setCampaign(campaign);
    request.setProposal(proposal);
    request.setStatus(PENDING);
    
    sponsorshipRepo.save(request);
    // ← BREAKPOINT #2: Register here too
    
    return request;
}
```

**Breakpoint #1 hits (already applied check):**

```
If user tries to apply twice:

What you see:
    campaignId: 42
    influencer: User { id: 100, name: "John", role: "INFLUENCER" }
    alreadyApplied: true
    
What's happening:
    1. JPA Query: SELECT COUNT(*) FROM sponsorship_requests 
                 WHERE campaign_id = 42 AND influencer_id = 100
    2. Result: 1 (one application already exists)
    3. Business logic says: throw exception!
    
   Expected flow:
    Controller catches exception → returns 400 Bad Request to frontend
    Frontend shows snackbar: "You already applied to this campaign"
```

**Breakpoint #2 hits (after saving):**

```
After save:

What you see:
    request: SponsorshipRequest {
        id: 1001,  // Auto-generated by database
        influencer: User { id: 100, name: "John" },
        campaign: Campaign { id: 42, name: "Nike..." },
        proposal: "I can reach 100K followers",
        status: "PENDING",
        createdAt: 2026-05-20 11:30:00
    }
    
What happened:
    1. Hibernated converted SponsorshipRequest to SQL INSERT
    2. INSERT INTO sponsorship_requests (influencer_id, campaign_id, proposal, status)
       VALUES (100, 42, 'I can reach...', 'PENDING')
    3. MySQL auto-generated id: 1001, sent back to Hibernate
    4. Hibernate populated id on Java object
    
Database now contains:
    ID: 1001
    INFLUENCER_ID: 100 (John)
    CAMPAIGN_ID: 42 (Nike campaign)
    STATUS: "PENDING"
    
Next:
    - Notification service notifies brand
    - Response sent to frontend
    - UI updates: "Application submitted!"
```

---

# INTERVIEW ANALOGIES

## Analogy 1: The Restaurant System

**Your app = Restaurant**

```
User Login (Reservation System):
    Customer enters restaurant
    Host checks: "Do we have reservation under brand@example.com?"
    Host gives you a stamp/token (JWT) proving you reserved
    You show this stamp for every request (getting food, refills, etc.)
    Stamp expires after 24 hours (need new reservation)

Database (Kitchen & Inventory):
    Recipes = Entity classes (what ingredients go in restaurant)
    Inventory = Database tables (actual stored ingredients)
    Orders = Requests (what customer wants)
    Order queue = Repository pattern (FIFO processing)

Campaigns (Daily Specials):
    Brand = Restaurant (offers special dish)
    Influencer = Critic (reviews dish)
    Application = "I want to review this special"
    Payment = "I pay critic $500 for review"
    Rating = Critic's score: "5 stars, delicious!"

Sponsorship Flow:
    1. Restaurant posts special (Brand creates campaign)
    2. Critic sees special (Influencer sees campaign)
    3. Critic applies (Influencer applies)
    4. Restaurant approves critic (Brand accepts influencer)
    5. Critic publishes review (Influencer completes work)
    6. Restaurant pays critic (Brand pays influencer)
    7. They rate each other (Mutual ratings)
```

## Analogy 2: The Package Delivery System

```
Frontend (Customer):
    Wants to send package (HTTP Request)
    Doesn't know how trucks work, just says "ship this"
    Gets receipt (HTTP Response)
    Checks tracking (UI updates)

HTTP Interceptor (Customs):
    Every package checked before sending
    Add customs form (JWT token) automatically
    If form missing or invalid → package rejected

Backend (Shipping Company):
    Receives packages (HTTP requests)
    Opens each package (deserialize JSON)
    Routes to right department (Spring controllers)
    Checks address validity (authorization checks)
    Stores in warehouse (repositories save to DB)
    Sends back receipt (HTTP response)

Database (Warehouse):
    Organized shelves (tables)
    Products stored (rows)
    Relationships between products (foreign keys)
    Inventory tracking (CRUD operations)
```

## Analogy 3: Government System

```
Roles (Government):
    ADMIN = President (can do anything)
    BRAND = Department (runs programs)
    INFLUENCER = Citizen (participates in programs)

Campaigns (Government Programs):
    Social Security = Brand creates campaign
    Citizens apply (Influencer), get approved (accepted)
    Perform work (complete), get benefits (payment)

Authentication (Citizenship):
    Need passport (JWT token) to access services
    Passport verified at every checkpoint (JWT filter)
    Passport expires (24 hours), need renewal
    Only verified citizens can access (AuthGuard)

Authorization (Permissions):
    President can veto laws (ADMIN edits campaign)
    Department heads can manage their department (BRAND edits own campaign)
    Citizens can't manage departments (@PreAuthorize checks)
```

---

# STRENGTHS & IMPROVEMENTS

## Current Strengths ✅

### 1. Clean Architecture
- ✓ Clear separation: Controller → Service → Repository
- ✓ No business logic leaking into controllers
- ✓ Services are testable units
- ✓ Dependency injection everywhere

### 2. Security Implementation
- ✓ JWT properly implemented (not sessions/cookies)
- ✓ Passwords BCrypt encrypted
- ✓ Role-based access control (RBAC)
- ✓ CORS properly configured
- ✓ Stateless API (scalable)

### 3. Frontend Architecture
- ✓ Components properly modularized
- ✓ Separate services for API communication
- ✓ Guards for route protection
- ✓ Interceptors for token injection
- ✓ Reactive patterns (Observables)

### 4. Database Design
- ✓ Normalized schema (no redundant data)
- ✓ Proper relationships (1-to-many, many-to-many)
- ✓ Foreign keys maintain referential integrity
- ✓ Auto-creation of tables (Hibernate DDL)

### 5. Feature Completeness
- ✓ Three roles with distinct workflows
- ✓ Complete payment system
- ✓ Mutual rating system
- ✓ Notification system
- ✓ Admin dashboard


## Areas for Improvement 🚀

### 1. Error Handling
**Current:** Throws exceptions, basic error messages
**Improvement:**
```java
// Add global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(
            code: "ENTITY_NOT_FOUND",
            message: e.getMessage(),
            timestamp: LocalDateTime.now()
        ));
    }
}

// Frontend error interceptor
export class ErrorInterceptor implements HttpInterceptor {
    intercept(req, next): Observable {
        return next.handle(req).pipe(
            catchError(error => {
                if (error.status === 404) {
                    this.snackBar.open("Not found", "error");
                } else if (error.status === 500) {
                    this.snackBar.open("Server error", "error");
                }
                return throwError(() => error);
            })
        );
    }
}
```

### 2. Input Validation
**Current:** Manual validation in services
**Improvement:**
```java
// Use Bean Validation annotations
public class CampaignRequest {
    @NotBlank(message = "Campaign name required")
    String name;
    
    @Min(value = 1, message = "Budget must be positive")
    @Max(value = 1000000, message = "Budget max $1M")
    Double budget;
    
    @Pattern(regexp = "^(Instagram|YouTube|TikTok|Twitter)$")
    String platform;
}

@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody CampaignRequest req) {
    // Validation happens automatically
    // If invalid → 400 Bad Request with validation errors
}
```

### 3. Logging
**Current:** Basic logging in application.properties
**Improvement:**
```java
// Add structured logging
@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    public void createPayment(PaymentRequest req) {
        log.info("Creating payment", new StructuredArguments(
            keyValue("campaignId", req.getCampaignId()),
            keyValue("influencerId", req.getInfluencerId()),
            keyValue("amount", req.getAmount())
        ));
        
        try {
            // Process payment
            log.info("Payment created successfully", 
                keyValue("paymentId", payment.getId()));
        } catch (Exception e) {
            log.error("Payment creation failed", e,
                keyValue("campaignId", req.getCampaignId()));
            throw e;
        }
    }
}
```

### 4. Caching
**Current:** Every request hits database
**Improvement:**
```java
@Service
@CacheConfig(cacheNames = "campaigns")
public class CampaignService {
    
    @Cacheable(key = "#id")  // Cache for 5 min
    public Campaign getCampaign(Long id) {
        return campaignRepo.findById(id).orElseThrow();
    }
    
    @CacheEvict(key = "#id")  // Clear cache when updated
    public Campaign updateCampaign(Long id, CampaignRequest req) {
        // Update logic
    }
}

// Result: 1000 requests for same campaign = 1 DB hit
```

### 5. Testing
**Current:** No unit tests visible
**Improvement:**
```java
@SpringBootTest
public class SponsorshipServiceTest {
    
    @MockBean private SponsorshipRepository repo;
    @Autowired private SponsorshipService service;
    
    @Test
    public void shouldNotAllowDuplicateApplications() {
        // Setup
        Long campaignId = 42L;
        User influencer = new User(1L, "John", "INFLUENCER");
        
        // Simulate existing application
        when(repo.existsByCampaignAndInfluencer(42, 1))
            .thenReturn(true);
        
        // Execute
        assertThrows(InvalidOperationException.class, () -> {
            service.applyForCampaign(campaignId, "proposal", influencer);
        });
        
        // Verify
        verify(repo).existsByCampaignAndInfluencer(42, 1);
        verify(repo, never()).save(any());
    }
}
```

### 6. Frontend Performance
**Current:** No lazy loading, bundle size optimization
**Improvement:**
```typescript
// Lazy load dashboard modules
const routes: Routes = [
    {
        path: 'dashboard',
        canActivate: [AuthGuard],
        children: [
            {
                path: 'brand',
                loadChildren: () =>
                    import('./components/dashboard/brand-dashboard/brand-dashboard.module')
                    .then(m => m.BrandDashboardModule)
            }
        ]
    }
];

// Result: Only load brand dashboard when user navigates there
```

### 7. API Documentation
**Current:** No documentation
**Improvement:**
```java
// Add Swagger/OpenAPI documentation
@RestController
@RequestMapping("/api/campaigns")
@Api(tags = "Campaigns", description = "Manage marketing campaigns")
public class CampaignController {
    
    @PostMapping
    @ApiOperation("Create new campaign")
    @ApiResponse(code = 201, message = "Campaign created")
    public ResponseEntity<?> create(@RequestBody CampaignRequest req) { }
}

// Result: http://localhost:7070/swagger-ui.html shows all endpoints
```

### 8. Database Indexing
**Current:** No custom indexes
**Improvement:**
```java
@Entity
@Table(indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_campaign_status", columnList = "campaign_id,status"),
    @Index(name = "idx_influencer_id", columnList = "influencer_id")
})
public class SponsorshipRequest {
    // Lookups by these columns will be faster
}
```

### 9. Payment Security
**Current:** Auto-completes payments
**Improvement:**
```java
// Use Stripe/PayPal integration
@Service
public class PaymentService {
    
    public void initiatePayment(PaymentRequest req) {
        // Create Stripe charge
        Charge charge = Stripe.Charges.create(chargeParams);
        
        // Only mark COMPLETED if Stripe confirms
        payment.setStatus(COMPLETED);
        payment.setTransactionId(charge.getId());
        
        // If Stripe says failed → payment stays PENDING
    }
}
```

### 10. Monitoring & Alerts
**Current:** No monitoring
**Improvement:**
```java
// Add metrics collection
@Service
public class PaymentService {
    @Autowired private MeterRegistry meterRegistry;
    
    public void createPayment() {
        meterRegistry.counter("payments.created").increment();
        
        try {
            // Create payment
            meterRegistry.timer("payment.creation.time")
                .record(() -> { /* logic */ });
        } catch (Exception e) {
            meterRegistry.counter("payments.failed").increment();
        }
    }
}

// Metrics visible in http://localhost:7070/actuator/metrics
// Can send to Prometheus/Grafana for dashboards
```

---

## Summary: Key Interview Talking Points

### What I Built
- ✓ Full-stack CRUD application (4000+ lines code)
- ✓ 3-tier architecture: frontend (Angular 17), backend (Spring Boot 3.2), database (MySQL)
- ✓ Role-based platform: Brands, Influencers, Admin

### Technical Decisions
- ✓ **Frontend**: Angular for scalable SPA with reactive patterns (Observables, Guards)
- ✓ **Backend**: Spring Boot for REST API, JWT stateless authentication
- ✓ **Database**: MySQL with Hibernate ORM, proper relationships and normalization

### Key Features
- ✓ User authentication (JWT tokens, 24-hour expiration)
- ✓ Campaign management (CRUD with role-based access)
- ✓ Sponsorship workflow (Application → Acceptance → Completion → Payment → Rating)
- ✓ Payment system with status tracking
- ✓ Two-way rating system
- ✓ Real-time notifications
- ✓ Admin dashboard for platform oversight

### Design Patterns Used
- MVC (Model-View-Controller)
- Repository Pattern
- Dependency Injection
- HTTP Interceptors
- Route Guards
- DTO Pattern
- RBAC (Role-Based Access Control)

### What I Learned
- ✓ Building scalable APIs (stateless, horizontal scaling)
- ✓ Securing applications (JWT, encryption, CORS)
- ✓ Frontend-backend communication (REST, HTTP interceptors)
- ✓ Database design (relationships, normalization)
- ✓ User experience (reactive, guards, notifications)

### If Asked "What Would You Improve?"
1. Add global error handling & input validation
2. Implement caching for frequently accessed data
3. Add comprehensive unit & integration tests
4. Integrate real payment gateway (Stripe/PayPal)
5. Add API documentation (Swagger)
6. Implement monitoring & logging
7. Optimize frontend with lazy loading
8. Add database indexes for common queries
