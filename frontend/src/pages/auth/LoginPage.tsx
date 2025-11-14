import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Container,
  Paper,
  Title,
  TextInput,
  PasswordInput,
  Button,
  Stack,
  Text,
  Anchor,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';

export const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  const form = useForm({
    initialValues: {
      email: '',
      password: '',
    },
    validate: {
      email: (value) => (/^\S+@\S+$/.test(value) ? null : t('auth.login.invalidEmail')),
      password: (value) => (value.length >= 6 ? null : t('auth.login.passwordMinLength')),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      setLoading(true);
      await login(values);
      navigate('/');
    } catch (error) {
      // Error handled in AuthContext
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container size={420} my={40}>
      <Title ta="center">{t('auth.login.title')}</Title>
      <Text c="dimmed" size="sm" ta="center" mt={5}>
        {t('auth.login.subtitle')}{' '}
        <Anchor component={Link} to="/register" size="sm">
          {t('auth.login.createAccount')}
        </Anchor>
      </Text>

      <Paper withBorder shadow="md" p={30} mt={30} radius="md">
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <Stack gap="md">
            <TextInput
              label={t('auth.login.email')}
              placeholder={t('auth.login.emailPlaceholder')}
              required
              {...form.getInputProps('email')}
            />

            <PasswordInput
              label={t('auth.login.password')}
              placeholder={t('auth.login.passwordPlaceholder')}
              required
              {...form.getInputProps('password')}
            />

            <Button type="submit" fullWidth loading={loading}>
              {t('auth.login.signIn')}
            </Button>
          </Stack>
        </form>
      </Paper>
    </Container>
  );
};
