import { Menu, Button } from '@mantine/core';
import { IconLanguage } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';

const languages = [
  { code: 'en', name: 'English', flag: '🇬🇧' },
  { code: 'tr', name: 'Türkçe', flag: '🇹🇷' },
];

export const LanguageSwitcher = () => {
  const { i18n } = useTranslation();

  const currentLanguage = languages.find((lang) => lang.code === i18n.language) || languages[0];

  const changeLanguage = (languageCode: string) => {
    i18n.changeLanguage(languageCode);
  };

  return (
    <Menu shadow="md" width={200}>
      <Menu.Target>
        <Button
          variant="subtle"
          leftSection={<IconLanguage size={18} />}
          rightSection={<span style={{ fontSize: '1.2em' }}>{currentLanguage.flag}</span>}
        >
          {currentLanguage.name}
        </Button>
      </Menu.Target>

      <Menu.Dropdown>
        <Menu.Label>Select Language</Menu.Label>
        {languages.map((language) => (
          <Menu.Item
            key={language.code}
            leftSection={<span style={{ fontSize: '1.5em' }}>{language.flag}</span>}
            onClick={() => changeLanguage(language.code)}
            style={{
              backgroundColor:
                currentLanguage.code === language.code ? 'var(--mantine-color-blue-light)' : undefined,
            }}
          >
            {language.name}
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
};
