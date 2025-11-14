import { ReactNode } from 'react';
import { AppShell, NavLink, Group, Title } from '@mantine/core';
import { useNavigate, useLocation } from 'react-router-dom';
import { IconBox, IconCategory, IconDashboard, IconArrowLeft } from '@tabler/icons-react';

interface AdminLayoutProps {
  children: ReactNode;
}

export const AdminLayout = ({ children }: AdminLayoutProps) => {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { path: '/admin', label: 'Dashboard', icon: IconDashboard },
    { path: '/admin/products', label: 'Products', icon: IconBox },
    { path: '/admin/categories', label: 'Categories', icon: IconCategory },
  ];

  return (
    <AppShell
      navbar={{ width: 250, breakpoint: 'sm' }}
      padding="md"
    >
      <AppShell.Navbar p="md">
        <Group mb="md">
          <Title order={4}>Admin Panel</Title>
        </Group>

        {navItems.map((item) => (
          <NavLink
            key={item.path}
            label={item.label}
            leftSection={<item.icon size={16} />}
            active={location.pathname === item.path}
            onClick={() => navigate(item.path)}
          />
        ))}

        <NavLink
          label="Back to Store"
          leftSection={<IconArrowLeft size={16} />}
          onClick={() => navigate('/')}
          mt="auto"
        />
      </AppShell.Navbar>

      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
};
