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
import { categoryService } from '@/services/categoryService';
import { Category } from '@/types/category';
import { CategoryForm } from '@/components/admin/CategoryForm';

export const AdminCategories = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpened, setModalOpened] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const data = await categoryService.getAll(); // Get all including inactive
      setCategories(data);
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to load categories',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingCategory(null);
    setModalOpened(true);
  };

  const handleEdit = (category: Category) => {
    setEditingCategory(category);
    setModalOpened(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this category?')) return;

    try {
      await categoryService.delete(id);
      notifications.show({
        title: 'Success',
        message: 'Category deleted successfully',
        color: 'green',
      });
      loadCategories();
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to delete category',
        color: 'red',
      });
    }
  };

  const handleFormSuccess = () => {
    setModalOpened(false);
    setEditingCategory(null);
    loadCategories();
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
          <Title order={2}>Categories Management</Title>
          <Button leftSection={<IconPlus size={16} />} onClick={handleCreate}>
            Add Category
          </Button>
        </Group>

        {categories.length === 0 ? (
          <Center h={200}>
            <Text c="dimmed">No categories found</Text>
          </Center>
        ) : (
          <Table striped highlightOnHover>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>Name</Table.Th>
                <Table.Th>Description</Table.Th>
                <Table.Th>Status</Table.Th>
                <Table.Th>Actions</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {categories.map((category) => (
                <Table.Tr key={category.id}>
                  <Table.Td>{category.name}</Table.Td>
                  <Table.Td>{category.description || '-'}</Table.Td>
                  <Table.Td>
                    <Badge color={category.isActive ? 'green' : 'gray'}>
                      {category.isActive ? 'Active' : 'Inactive'}
                    </Badge>
                  </Table.Td>
                  <Table.Td>
                    <Group gap="xs">
                      <ActionIcon
                        variant="light"
                        color="blue"
                        onClick={() => handleEdit(category)}
                      >
                        <IconEdit size={16} />
                      </ActionIcon>
                      <ActionIcon
                        variant="light"
                        color="red"
                        onClick={() => handleDelete(category.id)}
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
          title={editingCategory ? 'Edit Category' : 'Create Category'}
          size="md"
        >
          <CategoryForm category={editingCategory} onSuccess={handleFormSuccess} />
        </Modal>
      </Container>
    </AdminLayout>
  );
};
