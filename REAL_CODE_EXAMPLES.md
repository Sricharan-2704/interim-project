# 🔬 Real Code Examples from Your Project

## How to Read the Code

This guide shows ACTUAL code from your backend and explains what each line does.

---

# Example 1: Campaign Entity

## File: `src/main/java/entity/Campaign.java`

```java
@Entity                              // Tell Spring: "This maps to database table"
@Table(name = "campaigns")          // Database table name is "campaigns"
@Data                                // Lombok: Auto-generate getters, setters, toString
@NoArgsConstructor                  // Lombok: Create empty constructor
@AllArgsConstructor                 // Lombok: Create constructor with all fields
public class Campaign {
    
    @Id                              // This is the PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment ID
    private Long id;                 // Unique identifier (1, 2, 3, ...)
    
    @NotBlank(message = "Campaign name is required")     // Validation rule
    private String name;             // Campaign title (required)
    
    @Column(length = 2000)          // Can store up to 2000 characters
    private String description;      // Campaign details
    
    private String platform;         // Instagram, YouTube, TikTok, etc.
    
    @Positive(message = "Budget must be positive")  // Can't be zero or negative
    private Double budget;           // Campaign budget in dollars
    
    private LocalDate startDate;     // When campaign starts (YYYY-MM-DD)
    
    private LocalDate endDate;       // When campaign ends
    
    private String eligibility;      // Requirements for influencers
    
    @Enumerated(EnumType.STRING)    // Store enum as text in database
    private CampaignStatus status = CampaignStatus.ACTIVE;  // Default: ACTIVE
    
    @ManyToOne(fetch = FetchType.EAGER)        // Many campaigns have ONE brand
    @JoinColumn(name = "brand_id")             // Foreign key column name
    private User brand;              // Reference to the brand who created it
}
```

### What This Creates in Database:

```sql
CREATE TABLE campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    platform VARCHAR(255),
    budget DOUBLE,
    start_date DATE,
    end_date DATE,
    eligibility VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    brand_id BIGINT FOREIGN KEY REFERENCES users(id)
);
```

### How It's Used:

When a brand creates a campaign in the app:
1. Angular sends campaign data to the backend
2. Backend creates a new `Campaign` object
3. Sets all the fields
4. Saves to database using `campaignRepository.save(campaign)`

---

# Example 2: Campaign Repository

## File: `src/main/java/repository/CampaignRepository.java`

```java
@Repository                                    // Tell Spring: "This accesses database"
public interface CampaignRepository 
        extends JpaRepository<Campaign, Long> {  // Campaign = entity type, Long = ID type
    
    // Spring automatically provides these methods:
    // findById(Long id)                       - Get ONE campaign by ID
    // findAll()                               - Get ALL campaigns
    // save(Campaign campaign)                 - Insert or update
    // delete(Campaign campaign)               - Delete campaign
    // deleteById(Long id)                     - Delete by ID
    
    // We can add custom queries:
    List<Campaign> findByStatus(CampaignStatus status);
    // Custom method: Get all campaigns with specific status
    // SQL: SELECT * FROM campaigns WHERE status = ?
    
    List<Campaign> findByBrandId(Long brandId);
    // Get all campaigns created by specific brand
    // SQL: SELECT * FROM campaigns WHERE brand_id = ?
}
```

### How Spring Creates Queries:

```
Method Name Pattern: find + By + FieldName

findByStatus()      → SQL: SELECT * FROM campaigns WHERE status = ?
findByBrandId()     → SQL: SELECT * FROM campaigns WHERE brand_id = ?
findByPlatform()    → SQL: SELECT * FROM campaigns WHERE platform = ?
existsById()        → SQL: SELECT EXISTS(SELECT 1 FROM campaigns WHERE id = ?)
```

### How It's Used in Service:

```java
// In CampaignService:
List<Campaign> activeCampaigns = campaignRepository.findByStatus(CampaignStatus.ACTIVE);
// Spring creates this SQL: SELECT * FROM campaigns WHERE status = 'ACTIVE'
```

---

# Example 3: Campaign Service (Business Logic)

## File: `src/main/java/service/CampaignService.java`

```java
@Service                              // Tell Spring: "This has business logic"
public class CampaignService {
    
    @Autowired                        // Inject repository
    private CampaignRepository campaignRepository;
    
    @Autowired                        // Inject auth service
    private AuthService authService;
    
    // ─── CREATE CAMPAIGN ───
    public Campaign createCampaign(CampaignRequest request) {
        // Step 1: Get current logged-in user (the brand)
        User brand = authService.getCurrentUser();
        
        // Step 2: Create new Campaign object
        Campaign campaign = new Campaign();
        
        // Step 3: Copy data from frontend request to entity
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setPlatform(request.getPlatform());
        campaign.setBudget(request.getBudget());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setEligibility(request.getEligibility());
        
        // Step 4: Set system fields
        campaign.setStatus(CampaignStatus.ACTIVE);  // New campaigns are ACTIVE
        campaign.setBrand(brand);                    // Assign to current brand
        
        // Step 5: Save to database
        return campaignRepository.save(campaign);
        // This executes: INSERT INTO campaigns (name, description, ...) VALUES (...)
    }
    
    // ─── UPDATE CAMPAIGN ───
    public Campaign updateCampaign(Long id, CampaignRequest request) {
        // Step 1: Get existing campaign
        Campaign campaign = getCampaignById(id);
        
        // Step 2: Check authorization (only owner can update)
        User currentUser = authService.getCurrentUser();
        if (!campaign.getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own campaigns");
            // Returns 403 Forbidden to frontend
        }
        
        // Step 3: Update fields
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setPlatform(request.getPlatform());
        campaign.setBudget(request.getBudget());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setEligibility(request.getEligibility());
        
        // Step 4: Save updated campaign
        return campaignRepository.save(campaign);
        // This executes: UPDATE campaigns SET name=..., description=... WHERE id=...
    }
    
    // ─── DELETE CAMPAIGN ───
    public void deleteCampaign(Long id) {
        // Step 1: Get campaign
        Campaign campaign = getCampaignById(id);
        
        // Step 2: Check authorization
        User currentUser = authService.getCurrentUser();
        if (!campaign.getBrand().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own campaigns");
        }
        
        // Step 3: Delete from database
        campaignRepository.delete(campaign);
        // This executes: DELETE FROM campaigns WHERE id=...
    }
    
    // ─── GET SINGLE CAMPAIGN ───
    public Campaign getCampaignById(Long id) {
        // Step 1: Find campaign in database
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        // .findById() returns Optional (may or may not exist)
        // .orElseThrow() throws error if not found
        
        return campaign;
    }
    
    // ─── GET ALL CAMPAIGNS ───
    public List<Campaign> getAllCampaigns() {
        // Simply get all campaigns from database
        return campaignRepository.findAll();
        // This executes: SELECT * FROM campaigns
    }
}
```

### How Data Flows:

```
Frontend sends: {name: "Summer Sale", budget: 5000, ...}
         │
         ▼
CampaignRequest DTO (automatically created by Spring)
         │
         ▼
Controller calls: campaignService.createCampaign(request)
         │
         ▼
Service creates Campaign entity, copies data
         │
         ▼
Service calls: campaignRepository.save(campaign)
         │
         ▼
Spring generates SQL: INSERT INTO campaigns (name, budget, ...)
         │
         ▼
MySQL executes insert, returns new ID
         │
         ▼
Service returns Campaign with new ID
         │
         ▼
Controller returns as JSON to frontend
```

---

# Example 4: Campaign Controller (API Endpoints)

## File: `src/main/java/controller/CampaignController.java`

```java
@RestController                              // This handles HTTP requests
@RequestMapping("/api/campaigns")           // Base URL: /api/campaigns
@CrossOrigin(origins = "http://localhost:4200")  // Allow Angular frontend
public class CampaignController {
    
    @Autowired
    private CampaignService campaignService;  // Can use service
    
    // ─── ENDPOINT 1: CREATE ───
    @PostMapping                              // POST /api/campaigns
    public ResponseEntity<?> createCampaign(
            @Valid @RequestBody CampaignRequest request) {
        // @Valid: Validate request before processing
        // @RequestBody: Convert JSON to CampaignRequest object
        
        try {
            Campaign campaign = campaignService.createCampaign(request);
            return ResponseEntity.ok("campaign created");  // 200 OK
        } catch (Exception e) {
            return ResponseEntity.badRequest()             // 400 Bad Request
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ─── ENDPOINT 2: UPDATE ───
    @PutMapping("/{id}")                     // PUT /api/campaigns/{id}
    public ResponseEntity<?> updateCampaign(
            @PathVariable Long id,           // {id} from URL
            @Valid @RequestBody CampaignRequest request) {
        
        try {
            Campaign campaign = campaignService.updateCampaign(id, request);
            return ResponseEntity.ok(campaign);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ─── ENDPOINT 3: DELETE ───
    @DeleteMapping("/{id}")                  // DELETE /api/campaigns/{id}
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        try {
            campaignService.deleteCampaign(id);
            return ResponseEntity.ok("Campaign deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ─── ENDPOINT 4: GET ONE ───
    @GetMapping("/{id}")                     // GET /api/campaigns/{id}
    public ResponseEntity<?> getCampaign(@PathVariable Long id) {
        try {
            Campaign campaign = campaignService.getCampaignById(id);
            return ResponseEntity.ok(campaign);  // Returns as JSON
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ─── ENDPOINT 5: GET ALL ───
    @GetMapping                              // GET /api/campaigns
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        return ResponseEntity.ok(
            campaignService.getAllCampaigns()
        );
        // Returns list of all campaigns as JSON array
    }
    
    // ─── ENDPOINT 6: GET ACTIVE ───
    @GetMapping("/active")                   // GET /api/campaigns/active
    public ResponseEntity<List<Campaign>> getActiveCampaigns() {
        return ResponseEntity.ok(
            campaignService.getActiveCampaigns()
        );
    }
    
    // ─── ENDPOINT 7: GET MY CAMPAIGNS ───
    @GetMapping("/my-campaigns")             // GET /api/campaigns/my-campaigns
    public ResponseEntity<List<Campaign>> getMyCampaigns() {
        // Only returns campaigns for current user (from JWT)
        return ResponseEntity.ok(
            campaignService.getBrandCampaigns()
        );
    }
}
```

### HTTP Examples:

```
CREATE:
POST /api/campaigns
Content-Type: application/json
Authorization: Bearer eyJhb...

{
  "name": "Summer Sale",
  "description": "50% off everything",
  "platform": "Instagram",
  "budget": 5000,
  "startDate": "2026-06-01",
  "endDate": "2026-08-31",
  "eligibility": "10K+ followers"
}

RESPONSE:
200 OK
{
  "id": 1,
  "name": "Summer Sale",
  "status": "ACTIVE",
  "brand": {...}
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET ALL:
GET /api/campaigns
Authorization: Bearer eyJhb...

RESPONSE:
200 OK
[
  {
    "id": 1,
    "name": "Summer Sale",
    ...
  },
  {
    "id": 2,
    "name": "Winter Sale",
    ...
  }
]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

UPDATE:
PUT /api/campaigns/1
Content-Type: application/json
Authorization: Bearer eyJhb...

{
  "name": "Super Summer Sale",
  "budget": 7000,
  ...
}

RESPONSE:
200 OK
{
  "id": 1,
  "name": "Super Summer Sale",
  "budget": 7000,
  ...
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

DELETE:
DELETE /api/campaigns/1
Authorization: Bearer eyJhb...

RESPONSE:
200 OK
"Campaign deleted"
```

---

# Example 5: How Everything Connects - Complete Request

## User Creates a Campaign (Step by Step)

### Step 1: Frontend (Angular)
```typescript
// User fills form and clicks "Create Campaign"
const campaignData = {
  name: 'Summer Promo',
  description: 'Big discount',
  budget: 5000,
  ...
};

// Frontend sends HTTP request:
POST /api/campaigns
with JWT token in header
with JSON body (campaignData)
```

### Step 2: Backend Receives Request
```
Request arrives at Spring Boot server
JwtAuthenticationFilter intercepts request
  ├─ Extracts JWT from Authorization header
  ├─ Calls jwtTokenProvider.validateToken()
  ├─ Valid? ✓ Continue
  └─ Gets user from token
    
Request forwarded to correct controller
```

### Step 3: Controller
```java
@PostMapping
public ResponseEntity<?> createCampaign(
        @Valid @RequestBody CampaignRequest request) {
    // request = {name: "Summer Promo", budget: 5000, ...}
    
    Campaign campaign = campaignService.createCampaign(request);
    return ResponseEntity.ok(campaign);
}
```

### Step 4: Service
```java
public Campaign createCampaign(CampaignRequest request) {
    // Get the user who's creating (from JWT)
    User brand = authService.getCurrentUser();  // Current user from JWT
    
    // Create entity
    Campaign campaign = new Campaign();
    campaign.setName(request.getName());       // "Summer Promo"
    campaign.setBudget(request.getBudget());   // 5000
    campaign.setBrand(brand);                  // User ID from JWT
    campaign.setStatus(CampaignStatus.ACTIVE); // Default
    
    // Save to database
    return campaignRepository.save(campaign);
}
```

### Step 5: Repository
```java
public Campaign save(Campaign campaign) {
    // Spring generates this SQL:
    // INSERT INTO campaigns (name, budget, brand_id, status)
    // VALUES ('Summer Promo', 5000, 1, 'ACTIVE')
    
    // MySQL executes insert, generates ID
    // Returns Campaign with ID filled in
    
    return campaign;  // {id: 1, name: "Summer Promo", ...}
}
```

### Step 6: Response Flows Back
```
Service returns Campaign → Controller
Controller converts to JSON → { "id": 1, "name": "Summer Promo", ... }
Controller sends HTTP 200 OK + JSON
JwtAuthenticationFilter allows response
Response sent to frontend
```

### Step 7: Frontend Receives
```typescript
// Angular gets response:
{
  id: 1,
  name: 'Summer Promo',
  budget: 5000,
  status: 'ACTIVE',
  ...
}

// Angular displays success message
// Adds campaign to local list
// Updates campaign list UI
```

---

# Example 6: Authentication Flow

## File: `src/main/java/service/AuthService.java`

```java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;    // Access users in database
    
    @Autowired
    private PasswordEncoder passwordEncoder;  // Encrypt passwords
    
    @Autowired
    private AuthenticationManager authenticationManager;  // Spring security
    
    @Autowired
    private JwtTokenProvider tokenProvider;   // Create JWT tokens
    
    // ─── LOGIN ───
    public AuthResponse login(LoginRequest request) {
        // Step 1: Get email and password from request
        String email = request.getEmail().trim().toLowerCase();
        // Normalize: "John@Example.COM" → "john@example.com"
        
        // Step 2: Authenticate user (Spring Security checks password)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        // If password wrong, throws exception → caught by GlobalExceptionHandler
        
        // Step 3: Set in SecurityContext (for current request)
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Step 4: Generate JWT token
        String token = tokenProvider.generateToken(authentication);
        // token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        
        // Step 5: Get user details
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Step 6: Return response with token
        return new AuthResponse(
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
        // response = {
        //   "token": "eyJhb...",
        //   "id": 1,
        //   "name": "John",
        //   "email": "john@example.com",
        //   "role": "INFLUENCER"
        // }
    }
    
    // ─── REGISTRATION ───
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Normalize email
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        
        // Step 2: Check if email already registered
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
            // Returns 400 Bad Request with message
        }
        
        // Step 3: Create new User entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Password is encrypted, never stored plain text!
        
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        // "influencer" → Role.INFLUENCER
        
        // Step 4: Save to database
        userRepository.save(user);
        // INSERT INTO users (name, email, password, role) VALUES (...)
        
        // Step 5: Automatically log them in (create token)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                normalizedEmail,
                request.getPassword()
            )
        );
        
        String token = tokenProvider.generateToken(authentication);
        
        // Step 6: Return response
        return new AuthResponse(
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
    }
    
    // ─── GET CURRENT USER ───
    public User getCurrentUser() {
        // Get current authenticated user from SecurityContext
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        String email = authentication.getName();  // Get username (email)
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
```

## File: `src/main/java/security/JwtTokenProvider.java`

```java
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")      // Read from application.properties
    private String jwtSecret;        // Secret key for signing
    
    @Value("${app.jwt.expiration}")  // How long token lasts (in milliseconds)
    private long jwtExpiration;
    
    // ─── GENERATE TOKEN ───
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        // Create and sign JWT
        String token = Jwts.builder()
                .subject(userDetails.getUsername())  // Put email as subject
                .issuedAt(now)                       // Token created time
                .expiration(expiryDate)              // Token expires time
                .signWith(getSigningKey())           // Sign with secret
                .compact();                          // Create final token string
        
        // Result: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJq..."
        return token;
    }
    
    // ─── VALIDATE TOKEN ───
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())     // Check signature
                    .build()
                    .parseSignedClaims(token);       // Parse token
            return true;  // Valid!
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // Invalid or expired
        }
    }
    
    // ─── GET USERNAME FROM TOKEN ───
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();  // Returns the email
    }
}
```

---

# Example 7: The Complete Request-Response Cycle

```
┌──────────────────────────────────────────────────────────┐
│                 STEP 1: FRONTEND SENDS                    │
├──────────────────────────────────────────────────────────┤
│ Angular FormComponent:                                   │
│ - User clicks "Login"                                    │
│ - Sends: POST /api/auth/login                            │
│ - Body: { "email": "user@example.com", "password": "..." }
│ - Headers: { "Content-Type": "application/json" }        │
└──────────────────┬───────────────────────────────────────┘
                   │ HTTP Request
                   ▼
┌──────────────────────────────────────────────────────────┐
│              STEP 2: SPRING ROUTING                       │
├──────────────────────────────────────────────────────────┤
│ - DispatcherServlet receives request                     │
│ - Matches URL /api/auth/login to AuthController         │
│ - Matches POST method to login() endpoint                │
└──────────────────┬───────────────────────────────────────┘
                   │ Request forwarded
                   ▼
┌──────────────────────────────────────────────────────────┐
│          STEP 3: SECURITY FILTER                          │
├──────────────────────────────────────────────────────────┤
│ JwtAuthenticationFilter:                                 │
│ - Is /api/auth/login protected? NO (public endpoint)     │
│ - Allow request to continue                              │
└──────────────────┬───────────────────────────────────────┘
                   │ Request allowed
                   ▼
┌──────────────────────────────────────────────────────────┐
│          STEP 4: ARGUMENT RESOLUTION                      │
├──────────────────────────────────────────────────────────┤
│ Spring converts JSON to LoginRequest DTO:                │
│ {                                                         │
│   "email": "user@example.com",                           │
│   "password": "secret123"                                │
│ }                                                         │
│                                                           │
│ Validates @NotBlank annotations                          │
└──────────────────┬───────────────────────────────────────┘
                   │ DTO created
                   ▼
┌──────────────────────────────────────────────────────────┐
│         STEP 5: CONTROLLER EXECUTES                       │
├──────────────────────────────────────────────────────────┤
│ AuthController.login(LoginRequest request):              │
│ - try {                                                   │
│   - AuthResponse response = authService.login(request);  │
│   - return ResponseEntity.ok(response);                  │
│ - } catch (Exception e) { ... }                          │
└──────────────────┬───────────────────────────────────────┘
                   │ Calls service
                   ▼
┌──────────────────────────────────────────────────────────┐
│          STEP 6: SERVICE EXECUTES                         │
├──────────────────────────────────────────────────────────┤
│ AuthService.login():                                     │
│ - Normalize email: "user@example.com"                    │
│ - Authenticate: check password against hash              │
│ - Generate JWT: "eyJhb..."                               │
│ - Get user from database                                 │
│ - Return AuthResponse DTO                                │
└──────────────────┬───────────────────────────────────────┘
                   │ Calls repository
                   ▼
┌──────────────────────────────────────────────────────────┐
│         STEP 7: REPOSITORY EXECUTES                       │
├──────────────────────────────────────────────────────────┤
│ UserRepository.findByEmail():                            │
│ - Generate SQL: SELECT * FROM users WHERE email = ?      │
│ - Execute query on MySQL                                 │
│ - Get User entity                                        │
│ - Return to service                                      │
└──────────────────┬───────────────────────────────────────┘
                   │ Returns data
                   ▼
┌──────────────────────────────────────────────────────────┐
│       STEP 8: SERVICE CREATES RESPONSE                    │
├──────────────────────────────────────────────────────────┤
│ AuthService returns AuthResponse:                        │
│ {                                                         │
│   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  │
│   "id": 1,                                                │
│   "name": "John",                                         │
│   "email": "user@example.com",                           │
│   "role": "INFLUENCER"                                   │
│ }                                                         │
└──────────────────┬───────────────────────────────────────┘
                   │ Returns response
                   ▼
┌──────────────────────────────────────────────────────────┐
│       STEP 9: RESPONSE SERIALIZATION                      │
├──────────────────────────────────────────────────────────┤
│ - Convert AuthResponse to JSON                           │
│ - Add HTTP headers: Content-Type: application/json       │
│ - Add status code: 200 OK                                │
└──────────────────┬───────────────────────────────────────┘
                   │ JSON response
                   ▼
┌──────────────────────────────────────────────────────────┐
│         STEP 10: FRONTEND RECEIVES                        │
├──────────────────────────────────────────────────────────┤
│ HTTP 200 OK                                              │
│ {                                                         │
│   "token": "eyJhb...",                                    │
│   "id": 1,                                                │
│   "name": "John",                                         │
│   "email": "user@example.com",                           │
│   "role": "INFLUENCER"                                   │
│ }                                                         │
│                                                           │
│ Angular:                                                  │
│ - Stores token in localStorage                           │
│ - Sets Authorization header for future requests          │
│ - Redirects to dashboard                                 │
│ - Shows user name in navbar                              │
└──────────────────────────────────────────────────────────┘
```

---

## Summary

This is how your backend works:

1. **Frontend sends** HTTP request with data
2. **Controller receives** and validates data
3. **Service processes** with business logic
4. **Repository queries** database
5. **Database returns** data
6. **Service converts** entity to DTO
7. **Controller sends** JSON response
8. **Frontend receives** and displays data

Every feature in your app follows this exact same pattern!
