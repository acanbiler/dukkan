import { Container, Title, SimpleGrid, Paper, Text, Group } from '@mantine/core';
import { IconBox, IconCategory } from '@tabler/icons-react';
import { AdminLayout } from '@/components/admin/AdminLayout';

export const AdminDashboard = () => {
  return (
    <AdminLayout>
      <Container size="xl">
        <Title order={2} mb="xl">
          Dashboard
        </Title>

        <SimpleGrid cols={{ base: 1, sm: 2, md: 3 }} spacing="lg">
          <Paper shadow="xs" p="xl" withBorder>
            <Group>
              <IconBox size={32} />
              <div>
                <Text size="xl" fw={700}>
                  Products
                </Text>
                <Text size="sm" c="dimmed">
                  Manage product catalog
                </Text>
              </div>
            </Group>
          </Paper>

          <Paper shadow="xs" p="xl" withBorder>
            <Group>
              <IconCategory size={32} />
              <div>
                <Text size="xl" fw={700}>
                  Categories
                </Text>
                <Text size="sm" c="dimmed">
                  Manage categories
                </Text>
              </div>
            </Group>
          </Paper>
        </SimpleGrid>
      </Container>
    </AdminLayout>
  );
};
