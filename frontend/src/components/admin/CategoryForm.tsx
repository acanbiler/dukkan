import { useState } from 'react';
import { TextInput, Textarea, Button, Stack, Switch } from '@mantine/core';
import { useForm } from '@mantine/form';
import { notifications } from '@mantine/notifications';
import { categoryService } from '@/services/categoryService';
import { Category, CreateCategoryRequest } from '@/types/category';

interface CategoryFormProps {
  category: Category | null;
  onSuccess: () => void;
}

export const CategoryForm = ({ category, onSuccess }: CategoryFormProps) => {
  const [loading, setLoading] = useState(false);

  const form = useForm({
    initialValues: {
      name: category?.name || '',
      description: category?.description || '',
      isActive: category?.isActive ?? true,
    },
    validate: {
      name: (value) => (value.trim().length > 0 ? null : 'Name is required'),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      setLoading(true);
      const request: CreateCategoryRequest = {
        name: values.name,
        description: values.description,
      };

      if (category) {
        await categoryService.update(category.id, { ...request, isActive: values.isActive });
        notifications.show({
          title: 'Success',
          message: 'Category updated successfully',
          color: 'green',
        });
      } else {
        await categoryService.create(request);
        notifications.show({
          title: 'Success',
          message: 'Category created successfully',
          color: 'green',
        });
      }

      onSuccess();
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: category ? 'Failed to update category' : 'Failed to create category',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Stack gap="md">
        <TextInput
          label="Category Name"
          placeholder="Enter category name"
          required
          {...form.getInputProps('name')}
        />

        <Textarea
          label="Description"
          placeholder="Enter category description"
          minRows={3}
          {...form.getInputProps('description')}
        />

        {category && (
          <Switch
            label="Active"
            {...form.getInputProps('isActive', { type: 'checkbox' })}
          />
        )}

        <Button type="submit" loading={loading} fullWidth>
          {category ? 'Update Category' : 'Create Category'}
        </Button>
      </Stack>
    </form>
  );
};
