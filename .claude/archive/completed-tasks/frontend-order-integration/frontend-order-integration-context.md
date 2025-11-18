# Frontend Order Integration - Context Document

**Last Updated: 2025-11-14**

## Project Context

### Purpose
This document captures key context, decisions, and references for the frontend order integration task. It serves as a quick reference guide for developers picking up this work.

---

## Key Decisions

### Decision 1: Use Existing Patterns
**Date**: 2025-11-14
**Decision**: Follow existing cart and auth patterns for order implementation
**Rationale**: Maintains consistency across codebase, reduces learning curve
**Impact**: Order service follows same structure as authService, order pages follow same patterns as cart pages

### Decision 2: No New Dependencies
**Date**: 2025-11-14
**Decision**: Use only existing npm packages (Mantine, i18next, axios, etc.)
**Rationale**: Avoid dependency bloat, keep bundle size small, use proven tools
**Impact**: All UI with Mantine components, all i18n with i18next, no new libraries needed

### Decision 3: Pagination Default
**Date**: 2025-11-14
**Decision**: Default page size of 10 orders for order history
**Rationale**: Balance between usability and performance, industry standard
**Impact**: Backend already supports pagination, frontend implements Mantine Pagination component

### Decision 4: Protected Routes Only
**Date**: 2025-11-14
**Decision**: All order pages require authentication (ProtectedRoute wrapper)
**Rationale**: Orders are user-specific, must be authenticated to view/place
**Impact**: Unauthenticated users redirected to login, cart saved in localStorage

### Decision 5: Cart Clearing Strategy
**Date**: 2025-11-14
**Decision**: Clear cart only after successful order placement (API response 200/201)
**Rationale**: Prevents cart loss if order fails, ensures order-cart consistency
**Impact**: clearCart() called in success handler, not before API call

---

## Key Files Reference

### Core Implementation Files

**Types & Interfaces:**
- `frontend/src/types/order.ts` - Order, OrderItem, PlaceOrderRequest, OrderStatus

**Services:**
- `frontend/src/services/orderService.ts` - API calls for orders (NEW)
- `frontend/src/services/authService.ts` - Pattern reference for API calls (EXISTING)
- `frontend/src/services/productService.ts` - Pattern reference (EXISTING)

**Pages:**
- `frontend/src/pages/CheckoutPage.tsx` - Order placement page (NEW)
- `frontend/src/pages/OrdersPage.tsx` - Order history list (NEW)
- `frontend/src/pages/OrderDetailPage.tsx` - Single order view (NEW)
- `frontend/src/pages/CartPage.tsx` - Reference for page patterns (EXISTING)

**Components:**
- `frontend/src/components/orders/OrderCard.tsx` - Order summary card (NEW)
- `frontend/src/components/orders/OrderItemsList.tsx` - Order items display (NEW)
- `frontend/src/components/orders/OrderStatusBadge.tsx` - Status indicator (NEW)
- `frontend/src/components/ProtectedRoute.tsx` - Auth protection (EXISTING)

**Context:**
- `frontend/src/context/CartContext.tsx` - Shopping cart state (UPDATE - export clearCart)
- `frontend/src/context/AuthContext.tsx` - Auth state reference (EXISTING)

**Translations:**
- `frontend/src/i18n/locales/en.json` - English translations (UPDATE)
- `frontend/src/i18n/locales/tr.json` - Turkish translations (UPDATE)

**Routing:**
- `frontend/src/App.tsx` - Route definitions (UPDATE - add /checkout, /orders, /orders/:id)

**Layout:**
- `frontend/src/components/layout/Header.tsx` - Navigation (UPDATE - add Orders link)

---

## Dependencies

### Backend Services

**Required Services (Must be Running):**

1. **PostgreSQL** (port 5432)
   - Database: dukkan_product, dukkan_user, dukkan_order
   - Start: `docker compose up -d postgres`

2. **Product Service** (port 8081)
   - Purpose: Product data and stock management
   - Start: `cd backend/product-service && mvn spring-boot:run`

3. **User Service** (port 8082)
   - Purpose: Authentication and user management
   - Start: `cd backend/user-service && mvn spring-boot:run`

4. **Order Service** (port 8083) ⭐ CRITICAL
   - Purpose: Order placement, history, cancellation
   - Start: `cd backend/order-service && mvn spring-boot:run`
   - API Docs: See ORDER_SERVICE_COMPLETE.md

5. **API Gateway** (port 8080)
   - Purpose: Routes requests to services, CORS, logging
   - Start: `cd backend/api-gateway && mvn spring-boot:run`

### Frontend Dependencies (Already Installed)

```json
{
  "react": "^19.0.0",
  "react-dom": "^19.0.0",
  "react-router-dom": "^7.1.1",
  "react-i18next": "^15.1.3",
  "i18next": "^24.2.0",
  "@mantine/core": "^8.3.7",
  "@mantine/hooks": "^8.3.7",
  "@mantine/form": "^8.3.7",
  "@mantine/notifications": "^8.3.7",
  "axios": "^1.7.9"
}
```

No new dependencies required.

---

## API Contract

### Order Service Endpoints (via API Gateway)

**Base URL:** `http://localhost:8080/api/v1`

#### 1. Place Order
```http
POST /orders
Content-Type: application/json
X-User-Id: <user-uuid>
Authorization: Bearer <jwt-token>

Request Body:
{
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    },
    {
      "productId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
      "quantity": 1
    }
  ]
}

Response (201 Created):
{
  "success": true,
  "data": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "orderNumber": "ORD-1731584400000-ABCD",
    "userId": "user-uuid",
    "status": "PENDING",
    "totalAmount": 299.97,
    "items": [
      {
        "id": "item-uuid",
        "productId": "550e8400-e29b-41d4-a716-446655440000",
        "productName": "Product Name",
        "quantity": 2,
        "price": 99.99,
        "subtotal": 199.98
      }
    ],
    "createdAt": "2025-11-14T10:00:00",
    "updatedAt": "2025-11-14T10:00:00"
  },
  "message": "Order placed successfully",
  "timestamp": "2025-11-14T10:00:00"
}

Error Response (400 Bad Request):
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Insufficient stock for product: Product Name",
    "details": ["Available: 1, Requested: 2"]
  },
  "timestamp": "2025-11-14T10:00:00"
}
```

#### 2. Get My Orders (Paginated)
```http
GET /orders/my-orders?page=0&size=10
X-User-Id: <user-uuid>
Authorization: Bearer <jwt-token>

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "order-uuid",
        "orderNumber": "ORD-1731584400000-ABCD",
        "status": "CONFIRMED",
        "totalAmount": 299.97,
        "items": [...],
        "createdAt": "2025-11-14T10:00:00",
        "updatedAt": "2025-11-14T10:00:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 10
  },
  "timestamp": "2025-11-14T10:00:00"
}
```

#### 3. Get Order By ID
```http
GET /orders/{orderId}
X-User-Id: <user-uuid>
Authorization: Bearer <jwt-token>

Response (200 OK):
{
  "success": true,
  "data": {
    "id": "order-uuid",
    "orderNumber": "ORD-1731584400000-ABCD",
    "status": "PENDING",
    "totalAmount": 299.97,
    "items": [
      {
        "id": "item-uuid",
        "productId": "product-uuid",
        "productName": "Product Name",
        "quantity": 2,
        "price": 99.99,
        "subtotal": 199.98
      }
    ],
    "createdAt": "2025-11-14T10:00:00",
    "updatedAt": "2025-11-14T10:00:00"
  },
  "timestamp": "2025-11-14T10:00:00"
}

Error Response (404 Not Found):
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Order not found with id: order-uuid"
  },
  "timestamp": "2025-11-14T10:00:00"
}
```

#### 4. Cancel Order
```http
DELETE /orders/{orderId}
X-User-Id: <user-uuid>
Authorization: Bearer <jwt-token>

Response (200 OK):
{
  "success": true,
  "message": "Order cancelled successfully",
  "timestamp": "2025-11-14T10:00:00"
}

Error Response (400 Bad Request):
{
  "success": false,
  "error": {
    "code": "INVALID_OPERATION",
    "message": "Cannot cancel order in CONFIRMED status"
  },
  "timestamp": "2025-11-14T10:00:00"
}
```

---

## Code Patterns

### API Service Pattern

**File:** `frontend/src/services/orderService.ts`

```typescript
import axios from 'axios';
import { authService } from './authService';
import type { Order, PlaceOrderRequest, PageResponse } from '../types/order';

const API_BASE_URL = '/api/v1';

export const orderService = {
  placeOrder: async (request: PlaceOrderRequest): Promise<Order> => {
    const user = authService.getUser();
    const token = authService.getToken();

    if (!user || !token) {
      throw new Error('User not authenticated');
    }

    const response = await axios.post(`${API_BASE_URL}/orders`, request, {
      headers: {
        'X-User-Id': user.userId,
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    return response.data.data;
  },

  getMyOrders: async (page = 0, size = 10): Promise<PageResponse<Order>> => {
    const user = authService.getUser();
    const token = authService.getToken();

    if (!user || !token) {
      throw new Error('User not authenticated');
    }

    const response = await axios.get(
      `${API_BASE_URL}/orders/my-orders?page=${page}&size=${size}`,
      {
        headers: {
          'X-User-Id': user.userId,
          'Authorization': `Bearer ${token}`
        }
      }
    );

    return response.data.data;
  },

  getOrderById: async (orderId: string): Promise<Order> => {
    const user = authService.getUser();
    const token = authService.getToken();

    if (!user || !token) {
      throw new Error('User not authenticated');
    }

    const response = await axios.get(`${API_BASE_URL}/orders/${orderId}`, {
      headers: {
        'X-User-Id': user.userId,
        'Authorization': `Bearer ${token}`
      }
    });

    return response.data.data;
  },

  cancelOrder: async (orderId: string): Promise<void> => {
    const user = authService.getUser();
    const token = authService.getToken();

    if (!user || !token) {
      throw new Error('User not authenticated');
    }

    await axios.delete(`${API_BASE_URL}/orders/${orderId}`, {
      headers: {
        'X-User-Id': user.userId,
        'Authorization': `Bearer ${token}`
      }
    });
  }
};
```

### Page Component Pattern

**File:** `frontend/src/pages/CheckoutPage.tsx`

```typescript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Container, Title, Button, Loader } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { useCart } from '../context/CartContext';
import { orderService } from '../services/orderService';
import type { PlaceOrderRequest } from '../types/order';

export const CheckoutPage = () => {
  const [loading, setLoading] = useState(false);
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { items, clearCart } = useCart();

  const handlePlaceOrder = async () => {
    if (items.length === 0) {
      notifications.show({
        title: t('common.error'),
        message: t('cart.empty'),
        color: 'red'
      });
      return;
    }

    try {
      setLoading(true);

      const request: PlaceOrderRequest = {
        items: items.map(item => ({
          productId: item.product.id,
          quantity: item.quantity
        }))
      };

      const order = await orderService.placeOrder(request);

      notifications.show({
        title: t('orders.success'),
        message: t('orders.orderPlaced', { orderNumber: order.orderNumber }),
        color: 'green'
      });

      clearCart();
      navigate('/orders');
    } catch (error: any) {
      notifications.show({
        title: t('common.error'),
        message: error.response?.data?.error?.message || t('orders.placeError'),
        color: 'red'
      });
    } finally {
      setLoading(false);
    }
  };

  if (items.length === 0) {
    return (
      <Container size="lg" py="xl">
        <Title order={2}>{t('cart.empty')}</Title>
      </Container>
    );
  }

  return (
    <Container size="lg" py="xl">
      <Title order={2}>{t('orders.checkout')}</Title>
      {/* Cart summary display */}
      <Button
        onClick={handlePlaceOrder}
        loading={loading}
        disabled={loading}
      >
        {t('orders.placeOrder')}
      </Button>
    </Container>
  );
};
```

### Translation Pattern

**Files:** `frontend/src/i18n/locales/en.json`, `frontend/src/i18n/locales/tr.json`

```json
// en.json
{
  "orders": {
    "title": "My Orders",
    "checkout": "Checkout",
    "placeOrder": "Place Order",
    "placeSuccess": "Order placed successfully!",
    "placeError": "Failed to place order",
    "orderNumber": "Order #{{orderNumber}}",
    "status": "Status",
    "total": "Total",
    "items": "Items",
    "noOrders": "No orders yet",
    "orderDate": "Order Date",
    "cancelOrder": "Cancel Order",
    "confirmCancel": "Are you sure you want to cancel this order?",
    "cancelSuccess": "Order cancelled successfully",
    "cancelError": "Failed to cancel order",
    "viewDetails": "View Details",
    "backToOrders": "Back to Orders"
  },
  "status": {
    "PENDING": "Pending",
    "CONFIRMED": "Confirmed",
    "CANCELLED": "Cancelled",
    "COMPLETED": "Completed"
  }
}

// tr.json
{
  "orders": {
    "title": "Siparişlerim",
    "checkout": "Ödeme",
    "placeOrder": "Sipariş Ver",
    "placeSuccess": "Sipariş başarıyla verildi!",
    "placeError": "Sipariş verilemedi",
    "orderNumber": "Sipariş #{{orderNumber}}",
    "status": "Durum",
    "total": "Toplam",
    "items": "Ürünler",
    "noOrders": "Henüz sipariş yok",
    "orderDate": "Sipariş Tarihi",
    "cancelOrder": "Siparişi İptal Et",
    "confirmCancel": "Bu siparişi iptal etmek istediğinizden emin misiniz?",
    "cancelSuccess": "Sipariş başarıyla iptal edildi",
    "cancelError": "Sipariş iptal edilemedi",
    "viewDetails": "Detayları Gör",
    "backToOrders": "Siparişlere Dön"
  },
  "status": {
    "PENDING": "Beklemede",
    "CONFIRMED": "Onaylandı",
    "CANCELLED": "İptal Edildi",
    "COMPLETED": "Tamamlandı"
  }
}
```

---

## Testing Scenarios

### Manual Test Cases

**Test Case 1: Successful Order Placement**
- Preconditions: User logged in, cart has 2+ items, products in stock
- Steps:
  1. Navigate to cart page
  2. Click "Proceed to Checkout"
  3. Verify cart summary displays correctly
  4. Click "Place Order"
  5. Wait for success notification
  6. Verify redirect to /orders
  7. Verify cart is empty
  8. Verify order appears in history
- Expected: Order placed, cart cleared, order visible in history

**Test Case 2: Insufficient Stock Error**
- Preconditions: User logged in, cart has item with insufficient stock
- Steps:
  1. Add product to cart with quantity > available stock
  2. Navigate to checkout
  3. Click "Place Order"
- Expected: Error notification with product name and available stock

**Test Case 3: Unauthenticated Checkout Attempt**
- Preconditions: User not logged in, cart has items
- Steps:
  1. Navigate to /checkout (direct URL)
- Expected: Redirect to /login, cart saved in localStorage

**Test Case 4: View Order History (Empty)**
- Preconditions: User logged in, no orders placed
- Steps:
  1. Navigate to /orders
- Expected: Empty state message "No orders yet"

**Test Case 5: View Order History (With Orders)**
- Preconditions: User logged in, 15 orders placed
- Steps:
  1. Navigate to /orders
  2. Verify 10 orders displayed on page 1
  3. Click pagination "Next"
  4. Verify 5 orders displayed on page 2
- Expected: Pagination works correctly

**Test Case 6: View Order Details**
- Preconditions: User logged in, 1+ order exists
- Steps:
  1. Navigate to /orders
  2. Click on an order card
  3. Verify order details display (number, status, items, total)
- Expected: Order detail page shows complete information

**Test Case 7: Cancel Pending Order**
- Preconditions: User logged in, order with PENDING status exists
- Steps:
  1. Navigate to order detail page for pending order
  2. Click "Cancel Order"
  3. Confirm in modal
  4. Wait for success notification
  5. Verify status changes to CANCELLED
- Expected: Order cancelled successfully

**Test Case 8: Cannot Cancel Confirmed Order**
- Preconditions: User logged in, order with CONFIRMED status exists
- Steps:
  1. Navigate to order detail page for confirmed order
  2. Verify "Cancel Order" button not visible or disabled
- Expected: Cannot cancel confirmed orders

**Test Case 9: Language Switch**
- Preconditions: User on checkout page, language is English
- Steps:
  1. Switch language to Turkish
  2. Verify all text translates to Turkish
  3. Switch back to English
  4. Verify all text translates to English
- Expected: All UI text translates correctly

**Test Case 10: Mobile Responsiveness**
- Preconditions: Browser viewport set to 375px width
- Steps:
  1. Navigate to /checkout
  2. Verify layout readable and usable
  3. Navigate to /orders
  4. Verify order cards stack vertically
  5. Click on order
  6. Verify detail page readable
- Expected: All pages mobile-friendly

---

## Common Issues and Solutions

### Issue 1: "Network Error" on Order Placement

**Symptoms:**
- Checkout button shows error notification
- Console shows: "Network Error" or "ERR_CONNECTION_REFUSED"

**Causes:**
- Order Service (port 8083) not running
- API Gateway (port 8080) not running
- CORS issue

**Solutions:**
1. Check Order Service is running: `curl http://localhost:8083/actuator/health`
2. Check API Gateway is running: `curl http://localhost:8080/health`
3. Check API Gateway logs for routing errors
4. Verify CORS configuration in API Gateway allows localhost:5173

**Prevention:**
- Start all services before frontend development
- Add health check in frontend before order operations (optional)

---

### Issue 2: "User Not Authenticated" Error

**Symptoms:**
- Order placement fails with 401 Unauthorized
- Or authService.getUser() returns null

**Causes:**
- JWT token expired
- User logged out but localStorage not cleared
- Token not included in request headers

**Solutions:**
1. Check localStorage for token: `localStorage.getItem('token')`
2. Decode JWT to check expiration (jwt.io)
3. Log out and log back in to refresh token
4. Verify X-User-Id and Authorization headers sent in request

**Prevention:**
- Implement token refresh logic in authService
- Check token validity before order operations
- Redirect to login when token invalid

---

### Issue 3: Cart Not Clearing After Order

**Symptoms:**
- Order placed successfully
- Cart still shows items after checkout
- Cart count doesn't update in header

**Causes:**
- clearCart() not called in success handler
- clearCart() called before API success
- localStorage not updating
- CartContext state not updating

**Solutions:**
1. Verify clearCart() called only in success handler (try block)
2. Check CartContext clearCart implementation
3. Verify localStorage.removeItem('cart') in clearCart
4. Force refresh or re-fetch cart after order

**Prevention:**
- Only clear cart after successful API response (status 200/201)
- Add logging to clearCart() function
- Test cart clearing explicitly

---

### Issue 4: Translation Keys Showing Instead of Text

**Symptoms:**
- UI shows "orders.placeOrder" instead of "Place Order"
- Or shows [missing "orders.placeOrder" translation]

**Causes:**
- Translation key doesn't exist in en.json or tr.json
- Typo in translation key (case-sensitive)
- i18n not properly initialized

**Solutions:**
1. Check translation key exists in both en.json and tr.json
2. Verify exact key name matches (case-sensitive)
3. Restart dev server after adding translations
4. Check i18n initialization in main.tsx

**Prevention:**
- Add translations to both locales simultaneously
- Use ESLint plugin for i18n to catch missing keys
- Test with both languages during development

---

### Issue 5: Order Service Returns 500 Error

**Symptoms:**
- Order placement fails with 500 Internal Server Error
- Backend logs show exception

**Causes:**
- Product Service not running (stock check fails)
- Database connection issue
- Invalid product ID in order request
- Backend validation error

**Solutions:**
1. Check Product Service is running (port 8081)
2. Check Order Service logs for exception details
3. Verify product IDs are valid UUIDs
4. Check database connectivity: `docker ps` (postgres running)
5. Check backend/order-service/logs

**Prevention:**
- Validate product IDs before sending to API
- Add error handling for specific backend errors
- Monitor backend service health

---

## Performance Considerations

### Optimization Strategies

**1. Lazy Loading:**
- Lazy load OrderDetailPage route (React.lazy)
- Only load order data when page visited
- Reduce initial bundle size

**2. Memoization:**
- Use React.memo() for OrderCard component (prevents re-render)
- Use useMemo() for order total calculations
- Use useCallback() for event handlers

**3. Pagination:**
- Fetch only 10 orders per page (not all orders)
- Cache previous page results (optional)
- Prefetch next page on hover (advanced)

**4. API Call Optimization:**
- Debounce search/filter operations (if added)
- Cancel pending requests on component unmount
- Use AbortController for axios requests

**5. Bundle Size:**
- No new dependencies added
- Code split order pages from main bundle
- Tree-shake unused Mantine components

### Performance Targets

- **Checkout Page Load**: < 2 seconds
- **Order Placement API**: < 500ms (p95)
- **Order History Load**: < 1 second (10 orders)
- **Bundle Size Increase**: < 50KB gzipped
- **Time to Interactive**: < 3 seconds

---

## Documentation References

### Essential Reading

1. **NEXT_SESSION.md** - Current status, immediate next steps, Order Service details
2. **ORDER_SERVICE_COMPLETE.md** - Complete Order Service API documentation
3. **INTEGRATION_COMPLETE.md** - Auth & i18n integration patterns
4. **I18N_GUIDE.md** - How to add translations properly
5. **frontend/README.md** - Frontend architecture, component patterns

### Code Reference Examples

1. **AuthContext** (`frontend/src/context/AuthContext.tsx`) - Context pattern with authentication
2. **CartContext** (`frontend/src/context/CartContext.tsx`) - Context pattern with localStorage
3. **authService** (`frontend/src/services/authService.ts`) - API service pattern
4. **CartPage** (`frontend/src/pages/CartPage.tsx`) - Page component pattern
5. **ProtectedRoute** (`frontend/src/components/ProtectedRoute.tsx`) - Route protection pattern

### Backend Documentation

1. **Order Service README** - `backend/order-service/README.md`
2. **API Gateway Config** - `backend/api-gateway/src/main/resources/application.yml`
3. **Database Schema** - `backend/order-service/src/main/resources/db/changelog/`

---

## Contact and Support

### When Stuck

**Check These First:**
1. Read error message carefully (API errors very descriptive)
2. Check browser console for JavaScript errors
3. Check network tab for failed API calls
4. Check backend logs for service errors
5. Verify all services running (ports 8080-8083)

**Resources:**
- Mantine UI Docs: https://mantine.dev/
- i18next Docs: https://www.i18next.com/
- React Router Docs: https://reactrouter.com/
- Axios Docs: https://axios-http.com/

**Code Examples:**
- Existing cart implementation (CartContext, CartPage)
- Existing auth implementation (AuthContext, LoginPage)
- Existing product pages (ProductsPage, ProductDetailPage)

---

**Last Updated**: 2025-11-14
**Status**: Ready for Implementation
**Next Review**: After Phase 1 completion
