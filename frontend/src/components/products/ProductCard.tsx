import { Card, Image, Text, Badge, Button, Group, Stack } from '@mantine/core';
import { useNavigate } from 'react-router-dom';
import { IconShoppingCart, IconEye } from '@tabler/icons-react';
import { Product } from '@/types/product';
import { useCart } from '@/context/CartContext';

interface ProductCardProps {
  product: Product;
}

export const ProductCard = ({ product }: ProductCardProps) => {
  const navigate = useNavigate();
  const { addToCart, isInCart } = useCart();

  const handleAddToCart = (e: React.MouseEvent) => {
    e.stopPropagation();
    addToCart(product);
  };

  return (
    <Card shadow="sm" padding="lg" radius="md" withBorder>
      <Card.Section>
        <Image
          src={product.imageUrls[0] || 'https://via.placeholder.com/300'}
          height={200}
          alt={product.name}
        />
      </Card.Section>

      <Stack gap="xs" mt="md">
        <Text fw={500} lineClamp={1}>
          {product.name}
        </Text>

        <Text size="sm" c="dimmed" lineClamp={2}>
          {product.description}
        </Text>

        <Group justify="space-between" mt="xs">
          <Text size="lg" fw={700}>
            ${product.price.toFixed(2)}
          </Text>
          <Badge color={product.inStock ? 'green' : 'red'} variant="light">
            {product.inStock ? 'In Stock' : 'Out of Stock'}
          </Badge>
        </Group>

        {product.lowStock && product.inStock && (
          <Badge color="yellow" variant="filled" size="sm">
            Low Stock
          </Badge>
        )}

        <Group gap="xs" mt="md">
          <Button
            style={{ flex: 1 }}
            variant="light"
            onClick={() => navigate(`/products/${product.id}`)}
            leftSection={<IconEye size={16} />}
          >
            Details
          </Button>
          <Button
            style={{ flex: 1 }}
            onClick={handleAddToCart}
            disabled={!product.inStock}
            leftSection={<IconShoppingCart size={16} />}
            color={isInCart(product.id) ? 'green' : 'blue'}
          >
            {isInCart(product.id) ? 'Added' : 'Add'}
          </Button>
        </Group>
      </Stack>
    </Card>
  );
};
