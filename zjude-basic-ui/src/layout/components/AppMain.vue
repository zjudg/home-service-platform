<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <transition
        name="fade-transform"
        mode="out-in"
      >
        <keep-alive :include="tagsViewStore.cachedViews">
          <component
            :is="Component"
            v-if="!route.meta.link"
            :key="route.path"
          />
        </keep-alive>
      </transition>
    </router-view>
    <iframe-toggle />
  </section>
</template>

<script setup>
import iframeToggle from './IframeToggle/index'
import useTagsViewStore from '@/store/modules/tagsView'

const tagsViewStore = useTagsViewStore()
</script>

<style lang="scss" scoped>
.app-main {
  /* 56= navbar  */
  min-height: calc(100vh - 56px);
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #faf7f5;
}

.fixed-header + .app-main {
  padding-top: 56px;
}

.hasTagsView {
  .app-main {
    /* 94 = navbar + tags-view = 56 + 38 */
    min-height: calc(100vh - 94px);
  }

  .fixed-header + .app-main {
    padding-top: 94px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #faf7f5;
}

::-webkit-scrollbar-thumb {
  background-color: #d8cfc8;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: #c4bcb6;
}
</style>
