import { Group, Image, Text, ActionIcon, NumberInput, Stack } from '@mantine/core';
import { IconTrash } from '@tabler/icons-react';
import { useCart } from '@/context/CartContext';
import { CartItem } from '@/types/cart';

interface CartItemProps {
  item: CartItem;
}

export const CartItemComponent = ({ item }: CartItemProps) => {
  const { updateQuantity, removeFromCart } = useCart();

  return (
    <Group align="flex-start" wrap="nowrap">
      <Image
        src={item.product.imageUrls[0] || 'https://via.placeholder.com/80'}
        w={80}
        h={80}
        radius="md"
      />
      <Stack gap="xs" style={{ flex: 1 }}>
        <Text fw={500} lineClamp={1}>
          {item.product.name}
        </Text>
        <Text size="sm" c="dimmed">
          ${item.product.price.toFixed(2)} each
        </Text>
        <Group gap="xs">
          <NumberInput
            value={item.quantity}
            onChange={(value) => updateQuantity(item.product.id, Number(value))}
            min={1}
            max={item.product.stockQuantity}
            w={80}
            size="xs"
          />
          <ActionIcon color="red" onClick={() => removeFromCart(item.product.id)} variant="subtle">
            <IconTrash size={18} />
          </ActionIcon>
        </Group>
        <Text fw={600}>${(item.product.price * item.quantity).toFixed(2)}</Text>
      </Stack>
    </Group>
  );
};
