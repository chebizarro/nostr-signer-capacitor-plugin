import tseslint from 'typescript-eslint';

export default tseslint.config(
  {
    ignores: ['build/**', 'dist/**', 'example-app/**', '.build/**', 'android/build/**', 'nostr-sample-app/**'],
  },
  ...tseslint.configs.recommended,
  {
    rules: {
      'no-fallthrough': 'off',
      'no-constant-condition': 'off',
      '@typescript-eslint/no-this-alias': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/explicit-module-boundary-types': ['warn', { allowArgumentsExplicitlyTypedAsAny: true }],
    },
  },
);
