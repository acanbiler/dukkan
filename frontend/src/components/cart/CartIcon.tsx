import { ActionIcon, Indicator } from '@mantine/core';
import { IconShoppingCart } from '@tabler/icons-react';
import { useCart } from '@/context/CartContext';

interface CartIconProps {
  onClick: () => void;
}

export const CartIcon = ({ onClick }: CartIconProps) => {
  const { cart } = useCart();

  return (
    <Indicator
      inline
      label={cart.totalItems}
      size={20}
      disabled={cart.totalItems === 0}
      color="red"
    >
      <ActionIcon variant="subtle" size="lg" onClick={onClick}>
        <IconShoppingCart size={24} />
      </ActionIcon>
    </Indicator>
  );
};
