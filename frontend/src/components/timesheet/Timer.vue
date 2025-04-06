// src/components/timesheet/Timer.vue
<template>
  <div class="timer-container">
    <div class="timer-display">{{ formattedTime }}</div>

    <div class="timer-controls">
      <v-btn
        v-if="!running"
        color="primary"
        icon
        @click="startTimer"
        class="control-btn"
      >
        <v-icon>mdi-play</v-icon>
      </v-btn>

      <v-btn
        v-else
        color="warning"
        icon
        @click="pauseTimer"
        class="control-btn"
      >
        <v-icon>mdi-pause</v-icon>
      </v-btn>

      <v-btn
        color="info"
        icon
        @click="resetTimer"
        class="control-btn"
      >
        <v-icon>mdi-refresh</v-icon>
      </v-btn>

      <v-btn
        color="success"
        icon
        @click="completeTask"
        class="control-btn"
      >
        <v-icon>mdi-check</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    initialSeconds: {
      type: Number,
      default: 0
    }
  },

  data() {
    return {
      seconds: this.initialSeconds,
      running: false,
      timerInterval: null
    }
  },

  computed: {
    formattedTime() {
      const hrs = Math.floor(this.seconds / 3600)
      const mins = Math.floor((this.seconds % 3600) / 60)
      const secs = this.seconds % 60

      return `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
    }
  },

  methods: {
    startTimer() {
      if (this.running) return

      this.running = true
      this.timerInterval = setInterval(() => {
        this.seconds++
      }, 1000)

      this.$emit('start')
    },

    pauseTimer() {
      this.running = false
      clearInterval(this.timerInterval)
      this.timerInterval = null

      this.$emit('pause', this.seconds)
    },

    resetTimer() {
      this.pauseTimer()
      this.seconds = 0

      this.$emit('reset')
    },

    completeTask() {
      this.pauseTimer()

      this.$emit('complete', this.seconds)
    }
  },

  beforeUnmount() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval)
    }
  }
}
</script>

<style scoped>
.timer-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
}

.timer-display {
  font-size: 2.5rem;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 16px;
}

.timer-controls {
  display: flex;
  gap: 12px;
}

.control-btn {
  transition: transform 0.2s;
}

.control-btn:hover {
  transform: scale(1.1);
}
</style>
