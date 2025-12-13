import { Drawer, Stack, Text, Button, Divider, Group } from '@mantine/core';
import { useNavigate } from 'react-router-dom';
import { useCart } from '@/context/CartContext';
import { CartItemComponent } from './CartItem';

interface CartDrawerProps {
  opened: boolean;
  onClose: () => void;
}

export const CartDrawer = ({ opened, onClose }: CartDrawerProps) => {
  const { cart, clearCart } = useCart();
  const navigate = useNavigate();

  const handleCheckout = () => {
    navigate('/cart');
    onClose();
  };

  return (
    <Drawer opened={opened} onClose={onClose} position="right" title="Shopping Cart" size="md">
      {cart.items.length === 0 ? (
        <Stack align="center" mt="xl">
          <Text c="dimmed">Your cart is empty</Text>
          <Button onClick={onClose}>Continue Shopping</Button>
        </Stack>
      ) : (
        <Stack h="100%" justify="space-between">
          <Stack gap="md">
            {cart.items.map((item) => (
              <CartItemComponent key={item.product.id} item={item} />
            ))}
          </Stack>

          <Stack gap="md">
            <Divider />
            <Group justify="space-between">
              <Text size="lg" fw={700}>
                Total:
              </Text>
              <Text size="xl" fw={700}>
                ${cart.totalPrice.toFixed(2)}
              </Text>
            </Group>
            <Text size="sm" c="dimmed">
              {cart.totalItems} {cart.totalItems === 1 ? 'item' : 'items'}
            </Text>
            <Button fullWidth size="lg" onClick={handleCheckout}>
              View Cart
            </Button>
            <Button fullWidth variant="subtle" onClick={clearCart}>
              Clear Cart
            </Button>
          </Stack>
        </Stack>
      )}
    </Drawer>
  );
};
