# Sponsorship App

A full-stack application connecting Brands and Influencers through sponsorship campaigns, with Admin oversight.

## Features

- **User Management**: Registration and login for Admin, Brand, and Influencer roles
- **Campaign Management**: Brands can create, edit, and manage campaigns
- **Sponsorship Requests**: Influencers can apply to campaigns, Brands can accept/reject
- **Payments**: Track payments and earnings
- **Ratings & Feedback**: Two-way rating system between Brands and Influencers
- **Notifications**: Real-time notifications for important events

## Tech Stack

### Backend
- Spring Boot 3.2
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Lombok

### Frontend
- Angular 17
- Angular Material
- RxJS

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven
- MySQL 8.0+

### Running the Backend

1. Navigate to the backend directory:
   ```bash
   cd sponsorship-app-backend
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. The backend will start on `http://localhost:7070`

### Running the Frontend

1. Navigate to the frontend directory:
   ```bash
   cd sponsorship-app-backend/frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run the application:
   ```bash
   ng serve
   ```

4. Open `http://localhost:4200` in your browser

## Demo Accounts

The application comes with pre-loaded demo accounts:

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@sponsorship.com | admin123 |
| Brand | brand@example.com | brand123 |
| Influencer | influencer@example.com | influencer123 |

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Get current user

### Campaigns
- `GET /api/campaigns` - Get all campaigns
- `GET /api/campaigns/active` - Get active campaigns
- `GET /api/campaigns/my-campaigns` - Get brand's campaigns
- `POST /api/campaigns` - Create campaign
- `PUT /api/campaigns/{id}` - Update campaign
- `DELETE /api/campaigns/{id}` - Delete campaign

### Sponsorship
- `POST /api/sponsorship/apply` - Apply for campaign
- `GET /api/sponsorship/my-applications` - Get influencer's applications
- `GET /api/sponsorship/brand-requests` - Get brand's received requests
- `PUT /api/sponsorship/{id}/status` - Update request status

### Payments
- `POST /api/payments` - Create payment
- `PUT /api/payments/{id}/complete` - Complete payment
- `GET /api/payments/influencer` - Get influencer payments
- `GET /api/payments/brand` - Get brand payments

### Ratings
- `POST /api/ratings` - Add rating
- `GET /api/ratings/my-ratings` - Get received ratings
- `GET /api/ratings/average/{userId}` - Get average rating

### Notifications
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/unread-count` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read

### Admin
- `GET /api/admin/stats` - Get dashboard statistics
- `GET /api/admin/users` - Get all users
- `DELETE /api/admin/users/{id}` - Delete user

## Database

The application uses **MySQL** database. 

### MySQL Setup

1. Install MySQL 8.0+ on your system
2. Create a database (optional - app creates it automatically):
   ```sql
   CREATE DATABASE sponsorshipdb;
   ```
3. Update credentials in `application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/sponsorshipdb
   spring.datasource.username=root
   spring.datasource.password=root
   ```

### Default Configuration
- **Host**: localhost
- **Port**: 3306
- **Database**: sponsorshipdb (auto-created)
- **Username**: root
- **Password**: root

> **Note**: Change the username and password in `application.properties` to match your MySQL installation.

## Project Structure

```
sponsorship-app-backend/
├── src/main/java/com/myapp/sponsorshipapp/
│   ├── config/           # Security & CORS configuration
│   ├── controller/       # REST controllers
│   ├── dto/              # Data transfer objects
│   ├── entity/           # JPA entities
│   ├── exception/        # Exception handlers
│   ├── repository/       # Data repositories
│   ├── security/         # JWT & authentication
│   └── service/          # Business logic
├── frontend/
│   └── src/app/
│       ├── components/   # Angular components
│       ├── guards/       # Route guards
│       ├── interceptors/ # HTTP interceptors
│       ├── models/       # TypeScript interfaces
│       └── services/     # Angular services
└── pom.xml
```

## License

This project is created for educational purposes.

