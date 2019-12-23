const typescriptEslintRecommended = require('@typescript-eslint/eslint-plugin/dist/configs/recommended.json');
const typescriptEslintPrettier = require('eslint-config-prettier/@typescript-eslint');

module.exports = {
    extends: ['@react-native-community'],
    overrides: [
        {
            files: ['*.ts', '*.tsx'],
            // Apply the recommended Typescript defaults and the prettier overrides to all Typescript files
            rules: Object.assign(typescriptEslintRecommended.rules, typescriptEslintPrettier.rules, {
                '@typescript-eslint/explicit-member-accessibility': 'off',
                '@typescript-eslint/no-empty-function': 'off',
                'jsx-quotes': 'off',
                '@typescript-eslint/explicit-function-return-type': 'off',
                '@typescript-eslint/no-use-before-define': 'off',
                '@typescript-eslint/no-explicit-any': 'off',
                '@typescript-eslint/interface-name-prefix': 'off',
                '@typescript-eslint/no-unused-vars': 'off',
            }),
        },
        {
            files: ['example/**/*.ts', 'example/**/*.tsx'],
            rules: {
                // Turn off rules which are useless and annoying for the example files
                '@typescript-eslint/explicit-function-return-type': 'off',
                'react-native/no-inline-styles': 'off',
            },
        },
    ],
};
