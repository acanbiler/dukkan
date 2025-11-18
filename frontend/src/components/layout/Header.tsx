import { useState } from 'react';
import { Group, Title, Button, Menu, Avatar, Text } from '@mantine/core';
import { Link } from 'react-router-dom';
import {
  IconShoppingCart,
  IconHome,
  IconBox,
  IconCategory,
  IconSettings,
  IconUser,
  IconLogout,
  IconLogin,
  IconPackage,
} from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@/context/AuthContext';
import { CartIcon } from '@/components/cart/CartIcon';
import { CartDrawer } from '@/components/cart/CartDrawer';
import { LanguageSwitcher } from './LanguageSwitcher';

export const Header = () => {
  const { t } = useTranslation();
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const [cartDrawerOpened, setCartDrawerOpened] = useState(false);

  return (
    <>
      <Group h="100%" px="md" justify="space-between">
        <Group>
          <IconShoppingCart size={28} />
          <Title order={3}>Dukkan</Title>
        </Group>

        <Group>
          <Button component={Link} to="/" variant="subtle" leftSection={<IconHome size={16} />}>
            {t('nav.home')}
          </Button>
          <Button component={Link} to="/products" variant="subtle" leftSection={<IconBox size={16} />}>
            {t('nav.products')}
          </Button>
          <Button component={Link} to="/categories" variant="subtle" leftSection={<IconCategory size={16} />}>
            {t('nav.categories')}
          </Button>

          {isAuthenticated && (
            <Button component={Link} to="/orders" variant="subtle" leftSection={<IconPackage size={16} />}>
              {t('nav.orders')}
            </Button>
          )}

          {isAdmin && (
            <Button component={Link} to="/admin" variant="subtle" leftSection={<IconSettings size={16} />}>
              {t('nav.admin')}
            </Button>
          )}

          <CartIcon onClick={() => setCartDrawerOpened(true)} />

          <LanguageSwitcher />

          {isAuthenticated ? (
            <Menu shadow="md" width={200}>
              <Menu.Target>
                <Button variant="subtle" leftSection={<Avatar size={24} radius="xl" />}>
                  {user?.firstName}
                </Button>
              </Menu.Target>

              <Menu.Dropdown>
                <Menu.Label>
                  {user?.email}
                </Menu.Label>
                <Menu.Item leftSection={<IconUser size={14} />} component={Link} to="/profile">
                  {t('nav.profile')}
                </Menu.Item>
                <Menu.Divider />
                <Menu.Item
                  color="red"
                  leftSection={<IconLogout size={14} />}
                  onClick={logout}
                >
                  {t('nav.logout')}
                </Menu.Item>
              </Menu.Dropdown>
            </Menu>
          ) : (
            <Button component={Link} to="/login" variant="filled" leftSection={<IconLogin size={16} />}>
              {t('nav.login')}
            </Button>
          )}
        </Group>
      </Group>

      <CartDrawer opened={cartDrawerOpened} onClose={() => setCartDrawerOpened(false)} />
    </>
  );
};
