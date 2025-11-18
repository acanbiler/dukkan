# Frontend Order Integration - Strategic Development Plan

**Last Updated: 2025-11-14**

## Executive Summary

This plan outlines the implementation of the frontend order management system for the Dukkan e-commerce platform. With the Order Service backend fully functional (port 8083), the next critical milestone is to connect the frontend shopping experience to enable customers to complete purchases and track their orders.

**Current Status**: Backend complete (Product, User, Order services operational), Frontend partially complete (auth, cart, product browsing), Missing checkout and order history.

**Objective**: Complete the end-to-end shopping experience by implementing checkout flow, order placement, and order history tracking on the frontend.

**Estimated Effort**: 2-3 days (16-24 hours)

**Success Criteria**:
- Customers can checkout from cart
- Orders are placed successfully via Order Service
- Customers can view order history
- Stock is reduced appropriately
- Full integration with i18n (English/Turkish)

---

## Current State Analysis

### Completed Infrastructure

**Backend Services (100% Complete):**
- ✅ Product Service (port 8081) - 29 classes, 28 endpoints, full CRUD
- ✅ User Service (port 8082) - Authentication, JWT, registration
- ✅ Order Service (port 8083) - Order placement, history, cancellation, stock management
- ✅ API Gateway (port 8080) - Routing, CORS, logging, header management

**Frontend (70% Complete):**
- ✅ Authentication: LoginPage, RegisterPage, AuthContext, ProtectedRoute
- ✅ Product Browsing: ProductsPage, ProductDetailPage, search functionality
- ✅ Shopping Cart: CartContext, CartDrawer, CartPage (localStorage persistence)
- ✅ Admin Panel: Product/Category CRUD with protected routes
- ✅ i18n: English/Turkish translations with LanguageSwitcher
- ✅ Layout: Header with user menu, language switcher, navigation

**Database:**
- ✅ PostgreSQL with 3 databases: dukkan_product, dukkan_user, dukkan_order
- ✅ Liquibase migrations for all schema changes
- ✅ Tables: products, categories, users, orders, order_items

### Gaps Identified

**Critical Missing Features:**
1. ❌ Checkout page - No way to place orders from cart
2. ❌ Order history page - Cannot view past orders
3. ❌ Order detail page - No detailed order view
4. ❌ Order types/interfaces - TypeScript definitions missing
5. ❌ Order service client - No API service for orders
6. ❌ Order translations - Missing i18n keys

**Integration Requirements:**
- Cart → Checkout flow connection
- Order placement → Cart clearing logic
- Authentication → Order API header injection (X-User-Id)
- Success notifications and error handling
- Loading states during order operations

### Technical Context

**Key Technologies:**
- React 19.0 with TypeScript
- Mantine UI 8.3.7 for components
- React Router 7.1.1 for navigation
- i18next for internationalization
- Axios for API communication
- React Context API for state management

**Architecture Patterns:**
- Service layer pattern (API calls in services/)
- Context API for global state (AuthContext, CartContext)
- Type-safe interfaces (types/)
- Protected routes for authenticated pages
- Consistent error handling with notifications

**API Contract (Order Service):**
```typescript
POST   /api/v1/orders              - Place order (requires X-User-Id header)
GET    /api/v1/orders/my-orders    - Get user's orders (paginated)
GET    /api/v1/orders/{id}         - Get order by ID
DELETE /api/v1/orders/{id}         - Cancel order
```

---

## Proposed Future State

### Architecture Vision

```
User Flow:
┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌─────────────┐
│ ProductsPage│ →  │  CartPage    │ →  │ CheckoutPage │ →  │ OrdersPage  │
│ (Browse)    │    │  (Review)    │    │  (Place)     │    │  (History)  │
└─────────────┘    └──────────────┘    └──────────────┘    └─────────────┘
                                              ↓                     ↓
                                        Order Service         Order Service
                                        (POST /orders)       (GET /my-orders)
                                              ↓
                                        Stock Reduction
                                        (Product Service)
```

### Component Structure

```
frontend/src/
├── types/
│   └── order.ts                    [NEW] Order, OrderItem, PlaceOrderRequest
├── services/
│   └── orderService.ts             [NEW] API calls for orders
├── pages/
│   ├── CheckoutPage.tsx            [NEW] Order placement
│   ├── OrdersPage.tsx              [NEW] Order history list
│   └── OrderDetailPage.tsx         [NEW] Single order view
├── components/
│   └── orders/
│       ├── OrderCard.tsx           [NEW] Order summary card
│       ├── OrderItemsList.tsx      [NEW] Order items display
│       └── OrderStatusBadge.tsx    [NEW] Status indicator
└── i18n/
    ├── locales/en.json             [UPDATE] Add order translations
    └── locales/tr.json             [UPDATE] Add order translations
```

### User Experience Flow

**1. Checkout Flow:**
- User adds products to cart (existing)
- User navigates to cart page (existing)
- User clicks "Proceed to Checkout" button (new)
- CheckoutPage displays cart summary with total
- User clicks "Place Order" button
- Loading spinner shows during API call
- On success: Order confirmation, cart cleared, redirect to orders
- On error: Error notification with retry option

**2. Order History Flow:**
- User clicks "Orders" in header menu (new link)
- OrdersPage fetches and displays paginated orders
- Each order shown as OrderCard with key info
- User can click order to view details
- OrderDetailPage shows complete order information
- User can cancel order if status allows

**3. Order Management:**
- Real-time status updates
- Stock automatically reduced on placement
- Clear error messages for insufficient stock
- Order numbers for customer reference
- Timestamps in user's locale

---

## Implementation Phases

### Phase 1: Foundation (4-6 hours)

**Goal**: Establish type definitions, API service layer, and translations.

**Tasks:**

#### 1.1 Create TypeScript Types (1 hour)
- **File**: `frontend/src/types/order.ts`
- **Effort**: S
- **Dependencies**: None
- **Description**: Define all TypeScript interfaces for orders
- **Acceptance Criteria**:
  - [ ] OrderItem interface (productId, quantity, price, productName)
  - [ ] PlaceOrderRequest interface (items: OrderItem[])
  - [ ] Order interface (id, orderNumber, status, totalAmount, items, createdAt, updatedAt)
  - [ ] OrderItemDTO interface (backend response format)
  - [ ] PageResponse<Order> interface (for paginated responses)
  - [ ] OrderStatus enum (PENDING, CONFIRMED, CANCELLED, etc.)
  - [ ] All types exported and documented with JSDoc comments

#### 1.2 Implement Order Service (2 hours)
- **File**: `frontend/src/services/orderService.ts`
- **Effort**: M
- **Dependencies**: Task 1.1 (types)
- **Description**: Create API service for order operations
- **Acceptance Criteria**:
  - [ ] placeOrder() method - POST to /api/v1/orders with X-User-Id header
  - [ ] getMyOrders() method - GET paginated orders with X-User-Id header
  - [ ] getOrderById() method - GET single order by ID
  - [ ] cancelOrder() method - DELETE order by ID
  - [ ] All methods use authService.getToken() and authService.getUser()
  - [ ] Proper error handling with try-catch
  - [ ] TypeScript return types match interfaces
  - [ ] Axios interceptors configured for auth headers

#### 1.3 Add Order Translations (1 hour)
- **Files**: `frontend/src/i18n/locales/en.json`, `frontend/src/i18n/locales/tr.json`
- **Effort**: S
- **Dependencies**: None
- **Description**: Add all order-related translation keys
- **Acceptance Criteria**:
  - [ ] English translations complete (orders.* keys)
  - [ ] Turkish translations complete (orders.* keys)
  - [ ] Keys include: title, placeOrder, orderNumber, status, total, items, noOrders, orderDate, cancelOrder, confirmCancel, cancelSuccess, placeSuccess, placeError
  - [ ] Status translations for all order statuses
  - [ ] Form validation messages
  - [ ] Loading and error messages

#### 1.4 Create Order Status Badge Component (1 hour)
- **File**: `frontend/src/components/orders/OrderStatusBadge.tsx`
- **Effort**: S
- **Dependencies**: Task 1.3 (translations)
- **Description**: Reusable status indicator component
- **Acceptance Criteria**:
  - [ ] Uses Mantine Badge component
  - [ ] Color coding: PENDING (yellow), CONFIRMED (green), CANCELLED (red), COMPLETED (blue)
  - [ ] Translates status text using i18next
  - [ ] Props: status (OrderStatus), size (optional)
  - [ ] Accessible with proper ARIA labels
  - [ ] Responsive sizing

---

### Phase 2: Checkout Implementation (6-8 hours)

**Goal**: Enable users to place orders from their shopping cart.

**Tasks:**

#### 2.1 Create Checkout Page Layout (2 hours)
- **File**: `frontend/src/pages/CheckoutPage.tsx`
- **Effort**: M
- **Dependencies**: Phase 1 complete
- **Description**: Build checkout page UI structure
- **Acceptance Criteria**:
  - [ ] Uses Mantine Container, Paper, Title components
  - [ ] Shows page title with translation
  - [ ] Displays cart items summary (product names, quantities, prices)
  - [ ] Shows subtotal, tax (if applicable), and total amount
  - [ ] "Place Order" button prominently displayed
  - [ ] "Back to Cart" button for navigation
  - [ ] Empty cart state with message
  - [ ] Responsive layout for mobile/desktop
  - [ ] Loading skeleton while cart data loads

#### 2.2 Implement Order Placement Logic (2 hours)
- **File**: `frontend/src/pages/CheckoutPage.tsx`
- **Effort**: M
- **Dependencies**: Task 2.1
- **Description**: Connect checkout to Order Service
- **Acceptance Criteria**:
  - [ ] Fetch cart items from CartContext
  - [ ] Transform cart items to PlaceOrderRequest format
  - [ ] Call orderService.placeOrder() on button click
  - [ ] Show loading state during API call (disable button, show spinner)
  - [ ] Handle success: show notification, clear cart, redirect to /orders
  - [ ] Handle errors: show error notification with specific message
  - [ ] Handle insufficient stock error specifically
  - [ ] Prevent duplicate submissions
  - [ ] Require user authentication (redirect to login if not authenticated)

#### 2.3 Integrate with Cart Context (1 hour)
- **File**: `frontend/src/pages/CheckoutPage.tsx`, `frontend/src/context/CartContext.tsx`
- **Effort**: S
- **Dependencies**: Task 2.2
- **Description**: Ensure proper cart clearing after order
- **Acceptance Criteria**:
  - [ ] Call clearCart() from CartContext on successful order
  - [ ] Update cart count in header immediately
  - [ ] Cart drawer shows empty state after order
  - [ ] Cart page redirects or shows empty state
  - [ ] No residual cart data in localStorage after clearing

#### 2.4 Add Checkout Route and Navigation (1 hour)
- **Files**: `frontend/src/App.tsx`, `frontend/src/pages/CartPage.tsx`, `frontend/src/components/layout/Header.tsx`
- **Effort**: S
- **Dependencies**: Task 2.1
- **Description**: Wire up checkout page to application
- **Acceptance Criteria**:
  - [ ] Add /checkout route to App.tsx with ProtectedRoute
  - [ ] Add "Proceed to Checkout" button to CartPage
  - [ ] Button disabled if cart is empty
  - [ ] Button navigates to /checkout route
  - [ ] Add "Checkout" link to header (optional, for quick access)
  - [ ] Ensure checkout page is only accessible when logged in

#### 2.5 Add Order Confirmation UI (1-2 hours)
- **File**: `frontend/src/pages/CheckoutPage.tsx` or create `OrderConfirmationPage.tsx`
- **Effort**: M
- **Dependencies**: Task 2.2
- **Description**: Show order confirmation after successful placement
- **Acceptance Criteria**:
  - [ ] Display success message with order number
  - [ ] Show order summary (items, total)
  - [ ] "View Orders" button navigating to /orders
  - [ ] "Continue Shopping" button navigating to /products
  - [ ] Thank you message with translation
  - [ ] Confetti or success animation (optional, nice-to-have)

---

### Phase 3: Order History (4-6 hours)

**Goal**: Display user's order history with pagination and filtering.

**Tasks:**

#### 3.1 Create Order Card Component (2 hours)
- **File**: `frontend/src/components/orders/OrderCard.tsx`
- **Effort**: M
- **Dependencies**: Phase 1 complete
- **Description**: Individual order display component
- **Acceptance Criteria**:
  - [ ] Uses Mantine Card component
  - [ ] Displays order number, date, status, total
  - [ ] Shows abbreviated item list (first 3 items + "X more")
  - [ ] Includes OrderStatusBadge component
  - [ ] Clickable to navigate to order detail
  - [ ] Responsive layout
  - [ ] Hover effect for better UX
  - [ ] Props: order (Order), onClick (optional)

#### 3.2 Create Orders Page (2-3 hours)
- **File**: `frontend/src/pages/OrdersPage.tsx`
- **Effort**: M
- **Dependencies**: Task 3.1
- **Description**: Order history list page
- **Acceptance Criteria**:
  - [ ] Fetch orders using orderService.getMyOrders()
  - [ ] Display orders in list/grid layout using OrderCard
  - [ ] Show loading spinner while fetching
  - [ ] Empty state when no orders exist
  - [ ] Error state with retry button
  - [ ] Pagination controls (Mantine Pagination component)
  - [ ] Default page size: 10 orders
  - [ ] Page title with translation
  - [ ] Total order count display

#### 3.3 Add Order Detail Page (2 hours)
- **File**: `frontend/src/pages/OrderDetailPage.tsx`
- **Effort**: M
- **Dependencies**: Task 3.1
- **Description**: Detailed view of single order
- **Acceptance Criteria**:
  - [ ] Fetch order by ID from URL params
  - [ ] Display all order information (number, status, date, total)
  - [ ] List all order items with quantities and prices
  - [ ] Show OrderStatusBadge
  - [ ] "Cancel Order" button (if status allows - PENDING only)
  - [ ] Confirmation modal before cancellation
  - [ ] "Back to Orders" button
  - [ ] Loading and error states
  - [ ] Breadcrumb navigation (optional)

#### 3.4 Implement Order Cancellation (1 hour)
- **File**: `frontend/src/pages/OrderDetailPage.tsx`
- **Effort**: S
- **Dependencies**: Task 3.3
- **Description**: Allow users to cancel pending orders
- **Acceptance Criteria**:
  - [ ] "Cancel Order" button only visible for PENDING status
  - [ ] Show confirmation modal with warning
  - [ ] Call orderService.cancelOrder() on confirm
  - [ ] Update UI to show CANCELLED status
  - [ ] Show success notification
  - [ ] Handle errors gracefully
  - [ ] Disable cancel button during API call
  - [ ] Redirect to orders list after cancellation (optional)

#### 3.5 Add Orders Route and Navigation (1 hour)
- **Files**: `frontend/src/App.tsx`, `frontend/src/components/layout/Header.tsx`
- **Effort**: S
- **Dependencies**: Task 3.2
- **Description**: Wire up order pages to application
- **Acceptance Criteria**:
  - [ ] Add /orders route to App.tsx with ProtectedRoute
  - [ ] Add /orders/:id route for order details
  - [ ] Add "Orders" link to header navigation
  - [ ] Orders link visible only when logged in
  - [ ] Active state styling for orders link
  - [ ] Mobile menu includes orders link

---

### Phase 4: Polish & Testing (4-6 hours)

**Goal**: Refine user experience, handle edge cases, and ensure quality.

**Tasks:**

#### 4.1 Enhance Loading States (1 hour)
- **Files**: All order pages
- **Effort**: S
- **Dependencies**: Phases 2-3 complete
- **Description**: Improve loading indicators
- **Acceptance Criteria**:
  - [ ] Replace spinners with Mantine Skeleton components where appropriate
  - [ ] Add loading overlay for page-level operations
  - [ ] Skeleton cards in OrdersPage while loading
  - [ ] Button loading states (spinner + disabled)
  - [ ] Smooth transitions between loading and content
  - [ ] Consistent loading patterns across all pages

#### 4.2 Implement Error Handling (1-2 hours)
- **Files**: All order pages and orderService
- **Effort**: M
- **Dependencies**: Phases 2-3 complete
- **Description**: Robust error handling for all scenarios
- **Acceptance Criteria**:
  - [ ] Network error handling (offline detection)
  - [ ] API error responses displayed to user
  - [ ] 401 Unauthorized → redirect to login
  - [ ] 404 Not Found → appropriate message
  - [ ] 500 Server Error → retry option
  - [ ] Insufficient stock error → specific message with product details
  - [ ] Validation error handling
  - [ ] Timeout handling for slow requests

#### 4.3 Add Form Validation (1 hour)
- **File**: `frontend/src/pages/CheckoutPage.tsx`
- **Effort**: S
- **Dependencies**: Phase 2 complete
- **Description**: Validate checkout process
- **Acceptance Criteria**:
  - [ ] Validate cart not empty before checkout
  - [ ] Validate user is authenticated
  - [ ] Validate all cart items have valid product IDs
  - [ ] Validate quantities are positive integers
  - [ ] Show validation errors with clear messages
  - [ ] Prevent submission if validation fails
  - [ ] Client-side validation before API call

#### 4.4 Optimize Performance (1 hour)
- **Files**: All order components
- **Effort**: S
- **Dependencies**: All phases complete
- **Description**: Performance optimization
- **Acceptance Criteria**:
  - [ ] Use React.memo() for OrderCard component
  - [ ] Implement useMemo for expensive calculations (totals)
  - [ ] Lazy load OrderDetailPage route
  - [ ] Debounce search/filter operations (if added)
  - [ ] Optimize image loading (if product images included)
  - [ ] Code splitting for order pages

#### 4.5 Mobile Responsiveness (1 hour)
- **Files**: All order pages
- **Effort**: S
- **Dependencies**: Phases 2-3 complete
- **Description**: Ensure mobile-friendly design
- **Acceptance Criteria**:
  - [ ] Test on mobile viewport (375px)
  - [ ] Checkout page readable on small screens
  - [ ] Order cards stack properly on mobile
  - [ ] Buttons touch-friendly (min 44px height)
  - [ ] Tables replaced with cards on mobile (if any)
  - [ ] Header navigation collapsed on mobile
  - [ ] Pagination controls mobile-optimized

#### 4.6 Add Manual Testing Checklist (1 hour)
- **File**: Create manual test scenarios document
- **Effort**: S
- **Dependencies**: All phases complete
- **Description**: Comprehensive manual testing
- **Test Scenarios**:
  - [ ] Place order with single item
  - [ ] Place order with multiple items
  - [ ] Place order with out-of-stock item (should fail)
  - [ ] View order history (empty state)
  - [ ] View order history (with orders)
  - [ ] View order details
  - [ ] Cancel pending order
  - [ ] Try to cancel confirmed order (should be disabled)
  - [ ] Checkout without login (should redirect)
  - [ ] Switch languages on all pages
  - [ ] Test on mobile device
  - [ ] Test with slow network (throttling)
  - [ ] Test with network offline

---

## Risk Assessment and Mitigation Strategies

### High-Priority Risks

#### Risk 1: Order Service Availability
**Severity**: High | **Likelihood**: Medium

**Description**: Order Service (port 8083) may not be running or accessible.

**Impact**: Complete order functionality breakdown, customers cannot checkout.

**Mitigation**:
- Add service health check before order operations
- Display maintenance message if service unavailable
- Implement retry logic with exponential backoff
- Provide clear error messages directing users to try again later
- Monitor service status in real-time

**Contingency**: Fallback to "Order Failed" page with contact information.

---

#### Risk 2: Stock Synchronization Issues
**Severity**: High | **Likelihood**: Medium

**Description**: Race condition between multiple users ordering the same product.

**Impact**: Overselling products, customer dissatisfaction, inventory errors.

**Mitigation**:
- Backend handles stock locking (already implemented)
- Frontend shows real-time stock levels (refresh before checkout)
- Clear error message when stock insufficient
- Suggest alternative products or notify when back in stock
- Add "Verify Availability" step before final order

**Contingency**: Manual order review process for edge cases.

---

#### Risk 3: Authentication Token Expiration
**Severity**: Medium | **Likelihood**: High

**Description**: JWT token expires during checkout process.

**Impact**: Order submission fails, user loses cart and must login again.

**Mitigation**:
- Implement token refresh logic in authService
- Check token validity before order submission
- Auto-redirect to login with return URL
- Preserve cart in localStorage during re-auth
- Show remaining session time (optional)

**Contingency**: User re-logs in and cart is restored from localStorage.

---

#### Risk 4: Cart Clearing Race Condition
**Severity**: Medium | **Likelihood**: Low

**Description**: Cart clears before order confirmation, or doesn't clear on success.

**Impact**: Duplicate orders or cart persists after checkout.

**Mitigation**:
- Clear cart only after successful API response (status 200/201)
- Use atomic operations for cart updates
- Add "Processing" flag to prevent duplicate submissions
- Log cart state changes for debugging
- Implement optimistic UI updates with rollback

**Contingency**: Backend prevents duplicate orders via idempotency keys.

---

#### Risk 5: Translation Completeness
**Severity**: Low | **Likelihood**: Medium

**Description**: Missing translations cause UI to show translation keys instead of text.

**Impact**: Poor user experience, especially for Turkish users.

**Mitigation**:
- Complete all translations before deployment
- Fallback to English if Turkish translation missing
- Automated translation validation script
- i18n linter in pre-commit hook
- Regular translation audits

**Contingency**: English fallback always available.

---

### Medium-Priority Risks

#### Risk 6: Performance Degradation with Large Order History
**Severity**: Medium | **Likelihood**: Medium

**Description**: Loading hundreds of orders causes slow page load and poor UX.

**Mitigation**:
- Implement pagination (10 orders per page)
- Use virtual scrolling for very long lists
- Add filters (date range, status) to reduce data
- Cache order list in memory (short-term)
- Optimize backend queries with proper indexing

---

#### Risk 7: Mobile Usability Issues
**Severity**: Medium | **Likelihood**: Medium

**Description**: Checkout and order pages not optimized for mobile screens.

**Mitigation**:
- Test all pages on mobile viewports (375px, 768px)
- Use responsive Mantine components
- Simplify mobile layout (vertical stacking)
- Touch-friendly buttons (44px minimum)
- Regular mobile testing during development

---

### Low-Priority Risks

#### Risk 8: Browser Compatibility
**Severity**: Low | **Likelihood**: Low

**Description**: Modern JavaScript features not supported in older browsers.

**Mitigation**:
- Vite transpiles to ES6+
- Target modern browsers (last 2 versions)
- Add polyfills if needed for specific features
- Graceful degradation for unsupported features

---

#### Risk 9: Localization Format Issues
**Severity**: Low | **Likelihood**: Medium

**Description**: Date, currency, number formats differ between locales.

**Impact**: Confusing display for Turkish users (dates, prices).

**Mitigation**:
- Use i18next formatting functions
- Use Intl.DateTimeFormat for dates
- Use Intl.NumberFormat for currency
- Test with both locales regularly
- Document formatting standards

---

## Success Metrics

### Functional Metrics

**Primary Goals:**
- [ ] 100% of checkout flows complete successfully (excluding stock errors)
- [ ] Order placement API success rate > 99%
- [ ] Order history loads in < 1 second
- [ ] Zero duplicate orders
- [ ] Cart clears correctly 100% of time

**User Experience:**
- [ ] Average checkout time < 2 minutes
- [ ] Order history page load < 1 second
- [ ] Mobile usability score > 90 (Google Lighthouse)
- [ ] Zero critical accessibility issues
- [ ] All translations complete (100% coverage)

**Code Quality:**
- [ ] All TypeScript types defined
- [ ] Zero TypeScript errors
- [ ] Zero ESLint warnings
- [ ] All components follow Mantine patterns
- [ ] Consistent error handling across pages

---

### Technical Metrics

**Performance:**
- [ ] Order placement API call < 500ms (p95)
- [ ] Order history API call < 300ms (p95)
- [ ] Frontend bundle size increase < 50KB
- [ ] First Contentful Paint < 1.5s
- [ ] Time to Interactive < 3s

**Reliability:**
- [ ] Order Service uptime > 99.5%
- [ ] API error rate < 1%
- [ ] Frontend crash rate < 0.1%
- [ ] Stock synchronization accuracy > 99.9%

**Security:**
- [ ] All order APIs require authentication
- [ ] X-User-Id header validated on backend
- [ ] JWT tokens verified on all requests
- [ ] No XSS vulnerabilities in order pages
- [ ] No sensitive data in localStorage (token only)

---

## Required Resources and Dependencies

### Human Resources

**Frontend Developer** (Primary):
- React 19 expertise
- TypeScript proficiency
- Mantine UI experience
- i18n implementation knowledge
- REST API integration skills

**Backend Developer** (Support):
- Available for Order Service issues
- API contract questions
- Database query optimization

**QA Tester** (Nice to Have):
- Manual testing of checkout flow
- Mobile device testing
- Cross-browser testing

### Technical Dependencies

**Running Services:**
- PostgreSQL (port 5432) - Database
- Product Service (port 8081) - Product data
- User Service (port 8082) - Authentication
- Order Service (port 8083) - Order management ⭐ CRITICAL
- API Gateway (port 8080) - Routing

**Development Tools:**
- Node.js 18+ and npm
- VS Code or similar IDE
- React DevTools
- Network debugging tools (Postman, curl)

**External Dependencies:**
- No new npm packages required
- All dependencies already in package.json
- Mantine components (already installed)
- i18next (already configured)

### Knowledge Requirements

**Must Know:**
- React Context API (CartContext, AuthContext)
- React Router navigation
- Axios API calls with interceptors
- Mantine component library
- i18next translation function

**Nice to Know:**
- Spring Boot microservices architecture
- JWT authentication flow
- PostgreSQL query optimization
- Docker Compose orchestration

### Documentation References

**Essential Reading:**
- `NEXT_SESSION.md` - Current status and immediate next steps
- `ORDER_SERVICE_COMPLETE.md` - Order Service API documentation
- `INTEGRATION_COMPLETE.md` - Auth & i18n integration guide
- `I18N_GUIDE.md` - How to add translations
- `frontend/README.md` - Frontend architecture and patterns

**Code References:**
- `frontend/src/context/CartContext.tsx` - Cart state management pattern
- `frontend/src/services/authService.ts` - API service pattern
- `frontend/src/pages/CartPage.tsx` - Page component pattern
- `frontend/src/components/layout/Header.tsx` - Navigation integration

---

## Timeline Estimates

### Detailed Breakdown

**Phase 1 - Foundation** (Day 1 Morning)
- Task 1.1: Types - 1 hour
- Task 1.2: Service - 2 hours
- Task 1.3: Translations - 1 hour
- Task 1.4: Badge Component - 1 hour
- **Total: 5 hours**

**Phase 2 - Checkout** (Day 1 Afternoon + Day 2 Morning)
- Task 2.1: Layout - 2 hours
- Task 2.2: Logic - 2 hours
- Task 2.3: Cart Integration - 1 hour
- Task 2.4: Routing - 1 hour
- Task 2.5: Confirmation - 2 hours
- **Total: 8 hours**

**Phase 3 - Order History** (Day 2 Afternoon)
- Task 3.1: Order Card - 2 hours
- Task 3.2: Orders Page - 3 hours
- Task 3.3: Detail Page - 2 hours
- Task 3.4: Cancellation - 1 hour
- Task 3.5: Routing - 1 hour
- **Total: 9 hours**

**Phase 4 - Polish** (Day 3)
- Task 4.1: Loading States - 1 hour
- Task 4.2: Error Handling - 2 hours
- Task 4.3: Validation - 1 hour
- Task 4.4: Performance - 1 hour
- Task 4.5: Responsiveness - 1 hour
- Task 4.6: Testing - 1 hour
- **Total: 7 hours**

### Critical Path

```
Day 1 (8 hours):
├─ Morning (4 hours): Phase 1 (Foundation)
└─ Afternoon (4 hours): Phase 2 start (Checkout UI)

Day 2 (8 hours):
├─ Morning (4 hours): Phase 2 complete (Checkout Logic)
└─ Afternoon (4 hours): Phase 3 start (Order History)

Day 3 (6 hours):
├─ Morning (4 hours): Phase 3 complete (Order History)
└─ Afternoon (2 hours): Phase 4 start (Polish)

Day 4 (Optional - 2 hours):
└─ Phase 4 complete + Testing
```

**Milestones:**
- End of Day 1: Checkout page exists and renders
- End of Day 2: Orders can be placed successfully
- End of Day 3: Order history fully functional
- End of Day 4: Production-ready with testing complete

---

## Implementation Notes

### Code Style and Patterns

**Follow Existing Patterns:**
```typescript
// Service pattern (see authService.ts)
export const orderService = {
  placeOrder: async (request: PlaceOrderRequest): Promise<Order> => {
    // Implementation
  }
};

// Page component pattern (see CartPage.tsx)
export const OrdersPage = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(false);
  const { t } = useTranslation();

  // useEffect for data fetching
  // Handler functions
  // JSX return
};

// Protected route usage
<Route path="/orders" element={
  <ProtectedRoute>
    <OrdersPage />
  </ProtectedRoute>
} />
```

### API Integration Best Practices

**Always Include Headers:**
```typescript
const user = authService.getUser();
const token = authService.getToken();

const response = await axios.post('/orders', request, {
  headers: {
    'X-User-Id': user.userId,
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

**Error Handling Pattern:**
```typescript
try {
  setLoading(true);
  const result = await orderService.placeOrder(request);
  notifications.show({
    title: t('orders.success'),
    message: t('orders.orderPlaced'),
    color: 'green'
  });
  navigate('/orders');
} catch (error) {
  notifications.show({
    title: t('common.error'),
    message: error.response?.data?.message || t('orders.error'),
    color: 'red'
  });
} finally {
  setLoading(false);
}
```

### Translation Usage

**Always use t() function:**
```typescript
// Good
<Title>{t('orders.title')}</Title>
<Button>{t('orders.placeOrder')}</Button>

// Bad
<Title>My Orders</Title>
<Button>Place Order</Button>
```

**Add keys to both locales:**
```json
// en.json
{
  "orders": {
    "title": "My Orders",
    "placeOrder": "Place Order"
  }
}

// tr.json
{
  "orders": {
    "title": "Siparişlerim",
    "placeOrder": "Sipariş Ver"
  }
}
```

---

## Testing Strategy

### Manual Testing Checklist

**Pre-Deployment Testing:**

**Checkout Flow:**
- [ ] Place order with 1 item
- [ ] Place order with 5+ items
- [ ] Place order with out-of-stock item (should fail gracefully)
- [ ] Verify cart clears after successful order
- [ ] Verify order appears in history immediately
- [ ] Test checkout without login (should redirect)
- [ ] Test with expired token (should re-authenticate)

**Order History:**
- [ ] View empty order history (new user)
- [ ] View order history with 1 order
- [ ] View order history with 20+ orders (pagination)
- [ ] Click on order card navigates to detail
- [ ] Order status badge shows correct color
- [ ] Orders sorted by date (newest first)

**Order Details:**
- [ ] All order information displays correctly
- [ ] Order items list shows all products
- [ ] Total amount matches sum of items
- [ ] Cancel button only visible for PENDING orders
- [ ] Cancel order succeeds and updates status
- [ ] Cannot cancel CONFIRMED or CANCELLED orders

**Internationalization:**
- [ ] Switch to Turkish - all text translates
- [ ] Switch back to English - all text translates
- [ ] Language persists after page refresh
- [ ] Dates format correctly for locale
- [ ] Currency format matches locale (if applicable)

**Mobile Testing:**
- [ ] Checkout page readable on iPhone SE (375px)
- [ ] Orders page scrollable and card layout works
- [ ] Order detail readable on mobile
- [ ] Buttons touch-friendly (44px min)
- [ ] Navigation menu accessible on mobile

**Error Scenarios:**
- [ ] Network offline - shows error message
- [ ] API returns 500 - shows error with retry
- [ ] Insufficient stock - clear error message with product name
- [ ] Invalid product ID - handled gracefully
- [ ] Order Service down - maintenance message

**Performance:**
- [ ] Checkout loads in < 2 seconds
- [ ] Order history loads in < 1 second (10 orders)
- [ ] No visible lag when placing order
- [ ] Smooth transitions and animations
- [ ] No console errors or warnings

### Automated Testing (Future)

**Unit Tests (Optional for this phase):**
- orderService.test.ts - Mock axios calls
- OrderCard.test.tsx - Component rendering
- CheckoutPage.test.tsx - User interactions

**Integration Tests (Optional for this phase):**
- Full checkout flow (end-to-end)
- Order history pagination
- Order cancellation workflow

---

## Rollback Plan

### If Critical Issues Occur

**Scenario 1: Order Placement Consistently Fails**

**Action Steps:**
1. Add feature flag to disable checkout button
2. Display maintenance message: "Checkout temporarily unavailable"
3. Allow cart browsing and product viewing
4. Investigate Order Service logs
5. Fix issue in Order Service or revert changes
6. Re-enable checkout after verification

**Rollback Command:**
```bash
git revert <commit-hash>
npm run build
# Redeploy frontend
```

---

**Scenario 2: Cart Clearing Issues (Duplicate Orders)**

**Action Steps:**
1. Disable automatic cart clearing
2. Add manual "Clear Cart" button
3. Investigate clearing logic
4. Add confirmation before checkout
5. Implement backend duplicate order prevention
6. Fix cart clearing logic

**Mitigation:**
- Backend validates duplicate orders via order uniqueness check
- Users can manually clear cart if needed
- Order history shows all orders (including duplicates) for transparency

---

**Scenario 3: Authentication Issues Blocking Checkout**

**Action Steps:**
1. Add guest checkout option (future feature)
2. Or disable order requirement for authentication temporarily
3. Or improve token refresh logic
4. Fix authentication flow
5. Re-enable full authentication

**Temporary Fix:**
```typescript
// Allow checkout without strict auth (NOT RECOMMENDED, temp only)
const handleCheckout = async () => {
  try {
    // Skip auth check temporarily
    const result = await orderService.placeOrder(request);
  } catch (error) {
    // Handle error
  }
};
```

---

## Post-Implementation Tasks

### After Phase 4 Complete

**Immediate:**
- [ ] Deploy to staging environment
- [ ] Run full manual test suite
- [ ] Fix any critical bugs found
- [ ] Get stakeholder approval
- [ ] Deploy to production

**Within 1 Week:**
- [ ] Monitor Order Service logs for errors
- [ ] Track order placement success rate
- [ ] Collect user feedback on checkout flow
- [ ] Identify performance bottlenecks
- [ ] Plan UX improvements based on data

**Within 1 Month:**
- [ ] Add automated tests for order flows
- [ ] Implement order email notifications
- [ ] Add order search/filter functionality
- [ ] Optimize order history loading (caching)
- [ ] Add order export feature (CSV/PDF)

---

## Future Enhancements

### Short-Term (Next Sprint)

**Payment Integration:**
- Add payment service (Stripe/PayPal)
- Update checkout flow with payment step
- Handle payment webhooks
- Add payment status to orders

**Order Notifications:**
- Email confirmation on order placement
- Order status change notifications
- SMS notifications (optional)

**Advanced Filtering:**
- Filter orders by status
- Filter orders by date range
- Search orders by order number
- Sort by amount, date, status

### Medium-Term (1-3 Months)

**Order Tracking:**
- Add shipping status to orders
- Track package delivery
- Integration with shipping providers
- Real-time tracking map (optional)

**Order Management (Admin):**
- Admin view of all orders
- Update order status (admin panel)
- Process refunds
- Generate order reports

**Enhanced UX:**
- Save favorite addresses
- One-click reorder
- Order templates for frequent purchases
- Estimated delivery date

### Long-Term (3+ Months)

**Advanced Features:**
- Subscription orders (recurring)
- Pre-orders for upcoming products
- Split payments (multiple payment methods)
- Order gift wrapping options
- Digital product downloads

**Analytics:**
- Order analytics dashboard
- Revenue reporting
- Customer order patterns
- Product popularity metrics

---

## Appendix

### Glossary

- **Order Service**: Backend microservice (port 8083) handling order operations
- **X-User-Id Header**: Custom header containing authenticated user ID for API requests
- **PlaceOrderRequest**: DTO containing array of order items to be placed
- **OrderStatus**: Enum representing order lifecycle (PENDING, CONFIRMED, CANCELLED, COMPLETED)
- **CartContext**: React Context managing shopping cart state
- **AuthContext**: React Context managing authentication state
- **i18n/i18next**: Internationalization library for multi-language support
- **Mantine UI**: React component library used for UI elements

### API Endpoints Reference

**Order Service (via API Gateway):**

```
POST   /api/v1/orders
Body:  { "items": [{ "productId": "uuid", "quantity": 2 }] }
Headers: X-User-Id, Authorization
Response: Order object with orderNumber, status, totalAmount

GET    /api/v1/orders/my-orders?page=0&size=10
Headers: X-User-Id, Authorization
Response: PageResponse<Order> with paginated orders

GET    /api/v1/orders/{orderId}
Headers: X-User-Id, Authorization
Response: Single Order object

DELETE /api/v1/orders/{orderId}
Headers: X-User-Id, Authorization
Response: Success message
```

### Component Hierarchy

```
App
├── Header (with Orders link)
├── Routes
│   ├── /checkout
│   │   └── CheckoutPage
│   │       ├── Cart Summary
│   │       ├── Order Total
│   │       └── Place Order Button
│   ├── /orders
│   │   └── OrdersPage
│   │       ├── OrderCard (repeated)
│   │       │   └── OrderStatusBadge
│   │       └── Pagination
│   └── /orders/:id
│       └── OrderDetailPage
│           ├── Order Information
│           ├── OrderItemsList
│           ├── OrderStatusBadge
│           └── Cancel Order Button
```

### File Structure Summary

```
frontend/src/
├── types/
│   └── order.ts                           [NEW]
├── services/
│   └── orderService.ts                    [NEW]
├── pages/
│   ├── CheckoutPage.tsx                   [NEW]
│   ├── OrdersPage.tsx                     [NEW]
│   └── OrderDetailPage.tsx                [NEW]
├── components/
│   └── orders/
│       ├── OrderCard.tsx                  [NEW]
│       ├── OrderItemsList.tsx             [NEW]
│       └── OrderStatusBadge.tsx           [NEW]
├── i18n/
│   └── locales/
│       ├── en.json                        [UPDATE]
│       └── tr.json                        [UPDATE]
├── App.tsx                                [UPDATE - Add routes]
├── context/CartContext.tsx                [UPDATE - Export clearCart]
└── components/layout/
    ├── Header.tsx                         [UPDATE - Add Orders link]
    └── CartPage.tsx                       [UPDATE - Add Checkout button]
```

---

## Quick Start Guide for Implementation

### Getting Started Checklist

**Before Starting:**
- [ ] Read this entire plan document
- [ ] Read `NEXT_SESSION.md` for current status
- [ ] Read `ORDER_SERVICE_COMPLETE.md` for API docs
- [ ] Ensure all services running (see start commands below)
- [ ] Test existing cart functionality works
- [ ] Verify authentication works (login/register)

**Start All Services:**
```bash
# Terminal 1 - Database
docker compose up -d postgres

# Terminal 2 - Product Service
cd backend/product-service && mvn spring-boot:run

# Terminal 3 - User Service
cd backend/user-service && mvn spring-boot:run

# Terminal 4 - Order Service ⭐
cd backend/order-service && mvn spring-boot:run

# Terminal 5 - API Gateway
cd backend/api-gateway && mvn spring-boot:run

# Terminal 6 - Frontend
cd frontend && npm run dev
```

**Verify Services Running:**
```bash
# Check all services respond
curl http://localhost:8080/health          # API Gateway
curl http://localhost:8081/actuator/health # Product Service
curl http://localhost:8082/actuator/health # User Service
curl http://localhost:8083/actuator/health # Order Service ⭐
```

**Recommended Implementation Order:**
1. Start with Phase 1 (Foundation) - Set up types and service
2. Then Phase 2 (Checkout) - Get order placement working
3. Then Phase 3 (Order History) - View past orders
4. Finally Phase 4 (Polish) - Refine and test

**Development Tips:**
- Use React DevTools to inspect context state
- Use Network tab to debug API calls
- Test with both English and Turkish languages
- Test on mobile viewport regularly
- Commit after each phase completion

---

**Last Updated**: 2025-11-14
**Status**: Ready for Implementation
**Estimated Completion**: 2-3 days (16-24 hours)
