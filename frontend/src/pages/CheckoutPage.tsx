import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Title,
  Paper,
  Stack,
  Table,
  Text,
  Button,
  Group,
  LoadingOverlay,
  Alert,
  Divider,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconAlertCircle, IconCheck, IconShoppingCart } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { orderService } from '../services/orderService';
import { PlaceOrderRequest } from '../types/order';

export const CheckoutPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { cart, clearCart } = useCart();
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    navigate('/login', { state: { from: '/checkout' } });
    return null;
  }

  // Redirect to cart if empty
  if (cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  const handlePlaceOrder = async () => {
    try {
      setLoading(true);

      // Prepare order request
      const orderRequest: PlaceOrderRequest = {
        items: cart.items.map((item) => ({
          productId: item.product.id,
          quantity: item.quantity,
        })),
      };

      // Place order
      await orderService.placeOrder(orderRequest);

      // Success!
      notifications.show({
        title: t('orders.checkout.success'),
        message: t('orders.orderPlaced'),
        color: 'green',
        icon: <IconCheck size={18} />,
      });

      // Clear cart
      clearCart();

      // Redirect to orders page
      navigate('/orders');
    } catch (error: any) {
      console.error('Failed to place order:', error);

      let errorMessage = t('orders.checkout.error');

      // Handle specific errors
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.status === 400) {
        errorMessage = 'Invalid order. Please check your cart items.';
      } else if (error.response?.status === 401) {
        errorMessage = t('errors.unauthorized');
        navigate('/login', { state: { from: '/checkout' } });
        return;
      }

      notifications.show({
        title: t('common.error'),
        message: errorMessage,
        color: 'red',
        icon: <IconAlertCircle size={18} />,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container size="md" py="xl">
      <LoadingOverlay visible={loading} overlayProps={{ blur: 2 }} />

      <Stack gap="lg">
        {/* Header */}
        <Group>
          <IconShoppingCart size={32} />
          <Title order={1}>{t('orders.checkout.title')}</Title>
        </Group>

        {/* Info Alert */}
        <Alert color="blue" title={t('orders.checkout.reviewOrder')}>
          {t('orders.checkout.summary')}
        </Alert>

        {/* Order Summary */}
        <Paper shadow="sm" p="md" withBorder>
          <Title order={3} mb="md">
            {t('orders.checkout.summary')}
          </Title>

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
              {cart.items.map((item) => (
                <Table.Tr key={item.product.id}>
                  <Table.Td>
                    <div>
                      <Text fw={500}>{item.product.name}</Text>
                      <Text size="xs" c="dimmed">
                        {t('products.sku')}: {item.product.sku}
                      </Text>
                    </div>
                  </Table.Td>
                  <Table.Td>{item.quantity}</Table.Td>
                  <Table.Td>${item.product.price.toFixed(2)}</Table.Td>
                  <Table.Td fw={500}>
                    ${(item.product.price * item.quantity).toFixed(2)}
                  </Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>

          <Divider my="md" />

          {/* Total */}
          <Group justify="flex-end">
            <Text size="lg" fw={700}>
              {t('cart.total')}:
            </Text>
            <Text size="xl" fw={700} c="blue">
              ${cart.totalPrice.toFixed(2)}
            </Text>
          </Group>
        </Paper>

        {/* Actions */}
        <Group justify="space-between">
          <Button
            variant="subtle"
            onClick={() => navigate('/cart')}
            disabled={loading}
          >
            {t('common.back')}
          </Button>
          <Button
            size="lg"
            onClick={handlePlaceOrder}
            loading={loading}
            disabled={cart.items.length === 0}
          >
            {t('orders.placeOrder')}
          </Button>
        </Group>
      </Stack>
    </Container>
  );
};
