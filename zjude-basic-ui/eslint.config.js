import neostandard from 'neostandard'
import pluginVue from 'eslint-plugin-vue'
import globals from 'globals'

const autoImportGlobals = {
  computed: 'readonly',
  createPinia: 'readonly',
  defineComponent: 'readonly',
  defineStore: 'readonly',
  getCurrentInstance: 'readonly',
  nextTick: 'readonly',
  onBeforeUnmount: 'readonly',
  onActivated: 'readonly',
  onMounted: 'readonly',
  reactive: 'readonly',
  ref: 'readonly',
  storeToRefs: 'readonly',
  toRefs: 'readonly',
  useRoute: 'readonly',
  useRouter: 'readonly',
  watch: 'readonly',
  watchEffect: 'readonly'
}

export default [
  {
    ignores: [
      'conf/**',
      'dist/**',
      'docker/**',
      'html/**',
      'node_modules/**',
      'public/**'
    ]
  },
  ...neostandard({
    files: ['**/*.{js,mjs,cjs,vue}'],
    noJsx: true
  }),
  ...pluginVue.configs['flat/recommended'],
  {
    files: ['**/*.{js,mjs,cjs,vue}'],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        ...autoImportGlobals
      }
    },
    rules: {
      'array-callback-return': 'warn',
      camelcase: 'warn',
      eqeqeq: 'warn',
      'no-unused-vars': 'warn',
      'vue/html-self-closing': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/no-mutating-props': 'warn',
      'vue/no-side-effects-in-computed-properties': 'warn',
      'vue/no-v-html': 'off',
      'vue/require-default-prop': 'off'
    }
  },
  {
    files: ['src/components/Crontab/result.vue'],
    rules: {
      'no-labels': 'off'
    }
  },
  {
    files: ['**/*.test.js', 'vitest.config.js'],
    languageOptions: {
      globals: globals.node
    }
  }
]
