import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Title,
  Text,
  Group,
  Badge,
  Button,
  Stack,
  Loader,
  Center,
  Paper,
  NumberInput,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconArrowLeft, IconShoppingCart } from '@tabler/icons-react';
import { productService } from '@/services/productService';
import { Product } from '@/types/product';
import { useCart } from '@/context/CartContext';

export const ProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addToCart, isInCart, getItemQuantity } = useCart();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    if (id) {
      loadProduct(id);
    }
  }, [id]);

  const loadProduct = async (productId: string) => {
    try {
      setLoading(true);
      const data = await productService.getById(productId);
      setProduct(data);
    } catch (error) {
      console.error('Failed to load product:', error);
      notifications.show({
        title: 'Error',
        message: 'Failed to load product',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = () => {
    if (product) {
      addToCart(product, quantity);
    }
  };

  if (loading) {
    return (
      <Center h={400}>
        <Loader size="lg" />
      </Center>
    );
  }

  if (!product) {
    return (
      <Container>
        <Text>Product not found</Text>
      </Container>
    );
  }

  const inCart = isInCart(product.id);
  const cartQuantity = getItemQuantity(product.id);

  return (
    <Container size="md">
      <Button
        variant="subtle"
        leftSection={<IconArrowLeft size={16} />}
        onClick={() => navigate('/products')}
        mb="xl"
      >
        Back to Products
      </Button>

      <Paper shadow="sm" p="xl">
        <Stack gap="md">
          <Group justify="space-between">
            <Title order={2}>{product.name}</Title>
            <Badge color={product.isActive ? 'green' : 'red'}>
              {product.isActive ? 'Active' : 'Inactive'}
            </Badge>
          </Group>

          <Text size="sm" c="dimmed">
            SKU: {product.sku}
          </Text>

          <Text>{product.description}</Text>

          <Group>
            <Text size="xl" fw={700}>
              ${product.price.toFixed(2)}
            </Text>
            <Badge color={product.inStock ? 'blue' : 'red'}>
              {product.inStock ? `${product.stockQuantity} in stock` : 'Out of stock'}
            </Badge>
            {product.lowStock && product.inStock && (
              <Badge color="yellow">Low Stock</Badge>
            )}
          </Group>

          {inCart && (
            <Text size="sm" c="blue">
              {cartQuantity} {cartQuantity === 1 ? 'item' : 'items'} in cart
            </Text>
          )}

          <Group>
            <NumberInput
              value={quantity}
              onChange={(value) => setQuantity(Number(value))}
              min={1}
              max={product.stockQuantity}
              w={100}
              disabled={!product.inStock}
            />
            <Button
              leftSection={<IconShoppingCart size={16} />}
              onClick={handleAddToCart}
              disabled={!product.inStock}
              color={inCart ? 'green' : 'blue'}
            >
              {inCart ? 'Add More to Cart' : 'Add to Cart'}
            </Button>
          </Group>

          <Text size="xs" c="dimmed">
            Created: {new Date(product.createdAt).toLocaleDateString()}
          </Text>
          <Text size="xs" c="dimmed">
            Updated: {new Date(product.updatedAt).toLocaleDateString()}
          </Text>
        </Stack>
      </Paper>
    </Container>
  );
};
