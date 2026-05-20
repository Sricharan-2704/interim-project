# Sponsorship App - Database Entity Relationship Diagram (ERD)

## 📊 Visual Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    SPONSORSHIP APP - DATABASE SCHEMA                                    │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────┘

    ┌─────────────────────┐
    │       USERS         │
    │─────────────────────│
    │ PK  id              │◄──────────────────────────────────────────────────────────────────────────────┐
    │     name (UNIQUE)   │                                                                               │
    │     email (UNIQUE)  │                                                                               │
    │     password        │                                                                               │
    │     role (ENUM)     │                                                                               │
    │     bio             │                                                                               │
    │     profile_image   │                                                                               │
    └─────────┬───────────┘                                                                               │
              │                                                                                           │
              │ 1                                                                                         │
              │                                                                                           │
              ▼ *                                                                                         │
    ┌─────────────────────┐         ┌─────────────────────────┐                                           │
    │     CAMPAIGNS       │         │  SPONSORSHIP_REQUESTS   │                                           │
    │─────────────────────│         │─────────────────────────│                                           │
    │ PK  id              │◄────────│ FK  campaign_id         │                                           │
    │     name            │    *    │ PK  id                  │                                           │
    │     description     │         │ FK  influencer_id       │───────────────────────────────────────────┤
    │     platform        │    1    │     proposal            │                                           │
    │     budget          │         │     status (ENUM)       │                                           │
    │     start_date      │         │     created_at          │                                           │
    │     end_date        │         │     updated_at          │                                           │
    │     eligibility     │         └─────────────────────────┘                                           │
    │     status (ENUM)   │                                                                               │
    │ FK  brand_id        │───────────────────────────────────────────────────────────────────────────────┤
    └─────────┬───────────┘                                                                               │
              │                                                                                           │
              │ 1                                                                                         │
              │                                                                                           │
              ├──────────────────┬──────────────────────────────────────────────────┐                     │
              │                  │                                                  │                     │
              ▼ *                ▼ *                                                ▼ *                   │
    ┌─────────────────────┐    ┌─────────────────────┐                    ┌─────────────────────┐         │
    │      PAYMENTS       │    │       RATINGS       │                    │   NOTIFICATIONS     │         │
    │─────────────────────│    │─────────────────────│                    │─────────────────────│         │
    │ PK  id              │    │ PK  id              │                    │ PK  id              │         │
    │ FK  campaign_id     │    │ FK  campaign_id     │                    │ FK  user_id         │─────────┘
    │ FK  influencer_id   │────│ FK  rater_id        │────────────────────│     title           │
    │ FK  brand_id        │────│ FK  rated_id        │────────────────────│     message         │
    │     amount          │    │     score (1-5)     │                    │     is_read         │
    │     status (ENUM)   │    │     feedback        │                    │     created_at      │
    │     created_at      │    │     created_at      │                    └─────────────────────┘
    │     paid_at         │    └─────────────────────┘
    │     transaction_id  │
    └─────────────────────┘
```

---

## 📋 Table Definitions

### 1. USERS Table
Central entity for all user types (Admin, Brand, Influencer)

| Column        | Type         | Constraints              | Description                    |
|---------------|--------------|--------------------------|--------------------------------|
| `id`          | BIGINT       | PK, AUTO_INCREMENT       | Unique user identifier         |
| `name`        | VARCHAR(255) | NOT NULL, UNIQUE         | Display name / username        |
| `email`       | VARCHAR(255) | NOT NULL, UNIQUE         | User email address             |
| `password`    | VARCHAR(255) | NOT NULL                 | Encrypted password             |
| `role`        | ENUM         | NOT NULL                 | ADMIN, BRAND, INFLUENCER       |
| `bio`         | VARCHAR(255) | NULLABLE                 | User biography                 |
| `profile_image`| VARCHAR(255)| NULLABLE                 | Profile image URL              |

---

### 2. CAMPAIGNS Table
Marketing campaigns created by Brands

| Column        | Type         | Constraints              | Description                    |
|---------------|--------------|--------------------------|--------------------------------|
| `id`          | BIGINT       | PK, AUTO_INCREMENT       | Unique campaign identifier     |
| `name`        | VARCHAR(255) | NOT NULL                 | Campaign title                 |
| `description` | VARCHAR(2000)| NULLABLE                 | Detailed campaign description  |
| `platform`    | VARCHAR(255) | NULLABLE                 | Target platform (Instagram, YouTube, etc.) |
| `budget`      | DOUBLE       | POSITIVE                 | Campaign budget amount         |
| `start_date`  | DATE         | NULLABLE                 | Campaign start date            |
| `end_date`    | DATE         | NULLABLE                 | Campaign end date              |
| `eligibility` | VARCHAR(255) | NULLABLE                 | Requirements for influencers   |
| `status`      | ENUM         | DEFAULT 'ACTIVE'         | ACTIVE, PAUSED, COMPLETED, CANCELLED, EXPIRED |
| `brand_id`    | BIGINT       | FK → users.id            | Brand who created the campaign |

---

### 3. SPONSORSHIP_REQUESTS Table
Applications from Influencers to Campaigns

| Column        | Type         | Constraints              | Description                    |
|---------------|--------------|--------------------------|--------------------------------|
| `id`          | BIGINT       | PK, AUTO_INCREMENT       | Unique request identifier      |
| `influencer_id`| BIGINT      | FK → users.id            | Influencer applying            |
| `campaign_id` | BIGINT       | FK → campaigns.id        | Target campaign                |
| `proposal`    | VARCHAR(2000)| NULLABLE                 | Influencer's pitch/proposal    |
| `status`      | ENUM         | DEFAULT 'PENDING'        | PENDING, ACCEPTED, REJECTED, COMPLETED |
| `created_at`  | TIMESTAMP    | DEFAULT NOW()            | Application timestamp          |
| `updated_at`  | TIMESTAMP    | NULLABLE                 | Last status update timestamp   |

---

### 4. PAYMENTS Table
Payment transactions between Brands and Influencers

| Column        | Type         | Constraints              | Description                    |
|---------------|--------------|--------------------------|--------------------------------|
| `id`          | BIGINT       | PK, AUTO_INCREMENT       | Unique payment identifier      |
| `campaign_id` | BIGINT       | FK → campaigns.id        | Related campaign               |
| `influencer_id`| BIGINT      | FK → users.id            | Payment recipient (Influencer) |
| `brand_id`    | BIGINT       | FK → users.id            | Payment sender (Brand)         |
| `amount`      | DOUBLE       | POSITIVE                 | Payment amount                 |
| `status`      | ENUM         | DEFAULT 'PENDING'        | PENDING, COMPLETED, FAILED, REFUNDED |
| `created_at`  | TIMESTAMP    | DEFAULT NOW()            | Payment creation timestamp     |
| `paid_at`     | TIMESTAMP    | NULLABLE                 | Actual payment timestamp       |
| `transaction_id`| VARCHAR(255)| NULLABLE                | External transaction reference |

---

### 5. RATINGS Table
Two-way rating system between Brands and Influencers

| Column        | Type         | Constraints              | Description                    |
|---------------|--------------|--------------------------|--------------------------------|
| `id`          | BIGINT       | PK, AUTO_INCREMENT       | Unique rating identifier       |
| `campaign_id` | BIGINT       | FK → campaigns.id        | Related campaign               |
| `rater_id`    | BIGINT       | FK → users.id            | Person giving the rating       |
| `rated_id`    | BIGINT       | FK → users.id            | Person being rated             |
| `score`       | INTEGER      | CHECK (1-5)              | Rating score (1 to 5 stars)    |
| `feedback`    | VARCHAR(1000)| NULLABLE                 | Written feedback               |
| `created_at`  | TIMESTAMP    | DEFAULT NOW()            | Rating timestamp               |

---

### 6. NOTIFICATIONS Table
System notifications for users

| Column        | Type         | Constraints              | Description                    |
|---------------|--------------|--------------------------|--------------------------------|
| `id`          | BIGINT       | PK, AUTO_INCREMENT       | Unique notification identifier |
| `user_id`     | BIGINT       | FK → users.id            | Notification recipient         |
| `title`       | VARCHAR(255) | NULLABLE                 | Notification title             |
| `message`     | VARCHAR(255) | NULLABLE                 | Notification body              |
| `is_read`     | BOOLEAN      | DEFAULT FALSE            | Read status                    |
| `created_at`  | TIMESTAMP    | DEFAULT NOW()            | Notification timestamp         |

---

## 🔗 Relationship Summary

| Relationship                          | Type        | Description                                           |
|---------------------------------------|-------------|-------------------------------------------------------|
| User (Brand) → Campaign               | 1 : Many    | A Brand can create multiple Campaigns                 |
| User (Influencer) → SponsorshipRequest| 1 : Many    | An Influencer can apply to multiple Campaigns         |
| Campaign → SponsorshipRequest         | 1 : Many    | A Campaign can have multiple applications             |
| Campaign → Payment                    | 1 : Many    | A Campaign can have multiple payments                 |
| User (Brand) → Payment                | 1 : Many    | A Brand can make multiple payments                    |
| User (Influencer) → Payment           | 1 : Many    | An Influencer can receive multiple payments           |
| Campaign → Rating                     | 1 : Many    | A Campaign can have multiple ratings                  |
| User (Rater) → Rating                 | 1 : Many    | A User can give multiple ratings                      |
| User (Rated) → Rating                 | 1 : Many    | A User can receive multiple ratings                   |
| User → Notification                   | 1 : Many    | A User can have multiple notifications                |

---

## 📊 Enum Definitions

### Role
```
ADMIN       - System administrator with full access
BRAND       - Company/Business creating campaigns
INFLUENCER  - Content creator applying to campaigns
```

### CampaignStatus
```
ACTIVE      - Campaign is open for applications
PAUSED      - Campaign temporarily stopped
COMPLETED   - Campaign successfully finished
CANCELLED   - Campaign was cancelled
EXPIRED     - Campaign end date has passed
```

### RequestStatus
```
PENDING     - Application awaiting review
ACCEPTED    - Application approved by Brand
REJECTED    - Application declined by Brand
COMPLETED   - Sponsorship work completed
```

### PaymentStatus
```
PENDING     - Payment initiated but not processed
COMPLETED   - Payment successfully transferred
FAILED      - Payment processing failed
REFUNDED    - Payment was refunded
```

---

## 🔄 Business Workflow

```
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                              SPONSORSHIP APP WORKFLOW                                        │
└──────────────────────────────────────────────────────────────────────────────────────────────┘

  ┌─────────┐                                                                    ┌─────────────┐
  │  BRAND  │                                                                    │ INFLUENCER  │
  └────┬────┘                                                                    └──────┬──────┘
       │                                                                                │
       │ 1. Register/Login                                                              │ 1. Register/Login
       │                                                                                │
       ▼                                                                                │
  ┌─────────────────┐                                                                   │
  │ Create Campaign │                                                                   │
  │ (ACTIVE status) │                                                                   │
  └────────┬────────┘                                                                   │
           │                                                                            │
           │                           ┌──────────────────┐                             │
           └──────────────────────────►│    CAMPAIGNS     │◄────────────────────────────┤
                                       │   (Available)    │                             │
                                       └────────┬─────────┘                             │
                                                │                                       │
                                                │ 2. Browse & Apply                     │
                                                │                                       ▼
                                                │                              ┌─────────────────┐
                                                │                              │ Submit Proposal │
                                                │                              │ (PENDING)       │
                                                │                              └────────┬────────┘
                                                │                                       │
           ┌────────────────────────────────────┼───────────────────────────────────────┘
           │                                    │
           ▼                                    ▼
  ┌─────────────────┐                  ┌─────────────────────┐
  │ Review Requests │                  │ SPONSORSHIP_REQUEST │
  │                 │◄─────────────────│     (PENDING)       │
  └────────┬────────┘                  └─────────────────────┘
           │
           │ 3. Accept/Reject
           │
     ┌─────┴─────┐
     │           │
     ▼           ▼
 ┌───────┐   ┌────────┐
 │ACCEPTED│   │REJECTED│
 └───┬───┘   └────────┘
     │
     │ 4. Work Completed
     │
     ▼
  ┌─────────────────┐                  ┌─────────────────────┐
  │ Create Payment  │─────────────────►│      PAYMENT        │
  │ (PENDING)       │                  │     (PENDING)       │
  └────────┬────────┘                  └──────────┬───────���──┘
           │                                      │
           │ 5. Process Payment                   │
           │                                      ▼
           │                           ┌─────────────────────┐
           │                           │      PAYMENT        │
           │                           │    (COMPLETED)      │
           │                           └──────────┬──────────┘
           │                                      │
           │                                      │ 6. Notification Sent
           │                                      │
           │                           ┌──────────▼──────────┐
           │                           │   NOTIFICATIONS     │
           │                           │ "Payment received!" │
           │                           └─────────────────────┘
           │
           │ 7. Exchange Ratings
           │
     ┌─────┴─────┐
     │           │
     ▼           ▼
 ┌───────────┐   ┌───────────┐
 │ Brand     │   │ Influencer│
 │ rates     │   │ rates     │
 │ Influencer│   │ Brand     │
 └─────┬─────┘   └─────┬─────┘
       │               │
       └───────┬───────┘
               │
               ▼
       ┌───────────────┐
       │    RATINGS    │
       │  (1-5 stars)  │
       └───────────────┘
```

---

## 🗃️ Foreign Key Constraints

```sql
-- Campaign → User (Brand)
ALTER TABLE campaigns ADD CONSTRAINT FK_campaign_brand 
    FOREIGN KEY (brand_id) REFERENCES users(id);

-- SponsorshipRequest → User (Influencer)
ALTER TABLE sponsorship_requests ADD CONSTRAINT FK_request_influencer 
    FOREIGN KEY (influencer_id) REFERENCES users(id);

-- SponsorshipRequest → Campaign
ALTER TABLE sponsorship_requests ADD CONSTRAINT FK_request_campaign 
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id);

-- Payment → Campaign
ALTER TABLE payments ADD CONSTRAINT FK_payment_campaign 
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id);

-- Payment → User (Influencer)
ALTER TABLE payments ADD CONSTRAINT FK_payment_influencer 
    FOREIGN KEY (influencer_id) REFERENCES users(id);

-- Payment → User (Brand)
ALTER TABLE payments ADD CONSTRAINT FK_payment_brand 
    FOREIGN KEY (brand_id) REFERENCES users(id);

-- Rating → Campaign
ALTER TABLE ratings ADD CONSTRAINT FK_rating_campaign 
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id);

-- Rating → User (Rater)
ALTER TABLE ratings ADD CONSTRAINT FK_rating_rater 
    FOREIGN KEY (rater_id) REFERENCES users(id);

-- Rating → User (Rated)
ALTER TABLE ratings ADD CONSTRAINT FK_rating_rated 
    FOREIGN KEY (rated_id) REFERENCES users(id);

-- Notification → User
ALTER TABLE notifications ADD CONSTRAINT FK_notification_user 
    FOREIGN KEY (user_id) REFERENCES users(id);
```

---

## 📈 Database Statistics (Cardinality)

| Entity              | Expected Volume | Notes                              |
|---------------------|-----------------|-------------------------------------|
| Users               | 1K - 100K       | Brands + Influencers + Admins      |
| Campaigns           | 100 - 10K       | Active marketing campaigns         |
| SponsorshipRequests | 1K - 100K       | ~10 requests per campaign average  |
| Payments            | 100 - 10K       | ~1 payment per accepted request    |
| Ratings             | 200 - 20K       | ~2 ratings per completed campaign  |
| Notifications       | 10K - 1M        | High volume, consider archiving    |

---

## 🔐 Data Integrity Rules

1. **User Uniqueness**: Both `name` and `email` must be unique across all users
2. **Password Security**: Passwords are stored encrypted (BCrypt)
3. **Budget Validation**: Campaign budget must be a positive number
4. **Rating Range**: Score must be between 1 and 5 (inclusive)
5. **Status Transitions**: Status changes follow defined workflow patterns
6. **Referential Integrity**: All foreign keys reference valid parent records

---

*Generated: May 14, 2026*
*Sponsorship App v1.0*

