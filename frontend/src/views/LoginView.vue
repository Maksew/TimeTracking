<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');

const login = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'Veuillez remplir tous les champs';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    await authStore.login({
      email: email.value,
      password: password.value
    });

    router.push('/');
  } catch (error) {
    errorMessage.value = error.message || 'Une erreur est survenue lors de la connexion';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container pa-4">
    <v-card class="mx-auto" max-width="500" color="#283593">
      <v-card-title class="text-h4 text-center pt-6">Connexion</v-card-title>

      <v-card-text>
        <v-alert v-if="errorMessage" type="error" density="compact" class="mb-4">
          {{ errorMessage }}
        </v-alert>

        <v-form @submit.prevent="login">
          <v-text-field
            v-model="email"
            label="Email"
            variant="outlined"
            prepend-inner-icon="mdi-email"
            type="email"
            class="mb-2"
            bg-color="#1a237e"
          ></v-text-field>

          <v-text-field
            v-model="password"
            label="Mot de passe"
            variant="outlined"
            prepend-inner-icon="mdi-lock"
            type="password"
            class="mb-4"
            bg-color="#1a237e"
          ></v-text-field>

          <v-btn
            block
            color="primary"
            size="large"
            type="submit"
            :loading="loading"
            class="mb-3"
          >
            Se connecter
          </v-btn>
        </v-form>
      </v-card-text>

      <v-card-actions class="justify-center pb-6">
        <p class="text-center">
          Pas encore de compte ?
          <v-btn variant="text" color="primary" to="/register">
            S'inscrire
          </v-btn>
        </p>
      </v-card-actions>
    </v-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}
</style>
