import { useState } from 'react';
import {
  Stack,
  Group,
  Button,
  Text,
  Image,
  ActionIcon,
  FileInput,
  Alert,
  rem,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconUpload, IconTrash, IconAlertCircle } from '@tabler/icons-react';
import { uploadImage, deleteImage, getImageUrl } from '../../services/imageService';

interface ImageUploadProps {
  productId: string;
  imageUrls: string[];
  onImagesChange: (imageUrls: string[]) => void;
  maxImages?: number;
}

export const ImageUpload = ({
  productId,
  imageUrls,
  onImagesChange,
  maxImages = 5,
}: ImageUploadProps) => {
  const [uploading, setUploading] = useState(false);
  const [file, setFile] = useState<File | null>(null);

  const handleUpload = async () => {
    if (!file) {
      notifications.show({
        title: 'Error',
        message: 'Please select a file to upload',
        color: 'red',
      });
      return;
    }

    if (imageUrls.length >= maxImages) {
      notifications.show({
        title: 'Error',
        message: `Maximum ${maxImages} images allowed`,
        color: 'red',
      });
      return;
    }

    setUploading(true);

    try {
      const updatedProduct = await uploadImage(productId, file);
      onImagesChange(updatedProduct.imageUrls || []);

      notifications.show({
        title: 'Success',
        message: 'Image uploaded successfully',
        color: 'green',
      });

      setFile(null);
    } catch (error: any) {
      const errorMessage = error.response?.data?.error?.message || 'Failed to upload image';
      notifications.show({
        title: 'Upload failed',
        message: errorMessage,
        color: 'red',
      });
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (imageUrl: string) => {
    try {
      const updatedProduct = await deleteImage(productId, imageUrl);
      onImagesChange(updatedProduct.imageUrls || []);

      notifications.show({
        title: 'Success',
        message: 'Image deleted successfully',
        color: 'green',
      });
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to delete image',
        color: 'red',
      });
    }
  };

  return (
    <Stack gap="md">
      {/* Upload controls */}
      {imageUrls.length < maxImages && (
        <Group>
          <FileInput
            placeholder="Choose image file"
            accept="image/jpeg,image/png,image/webp"
            value={file}
            onChange={setFile}
            style={{ flex: 1 }}
            leftSection={<IconUpload style={{ width: rem(16), height: rem(16) }} />}
          />
          <Button
            onClick={handleUpload}
            loading={uploading}
            disabled={!file}
          >
            Upload
          </Button>
        </Group>
      )}

      {/* Image limit info */}
      <Text size="sm" c="dimmed">
        {imageUrls.length} / {maxImages} images uploaded
        {imageUrls.length >= maxImages && ' (Maximum reached)'}
      </Text>

      {/* Validation alert */}
      {imageUrls.length === 0 && (
        <Alert icon={<IconAlertCircle size={16} />} color="blue">
          Upload at least one product image (JPEG, PNG, or WebP, max 5MB each)
        </Alert>
      )}

      {/* Current images */}
      {imageUrls.length > 0 && (
        <Stack gap="sm">
          <Text fw={500}>Current Images:</Text>
          <Group>
            {imageUrls.map((imageUrl, index) => (
              <Stack key={index} gap="xs" align="center">
                <Image
                  src={getImageUrl(imageUrl)}
                  alt={`Product image ${index + 1}`}
                  w={120}
                  h={120}
                  fit="cover"
                  radius="md"
                  fallbackSrc="https://via.placeholder.com/120x120?text=Error"
                />
                <Group gap="xs">
                  {index === 0 && (
                    <Text size="xs" c="dimmed">
                      Primary
                    </Text>
                  )}
                  <ActionIcon
                    color="red"
                    variant="light"
                    onClick={() => handleDelete(imageUrl)}
                    size="sm"
                  >
                    <IconTrash size={14} />
                  </ActionIcon>
                </Group>
              </Stack>
            ))}
          </Group>
        </Stack>
      )}
    </Stack>
  );
};
