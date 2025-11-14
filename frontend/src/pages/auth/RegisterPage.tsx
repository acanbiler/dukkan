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
  Group,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  const form = useForm({
    initialValues: {
      email: '',
      password: '',
      confirmPassword: '',
      firstName: '',
      lastName: '',
    },
    validate: {
      email: (value) => (/^\S+@\S+$/.test(value) ? null : t('auth.register.invalidEmail')),
      password: (value) => (value.length >= 6 ? null : t('auth.register.passwordMinLength')),
      confirmPassword: (value, values) =>
        value === values.password ? null : t('auth.register.passwordsDoNotMatch'),
      firstName: (value) => (value.length >= 2 ? null : t('auth.register.firstNameMinLength')),
      lastName: (value) => (value.length >= 2 ? null : t('auth.register.lastNameMinLength')),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      setLoading(true);
      await register({
        email: values.email,
        password: values.password,
        firstName: values.firstName,
        lastName: values.lastName,
      });
      navigate('/');
    } catch (error) {
      // Error handled in AuthContext
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container size={420} my={40}>
      <Title ta="center">{t('auth.register.title')}</Title>
      <Text c="dimmed" size="sm" ta="center" mt={5}>
        {t('auth.register.subtitle')}{' '}
        <Anchor component={Link} to="/login" size="sm">
          {t('auth.register.signIn')}
        </Anchor>
      </Text>

      <Paper withBorder shadow="md" p={30} mt={30} radius="md">
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <Stack gap="md">
            <Group grow>
              <TextInput
                label={t('auth.register.firstName')}
                placeholder={t('auth.register.firstNamePlaceholder')}
                required
                {...form.getInputProps('firstName')}
              />

              <TextInput
                label={t('auth.register.lastName')}
                placeholder={t('auth.register.lastNamePlaceholder')}
                required
                {...form.getInputProps('lastName')}
              />
            </Group>

            <TextInput
              label={t('auth.register.email')}
              placeholder={t('auth.register.emailPlaceholder')}
              required
              {...form.getInputProps('email')}
            />

            <PasswordInput
              label={t('auth.register.password')}
              placeholder={t('auth.register.passwordPlaceholder')}
              required
              {...form.getInputProps('password')}
            />

            <PasswordInput
              label={t('auth.register.confirmPassword')}
              placeholder={t('auth.register.confirmPasswordPlaceholder')}
              required
              {...form.getInputProps('confirmPassword')}
            />

            <Button type="submit" fullWidth loading={loading}>
              {t('auth.register.createAccount')}
            </Button>
          </Stack>
        </form>
      </Paper>
    </Container>
  );
};
