<template>
  <header class="top-bar">
    <!-- Left icon: Settings -->
    <div class="icon-settings" @click="onSettingsClick">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        height="24"
        viewBox="0 0 24 24"
        width="24"
        fill="#fff"
      >
        <path d="M0 0h24v24H0V0z" fill="none" />
        <path
          d="M19.14 12.94c.04-.3.06-.61.06-.94s-.02-.64-.06-.94l2.03-1.58c.18-.14.23-.39.13-.59l-1.92-3.32
          a.495.495 0 0 0-.58-.22l-2.39.96a7.03 7.03 0 0 0-1.63-.94l-.36-2.57A.488.488 0 0 0 14.39 3h-3.78
          c-.25 0-.46.18-.49.43l-.36 2.57c-.59.24-1.14.55-1.63.94l-2.39-.96a.5.5 0 0 0-.58.22l-1.92 3.32
          c-.1.2-.05.45.13.59l2.03 1.58c-.05.3-.08.64-.08.94s.03.64.08.94l-2.03 1.58a.49.49 0 0 0-.13.59
          l1.92 3.32c.12.21.37.29.58.22l2.39-.96c.49.38 1.04.7 1.63.94l.36 2.57c.03.25.24.43.49.43h3.78
          c.25 0 .46-.18.49-.43l.36-2.57c.59-.24 1.14-.55 1.63-.94l2.39.96c.22.07.47-.01.58-.22l1.92-3.32
          c.1-.2.05-.45-.13-.59l-2.05-1.58zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5S10.07 8.5 12 8.5s3.5
          1.57 3.5 3.5-1.57 3.5-3.5 3.5z"
        />
      </svg>
    </div>

    <!-- Right icon: User -->
    <div class="icon-user" @click="onUserClick">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        height="24"
        viewBox="0 0 24 24"
        width="24"
        fill="#fff"
      >
        <path d="M0 0h24v24H0V0z" fill="none" />
        <path
          d="M12 12c2.14 0 3.86-1.72 3.86-3.86S14.14 4.29 12 4.29 8.14 6.01 8.14 8.14
          9.86 12 12 12zm0 2.14c-2.57 0-7.71 1.29-7.71 3.86v1.71h15.43v-1.71c0-2.57-5.14-3.86-7.72-3.86z"
        />
      </svg>
    </div>
  </header>
</template>

<script>
export default {
  name: 'TopBar',
  methods: {
    onSettingsClick() {
      alert('Settings clicked!')
    },
    onUserClick() {
      alert('User icon clicked!')
    }
  }
}
</script>

<style scoped>
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background-color: #24244f;
}

.icon-settings,
.icon-user {
  width: 24px;
  height: 24px;
  cursor: pointer;
}
</style>
