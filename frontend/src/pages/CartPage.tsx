import { Container, Title, Stack, Text, Button, Group, Paper, Divider } from '@mantine/core';
import { useNavigate } from 'react-router-dom';
import { IconArrowLeft } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { useCart } from '@/context/CartContext';
import { CartItemComponent } from '@/components/cart/CartItem';

export const CartPage = () => {
  const { t } = useTranslation();
  const { cart, clearCart } = useCart();
  const navigate = useNavigate();

  if (cart.items.length === 0) {
    return (
      <Container size="md" mt="xl">
        <Stack align="center" gap="xl">
          <Title order={2}>{t('cart.empty')}</Title>
          <Text c="dimmed">{t('products.noProducts')}</Text>
          <Button onClick={() => navigate('/products')} leftSection={<IconArrowLeft size={16} />}>
            {t('cart.continueShopping')}
          </Button>
        </Stack>
      </Container>
    );
  }

  return (
    <Container size="lg" py="xl">
      <Group justify="space-between" mb="xl">
        <Title order={2}>{t('cart.title')}</Title>
        <Button variant="subtle" onClick={() => navigate('/products')}>
          {t('cart.continueShopping')}
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
            <Title order={3}>{t('orders.checkout.summary')}</Title>
            <Divider />

            <Group justify="space-between">
              <Text>{t('cart.items')} ({cart.totalItems}):</Text>
              <Text>${cart.totalPrice.toFixed(2)}</Text>
            </Group>

            <Group justify="space-between">
              <Text>{t('cart.subtotal')}:</Text>
              <Text c="green">FREE</Text>
            </Group>

            <Divider />

            <Group justify="space-between">
              <Text size="lg" fw={700}>
                {t('cart.total')}:
              </Text>
              <Text size="xl" fw={700}>
                ${cart.totalPrice.toFixed(2)}
              </Text>
            </Group>

            <Button fullWidth size="lg" mt="md" onClick={() => navigate('/checkout')}>
              {t('cart.proceedToCheckout')}
            </Button>

            <Button fullWidth variant="subtle" onClick={clearCart}>
              {t('cart.clear')}
            </Button>
          </Stack>
        </Paper>
      </Group>
    </Container>
  );
};
