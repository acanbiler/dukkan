import { useState, useEffect } from 'react';
import { Container, Title, Loader, Text, Center, Stack, Paper, Group, Badge } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { categoryService } from '@/services/categoryService';
import { Category } from '@/types/category';

export const CategoriesPage = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const data = await categoryService.getAll(true);
      setCategories(data);
    } catch (error) {
      console.error('Failed to load categories:', error);
      notifications.show({
        title: 'Error',
        message: 'Failed to load categories',
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
    <Container size="md">
      <Title order={2} mb="xl">
        Categories
      </Title>

      {categories.length === 0 ? (
        <Center h={200}>
          <Text c="dimmed">No categories found</Text>
        </Center>
      ) : (
        <Stack gap="md">
          {categories.map((category) => (
            <Paper key={category.id} shadow="xs" p="md" withBorder>
              <Group justify="space-between">
                <div>
                  <Title order={4}>{category.name}</Title>
                  {category.description && (
                    <Text size="sm" c="dimmed" mt="xs">
                      {category.description}
                    </Text>
                  )}
                </div>
                <Badge color={category.isActive ? 'green' : 'red'}>
                  {category.isActive ? 'Active' : 'Inactive'}
                </Badge>
              </Group>
            </Paper>
          ))}
        </Stack>
      )}
    </Container>
  );
};
