import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Title,
  Paper,
  Stack,
  Table,
  Text,
  Badge,
  Button,
  Group,
  LoadingOverlay,
  Alert,
  Pagination,
  Center,
  Modal,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconAlertCircle, IconCheck, IconPackage, IconShoppingBag } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { Order, OrderStatus } from '../types/order';

export const OrdersPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [detailsOpened, setDetailsOpened] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    navigate('/login', { state: { from: '/orders' } });
    return null;
  }

  useEffect(() => {
    fetchOrders();
  }, [page]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await orderService.getMyOrders(page - 1, 10);
      setOrders(response.content);
      setTotalPages(response.totalPages);
    } catch (error: any) {
      console.error('Failed to fetch orders:', error);
      notifications.show({
        title: t('common.error'),
        message: error.response?.data?.message || t('errors.generic'),
        color: 'red',
        icon: <IconAlertCircle size={18} />,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = (order: Order) => {
    setSelectedOrder(order);
    setDetailsOpened(true);
  };

  const handleCancelOrder = async (orderId: string) => {
    try {
      setCancelling(true);
      await orderService.cancelOrder(orderId);

      notifications.show({
        title: t('common.success'),
        message: t('orders.orderCancelled'),
        color: 'green',
        icon: <IconCheck size={18} />,
      });

      // Refresh orders
      await fetchOrders();
      setDetailsOpened(false);
    } catch (error: any) {
      console.error('Failed to cancel order:', error);
      notifications.show({
        title: t('common.error'),
        message: error.response?.data?.message || t('orders.cannotCancel'),
        color: 'red',
        icon: <IconAlertCircle size={18} />,
      });
    } finally {
      setCancelling(false);
    }
  };

  const getStatusColor = (status: OrderStatus): string => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'yellow';
      case OrderStatus.CONFIRMED:
        return 'blue';
      case OrderStatus.PROCESSING:
        return 'cyan';
      case OrderStatus.SHIPPED:
        return 'grape';
      case OrderStatus.DELIVERED:
        return 'green';
      case OrderStatus.CANCELLED:
        return 'red';
      default:
        return 'gray';
    }
  };

  const canCancelOrder = (order: Order): boolean => {
    return order.status === OrderStatus.PENDING || order.status === OrderStatus.CONFIRMED;
  };

  return (
    <Container size="xl" py="xl">
      <LoadingOverlay visible={loading} overlayProps={{ blur: 2 }} />

      <Stack gap="lg">
        {/* Header */}
        <Group>
          <IconPackage size={32} />
          <Title order={1}>{t('orders.title')}</Title>
        </Group>

        {/* Orders List */}
        {orders.length === 0 && !loading ? (
          <Center py="xl">
            <Stack align="center" gap="md">
              <IconShoppingBag size={64} stroke={1} color="gray" />
              <Text c="dimmed" size="lg">
                {t('orders.noOrders')}
              </Text>
              <Button onClick={() => navigate('/products')}>
                {t('cart.continueShopping')}
              </Button>
            </Stack>
          </Center>
        ) : (
          <>
            <Paper shadow="sm" p="md" withBorder>
              <Table>
                <Table.Thead>
                  <Table.Tr>
                    <Table.Th>{t('orders.orderNumber', { number: '' })}</Table.Th>
                    <Table.Th>{t('orders.date')}</Table.Th>
                    <Table.Th>{t('orders.items')}</Table.Th>
                    <Table.Th>{t('orders.total')}</Table.Th>
                    <Table.Th>{t('orders.status')}</Table.Th>
                    <Table.Th>{t('common.actions')}</Table.Th>
                  </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                  {orders.map((order) => (
                    <Table.Tr key={order.id}>
                      <Table.Td>
                        <Text fw={500}>{order.orderNumber}</Text>
                      </Table.Td>
                      <Table.Td>
                        {new Date(order.createdAt).toLocaleDateString()}
                      </Table.Td>
                      <Table.Td>{order.items.length}</Table.Td>
                      <Table.Td fw={500}>${order.totalAmount.toFixed(2)}</Table.Td>
                      <Table.Td>
                        <Badge color={getStatusColor(order.status)}>
                          {t(`orders.status.${order.status}`)}
                        </Badge>
                      </Table.Td>
                      <Table.Td>
                        <Button
                          size="xs"
                          variant="light"
                          onClick={() => handleViewDetails(order)}
                        >
                          {t('orders.viewDetails')}
                        </Button>
                      </Table.Td>
                    </Table.Tr>
                  ))}
                </Table.Tbody>
              </Table>
            </Paper>

            {/* Pagination */}
            {totalPages > 1 && (
              <Center>
                <Pagination value={page} onChange={setPage} total={totalPages} />
              </Center>
            )}
          </>
        )}
      </Stack>

      {/* Order Details Modal */}
      <Modal
        opened={detailsOpened}
        onClose={() => setDetailsOpened(false)}
        title={t('orders.orderDetails')}
        size="lg"
      >
        {selectedOrder && (
          <Stack gap="md">
            {/* Order Info */}
            <Group justify="space-between">
              <div>
                <Text size="sm" c="dimmed">
                  {t('orders.orderNumber', { number: '' })}
                </Text>
                <Text fw={500}>{selectedOrder.orderNumber}</Text>
              </div>
              <Badge color={getStatusColor(selectedOrder.status)} size="lg">
                {t(`orders.status.${selectedOrder.status}`)}
              </Badge>
            </Group>

            <div>
              <Text size="sm" c="dimmed">
                {t('orders.date')}
              </Text>
              <Text>{new Date(selectedOrder.createdAt).toLocaleString()}</Text>
            </div>

            {/* Order Items */}
            <div>
              <Text fw={500} mb="sm">
                {t('orders.items')}
              </Text>
              <Table>
                <Table.Thead>
                  <Table.Tr>
                    <Table.Th>{t('products.title')}</Table.Th>
                    <Table.Th>{t('cart.quantity')}</Table.Th>
                    <Table.Th>{t('products.price')}</Table.Th>
                    <Table.Th>{t('cart.subtotal')}</Table.Th>
                  </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                  {selectedOrder.items.map((item) => (
                    <Table.Tr key={item.id}>
                      <Table.Td>
                        <div>
                          <Text>{item.productName}</Text>
                          <Text size="xs" c="dimmed">
                            {t('products.sku')}: {item.productSku}
                          </Text>
                        </div>
                      </Table.Td>
                      <Table.Td>{item.quantity}</Table.Td>
                      <Table.Td>${item.priceAtPurchase.toFixed(2)}</Table.Td>
                      <Table.Td fw={500}>${item.subtotal.toFixed(2)}</Table.Td>
                    </Table.Tr>
                  ))}
                </Table.Tbody>
              </Table>
            </div>

            {/* Total */}
            <Group justify="flex-end">
              <Text size="lg" fw={700}>
                {t('cart.total')}:
              </Text>
              <Text size="xl" fw={700} c="blue">
                ${selectedOrder.totalAmount.toFixed(2)}
              </Text>
            </Group>

            {/* Actions */}
            <Group justify="space-between" mt="md">
              <Button variant="subtle" onClick={() => setDetailsOpened(false)}>
                {t('common.close')}
              </Button>
              {canCancelOrder(selectedOrder) && (
                <Button
                  color="red"
                  onClick={() => handleCancelOrder(selectedOrder.id)}
                  loading={cancelling}
                >
                  {t('orders.cancelOrder')}
                </Button>
              )}
            </Group>
          </Stack>
        )}
      </Modal>
    </Container>
  );
};
