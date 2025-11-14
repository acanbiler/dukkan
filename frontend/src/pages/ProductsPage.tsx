import { useState, useEffect } from 'react';
import { Container, Title, Grid, Loader, Text, Center, TextInput, Group } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconSearch } from '@tabler/icons-react';
import { productService } from '@/services/productService';
import { Product } from '@/types/product';
import { ProductCard } from '@/components/products/ProductCard';

export const ProductsPage = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const response = await productService.getAll(0, 20, true);
      setProducts(response.content);
    } catch (error) {
      console.error('Failed to load products:', error);
      notifications.show({
        title: 'Error',
        message: 'Failed to load products',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadProducts();
      return;
    }

    try {
      setLoading(true);
      const response = await productService.search(searchQuery, 0, 20);
      setProducts(response.content);
    } catch (error) {
      console.error('Search failed:', error);
      notifications.show({
        title: 'Error',
        message: 'Search failed',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Center h={400}>
        <Loader size="lg" />
      </Center>
    );
  }

  return (
    <Container size="xl">
      <Group justify="space-between" mb="xl">
        <Title order={2}>Products</Title>
        <TextInput
          placeholder="Search products..."
          leftSection={<IconSearch size={16} />}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.currentTarget.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          w={300}
        />
      </Group>

      {products.length === 0 ? (
        <Center h={200}>
          <Text c="dimmed">No products found</Text>
        </Center>
      ) : (
        <Grid>
          {products.map((product) => (
            <Grid.Col key={product.id} span={{ base: 12, sm: 6, md: 4, lg: 3 }}>
              <ProductCard product={product} />
            </Grid.Col>
          ))}
        </Grid>
      )}
    </Container>
  );
};
