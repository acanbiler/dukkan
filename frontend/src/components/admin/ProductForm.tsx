import { useState, useEffect } from 'react';
import { TextInput, NumberInput, Textarea, Button, Stack, Select, Switch } from '@mantine/core';
import { useForm } from '@mantine/form';
import { notifications } from '@mantine/notifications';
import { productService } from '@/services/productService';
import { categoryService } from '@/services/categoryService';
import { Product, CreateProductRequest } from '@/types/product';
import { Category } from '@/types/category';

interface ProductFormProps {
  product: Product | null;
  onSuccess: () => void;
}

export const ProductForm = ({ product, onSuccess }: ProductFormProps) => {
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);

  const form = useForm({
    initialValues: {
      name: product?.name || '',
      description: product?.description || '',
      price: product?.price || 0,
      stockQuantity: product?.stockQuantity || 0,
      sku: product?.sku || '',
      categoryId: product?.categoryId || '',
      isActive: product?.isActive ?? true,
    },
    validate: {
      name: (value) => (value.trim().length > 0 ? null : 'Name is required'),
      sku: (value) => (value.trim().length > 0 ? null : 'SKU is required'),
      price: (value) => (value > 0 ? null : 'Price must be greater than 0'),
      stockQuantity: (value) => (value >= 0 ? null : 'Stock cannot be negative'),
      categoryId: (value) => (value ? null : 'Category is required'),
    },
  });

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await categoryService.getAll(true);
      setCategories(data);
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to load categories',
        color: 'red',
      });
    }
  };

  const handleSubmit = async (values: typeof form.values) => {
    try {
      setLoading(true);
      const request: CreateProductRequest = {
        name: values.name,
        description: values.description,
        price: values.price,
        stockQuantity: values.stockQuantity,
        sku: values.sku,
        categoryId: values.categoryId,
      };

      if (product) {
        await productService.update(product.id, { ...request, isActive: values.isActive });
        notifications.show({
          title: 'Success',
          message: 'Product updated successfully',
          color: 'green',
        });
      } else {
        await productService.create(request);
        notifications.show({
          title: 'Success',
          message: 'Product created successfully',
          color: 'green',
        });
      }

      onSuccess();
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: product ? 'Failed to update product' : 'Failed to create product',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  const categoryOptions = categories.map((cat) => ({
    value: cat.id,
    label: cat.name,
  }));

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Stack gap="md">
        <TextInput
          label="Product Name"
          placeholder="Enter product name"
          required
          {...form.getInputProps('name')}
        />

        <TextInput
          label="SKU"
          placeholder="Enter SKU"
          required
          {...form.getInputProps('sku')}
        />

        <Textarea
          label="Description"
          placeholder="Enter product description"
          minRows={3}
          {...form.getInputProps('description')}
        />

        <NumberInput
          label="Price"
          placeholder="0.00"
          decimalScale={2}
          min={0}
          prefix="$"
          required
          {...form.getInputProps('price')}
        />

        <NumberInput
          label="Stock Quantity"
          placeholder="0"
          min={0}
          required
          {...form.getInputProps('stockQuantity')}
        />

        <Select
          label="Category"
          placeholder="Select category"
          data={categoryOptions}
          required
          searchable
          {...form.getInputProps('categoryId')}
        />

        {product && (
          <Switch
            label="Active"
            {...form.getInputProps('isActive', { type: 'checkbox' })}
          />
        )}

        <Button type="submit" loading={loading} fullWidth>
          {product ? 'Update Product' : 'Create Product'}
        </Button>
      </Stack>
    </form>
  );
};
