<template>
  <div class="settings-page-header">
    <div class="settings-page-header__title">
      <span>{{ title }}</span>
      <span
        v-if="hint"
        class="settings-page-header__hint"
      >
        {{ hint }}
      </span>
    </div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  hint: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
.settings-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.settings-page-header__title {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.settings-page-header__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 400;
}

@media (max-width: 768px) {
  .settings-page-header {
    display: block;
  }

  .settings-page-header__title {
    margin-bottom: 12px;
  }

  .settings-page-header__hint {
    display: block;
    margin-top: 6px;
  }
}
</style>
