import { Container, Title, Stack, Text, Button, Group, Paper, Divider } from '@mantine/core';
import { useNavigate } from 'react-router-dom';
import { IconArrowLeft } from '@tabler/icons-react';
import { useCart } from '@/context/CartContext';
import { CartItemComponent } from '@/components/cart/CartItem';

export const CartPage = () => {
  const { cart, clearCart } = useCart();
  const navigate = useNavigate();

  if (cart.items.length === 0) {
    return (
      <Container size="md" mt="xl">
        <Stack align="center" gap="xl">
          <Title order={2}>Your Cart is Empty</Title>
          <Text c="dimmed">Add some products to get started</Text>
          <Button onClick={() => navigate('/products')} leftSection={<IconArrowLeft size={16} />}>
            Continue Shopping
          </Button>
        </Stack>
      </Container>
    );
  }

  return (
    <Container size="lg">
      <Group justify="space-between" mb="xl">
        <Title order={2}>Shopping Cart</Title>
        <Button variant="subtle" onClick={() => navigate('/products')}>
          Continue Shopping
        </Button>
      </Group>

      <Group align="flex-start" gap="xl" wrap="nowrap">
        {/* Cart Items */}
        <Stack style={{ flex: 1 }} gap="md">
          {cart.items.map((item) => (
            <Paper key={item.product.id} shadow="xs" p="md" withBorder>
              <CartItemComponent item={item} />
            </Paper>
          ))}
        </Stack>

        {/* Cart Summary */}
        <Paper shadow="sm" p="xl" withBorder w={350}>
          <Stack gap="md">
            <Title order={3}>Order Summary</Title>
            <Divider />

            <Group justify="space-between">
              <Text>Items ({cart.totalItems}):</Text>
              <Text>${cart.totalPrice.toFixed(2)}</Text>
            </Group>

            <Group justify="space-between">
              <Text>Shipping:</Text>
              <Text c="green">FREE</Text>
            </Group>

            <Divider />

            <Group justify="space-between">
              <Text size="lg" fw={700}>
                Total:
              </Text>
              <Text size="xl" fw={700}>
                ${cart.totalPrice.toFixed(2)}
              </Text>
            </Group>

            <Button fullWidth size="lg" mt="md">
              Proceed to Checkout
            </Button>

            <Button fullWidth variant="subtle" onClick={clearCart}>
              Clear Cart
            </Button>
          </Stack>
        </Paper>
      </Group>
    </Container>
  );
};
