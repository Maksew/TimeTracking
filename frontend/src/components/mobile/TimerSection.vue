<template>
  <div class="timer-section">
    <div class="timer-display">{{ currentTime }}</div>
    <button class="play-btn" @click="onPlayPause">
      <svg
        v-if="!isRunning"
        xmlns="http://www.w3.org/2000/svg"
        height="48"
        viewBox="0 0 24 24"
        width="48"
        fill="#fff"
      >
        <path d="M0 0h24v24H0z" fill="none" />
        <path d="M8 5v14l11-7z" />
      </svg>
      <svg
        v-else
        xmlns="http://www.w3.org/2000/svg"
        height="48"
        viewBox="0 0 24 24"
        width="48"
        fill="#fff"
      >
        <path d="M0 0h24v24H0z" fill="none" />
        <path d="M6 6h4v12H6zm8 0h4v12h-4z" />
      </svg>
    </button>
  </div>
</template>

<script>
export default {
  name: 'TimerSection',
  data() {
    return {
      isRunning: false,
      currentTime: '00:00:00'
    }
  },
  methods: {
    onPlayPause() {
      this.isRunning = !this.isRunning
      alert(this.isRunning ? 'Timer started' : 'Timer paused')
      // Add real timer logic here if needed
    }
  }
}
</script>

<style scoped>
.timer-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.timer-display {
  font-size: 36px;
  margin-bottom: 16px;
}

.play-btn {
  width: 60px;
  height: 60px;
  border: none;
  background: #3d3da9;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-btn:hover {
  background: #5656b8;
}
</style>
