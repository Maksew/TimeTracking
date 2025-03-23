import { doAjaxRequestWithAuth } from '@/util/httpInterceptor'

export default {
  getAllTasks() {
    return doAjaxRequestWithAuth('/api/tasks')
  },

  getTaskById(id) {
    return doAjaxRequestWithAuth(`/api/tasks/${id}`)
  },

  getTasksByRepetition(repetition) {
    return doAjaxRequestWithAuth(`/api/tasks/repetition/${repetition}`)
  },

  createTask(task) {
    return doAjaxRequestWithAuth('/api/tasks', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task)
    })
  },

  updateTask(id, task) {
    return doAjaxRequestWithAuth(`/api/tasks/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(task)
    })
  },

  deleteTask(id) {
    return doAjaxRequestWithAuth(`/api/tasks/${id}`, {
      method: 'DELETE'
    })
  }
}
