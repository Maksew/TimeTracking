import './assets/main.css' // optional global styles

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css' // For MDI icons

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth';

const vuetify = createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'dark', // or 'light', if you prefer
    themes: {
      dark: {
        colors: {
          primary: '#3F51B5',   // Indigo
          secondary: '#9C27B0', // Purple
          accent: '#E91E63',    // Pink
          error: '#F44336',
          warning: '#FF9800',
          info: '#2196F3',
          success: '#4CAF50',
          background: '#081a3c', // Dark blue background
        },
      },
    },
  },
})

const app = createApp(App)

const pinia = createPinia();
app.use(pinia);

const authStore = useAuthStore();
authStore.initAuth();

app.use(router)
app.use(vuetify)
app.mount('#app')


