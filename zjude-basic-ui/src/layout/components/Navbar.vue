<template>
  <div class="navbar">
    <hamburger
      id="hamburger-container"
      :is-active="appStore.sidebar.opened"
      class="hamburger-container"
      @toggle-click="toggleSideBar"
    />
    <breadcrumb
      v-if="!settingsStore.topNav"
      id="breadcrumb-container"
      class="breadcrumb-container"
    />
    <top-nav
      v-if="settingsStore.topNav"
      id="topmenu-container"
      class="topmenu-container"
    />

    <div class="right-menu">
      <div class="avatar-container">
        <el-dropdown
          class="right-menu-item hover-effect"
          trigger="click"
          @command="handleCommand"
        >
          <div class="avatar-wrapper">
            <img
              :src="logo"
              class="user-avatar"
              alt="妙洁家政"
            />
            <el-icon><caret-bottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <router-link to="/user/profile">
                <el-dropdown-item>个人中心</el-dropdown-item>
              </router-link>
              <el-dropdown-item
                divided
                command="logout"
              >
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import logo from '@/assets/logo/logo.png'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'

const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

function toggleSideBar () {
  appStore.toggleSideBar()
}

function handleCommand (command) {
  switch (command) {
    case 'logout':
      logout()
      break
    default:
      break
  }
}

function logout () {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => { })
}

</script>

<style lang='scss' scoped>
.navbar {
  height: 56px;
  overflow: hidden;
  position: relative;
  background: #fff;
  border-bottom: 1px solid #f2ede9;
  box-shadow: 0 1px 3px rgba(31, 28, 26, 0.04);

  .hamburger-container {
    line-height: 52px;
    height: 100%;
    float: left;
    padding: 0 10px;
    cursor: pointer;
    color: #7a726c;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: #faf7f5;
      color: #e06a3d;
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 56px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 56px;
    display: flex;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #7a726c;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: #faf7f5;
        }
      }
    }

    .avatar-container {
      margin-right: 28px;

      .avatar-wrapper {
        margin-top: 8px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 38px;
          height: 38px;
          padding: 3px;
          object-fit: contain;
          border: 1px solid #ece6e1;
          border-radius: 10px;
          background: #fbf8f6;
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -18px;
          top: 22px;
          font-size: 12px;
          color: #a39b95;
        }
      }
    }
  }
}
</style>
