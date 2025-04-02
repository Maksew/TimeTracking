<template>
  <div class="app-container">
    <TopBar />
    <div class="choose-task-btn">
      <button @click="onChooseTasks">Choisir Tâches</button>
    </div>
    <TimerSection />
    <TaskCategories :timesheets="timesheets" />
  </div>
</template>

<script>
import TopBar from '@/components/mobile/TopBar.vue'
import TimerSection from '@/components/mobile/TimerSection.vue'
import TaskCategories from '@/components/mobile/TaskCategories.vue'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'MobileHome',
  components: {
    TopBar,
    TimerSection,
    TaskCategories
  },
  data() {
    return {
      timesheets: []
    }
  },
  mounted() {
    // Use the provided static JSON data for testing
    this.timesheets = [
      {
        "id": 1,
        "categoryName": "Personnel",
        "icon": "mdi-home",
        "timeSheetTasks": [
          {
            "id": 101,
            "name": "Rangement Maison",
            "icon": "mdi-broom",
            "time": "00:00:00",
            "completed": false
          },
          {
            "id": 102,
            "name": "Brossage des dents",
            "icon": "mdi-tooth",
            "time": "00:00:00",
            "completed": false
          }
        ]
      },
      {
        "id": 2,
        "categoryName": "Maison",
        "icon": "mdi-leaf",
        "timeSheetTasks": [
          {
            "id": 201,
            "name": "Tondre la pelouse",
            "icon": "mdi-grass",
            "time": "01:05:32",
            "completed": false
          }
        ]
      },
      {
        "id": 3,
        "categoryName": "ISIS Castres",
        "icon": "mdi-school",
        "timeSheetTasks": [
          {
            "id": 301,
            "name": "Révision Maths",
            "icon": "mdi-book-open-variant",
            "time": "01:41:53",
            "completed": false
          },
          {
            "id": 302,
            "name": "TD Réseau",
            "icon": "mdi-lan",
            "time": "00:00:00",
            "completed": false
          }
        ]
      }
    ]
  },
  methods: {
    onChooseTasks() {
      alert('Choisir Tâches clicked!')
      // Additional logic
    }
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  margin: 0;
  padding: 0;
  background: linear-gradient(to bottom, #1c1c2b, #000000);
  color: #fff;
  font-family: 'Arial', sans-serif;
}

.choose-task-btn {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.choose-task-btn button {
  background-color: #3d3da9;
  color: #fff;
  border: none;
  border-radius: 24px;
  padding: 12px 24px;
  font-size: 16px;
  cursor: pointer;
  font-weight: 600;
}

.choose-task-btn button:hover {
  background-color: #5656b8;
}
</style>

