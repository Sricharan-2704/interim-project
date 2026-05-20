# Quick Start Guide - Password Change & Work Submission Features

## ✅ What Has Been Implemented

### Feature 1: Password Change for Users
- ✅ Backend: `POST /api/auth/change-password` endpoint
- ✅ Frontend: New change-password component with form
- ✅ Security: Old password verification before change
- ✅ Validation: Password format requirements (letters, numbers, @)
- ✅ Error handling: Clear error messages for validation failures

### Feature 2: Work Submission & Approval Workflow
- ✅ Backend: New endpoints for work submission and approval
- ✅ Frontend: UI for influencers to submit work, brands to approve
- ✅ Payment Lock: Payments disabled until work is approved
- ✅ Notifications: Automatic notifications to both parties
- ✅ Status Tracking: Timeline shows work progress

---

## 🔧 What You Need to Do

### Step 1: Database Migration
Run this SQL migration to add work tracking columns:

```sql
ALTER TABLE sponsorship_requests ADD COLUMN work_description VARCHAR(2000);
ALTER TABLE sponsorship_requests ADD COLUMN work_submitted_at TIMESTAMP NULL;
ALTER TABLE sponsorship_requests ADD COLUMN work_completed_at TIMESTAMP NULL;
```

Or use Spring Data JPA migration if you prefer.

### Step 2: Rebuild Backend
```bash
mvn clean install
mvn spring-boot:run
```

### Step 3: Rebuild Frontend
```bash
cd frontend
npm install
ng serve
```

---

## 📱 How to Use the Features

### Using Change Password
1. **User**: Click account menu → Change Password
2. **Enter**: Current password, new password, confirm
3. **New Password Requirements**:
   - At least 6 characters
   - Must contain letters (a-zA-Z)
   - Must contain numbers (0-9)
   - Must contain @ symbol
4. **Submit**: Button shows loading state during submission
5. **Success**: Notification confirms password changed

### Using Work Submission (Influencer)
1. **View**: Go to "My Applications" → Find accepted sponsorship
2. **Status Shows**: ACCEPTED
3. **Click**: "Submit Work" button
4. **Enter**: Work description (optional but recommended)
5. **Notification**: Brand receives notification of submitted work

### Approving Work (Brand)
1. **View**: Go to "Sponsorship Requests" 
2. **See**: Work status showing submitted work
3. **Click**: "Approve Work" button to review and accept
4. **Unlock Payment**: "Make Payment" button becomes available
5. **Process**: Click "Make Payment" to pay influencer

### Payment Processing (Updated)
1. **Check**: Can only pay after brand approves work
2. **Submit**: Click "Make Payment" button
3. **Enter**: Payment amount
4. **Confirm**: Payment processes
5. **Status**: Changes to "Payment Completed"
6. **Lock**: Payment button disabled, cannot pay twice

---

## 🔍 API Reference

### Change Password
```
POST /api/auth/change-password
Content-Type: application/json

{
  "oldPassword": "currentPass@123",
  "newPassword": "newPass@456",
  "confirmPassword": "newPass@456"
}

Response: { "success": true, "message": "Password changed successfully" }
```

### Submit Work
```
POST /api/sponsorship/{sponsorshipRequestId}/submit-work?workDescription={description}
Authorization: Bearer <token>

Response: SponsorshipRequest with workSubmittedAt timestamp
```

### Approve Work
```
PUT /api/sponsorship/{sponsorshipRequestId}/mark-work-complete
Authorization: Bearer <token>

Response: SponsorshipRequest with workCompletedAt timestamp
```

---

## ✨ Key Features Overview

| Feature | Who | When | Result |
|---------|-----|------|--------|
| Change Password | Any User | Anytime | Password updated securely |
| Submit Work | Influencer | After ACCEPTED | Brand notified |
| Approve Work | Brand | After submission | Payment becomes available |
| Make Payment | Brand | After approval | Influencer receives payment |

---

## 🧪 Testing Scenarios

### Scenario 1: Change Password
1. Login with test account (password: `Test@123`)
2. Navigate to settings/change password
3. Enter current password: `Test@123`
4. Enter new password: `NewPass@456`
5. Confirm password: `NewPass@456`
6. Click change button
7. See success notification
8. Logout and login with new password

### Scenario 2: Complete Work Workflow
1. **Day 1**: Influencer applies to campaign (request: PENDING)
2. **Day 2**: Brand accepts application (request: ACCEPTED)
3. **Day 3**: Influencer submits work (workSubmittedAt set)
4. **Day 4**: Brand reviews and approves (workCompletedAt set)
5. **Day 5**: Brand makes payment (request: COMPLETED)
6. **Day 6**: Both rate each other

### Scenario 3: Payment Prevention
1. Create sponsorship request
2. Brand accepts it
3. Try to make payment **before** work submission → FAILS
   - Error: "Work must be completed before payment can be made"
4. After work submission and approval → SUCCEEDS
5. Try to pay again → FAILS
   - Error: "Payment has already been made for this sponsorship"

---

## ⚠️ Important Notes

### Password Requirements
Users must set passwords with:
- Minimum 6 characters
- At least one letter (a-z, A-Z)
- At least one number (0-9)
- At least one @ symbol

Example valid: `MyPass@123`

### Work Submission Workflow
- Work can **only** be submitted for ACCEPTED sponsorships
- Work can **only** be submitted once per sponsorship
- Payment requires work to be marked complete by brand
- Brand gets notification immediately upon work submission

### Payment Changes
- Old behavior: Payment created immediately
- **New behavior**: Payment requires brand approval of work
- This prevents payment for incomplete work
- Protects both influencer and brand

---

## 🐛 Troubleshooting

### "Password is incorrect"
- Make sure you're entering the CURRENT password, not new password
- Passwords are case-sensitive
- Check for extra spaces

### "Work can only be submitted for accepted requests"
- Brand must first accept the sponsorship request
- Status must be ACCEPTED before work can be submitted

### "Work must be completed before payment"
- Work must be submitted first (influencer)
- Brand must approve the work (click "Approve Work")
- Then payment becomes available

### "This field is required"
- New password field is required
- All password fields must be filled
- Confirm password must match new password

---

## 📞 Support

For issues or questions:
1. Check this guide's troubleshooting section
2. Review the IMPLEMENTATION_GUIDE.md for complete documentation
3. Check browser console for error messages
4. Verify backend is running and database is migrated
5. Check network tab in dev tools for API responses

---

## 📋 Checklist Before Going Live

- [ ] Database migration applied
- [ ] Backend rebuilt and running
- [ ] Frontend rebuilt and serving
- [ ] Test password change functionality
- [ ] Test work submission flow
- [ ] Test payment is blocked before work approval
- [ ] Test payment succeeds after work approval
- [ ] Test payment cannot be made twice
- [ ] Test notifications are sent
- [ ] Test all user roles (influencer, brand, admin)
- [ ] Clear cache and restart browsers

---

## 🎉 You're All Set!

The implementation is complete with:
- ✅ Secure password change
- ✅ Work submission workflow
- ✅ Payment gating on work approval
- ✅ Comprehensive notifications
- ✅ Full frontend/backend integration

Start using these features by following the "How to Use" section above!
