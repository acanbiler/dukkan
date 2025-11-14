import { Container, Title, Text, Button, Stack } from '@mantine/core';
import { Link } from 'react-router-dom';
import { IconBox } from '@tabler/icons-react';

export const HomePage = () => {
  return (
    <Container size="md" mt="xl">
      <Stack align="center" gap="xl">
        <Title order={1} size="3rem">
          Welcome to Dukkan
        </Title>
        <Text size="xl" c="dimmed" ta="center">
          Your one-stop shop for all your needs
        </Text>
        <Button
          component={Link}
          to="/products"
          size="lg"
          leftSection={<IconBox size={20} />}
        >
          Browse Products
        </Button>
      </Stack>
    </Container>
  );
};
