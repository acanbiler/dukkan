# Internationalization (i18n) Guide

## Overview

Dukkan uses **react-i18next** for internationalization support. Currently supports:
- 🇬🇧 English (default)
- 🇹🇷 Turkish

## Setup

### Dependencies
```json
{
  "react-i18next": "^15.1.3",
  "i18next": "^24.2.0",
  "i18next-browser-languagedetector": "^8.0.2"
}
```

### Configuration
Located in: `frontend/src/i18n/config.ts`

Features:
- Automatic language detection from browser/localStorage
- Fallback to English
- Persistent language preference in localStorage

## Translation Files

### Location
- English: `frontend/src/i18n/locales/en.json`
- Turkish: `frontend/src/i18n/locales/tr.json`

### Structure
```json
{
  "common": {
    "loading": "Loading...",
    "error": "Error",
    ...
  },
  "auth": {
    "login": {
      "title": "Welcome back!",
      ...
    }
  },
  "products": { ... },
  "cart": { ... },
  "admin": { ... }
}
```

## Usage in Components

### Basic Usage
```tsx
import { useTranslation } from 'react-i18next';

export const MyComponent = () => {
  const { t } = useTranslation();

  return (
    <div>
      <h1>{t('common.loading')}</h1>
      <p>{t('auth.login.title')}</p>
    </div>
  );
};
```

### With Variables (Interpolation)
```tsx
// Translation:
// "welcome": "Welcome, {{name}}!"

const { t } = useTranslation();
<p>{t('auth.login.success', { name: user.firstName })}</p>
// Output: "Welcome, John!"
```

### Pluralization
```tsx
// Translation:
// "items": "{{count}} item",
// "items_plural": "{{count}} items"

<p>{t('cart.items', { count: 5 })}</p>
// Output: "5 items"
```

### In Form Validation
```tsx
const form = useForm({
  validate: {
    email: (value) =>
      /^\S+@\S+$/.test(value) ? null : t('validation.email'),
  },
});
```

## Language Switcher Component

### Usage
```tsx
import { LanguageSwitcher } from './components/layout/LanguageSwitcher';

<Header>
  <LanguageSwitcher />
</Header>
```

The component:
- Shows current language with flag
- Dropdown menu to switch languages
- Persists selection in localStorage

### Available in
`frontend/src/components/layout/LanguageSwitcher.tsx`

## Adding a New Language

### Step 1: Create Translation File
```bash
# Create new locale file
touch frontend/src/i18n/locales/fr.json
```

### Step 2: Add Translations
Copy structure from `en.json` and translate all strings to French.

### Step 3: Register in Config
```tsx
// frontend/src/i18n/config.ts
import fr from './locales/fr.json';

const resources = {
  en: { translation: en },
  tr: { translation: tr },
  fr: { translation: fr }, // Add new language
};

i18n.init({
  supportedLngs: ['en', 'tr', 'fr'], // Add to supported
});
```

### Step 4: Add to Language Switcher
```tsx
// frontend/src/components/layout/LanguageSwitcher.tsx
const languages = [
  { code: 'en', name: 'English', flag: '🇬🇧' },
  { code: 'tr', name: 'Türkçe', flag: '🇹🇷' },
  { code: 'fr', name: 'Français', flag: '🇫🇷' }, // Add new
];
```

## Adding New Translation Keys

### Step 1: Add to English
```json
// frontend/src/i18n/locales/en.json
{
  "products": {
    "newFeature": "New Feature Text"
  }
}
```

### Step 2: Add to Turkish
```json
// frontend/src/i18n/locales/tr.json
{
  "products": {
    "newFeature": "Yeni Özellik Metni"
  }
}
```

### Step 3: Use in Component
```tsx
<Text>{t('products.newFeature')}</Text>
```

## Best Practices

### 1. Organize by Feature
```json
{
  "products": { ... },
  "cart": { ... },
  "admin": {
    "products": { ... },
    "categories": { ... }
  }
}
```

### 2. Use Descriptive Keys
❌ Bad:
```json
{ "text1": "Welcome" }
```

✅ Good:
```json
{ "auth.login.title": "Welcome" }
```

### 3. Extract All Hardcoded Strings
Don't leave hardcoded text in JSX:

❌ Bad:
```tsx
<Button>Submit</Button>
```

✅ Good:
```tsx
<Button>{t('common.submit')}</Button>
```

### 4. Keep Translations Consistent
Use same terminology across all translations:
- "Product" → always "Product" (not "Item" sometimes)
- "Ürün" → always "Ürün" (Turkish)

### 5. Handle Missing Translations
i18next automatically:
- Falls back to English if translation missing
- Shows key path if both missing (e.g., "products.newFeature")

## Common Translation Patterns

### Buttons & Actions
```json
{
  "common": {
    "save": "Save",
    "cancel": "Cancel",
    "delete": "Delete",
    "edit": "Edit"
  }
}
```

### Error Messages
```json
{
  "errors": {
    "generic": "Something went wrong",
    "network": "Network error. Please check your connection.",
    "notFound": "Not found"
  }
}
```

### Validation
```json
{
  "validation": {
    "required": "This field is required",
    "minLength": "Minimum length is {{min}} characters",
    "email": "Invalid email address"
  }
}
```

## Testing Translations

### Manual Testing
1. Switch language using LanguageSwitcher
2. Navigate through all pages
3. Check forms, errors, notifications
4. Verify all text is translated

### Automated Testing (Future)
```tsx
import { render } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from '../i18n/config';

test('renders in Turkish', () => {
  i18n.changeLanguage('tr');
  const { getByText } = render(
    <I18nextProvider i18n={i18n}>
      <LoginPage />
    </I18nextProvider>
  );
  expect(getByText('Tekrar hoş geldiniz!')).toBeInTheDocument();
});
```

## Current Translation Coverage

✅ **Fully Translated:**
- Login Page
- Register Page
- Common UI elements
- Navigation
- Validation messages
- Error messages

⚠️ **Partially Translated:**
- Product pages (translations exist, components need updating)
- Cart page (translations exist, components need updating)
- Admin panel (translations exist, components need updating)

❌ **Not Translated:**
- Product detail pages
- Category pages
- Admin dashboard

## Roadmap

### Phase 1 (Completed)
- ✅ Setup i18next
- ✅ English translations
- ✅ Turkish translations
- ✅ Language switcher component
- ✅ Auth pages translated

### Phase 2 (Next)
- [ ] Translate all customer-facing pages
- [ ] Translate admin panel
- [ ] Add notifications translations
- [ ] Update AuthContext messages

### Phase 3 (Future)
- [ ] Add more languages (Arabic, German, French)
- [ ] RTL support for Arabic
- [ ] Backend i18n for emails
- [ ] Date/time/currency formatting per locale

## Language Support Checklist

When adding new page/feature:
- [ ] Extract all hardcoded strings
- [ ] Add English translations to `en.json`
- [ ] Add Turkish translations to `tr.json`
- [ ] Use `t()` function in all JSX
- [ ] Test language switching
- [ ] Update this guide if new patterns emerge

## Troubleshooting

### Translation Not Showing
1. Check translation key exists in JSON files
2. Verify JSON syntax is valid (no trailing commas)
3. Check console for i18next errors
4. Restart dev server after JSON changes

### Wrong Language Displayed
1. Clear localStorage: `localStorage.removeItem('i18nextLng')`
2. Check browser language settings
3. Force language: `i18n.changeLanguage('en')`

### Translation Not Updating
- i18next caches translations
- Restart dev server
- Hard refresh browser (Cmd+Shift+R)

## Resources

- [react-i18next Documentation](https://react.i18next.com/)
- [i18next Documentation](https://www.i18next.com/)
- [Translation JSON Schema](https://json.schemastore.org/i18next.json)

---

**Last Updated:** 2025-11-14
**Supported Languages:** English (en), Turkish (tr)
**Coverage:** ~40% (auth pages complete)
