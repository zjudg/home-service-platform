import pluginVue from 'eslint-plugin-vue'

export default [
  ...pluginVue.configs['flat/recommended'],
  {
    ignores: ['node_modules/**', 'dist/**', 'unpackage/**'],
    rules: {
      'no-console': 'warn',
      'vue/multi-word-component-names': 'off'
    }
  }
]
