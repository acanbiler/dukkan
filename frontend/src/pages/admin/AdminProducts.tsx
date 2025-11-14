import { useState, useEffect } from 'react';
import {
  Container,
  Title,
  Button,
  Table,
  Group,
  ActionIcon,
  Badge,
  Loader,
  Center,
  Text,
  Modal,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconPlus, IconEdit, IconTrash } from '@tabler/icons-react';
import { AdminLayout } from '@/components/admin/AdminLayout';
import { productService } from '@/services/productService';
import { Product } from '@/types/product';
import { ProductForm } from '@/components/admin/ProductForm';

export const AdminProducts = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpened, setModalOpened] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const response = await productService.getAll(0, 100, false); // Get all including inactive
      setProducts(response.content);
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to load products',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingProduct(null);
    setModalOpened(true);
  };

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    setModalOpened(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
      await productService.delete(id);
      notifications.show({
        title: 'Success',
        message: 'Product deleted successfully',
        color: 'green',
      });
      loadProducts();
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to delete product',
        color: 'red',
      });
    }
  };

  const handleFormSuccess = () => {
    setModalOpened(false);
    setEditingProduct(null);
    loadProducts();
  };

  if (loading) {
    return (
      <AdminLayout>
        <Center h={400}>
          <Loader size="lg" />
        </Center>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <Container size="xl">
        <Group justify="space-between" mb="xl">
          <Title order={2}>Products Management</Title>
          <Button leftSection={<IconPlus size={16} />} onClick={handleCreate}>
            Add Product
          </Button>
        </Group>

        {products.length === 0 ? (
          <Center h={200}>
            <Text c="dimmed">No products found</Text>
          </Center>
        ) : (
          <Table striped highlightOnHover>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>SKU</Table.Th>
                <Table.Th>Name</Table.Th>
                <Table.Th>Price</Table.Th>
                <Table.Th>Stock</Table.Th>
                <Table.Th>Status</Table.Th>
                <Table.Th>Actions</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {products.map((product) => (
                <Table.Tr key={product.id}>
                  <Table.Td>{product.sku}</Table.Td>
                  <Table.Td>{product.name}</Table.Td>
                  <Table.Td>${product.price.toFixed(2)}</Table.Td>
                  <Table.Td>
                    <Badge color={product.inStock ? 'green' : 'red'}>
                      {product.stockQuantity}
                    </Badge>
                  </Table.Td>
                  <Table.Td>
                    <Badge color={product.isActive ? 'green' : 'gray'}>
                      {product.isActive ? 'Active' : 'Inactive'}
                    </Badge>
                  </Table.Td>
                  <Table.Td>
                    <Group gap="xs">
                      <ActionIcon
                        variant="light"
                        color="blue"
                        onClick={() => handleEdit(product)}
                      >
                        <IconEdit size={16} />
                      </ActionIcon>
                      <ActionIcon
                        variant="light"
                        color="red"
                        onClick={() => handleDelete(product.id)}
                      >
                        <IconTrash size={16} />
                      </ActionIcon>
                    </Group>
                  </Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        )}

        <Modal
          opened={modalOpened}
          onClose={() => setModalOpened(false)}
          title={editingProduct ? 'Edit Product' : 'Create Product'}
          size="lg"
        >
          <ProductForm product={editingProduct} onSuccess={handleFormSuccess} />
        </Modal>
      </Container>
    </AdminLayout>
  );
};
