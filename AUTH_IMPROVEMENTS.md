# Authentication System - Future Improvements

## Current Status

✅ **Completed Features:**
- User registration with email/password
- Login with JWT token generation
- BCrypt password hashing
- Role-based access control (CUSTOMER, ADMIN)
- Token storage in localStorage
- AuthContext for global state management
- Login and Register pages
- API Gateway routing for auth endpoints

## Priority 1: Essential Security Improvements

### 1. Admin Route Protection
**Status**: ❌ Not Implemented
**Priority**: HIGH
**Effort**: Low

**What's needed:**
- Create `ProtectedRoute` component for frontend
- Check `isAuthenticated` and `isAdmin` before rendering admin pages
- Redirect to `/login` if unauthorized
- Add backend validation in API Gateway or services

**Implementation:**
```tsx
// frontend/src/components/ProtectedRoute.tsx
const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/" />;
  }

  return <>{children}</>;
};
```

**Backend:**
- Add JWT filter to API Gateway
- Validate token on protected routes
- Extract role from token and check permissions

---

### 2. Token Refresh Mechanism
**Status**: ❌ Not Implemented
**Priority**: HIGH
**Effort**: Medium

**Current Issue:**
- Tokens expire after 24 hours
- Users must login again
- No automatic token renewal

**What's needed:**
- Add refresh token endpoint to User Service
- Store refresh token (longer expiration, e.g., 7 days)
- Auto-refresh access token before expiration
- Add axios interceptor to retry failed requests with new token

**Implementation Steps:**
1. Add `RefreshToken` entity in User Service
2. Create `POST /api/v1/auth/refresh` endpoint
3. Return both access token and refresh token on login/register
4. Add axios interceptor on 401 response to auto-refresh
5. Store refresh token in httpOnly cookie (more secure)

**Files to modify:**
- `backend/user-service/src/main/java/com/dukkan/user/model/RefreshToken.java`
- `backend/user-service/src/main/java/com/dukkan/user/service/AuthService.java`
- `backend/user-service/src/main/java/com/dukkan/user/controller/AuthController.java`
- `frontend/src/services/api.ts` (add interceptor)

---

### 3. Email Verification
**Status**: ❌ Not Implemented
**Priority**: MEDIUM
**Effort**: Medium-High

**Current Issue:**
- Users can register with any email
- No verification that email is valid
- `emailVerified` flag exists but not used

**What's needed:**
- Email service integration (SendGrid, AWS SES, or SMTP)
- Generate verification token on registration
- Send verification email with link
- Create email verification endpoint
- Block certain actions until verified

**Implementation Steps:**
1. Add email service dependency (Spring Mail or SendGrid)
2. Create `EmailService` for sending emails
3. Generate verification token (UUID or JWT)
4. Store token with expiration
5. Create `GET /api/v1/auth/verify-email?token=xxx` endpoint
6. Send email with verification link
7. Update `emailVerified` flag on verification

**New Services:**
- `backend/user-service/src/main/java/com/dukkan/user/service/EmailService.java`
- Email templates in `src/main/resources/templates/`

**Configuration:**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
```

---

### 4. Password Reset
**Status**: ❌ Not Implemented
**Priority**: MEDIUM
**Effort**: Medium

**What's needed:**
- "Forgot Password" link on login page
- Password reset request endpoint
- Email with reset link
- Password reset form
- Token-based password reset

**Implementation Steps:**
1. Create `PasswordResetToken` entity with expiration
2. Add `POST /api/v1/auth/forgot-password` endpoint (send email)
3. Add `POST /api/v1/auth/reset-password` endpoint (validate token + update password)
4. Create frontend "Forgot Password" page
5. Create frontend "Reset Password" page with token validation

**New Pages:**
- `frontend/src/pages/auth/ForgotPasswordPage.tsx`
- `frontend/src/pages/auth/ResetPasswordPage.tsx`

---

## Priority 2: User Experience Improvements

### 5. "Remember Me" Functionality
**Status**: ❌ Not Implemented
**Priority**: LOW
**Effort**: Low

**What's needed:**
- Checkbox on login form
- Extend token expiration if "Remember Me" is checked
- Store preference in localStorage

---

### 6. Social Login (OAuth)
**Status**: ❌ Not Implemented
**Priority**: LOW
**Effort**: High

**Options:**
- Google OAuth 2.0
- Facebook Login
- Apple Sign In

**What's needed:**
- OAuth client configuration
- Redirect URLs
- Spring Security OAuth integration
- Frontend OAuth buttons

---

### 7. Two-Factor Authentication (2FA)
**Status**: ❌ Not Implemented
**Priority**: LOW
**Effort**: High

**Options:**
- TOTP (Google Authenticator, Authy)
- SMS-based OTP
- Email-based OTP

**What's needed:**
- TOTP secret generation
- QR code generation for setup
- OTP validation endpoint
- Frontend 2FA setup and verification pages

---

## Priority 3: Security Hardening

### 8. Rate Limiting
**Status**: ❌ Not Implemented
**Priority**: MEDIUM
**Effort**: Low-Medium

**Current Issue:**
- No protection against brute force attacks
- Unlimited login attempts

**What's needed:**
- Rate limiting on auth endpoints
- IP-based throttling
- Account lockout after failed attempts

**Options:**
- Spring Cloud Gateway rate limiting
- Redis-based rate limiter
- Bucket4j library

**Configuration:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

---

### 9. Password Strength Requirements
**Status**: ⚠️ Partial (min 6 chars only)
**Priority**: MEDIUM
**Effort**: Low

**Current validation:**
- Minimum 6 characters

**Improved validation needed:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character
- Password complexity score (using zxcvbn library)

**Frontend:**
```tsx
import zxcvbn from 'zxcvbn';

const validatePassword = (password: string) => {
  const result = zxcvbn(password);
  return result.score >= 3; // Good or better
};
```

**Backend:**
- Add validation annotation with regex
- Return detailed password requirements in error

---

### 10. CSRF Protection
**Status**: ❌ Not Implemented (disabled in SecurityConfig)
**Priority**: MEDIUM
**Effort**: Low

**Current config:**
```java
http.csrf(csrf -> csrf.disable())
```

**What's needed:**
- Enable CSRF for state-changing operations
- Use CSRF tokens in forms
- Configure cookie-based CSRF

**Note:** With JWT stateless auth, CSRF is less critical but still recommended for defense in depth.

---

### 11. Token Storage Security
**Status**: ⚠️ localStorage (vulnerable to XSS)
**Priority**: MEDIUM
**Effort**: Low-Medium

**Current implementation:**
- JWT stored in localStorage
- Vulnerable to XSS attacks

**Better options:**
1. **httpOnly cookies** (recommended)
   - Not accessible via JavaScript
   - Automatic CSRF protection needed

2. **Memory only** with refresh token in httpOnly cookie
   - Access token in memory
   - Refresh token in secure cookie

**Implementation:**
```java
// Backend: Set token in httpOnly cookie
ResponseCookie cookie = ResponseCookie.from("token", token)
    .httpOnly(true)
    .secure(true)
    .sameSite("Strict")
    .maxAge(Duration.ofDays(1))
    .build();

response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
```

---

## Priority 4: Audit & Compliance

### 12. Login Activity Tracking
**Status**: ❌ Not Implemented
**Priority**: LOW
**Effort**: Medium

**What's needed:**
- `LoginHistory` entity
- Track login attempts (success/failure)
- IP address, user agent, timestamp
- Show user their login history
- Detect suspicious activity

**Schema:**
```sql
CREATE TABLE login_history (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    success BOOLEAN,
    attempted_at TIMESTAMP,
    location VARCHAR(100)
);
```

---

### 13. Account Deactivation/Deletion
**Status**: ⚠️ Partial (isActive flag exists)
**Priority**: LOW
**Effort**: Low

**What's needed:**
- User can request account deletion
- Admin can deactivate accounts
- Soft delete with data retention
- GDPR compliance (right to be forgotten)

**Endpoints:**
- `POST /api/v1/users/me/deactivate`
- `DELETE /api/v1/users/me`
- `POST /api/v1/admin/users/{id}/deactivate`

---

### 14. Password History
**Status**: ❌ Not Implemented
**Priority**: LOW
**Effort**: Low

**What's needed:**
- Prevent password reuse
- Store hash of last 5-10 passwords
- Validate new password doesn't match history

---

## Priority 5: Admin Features

### 15. Admin User Management
**Status**: ❌ Not Implemented
**Priority**: MEDIUM
**Effort**: Medium

**What's needed:**
- List all users (paginated)
- Search users
- View user details
- Deactivate/activate users
- Change user roles
- Delete users

**Endpoints:**
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/users/{id}`
- `PUT /api/v1/admin/users/{id}/role`
- `PUT /api/v1/admin/users/{id}/activate`
- `PUT /api/v1/admin/users/{id}/deactivate`

**Frontend:**
- Admin user management page
- User list with filters
- User detail modal

---

## Implementation Roadmap

### Phase 1: Critical Security (Week 1-2)
1. ✅ Admin route protection (frontend + backend)
2. ✅ Token refresh mechanism
3. ✅ Rate limiting on auth endpoints

### Phase 2: Essential Features (Week 3-4)
4. ✅ Email verification
5. ✅ Password reset
6. ✅ Password strength validation

### Phase 3: Security Hardening (Week 5-6)
7. ✅ httpOnly cookie token storage
8. ✅ CSRF protection
9. ✅ Login activity tracking

### Phase 4: Nice-to-Have (Future)
10. Social login
11. Two-factor authentication
12. Admin user management
13. Password history

---

## Testing Requirements

For each feature:
- [ ] Unit tests (service layer)
- [ ] Integration tests (controller layer)
- [ ] E2E tests (user flows)
- [ ] Security tests (penetration testing)

---

## Documentation Updates Needed

- [ ] Update API documentation (Swagger)
- [ ] Update ARCHITECTURE.md
- [ ] Update CLAUDE.md with auth patterns
- [ ] Create AUTH_GUIDE.md for developers
- [ ] Update frontend README with auth usage

---

## Dependencies to Add

**Backend:**
```xml
<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

**Frontend:**
```json
{
  "zxcvbn": "^4.4.2",
  "js-cookie": "^3.0.5"
}
```

---

## Security Checklist

- [ ] All passwords hashed with BCrypt
- [ ] Tokens signed with strong secret (256+ bits)
- [ ] HTTPS enforced in production
- [ ] Sensitive data never logged
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (JPA handles this)
- [ ] XSS prevention (React handles this)
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Token expiration enforced
- [ ] httpOnly cookies for tokens
- [ ] CSRF protection enabled
- [ ] Security headers configured

---

**Last Updated:** 2025-11-14
**Status:** Authentication v1.0 complete, improvements documented
