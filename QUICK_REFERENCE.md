# ⚡ Quick Reference Guide - Backend Architecture

## 📚 Where to Look for What

| I need to... | Look in... | Specific file(s) |
|---|---|---|
| **Understand how data is stored** | entity/ | User.java, Campaign.java, Payment.java, etc. |
| **Add a new feature** | controller/, service/, repository/ | Create matching files in each layer |
| **Make a database query** | repository/ | UserRepository, CampaignRepository, etc. |
| **Add business logic** | service/ | AuthService, CampaignService, etc. |
| **Create an API endpoint** | controller/ | AuthController, CampaignController, etc. |
| **Send data to frontend** | dto/ | AuthResponse, CampaignRequest, etc. |
| **Handle authentication** | security/ | JwtTokenProvider, JwtAuthenticationFilter |
| **Configure the app** | config/ | SecurityConfig, CorsConfig, DataInitializer |
| **Handle errors** | exception/ | GlobalExceptionHandler |

---

## 🔑 Key Concepts at a Glance

### **Entity**
- Represents 1 database table
- Annotated with `@Entity` and `@Table`
- Has `@Id` for unique identifier
- Example: `User.java` = users table

### **Repository**
- Extends `JpaRepository<Entity, Long>`
- Talks to database
- Spring auto-generates SQL
- Example: `userRepository.findByEmail()`

### **Service**
- Annotated with `@Service`
- Contains business logic/rules
- Uses repositories for data
- Converts entities to DTOs
- Example: `authService.login()`

### **Controller**
- Annotated with `@RestController`
- Receives HTTP requests
- Calls services
- Returns responses as JSON
- Example: `POST /api/auth/login`

### **DTO**
- Simple class for communication
- Holds only data frontend needs
- Never includes passwords
- Example: `AuthResponse` has token but not password

### **JWT Token**
- Proves user is logged in
- Sent with every request in header
- Expires after set time
- Backend validates on each request

### **@Autowired**
- Spring automatically injects dependency
- No need to manually create objects
- Works with @Service, @Repository, etc.
- Magic that happens behind scenes

---

## 🌊 Data Flow: The 5-Step Journey

```
1. REQUEST ENTERS
   Frontend → HTTP → Backend Controller

2. CONTROLLER RECEIVES
   AuthController.register(RegisterRequest)

3. SERVICE PROCESSES
   AuthService.register()
   ├─ Validates data
   ├─ Calls userRepository.save()
   └─ Generates JWT

4. REPOSITORY EXECUTES
   UserRepository.save(user)
   └─ Runs SQL: INSERT INTO users

5. RESPONSE RETURNS
   JSON → HTTP → Frontend
```

---

## 📊 File Type Reference

### **Entity Files** (`entity/`)
- Purpose: Define data structure
- Naming: Singular (User, Campaign, Payment)
- Database: Maps to table
- Contains: Fields + relationships + validations
- Example: `User.java` = users table

### **Repository Files** (`repository/`)
- Purpose: Database access
- Naming: EntityNameRepository (UserRepository)
- Extends: JpaRepository<Entity, Long>
- Contains: Custom query methods
- Returns: Entities or primitives

### **Service Files** (`service/`)
- Purpose: Business logic
- Naming: EntityNameService (UserService)
- Has: @Autowired repositories + services
- Contains: Validation + processing + converting
- Returns: DTOs

### **Controller Files** (`controller/`)
- Purpose: HTTP endpoints
- Naming: EntityNameController (UserController)
- Has: @Autowired services
- Contains: Endpoint methods
- Returns: ResponseEntity with DTO + HTTP status

### **DTO Files** (`dto/`)
- Purpose: Data transfer format
- Naming: Descriptive (AuthResponse, LoginRequest)
- Simple: Just fields + getters/setters
- Returns: From controllers to frontend
- As: JSON in HTTP response

---

## 🚀 The "Add New Feature" Checklist

### Scenario: Add ability to delete campaigns

**Step 1:** Update `Campaign.java` entity
- No change needed (already has all fields)

**Step 2:** Update `CampaignRepository.java`
```java
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    // Spring auto-provides delete methods:
    // deleteById(Long id)
    // delete(Campaign campaign)
}
```

**Step 3:** Update `CampaignService.java`
```java
@Service
public class CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;
    
    public void deleteCampaign(Long campaignId) {
        // Check if user is campaign owner
        Campaign campaign = campaignRepository.findById(campaignId)...
        // Check authorization
        // delete
        campaignRepository.deleteById(campaignId);
    }
}
```

**Step 4:** Update `CampaignController.java`
```java
@RestController
public class CampaignController {
    @Autowired
    private CampaignService campaignService;
    
    @DeleteMapping("/api/campaign/{id}")
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(new ApiResponse(true, "Deleted"));
    }
}
```

**Step 5:** No DTO needed (delete has no response body)

**Done!** Now frontend can call: `DELETE /api/campaign/{id}`

---

## 🔐 Security Rules in This App

| Endpoint | Public? | Requires Role |
|---|---|---|
| POST /api/auth/login | ✅ YES | None |
| POST /api/auth/register | ✅ YES | None |
| GET /api/auth/me | ❌ NO | Any (just logged in) |
| POST /api/campaign | ❌ NO | BRAND |
| GET /api/campaign/all | ✅ YES | None |
| PUT /api/campaign/{id} | ❌ NO | BRAND (must be owner) |
| POST /api/sponsorship | ❌ NO | INFLUENCER |
| PUT /api/sponsorship/{id}/approve | ❌ NO | BRAND (owner) |
| GET /api/admin/dashboard | ❌ NO | ADMIN |

**Protected = Need JWT token in Authorization header**

---

## 💾 Database Operations (CRUD)

### **CREATE** - Add new data
```java
UserRepository.save(newUser)  // INSERT INTO users
```

### **READ** - Get existing data
```java
UserRepository.findById(1)     // SELECT * FROM users WHERE id=1
UserRepository.findAll()       // SELECT * FROM users
UserRepository.findByEmail()   // SELECT * FROM users WHERE email=?
```

### **UPDATE** - Modify existing data
```java
user.setName("New Name");      // Change field
UserRepository.save(user)      // UPDATE users SET name=...
```

### **DELETE** - Remove data
```java
UserRepository.deleteById(1)   // DELETE FROM users WHERE id=1
```

---

## 🔍 How to Debug Issues

| Problem | Where to look |
|---|---|
| "Email not found error" | Check UserRepository query methods |
| "Data not being saved" | Check @Entity and @Column annotations |
| "JWT token invalid" | Check JwtTokenProvider.validateToken() |
| "Endpoint not found" | Check @RequestMapping and @PostMapping |
| "Service injection fails" | Check @Service and @Autowired annotations |
| "Controller doesn't receive data" | Check @RequestBody and @Valid |
| "Password too weak" | Check SecurityConfig passwordEncoder |
| "CORS error in browser" | Check CorsConfig allowedOrigins |

---

## 📝 Important Annotations Guide

### **Class-level Annotations**

```java
@Entity           // This maps to a database table
@Repository       // This accesses the database
@Service          // This contains business logic
@RestController   // This handles HTTP requests
@Component        // Generic Spring-managed component
@Autowired        // Inject dependency automatically
```

### **Method Annotations**

```java
@PostMapping("/path")      // Handles POST requests
@GetMapping("/path")       // Handles GET requests
@PutMapping("/path")       // Handles PUT requests
@DeleteMapping("/path")    // Handles DELETE requests
@RequestBody               // Convert JSON to object
@PathVariable              // Get {id} from URL
@Valid                     // Validate input
```

### **Field Annotations**

```java
@Id                        // Primary key
@GeneratedValue            // Auto-increment ID
@Column                    // Database column
@Table                     // Database table name
@ManyToOne                 // Relationship: many-to-one
@OneToMany                 // Relationship: one-to-many
@Enumerated(EnumType.STRING)  // Store enum as string
```

---

## 🧪 Testing Different Features

### **Test: User Registration**
```
Frontend sends:
POST /api/auth/register
{
  "name": "John",
  "email": "john@example.com",
  "password": "secret123",
  "role": "INFLUENCER"
}

Expected response:
200 OK
{
  "token": "eyJh...",
  "id": 1,
  "name": "John",
  "email": "john@example.com",
  "role": "INFLUENCER"
}
```

### **Test: Create Campaign**
```
Frontend sends:
POST /api/campaign
Header: Authorization: Bearer <token>
{
  "name": "Summer Sale",
  "description": "...",
  "budget": 10000,
  "startDate": "2026-06-01",
  "endDate": "2026-08-31"
}

Expected response:
201 CREATED
{
  "id": 1,
  "name": "Summer Sale",
  "status": "ACTIVE",
  ...
}
```

### **Test: Request Sponsorship**
```
Frontend sends:
POST /api/sponsorship
Header: Authorization: Bearer <token>
{
  "campaignId": 1,
  "message": "I want to promote your product!"
}

Expected response:
201 CREATED
{
  "id": 1,
  "influencerId": 2,
  "campaignId": 1,
  "status": "PENDING",
  ...
}
```

---

## ❓ FAQ

**Q: Why separate Service and Repository?**
A: Service has business rules, Repository talks to database. Easy to change database without changing business logic.

**Q: What's @Autowired for?**
A: Spring automatically creates and injects the object. No need to write `new AuthService()`.

**Q: Why convert Entity to DTO?**
A: Security (hide passwords), performance (send only needed fields), flexibility (change API without changing database).

**Q: What's a JWT token?**
A: A signed string that proves user is logged in. Server creates it, user sends it with each request, server validates it.

**Q: Why not send Entity directly?**
A: Because Entity contains all database fields including password. DTO only includes safe fields.

**Q: How does Spring find the database?**
A: From `application.properties` file with database URL and credentials.

**Q: What if database query fails?**
A: GlobalExceptionHandler catches it and sends user-friendly error message.

**Q: Can I add custom query to Repository?**
A: Yes! Example: `List<User> findByRoleAndIsActive(Role role, boolean active)`

**Q: How does Spring know which bean to inject?**
A: By type. If field is `UserRepository`, Spring injects `UserRepository` bean.

**Q: What if two repositories have same name?**
A: Use `@Qualifier` annotation to specify which one.

---

## 🎯 Remember These 4 Rules

1. **Each package has ONE responsibility**
   - Entity: Define structure
   - Repository: Access database
   - Service: Apply business logic
   - Controller: Receive HTTP, send response

2. **Always use DTOs for communication**
   - Internal: Use Entities
   - External (to frontend): Use DTOs

3. **Authorization happens in Controller**
   - Check user role/permission before calling service
   - Don't let unauthorized requests reach database

4. **Business logic in Service, not Controller**
   - Controller just receives and sends
   - Service does the thinking
   - Repository just queries

---

## 🎓 Summary

Your backend is organized in **8 packages with 47 files** that all work together:

1. **entity/** - What data looks like
2. **repository/** - How to access data
3. **service/** - What rules apply
4. **controller/** - How to receive requests
5. **dto/** - How to send data  
6. **security/** - How to verify users
7. **config/** - How to set up app
8. **exception/** - How to handle errors

Each feature follows the same pattern:
**Frontend → Controller → Service → Repository → Database → Response**

You now understand the complete backend! 🎉
