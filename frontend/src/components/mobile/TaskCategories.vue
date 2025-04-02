<template>
  <div class="categories-container">
    <div
      v-for="(ts, idx) in timesheets"
      :key="ts.id"
      class="category-block"
    >
      <!-- Example: dynamic heading for category -->
      <h3
        class="category-title"
        :class="{ complete: incompleteCount(ts) === 0 }"
      >
        {{ getCategoryName(ts) }}
        <!-- Show incomplete count vs. "Tâches complètes!" -->
        <span v-if="incompleteCount(ts) > 0">
          - {{ incompleteCount(ts) }} tâches incomplètes
        </span>
        <span v-else>
          - Tâches complètes !
        </span>
      </h3>

      <!-- List tasks within this timesheet -->
      <div
        v-for="(task, tIdx) in ts.timeSheetTasks"
        :key="task.id || tIdx"
        class="task-card"
      >
        <div class="task-info">
          <div class="task-title">
            <!-- Left icon (Material Design icon) -->
            <i
              :class="[
                'mdi',
                task.icon ? task.icon : 'mdi-file-document-outline'
              ]"
              class="task-icon"
            ></i>
            <!-- Task name/title -->
            <span>{{ task.name || 'Unnamed Task' }}</span>
            <!-- Example sub-label for "X Tâches" or "1 Tâche" -->
            <span class="task-count" v-if="task.subCount">
              ({{ task.subCount }} Tâches)
            </span>
          </div>
          <!-- Display a time or any other info -->
          <div class="task-time">
            {{ task.time || '00:00:00' }}
          </div>
        </div>

        <!-- Right-side menu icon -->
        <div class="task-menu">
          <i class="mdi mdi-dots-vertical"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TaskCategories',
  props: {
    timesheets: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    // Example: get a "category name" for each timesheet
    // Adjust this logic to match your data
    getCategoryName(ts) {
      // If your timesheet has a field like "title" or "categoryName", use that
      // If not, you can build something like "Timesheet #ID" or "Entry Date"
      return ts.categoryName
        ? ts.categoryName
        : `Timesheet #${ts.id}`
    },

    // Example: count incomplete tasks. Adjust the logic to your data structure.
    // Maybe you have task.completed = true/false
    incompleteCount(ts) {
      if (!ts.timeSheetTasks) return 0
      // Example logic: tasks that are not completed
      // If your data doesn't track "completed", you can skip or do a dummy value
      return ts.timeSheetTasks.filter((t) => !t.completed).length
    }
  }
}
</script>

<style scoped>
.categories-container {
  margin: 20px;
}

/* Each timesheet as a "category block" */
.category-block {
  margin-bottom: 24px;
}

/* Category title styling */
.category-title {
  font-size: 18px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #fff;
}

/* If the category is "complete" (no incomplete tasks), highlight in green */
.category-title.complete {
  color: #00ff00;
}

/* A single task card */
.task-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #2b2b2b;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 8px;
  color: #fff;
}

/* The left side with icon and text */
.task-info {
  display: flex;
  flex-direction: column;
}

/* Title row that holds the icon, name, and optional subcount */
.task-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin: 0;
}

/* The icon itself */
.task-icon {
  font-size: 20px;
  color: #fff;
}

/* Subcount styling, e.g. "(5 Tâches)" */
.task-count {
  font-weight: normal;
  font-size: 0.9em;
  color: #bbb;
}

/* The time or other details below the main title */
.task-time {
  margin: 2px 0 0;
  font-size: 0.9em;
  color: #ccc;
}

/* Right side menu icon */
.task-menu {
  font-size: 1.2em;
  cursor: pointer;
  color: #ccc;
}
</style>
