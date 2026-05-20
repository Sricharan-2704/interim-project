# 🎨 Backend Architecture - Visual Diagrams

## 1. Layer Architecture

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃        PRESENTATION LAYER             ┃
┃   Angular Frontend (Browser/Client)   ┃
┃   Sends HTTP Requests & Receives JSON ┃
┗━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━┛
                 │ HTTP (JSON over Network)
                 │
┏━━━━━━━━━━━━━━━━▼━━━━━━━━━━━━━━━━━━━━┓
┃        API LAYER (Controllers)        ┃
┃   AuthController                      ┃
┃   CampaignController                  ┃
┃   SponsorshipController               ┃
┃   PaymentController                   ┃
┃   RatingController                    ┃
┃   NotificationController              ┃
┃   AdminController                     ┃
┗━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━┛
                 │ Method Calls
                 │
┏━━━━━━━━━━━━━━━━▼━━━━━━━━━━━━━━━━━━━━┓
┃      BUSINESS LOGIC LAYER (Services) ┃
┃   AuthService  (login, register)     ┃
┃   CampaignService  (CRUD)            ┃
┃   SponsorshipService  (requests)     ┃
┃   PaymentService  (transactions)     ┃
┃   RatingService  (reviews)           ┃
┃   NotificationService  (alerts)      ┃
┃   AdminService  (statistics)         ┃
┗━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━┛
                 │ Database Operations
                 │
┏━━━━━━━━━━━━━━━━▼━━━━━━━━━━━━━━━━━━━━┓
┃   DATA ACCESS LAYER (Repositories)   ┃
┃   UserRepository                      ┃
┃   CampaignRepository                  ┃
┃   SponsorshipRequestRepository        ┃
┃   PaymentRepository                   ┃
┃   RatingRepository                    ┃
┃   NotificationRepository              ┃
┗━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━┛
                 │ SQL Queries
                 │
┏━━━━━━━━━━━━━━━━▼━━━━━━━━━━━━━━━━━━━━┓
┃        DATABASE LAYER                 ┃
┃   MySQL Database                      ┃
┃   Tables: users, campaigns,           ┃
┃           payments, ratings,          ┃
┃           notifications,              ┃
┃           sponsorship_requests        ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

DATA FLOWS:
Request:  Frontend → Controllers → Services → Repositories → Database
Response: Database → Repositories → Services → Controllers → Frontend
```

---

## 2. Authentication Flow

```
┌─────────────────────────────────────────┐
│     USER LOGIN FLOW                     │
│                                         │
│  1. User enters email & password        │
│     in Frontend (Angular)               │
└────────┬────────────────────────────────┘
         │ HTTP POST /api/auth/login
         │ {email, password}
         │
┌────────▼────────────────────────────────┐
│  AuthController.login()                 │
│                                         │
│  - Receives LoginRequest DTO            │
│  - Calls authService.login()            │
└────────┬────────────────────────────────┘
         │ Method call
         │
┌────────▼────────────────────────────────┐
│  AuthService.login()                    │
│                                         │
│  - Validates email & password           │
│  - Calls userRepository.findByEmail()   │
└────────┬────────────────────────────────┘
         │ Database query
         │
┌────────▼────────────────────────────────┐
│  UserRepository.findByEmail()           │
│                                         │
│  - Executes SQL query                   │
│  - Returns User entity or null          │
└────────┬────────────────────────────────┘
         │ Returns User
         │
┌────────▼────────────────────────────────┐
│  AuthService (continued)                │
│                                         │
│  - Verifies password                    │
│  - Calls jwtTokenProvider.generateToken()│
│  - Returns AuthResponse DTO             │
│    {token, id, name, email, role}      │
└────────┬────────────────────────────────┘
         │ Returns DTO
         │
┌────────▼────────────────────────────────┐
│  AuthController.login() returns         │
│                                         │
│  HTTP 200 OK + AuthResponse JSON        │
└────────┬────────────────────────────────┘
         │ HTTP Response
         │
┌────────▼────────────────────────────────┐
│  Angular Frontend                       │
│                                         │
│  - Receives token                       │
│  - Stores in localStorage               │
│  - Sets in Authorization header         │
│  - Redirects to dashboard               │
│                                         │
│  Future requests include:               │
│  Authorization: Bearer <token>          │
└─────────────────────────────────────────┘
```

---

## 3. Subsequent Requests (Authenticated)

```
┌──────────────────────────────────────┐
│  Frontend (with stored JWT token)    │
│  Sends: GET /api/campaign/all        │
│         Header: Authorization: Bearer eyJh..│
└────────┬─────────────────────────────┘
         │ HTTP Request with JWT
         │
┌────────▼─────────────────────────────┐
│  JwtAuthenticationFilter              │
│                                      │
│  - Extracts token from header        │
│  - Calls tokenProvider.validateToken()│
│  - If valid, allows request          │
│    to continue                        │
│  - If invalid, returns 401 Unauthorized│
└────────┬─────────────────────────────┘
         │ Forwards request if valid
         │
┌────────▼─────────────────────────────┐
│  CampaignController.getAllCampaigns()│
│                                      │
│  - Receives HTTP request             │
│  - Calls campaignService.getAll()    │
└────────┬─────────────────────────────┘
         │
┌────────▼─────────────────────────────┐
│  CampaignService.getAll()            │
│                                      │
│  - Calls campaignRepository.findAll()│
└────────┬─────────────────────────────┘
         │
┌────────▼─────────────────────────────┐
│  CampaignRepository.findAll()        │
│                                      │
│  - Executes: SELECT * FROM campaigns │
│  - Returns List<Campaign> entities   │
└────────┬─────────────────────────────┘
         │ Returns list
         │
┌────────▼─────────────────────────────┐
│  CampaignService (converts)          │
│                                      │
│  - Entity List → DTO List            │
│  - Returns List<CampaignResponse>    │
└────────┬─────────────────────────────┘
         │
┌────────▼─────────────────────────────┐
│  CampaignController returns          │
│                                      │
│  HTTP 200 OK + JSON array           │
│  [                                   │
│    {"id": 1, "name": "Campaign 1"...│
│    {"id": 2, "name": "Campaign 2"...│
│  ]                                   │
└────────┬─────────────────────────────┘
         │ HTTP Response
         │
┌────────▼─────────────────────────────┐
│  Frontend (Angular)                  │
│                                      │
│  - Receives JSON array               │
│  - Displays campaigns on page        │
└──────────────────────────────────────┘
```

---

## 4. Dependency Injection Flow

```
SPRING APPLICATION STARTUP:

┌────────────────────────────────────┐
│ SponsorshipAppBackendApplication   │
│ .main() method                     │
│                                    │
│ SpringApplication.run()            │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│  Spring Boot Initialization        │
│                                    │
│  Scans packages for:              │
│  - @Repository classes            │
│  - @Service classes               │
│  - @Controller classes            │
│  - @Component classes             │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│  CREATES BEANS (Instances)         │
│                                    │
│  Repository Layer:                │
│  ✓ UserRepository bean            │
│  ✓ CampaignRepository bean        │
│  ✓ ... (all repositories)         │
│                                    │
│  Service Layer:                   │
│  ✓ AuthService bean               │
│    └─ needs UserRepository         │
│    └─ needs JwtTokenProvider       │
│  ✓ CampaignService bean           │
│    └─ needs CampaignRepository     │
│    └─ needs UserRepository         │
│  ✓ ... (all services)             │
│                                    │
│  Controller Layer:                │
│  ✓ AuthController bean            │
│    └─ needs AuthService           │
│  ✓ CampaignController bean        │
│    └─ needs CampaignService       │
│  ✓ ... (all controllers)          │
│                                    │
│  Security Layer:                  │
│  ✓ JwtTokenProvider bean          │
│  ✓ JwtAuthenticationFilter bean   │
│  ✓ CustomUserDetailsService bean  │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│  DEPENDENCY INJECTION              │
│                                    │
│  For each @Autowired field:       │
│  - Spring looks in IoC container  │
│  - Finds matching bean            │
│  - Injects it into the field      │
│                                    │
│  Example:                         │
│  @Service                         │
│  AuthService {                    │
│    @Autowired                     │
│    UserRepository repo;  ← Inject │
│  }                                │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│  ALL WIRED & READY                 │
│                                    │
│  Server starts on port 8080       │
│  Ready to receive requests         │
└────────────────────────────────────┘
```

---

## 5. Request Processing Pipeline

```
HTTP REQUEST ARRIVES
         │
         ▼
┌──────────────────────────────┐
│ JwtAuthenticationFilter      │
│ (Security checkpoint)        │
├──────────────────────────────┤
│ ✓ Extract JWT from header    │
│ ✓ Validate token             │
│ ✓ Get user from token        │
│ ✓ Set in SecurityContext     │
└──────┬───────────────────────┘
       │
       ├─ Token invalid? → Return 401
       │
       ▼
┌──────────────────────────────────┐
│ DispatcherServlet                │
│ (Main servlet that routes)       │
└──────┬───────────────────────────┘
       │
       ▼
┌────────────────────────────────┐
│ Compare @RequestMapping paths  │
│ Find matching controller       │
│ method                         │
└──────┬───────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ Argument Resolver          │
│                            │
│ - Convert JSON to DTO      │
│ - Validate with @Valid     │
│ - Check constraints        │
└──────┬────────────────────┘
       │
       ├─ Validation fails? → Return 400
       │
       ▼
┌──────────────────────────┐
│ Controller Method        │
│ Executes                 │
│                          │
│ - Calls Service method   │
│ - Gets response          │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────┐
│ Service Method           │
│ Executes                 │
│                          │
│ - Business logic         │
│ - Repository calls       │
│ - Data processing        │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────┐
│ Repository Method        │
│ Executes                 │
│                          │
│ - Database query         │
│ - Returns Entity         │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────┐
│ Database Response        │
│ Returns data             │
└──────┬──────────────────┘
       │
       ▼ (Response flows back up)
       │
┌──────────────────────────┐
│ Service converts:        │
│ Entity → DTO            │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────┐
│ Status Code Resolver     │
│                          │
│ Sets: 200, 201, etc.    │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────┐
│ Response Formatter       │
│                          │
│ Converts DTO to JSON    │
│ Sets Content-Type       │
└──────┬──────────────────┘
       │
       ▼
┌──────────────────────────┐
│ HTTP RESPONSE SENT       │
│                          │
│ - Status Code (200, 201) │
│ - Headers (JSON type)    │
│ - Body (JSON data)       │
└──────┬──────────────────┘
       │
       ▼
    FRONTEND receives response
    Displays data to user
```

---

## 6. Feature: Create Campaign

```
USER ACTION: Click "Create Campaign" button
         │
         ▼
┌────────────────────────────────────┐
│ Angular Form                       │
│ Sends POST /api/campaign           │
│ Body: {                            │
│   "name": "...",                   │
│   "description": "...",            │
│   "budget": 5000,                  │
│   "startDate": "2026-05-20",       │
│   "endDate": "2026-06-20"          │
│ }                                  │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│ CampaignController                 │
│ .createCampaign(CampaignRequest)   │
│                                    │
│ - Receives request DTO             │
│ - Calls campaignService.create()   │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│ CampaignService.create()           │
│                                    │
│ - Validate data                    │
│ - Get current user (brand)         │
│ - Create Campaign entity:          │
│   ├─ campaign.setName()            │
│   ├─ campaign.setDescription()     │
│   ├─ campaign.setBudget()          │
│   ├─ campaign.setBrand(user)       │
│   ├─ campaign.setStatus(ACTIVE)    │
│   └─ campaignRepository.save()     │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│ CampaignRepository.save()          │
│                                    │
│ - Generate INSERT SQL              │
│ - Database creates row             │
│ - Returns Campaign with ID         │
└────────┬───────────────────────────┘
         │ Returns to service
         │
┌────────▼───────────────────────────┐
│ CampaignService                    │
│ Converts Campaign entity to        │
│ CampaignResponse DTO               │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│ CampaignController                 │
│ Returns:                           │
│ HTTP 201 CREATED                   │
│ {                                  │
│   "id": 1,                         │
│   "name": "Summer Promo",          │
│   "budget": 5000,                  │
│   "status": "ACTIVE"               │
│ }                                  │
└────────┬───────────────────────────┘
         │
┌────────▼───────────────────────────┐
│ Angular Frontend                   │
│                                    │
│ - Stores campaign data             │
│ - Updates campaign list            │
│ - Shows success message            │
│ - Redirects to campaign detail     │
└────────────────────────────────────┘
```

---

## 7. Data Model Relationships

```
┌─────────────────┐
│      USER       │
├─────────────────┤
│ id (PK)         │
│ name            │
│ email           │
│ password        │
│ role (enum)     │    ┌──────────────────────┐
│ bio             ├───┤CREATES/MANAGES MANY  │
│ profileImage    │    │      CAMPAIGN        │
└─────────────────┘    └──────────────────────┘
         │                      │
         │                      ▼
         │              ┌─────────────────┐
         │              │    CAMPAIGN     │
         │              ├─────────────────┤
         │              │ id (PK)         │
         │              │ name            │
         │              │ description     │
         │              │ budget          │
         │              │ startDate       │
         │              │ endDate         │
         │              │ status (enum)   │
         │              │ brand_id (FK)   │
         │              └─────────────────┘
         │                      │
         │                      ▼
         │              ┌──────────────────────┐
         │              │SPONSORSHIP_REQUEST   │
         │              ├──────────────────────┤
         │              │ id (PK)              │
         │              │ influencer_id (FK)   │
         │              │ campaign_id (FK)     │
         │              │ message              │
         │              │ status (enum)        │
         │              └──────────────────────┘
         │                      │
         │                      ▼
         │              ┌─────────────────┐
         │              │    PAYMENT      │
         │              ├─────────────────┤
         │              │ id (PK)         │
         │              │ amount          │
         │              │ paymentDate     │
         │              │ status (enum)   │
         │              └─────────────────┘
         │
         ├────────────────────────────┐
         │                            ▼
         │                   ┌─────────────────┐
         │                   │     RATING      │
         │                   ├─────────────────┤
         │                   │ id (PK)         │
         │                   │ rater_id (FK)   │
         │                   │ ratee_id (FK)   │
         │                   │ score           │
         │                   │ comment         │
         │                   └─────────────────┘
         │
         └───► ┌──────────────────┐
               │  NOTIFICATION    │
               ├──────────────────┤
               │ id (PK)          │
               │ recipient_id(FK) │
               │ message          │
               │ type             │
               │ isRead           │
               └──────────────────┘

FK = Foreign Key (references another table)
PK = Primary Key (unique identifier)
```

---

## 8. Security Architecture

```
INCOMING REQUEST
       │
       ▼
┌──────────────────────────┐
│ HTTP Request with        │
│ Authorization Header:    │
│                          │
│ Authorization: Bearer    │
│ eyJhbGciOiJIUzI1NiIs... │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────┐
│ JwtAuthenticationFilter  │
│                          │
│ 1. Extract token        │
│    from header          │
│                          │
│ 2. Call                 │
│    jwtTokenProvider     │
│    .validateToken()     │
└──────┬───────────────────┘
       │
       ├─ Invalid/Expired?
       │  └─► Return 401 UNAUTHORIZED
       │
       ▼
┌──────────────────────────┐
│ Token is Valid!          │
│                          │
│ 1. Extract username(email)│
│ 2. Load user details     │
│    from CustomUser       │
│    DetailsService       │
│ 3. Set in SecurityContext│
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────┐
│ SecurityContext          │
│ Authenticated User:      │
│ ├─ username: john@...   │
│ ├─ password: *****      │
│ ├─ authorities: [ROLE]  │
│ └─ authenticated: true  │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────┐
│ Controller executes      │
│                          │
│ Can access:             │
│ SecurityContextHolder   │
│ .getContext()           │
│ .getAuthentication()    │
│ .getName()  ← email    │
└──────┬───────────────────┘
       │
       ▼
   REQUEST CONTINUES
   To Service & Database
```

---

## 9. Complete Application Startup Sequence

```
1. IDE/Terminal: mvn spring-boot:run
                      │
                      ▼
2. Java starts SponsorshipAppBackendApplication.main()
                      │
                      ▼
3. SpringApplication.run() initializes Spring IoC Container
                      │
    ┌───────────────┬─────────────┬─────────────┐
    │               │             │             │
    ▼               ▼             ▼             ▼
4. Component Scanning    Auto-Configuration
   ├─ @Repository       ├─ DataSource
   ├─ @Service          ├─ JPA/Hibernate
   ├─ @Controller       ├─ Spring Security
   ├─ @Component        └─ Jackson (JSON)
   └─ @Configuration
                      │
    ┌───────────────┴─────────────┬─────────────┐
    │                             │             │
    ▼                             ▼             ▼
5. CREATE BEANS            ESTABLISH       LOAD
   (Instances)             CONNECTIONS     CONFIGURATION
   
   Repositories:           Database        application.properties
   ├─ UserRepository       Connection      ├─ server.port = 8080
   ├─ Campaign...          Pool            ├─ spring.datasource
   └─ etc.                 ├─ MySQL       │  .url = jdbc:...
                           └─ Hibernate   └─ jwt.secret = ...
   Services:
   ├─ AuthService
   │  └─ @Autowired:
   │     └─ UserRepository
   │     └─ JwtTokenProvider
   ├─ CampaignService
   │  └─ @Autowired:
   │     └─ CampaignRepository
   └─ etc.
   
   Controllers:
   ├─ AuthController
   │  └─ @Autowired:
   │     └─ AuthService
   ├─ CampaignController
   │  └─ @Autowired:
   │     └─ CampaignService
   └─ etc.
                      │
    ┌───────────────┬─┴──────────┐
    │               │            │
    ▼               ▼            ▼
6. DEPENDENCY     BEAN          SECURITY
   INJECTION      INITIALIZATION FILTERS
   
   All @Autowired Field:  Dependency Resolver:  Servlet Mapping:
   ├─ userRepository      ├─ Finds matching    ├─ Dispatcher
   ├─ passwordEncoder     │  bean in IoC       │  Servlet
   ├─ authService         │  container         ├─ Security
   ├─ jwtTokenProvider    │                    │  Filter
   └─ etc.                └─ Injects it        └─ etc.
                      │
                      ▼
7. DataInitializer.java runs
   ├─ Check if data exists
   ├─ If not, create sample data:
   │  ├─ Sample users
   │  ├─ Sample campaigns
   │  └─ Sample requests
   └─ Database populated
                      │
                      ▼
8. Server Started Successfully!
   ├─ Port: 8080
   ├─ Ready for requests
   ├─ Logging started
   └─ Listening on http://localhost:8080
                      │
                      ▼
9. Frontend (Angular) can now connect
   └─ API endpoints ready
      └─ POST /api/auth/login
      └─ POST /api/auth/register
      └─ GET /api/campaign/all
      └─ etc.
```

---

## Key Takeaway: How It All Works Together

```
                   The Complete Cycle
                   

User clicks button in Angular
              │
              ▼
Frontend sends HTTP request with data
              │
              ▼
Spring receives request via Controller
              │
              ▼
Controller calls Service
              │
              ▼
Service applies business logic
              │
              ▼
Service calls Repository
              │
              ▼
Repository queries Database (MySQL)
              │
              ▼
Database returns data
              │
              ▼
Repository sends Entity objects back
              │
              ▼
Service converts Entity → DTO
              │
              ▼
Controller sends response (JSON)
              │
              ▼
Frontend receives JSON
              │
              ▼
Angular displays data to user


EACH PIECE IS RESPONSIBLE FOR:

┌──────────────┐
│  Entity      │ ← What does data look like?
├──────────────┤
│   Field 1    │
│   Field 2    │
│   Field 3    │
└──────────────┘
        △
        │
┌──────────────┐      ┌──────────────┐
│ Repository   │ ─→   │   MySQL      │
├──────────────┤      ├──────────────┤
│How to access │      │Stores actual │
│   data?      │      │    data      │
└──────────────┘      └──────────────┘
        △
        │
┌──────────────┐
│   Service    │
├──────────────┤
│What rules to │
│   apply?     │
└──────────────┘
        △
        │
┌──────────────┐      ┌──────────────┐
│ Controller   │ ─→   │     DTO      │
├──────────────┤      ├──────────────┤
│Receive HTTP  │      │How to show   │
│requests?     │      │   data?      │
└──────────────┘      └──────────────┘
        △
        │
    FRONTEND
```
