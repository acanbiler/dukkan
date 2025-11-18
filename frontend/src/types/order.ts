// Order-related TypeScript types

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export interface OrderItemRequest {
  productId: string;
  quantity: number;
}

export interface PlaceOrderRequest {
  items: OrderItemRequest[];
}

export interface OrderItemDTO {
  id: string;
  productId: string;
  productName: string;
  productSku: string;
  quantity: number;
  priceAtPurchase: number;
  subtotal: number;
}

export interface Order {
  id: string;
  userId: string;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  items: OrderItemDTO[];
  createdAt: string;
  updatedAt: string;
}

export interface OrdersPageResponse {
  content: Order[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
