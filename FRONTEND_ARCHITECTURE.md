# Frontend Architecture Overview

## Project Information
- **Type**: Angular 17 SPA (Single Page Application)
- **Package Manager**: npm
- **UI Framework**: Angular Material 17 + Bootstrap 5
- **Build Tool**: Angular CLI 17
- **Language**: TypeScript 5.4
- **Styling**: SCSS
- **API Base URL**: `http://localhost:7070/api` (dev), configured in environment files

---

## 1. Directory Structure

### Root Level
```
frontend/
├── angular.json          # Angular CLI configuration
├── package.json          # Project dependencies and scripts
├── tsconfig.json         # TypeScript configuration
├── .angular/             # Angular CLI cache
├── dist/                 # Production build output
├── node_modules/         # Dependencies
└── src/                  # Source code root
```

### Source Structure (`src/`)
```
src/
├── index.html            # HTML entry point
├── main.ts              # Angular bootstrap file
├── styles.scss          # Global styles
├── favicon.ico          # Favicon
├── assets/              # Static assets (images, icons)
├── environments/        # Environment configurations
│   ├── environment.ts       # Development config
│   └── environment.prod.ts  # Production config
└── app/                 # Application root module
```

---

## 2. Application Module Structure (`src/app/`)

### Core Files
- **[app.component.ts](frontend/src/app/app.component.ts)** - Root component with navbar and router outlet
- **[app.module.ts](frontend/src/app/app.module.ts)** - Main module with all imports and declarations
- **[app-routing.module.ts](frontend/src/app/app-routing.module.ts)** - Routing configuration

### Directory Organization
```
app/
├── components/          # Feature components
├── services/            # API communication services
├── models/              # TypeScript interfaces/models
├── guards/              # Route guards
├── interceptors/        # HTTP interceptors
├── pipes/               # Custom pipes
```

---

## 3. Components Structure

### Component Organization by Feature

#### **Authentication Components** (`components/auth/`)
- **[login/login.component.ts](frontend/src/app/components/auth/login/login.component.ts)**
  - Reactive form for user login
  - Email validation
  - Password visibility toggle
  - Auto-redirect to dashboard if already logged in
  
- **[signup/signup.component.ts](frontend/src/app/components/auth/signup/signup.component.ts)**
  - User registration form
  - Role selection (ADMIN, BRAND, INFLUENCER)
  - Form validation

#### **Dashboard Components** (`components/dashboard/`)
- **[admin-dashboard/admin-dashboard.component.ts](frontend/src/app/components/dashboard/admin-dashboard/admin-dashboard.component.ts)**
  - Statistics overview for admins
  - User management
  - System-wide analytics
  
- **[brand-dashboard/brand-dashboard.component.ts](frontend/src/app/components/dashboard/brand-dashboard/brand-dashboard.component.ts)**
  - Campaign management
  - Sponsorship requests received
  - Total spending overview
  - Campaign filtering by status
  
- **[influencer-dashboard/influencer-dashboard.component.ts](frontend/src/app/components/dashboard/influencer-dashboard/influencer-dashboard.component.ts)**
  - Active campaign opportunities
  - Application management
  - Earnings and ratings

#### **Campaign Components** (`components/campaign/`)
- **[campaign-list/campaign-list.component.ts](frontend/src/app/components/campaign/campaign-list/campaign-list.component.ts)**
  - Display all active campaigns
  - Search and filter functionality
  
- **[campaign-form/campaign-form.component.ts](frontend/src/app/components/campaign/campaign-form/campaign-form.component.ts)**
  - Create new campaigns
  - Edit existing campaigns
  - Form validation
  
- **[campaign-detail/campaign-detail.component.ts](frontend/src/app/components/campaign/campaign-detail/campaign-detail.component.ts)**
  - View full campaign details
  - Apply for sponsorship (influencers)
  - View applications (brands)

#### **Sponsorship Components** (`components/sponsorship/`)
- **[sponsorship-request/sponsorship-request.component.ts](frontend/src/app/components/sponsorship/sponsorship-request/sponsorship-request.component.ts)**
  - List sponsorship requests
  - Accept/Reject requests
  - Track work submissions
  - Complete work flow

#### **Payment Component** (`components/payment/`)
- **[payment.component.ts](frontend/src/app/components/payment/payment.component.ts)**
  - Display payment history
  - Payment status tracking
  - Transaction details

#### **Rating Component** (`components/rating/`)
- **[rating.component.ts](frontend/src/app/components/rating/rating.component.ts)**
  - View user ratings
  - Average rating display
  - Rating history

#### **Notification Component** (`components/notification/`)
- **[notification.component.ts](frontend/src/app/components/notification/notification.component.ts)**
  - Display all notifications
  - Mark as read
  - Unread count badge

#### **Shared Components** (`components/shared/`)
- **[navbar/navbar.component.ts](frontend/src/app/components/shared/navbar/navbar.component.ts)**
  - Navigation bar with role-based menu
  - Notification badge
  - User profile menu
  - Logout functionality
  - Change password dialog trigger
  
- **[rating-dialog/rating-dialog.component.ts](frontend/src/app/components/shared/rating-dialog/rating-dialog.component.ts)**
  - Modal for adding/editing ratings
  - Star rating input
  - Feedback textarea
  
- **[payment-dialog/payment-dialog.component.ts](frontend/src/app/components/shared/payment-dialog/payment-dialog.component.ts)**
  - Modal for payment transactions
  - Amount input
  - Payment confirmation
  
- **[change-password/change-password.component.ts](frontend/src/app/components/shared/change-password/change-password.component.ts)**
  - Change user password
  - Confirm password matching

---

## 4. Services (`src/app/services/`)

### [auth.service.ts](frontend/src/app/services/auth.service.ts)
**Purpose**: Authentication and authorization management
**Key Methods**:
- `login(request: LoginRequest)` - User login with email/password
- `register(request: RegisterRequest)` - User registration with role
- `logout()` - Clear tokens and user data
- `getToken()` - Retrieve JWT token from localStorage
- `getCurrentUser()` - Get current logged-in user
- `isLoggedIn()` - Check if user is authenticated
- `hasRole(role: string)` - Check user's role
- `getDashboardRoute()` - Get role-specific dashboard route
- `changePassword()` - Change user password
**Storage**: Uses localStorage for token and user data

### [campaign.service.ts](frontend/src/app/services/campaign.service.ts)
**Purpose**: Campaign CRUD operations and queries
**Key Methods**:
- `getAllCampaigns()` - Get all campaigns
- `getActiveCampaigns()` - Get only active campaigns
- `getMyCampaigns()` - Get brand's campaigns
- `getCampaignById(id)` - Get single campaign detail
- `createCampaign(campaign)` - Create new campaign
- `updateCampaign(id, campaign)` - Update campaign
- `deleteCampaign(id)` - Delete campaign
- `searchCampaigns(name, platform, status)` - Search with filters
- `updateCampaignStatus(id, status)` - Change campaign status

### [sponsorship.service.ts](frontend/src/app/services/sponsorship.service.ts)
**Purpose**: Sponsorship request management
**Key Methods**:
- `applyForCampaign(request)` - Influencer applies for campaign
- `getMyApplications()` - Get influencer's applications
- `getBrandRequests()` - Get requests brand received
- `getCampaignRequests(campaignId)` - Get requests for specific campaign
- `getRequestById(id)` - Get single request details
- `updateRequestStatus(id, status)` - Accept/Reject request
- `submitWork(id, workDescription)` - Submit completed work
- `markWorkAsComplete(id)` - Brand marks work as complete

### [payment.service.ts](frontend/src/app/services/payment.service.ts)
**Purpose**: Payment processing and tracking
**Key Methods**:
- `createPayment(request)` - Create payment transaction
- `completePayment(id)` - Mark payment as completed
- `getPaymentById(id)` - Get payment details
- `getInfluencerPayments()` - Get influencer's payments received
- `getBrandPayments()` - Get brand's payment records
- `getEarnings()` - Get total earnings (influencer)
- `getSpending()` - Get total spending (brand)

### [rating.service.ts](frontend/src/app/services/rating.service.ts)
**Purpose**: User rating and feedback system
**Key Methods**:
- `addRating(request)` - Add new rating
- `getUserRatings(userId)` - Get ratings for a user
- `getMyRatings()` - Get ratings by current user
- `getAverageRating(userId)` - Get user's average rating

### [notification.service.ts](frontend/src/app/services/notification.service.ts)
**Purpose**: Notification management
**Key Methods**:
- `getNotifications()` - Get all notifications
- `getUnreadNotifications()` - Get unread only
- `getUnreadCount()` - Get count of unread
- `markAsRead(id)` - Mark single notification as read
- `markAllAsRead()` - Mark all as read
- `refreshUnreadCount()` - Refresh unread count
**State Management**: Uses BehaviorSubject for unread count

### [admin.service.ts](frontend/src/app/services/admin.service.ts)
**Purpose**: Admin dashboard and system management
**Key Methods**:
- `getStats()` - Get dashboard statistics
- `getAllUsers()` - Get all users
- `getUsersByRole(role)` - Filter users by role
- `deleteUser(id)` - Delete user account
- `getAllCampaigns()` - Get all campaigns (admin view)
- `getAllRequests()` - Get all sponsorship requests
- `getAllPayments()` - Get all payment records
- `getAllRatings()` - Get all ratings

---

## 5. Models/Interfaces (`src/app/models/`)

### [user.model.ts](frontend/src/app/models/user.model.ts)
```typescript
interface User {
  id: number;
  name: string;
  email: string;
  role: 'ADMIN' | 'BRAND' | 'INFLUENCER';
  bio?: string;
  profileImage?: string;
}

interface AuthResponse {
  token: string;
  type: string;
  id: number;
  name: string;
  email: string;
  role: string;
}

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: string;
}
```

### [campaign.model.ts](frontend/src/app/models/campaign.model.ts)
```typescript
interface Campaign {
  id: number;
  name: string;
  description: string;
  platform: string;
  budget: number;
  startDate: string;
  endDate: string;
  eligibility: string;
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
  brand: User;
}

interface CampaignRequest {
  name: string;
  description: string;
  platform: string;
  budget: number;
  startDate: string;
  endDate: string;
  eligibility: string;
}
```

### [sponsorship.model.ts](frontend/src/app/models/sponsorship.model.ts)
```typescript
interface SponsorshipRequest {
  id: number;
  influencer: User;
  campaign: Campaign;
  proposal: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'COMPLETED';
  workDescription?: string;
  workSubmittedAt?: string;
  workCompletedAt?: string;
  createdAt: string;
  updatedAt?: string;
}

interface SponsorshipApplicationRequest {
  campaignId: number;
  proposal: string;
}
```

### [payment.model.ts](frontend/src/app/models/payment.model.ts)
```typescript
interface Payment {
  id: number;
  campaign: Campaign;
  influencer: User;
  brand: User;
  amount: number;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  createdAt: string;
  paidAt?: string;
  transactionId: string;
}

interface PaymentRequest {
  campaignId: number;
  influencerId: number;
  amount: number;
}
```

### [rating.model.ts](frontend/src/app/models/rating.model.ts)
```typescript
interface Rating {
  id: number;
  campaign: Campaign;
  rater: User;
  rated: User;
  score: number;
  feedback: string;
  createdAt: string;
}

interface RatingRequest {
  campaignId: number;
  ratedUserId: number;
  score: number;
  feedback: string;
}
```

### [notification.model.ts](frontend/src/app/models/notification.model.ts)
```typescript
interface Notification {
  id: number;
  user: User;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}
```

### [common.model.ts](frontend/src/app/models/common.model.ts)
```typescript
interface DashboardStats {
  totalCampaigns: number;
  activeCampaigns: number;
  totalRequests: number;
  pendingRequests: number;
  totalEarnings: number;
  totalSpending: number;
  averageRating: number;
  totalUsers: number;
  totalBrands: number;
  totalInfluencers: number;
}

interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}
```

---

## 6. Route Guards (`src/app/guards/`)

### [auth.guard.ts](frontend/src/app/guards/auth.guard.ts)
**Purpose**: Protect routes requiring authentication
**Implementation**: `CanActivate`
**Logic**:
- Checks if user is logged in (has token)
- Redirects to login page if not authenticated
- Allows navigation if authenticated

### [role.guard.ts](frontend/src/app/guards/role.guard.ts)
**Purpose**: Enforce role-based access control
**Implementation**: `CanActivate`
**Logic**:
- Retrieves expected role from route data
- Gets current user role
- Redirects to appropriate dashboard if role doesn't match
- Allows navigation only if role matches

---

## 7. HTTP Interceptors (`src/app/interceptors/`)

### [auth.interceptor.ts](frontend/src/app/interceptors/auth.interceptor.ts)
**Purpose**: Automatically attach JWT token to all HTTP requests
**Functionality**:
- Retrieves token from localStorage
- Adds `Authorization: Bearer <token>` header to requests
- Intercepts 401 errors (unauthorized)
- Logs out user on 401 and redirects to login
- Propagates other errors to components

**Registered in**: `app.module.ts` providers

---

## 8. Pipes (`src/app/pipes/`)

### [filter.pipe.ts](frontend/src/app/pipes/filter.pipe.ts)
**Purpose**: Filter arrays by field value
**Usage**: `*ngFor="let item of items | filter:'status':'ACTIVE'"`
**Implementation**: Filters array items where specified field matches value
**Parameters**:
- `items`: Array to filter
- `field`: Object property name
- `value`: Value to match

---

## 9. Routing Structure (`app-routing.module.ts`)

### Route Configuration

```
/                          → /login (redirect)
├── /login                 → LoginComponent
│
├── /signup                → SignupComponent
│
├── /dashboard/admin       → AdminDashboardComponent (AuthGuard + RoleGuard: ADMIN)
├── /dashboard/brand       → BrandDashboardComponent (AuthGuard + RoleGuard: BRAND)
├── /dashboard/influencer  → InfluencerDashboardComponent (AuthGuard + RoleGuard: INFLUENCER)
│
├── /campaigns             → CampaignListComponent (AuthGuard)
├── /campaigns/new         → CampaignFormComponent (AuthGuard + RoleGuard: BRAND)
├── /campaigns/edit/:id    → CampaignFormComponent (AuthGuard + RoleGuard: BRAND)
├── /campaigns/:id         → CampaignDetailComponent (AuthGuard)
│
├── /sponsorship-requests  → SponsorshipRequestComponent (AuthGuard)
├── /payments              → PaymentComponent (AuthGuard)
├── /ratings               → RatingComponent (AuthGuard)
├── /notifications         → NotificationComponent (AuthGuard)
│
/** (wildcard)            → /login (redirect)
```

---

## 10. Module Imports (`app.module.ts`)

### Angular Core
- `BrowserModule` - Browser support
- `BrowserAnimationsModule` - Animation support
- `HttpClientModule` - HTTP requests
- `FormsModule` - Template-driven forms
- `ReactiveFormsModule` - Reactive forms
- `AppRoutingModule` - Routing

### Angular Material Modules (v17.3.0)
- UI Components: Toolbar, Button, Card, Input, FormField
- Layout: Sidenav, Menu, Tabs
- Table: Table, Paginator, Sort
- Dialogs: Dialog, SnackBar
- Indicators: ProgressSpinner, Badge, Chips
- Selection: Select, List
- Date: Datepicker, NativeDateModule
- Utilities: Icon, Tooltip

### HTTP Interceptors
- `AuthInterceptor` - JWT token injection

### All Components Declared
- Auth components (Login, Signup)
- Dashboard components (Admin, Brand, Influencer)
- Campaign components (List, Form, Detail)
- Sponsorship, Payment, Rating, Notification components
- Shared components (Navbar, Dialogs, ChangePassword)
- Custom pipes (FilterPipe)

---

## 11. Environment Configuration

### Development (`environment.ts`)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:7070/api'
};
```

### Production (`environment.prod.ts`)
```typescript
export const environment = {
  production: true,
  apiUrl: 'http://localhost:7070/api'
};
```

---

## 12. Build Configuration (`angular.json`)

### Project Settings
- **Name**: `sponsorship-app-frontend`
- **Type**: Application
- **Source Root**: `src/`
- **Prefix**: `app`
- **Default Style**: `scss`

### Build Options
- **Output Path**: `dist/sponsorship-app-frontend`
- **Entry Point**: `src/main.ts` (browser: true)
- **Index**: `src/index.html`
- **Polyfills**: `zone.js`
- **Styles**: 
  - Angular Material theme: `indigo-pink.css`
  - Global styles: `src/styles.scss`
- **Assets**: `src/favicon.ico`, `src/assets`

### Production Configuration
- **Budgets**: 
  - Initial warning: 2MB
  - Initial error: 5MB
- **Output Hashing**: All files
- **Optimization**: Enabled

### Development Configuration
- **Optimization**: Disabled
- **Source Maps**: Enabled

---

## 13. Dependencies (`package.json`)

### Runtime Dependencies
- `@angular/*` - v17.3.0 (core, common, forms, router, etc.)
- `@angular/cdk` - v17.3.0 (component dev kit)
- `@angular/material` - v17.3.0 (Material components)
- `bootstrap` - v5.3.0 (CSS framework)
- `bootstrap-icons` - v1.11.0 (icons)
- `rxjs` - v7.8.0 (reactive programming)
- `tslib` - v2.3.0 (TS helpers)
- `zone.js` - v0.14.3 (zone management)

### Dev Dependencies
- `@angular-devkit/build-angular` - v17.3.0
- `@angular/cli` - v17.3.0
- `@angular/compiler-cli` - v17.3.0
- `typescript` - v5.4.2

### NPM Scripts
- `npm start` → `ng serve` (dev server)
- `npm run build` → `ng build` (production build)
- `npm run watch` → `ng build --watch --configuration development` (watch mode)

---

## 14. Data Flow & Authentication

### Authentication Flow
1. User logs in with email/password
2. Backend validates and returns JWT token
3. Token stored in localStorage via `AuthService`
4. `AuthInterceptor` automatically adds token to all requests
5. On 401 error, user logged out and redirected to login

### Role-Based Access Control
1. User role stored in localStorage
2. Route guards check role before navigation
3. Different dashboards for ADMIN, BRAND, INFLUENCER roles
4. UI components conditionally display based on user role

### API Communication Pattern
```typescript
Service → HttpClient (with AuthInterceptor)
          ↓
Backend API (http://localhost:7070/api)
          ↓
Response → Component → BehaviorSubject (if state management)
          ↓
Template renders data
```

---

## 15. Key Features Summary

### Authentication
- Login/Signup with role selection
- JWT token-based authentication
- Password change functionality
- Automatic logout on token expiration

### Campaign Management
- Browse active campaigns
- Create campaigns (brands only)
- Edit campaigns (own campaigns only)
- View campaign details
- Search and filter campaigns

### Sponsorship
- Influencers apply for campaigns
- Brands review applications
- Accept/Reject requests
- Work submission and completion tracking

### Payments
- Payment history tracking
- Transaction status monitoring
- Earnings/Spending overview

### Ratings & Feedback
- Rate users after collaboration
- View user ratings
- Feedback management

### Notifications
- Real-time notification display
- Mark as read functionality
- Unread count badge

### Admin Panel
- System-wide statistics
- User management
- Campaign oversight
- Payment monitoring

---

## 16. Configuration Files Summary

| File | Purpose |
|------|---------|
| `angular.json` | Angular CLI configuration and build settings |
| `tsconfig.json` | TypeScript compiler options |
| `package.json` | Dependencies and npm scripts |
| `environment.ts` | Dev environment variables |
| `environment.prod.ts` | Production environment variables |

---

## 17. File Count Summary

- **Components**: 15 (Auth, Dashboard, Campaign, Sponsorship, Payment, Rating, Notification, Shared)
- **Services**: 7 (Auth, Campaign, Sponsorship, Payment, Rating, Notification, Admin)
- **Models**: 7 interfaces (User, Campaign, Sponsorship, Payment, Rating, Notification, Common)
- **Guards**: 2 (Auth, Role)
- **Interceptors**: 1 (Auth)
- **Pipes**: 1 (Filter)
- **Modules**: 1 (App Module)
- **Routing**: 1 (App Routing Module)

---

## Development Notes

### Local Development
```bash
npm install              # Install dependencies
npm start               # Start dev server (localhost:4200)
npm run build           # Production build
npm run watch           # Build in watch mode
```

### Backend Connection
- API runs on `localhost:7070`
- Ensure backend is running before starting frontend dev server
- Requests fail gracefully with user-friendly error messages

### State Management
- Simple state management using:
  - `BehaviorSubject` for notifications (unread count)
  - `localStorage` for authentication
  - Component-level RxJS subscriptions
  - No additional state library (NgRx) used

### Error Handling
- HTTP errors caught by interceptor
- 401 errors trigger logout
- Other errors displayed in snackbars
- Components handle subscription errors

### Material Theme
- Material Indigo-Pink prebuilt theme
- Bootstrap 5 for grid and utilities
- Custom SCSS in `src/styles.scss`
- Component-level styling with `.scss` files
