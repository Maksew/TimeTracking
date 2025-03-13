<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const pseudo = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const loading = ref(false);
const errorMessage = ref('');

const register = async () => {
  if (!pseudo.value || !email.value || !password.value || !confirmPassword.value) {
    errorMessage.value = 'Veuillez remplir tous les champs';
    return;
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'Les mots de passe ne correspondent pas';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    await authStore.register({
      pseudo: pseudo.value,
      email: email.value,
      password: password.value
    });

    router.push('/');
  } catch (error) {
    errorMessage.value = error.message || 'Une erreur est survenue lors de l\'inscription';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="register-container pa-4">
    <v-card class="mx-auto" max-width="500" color="#283593">
      <v-card-title class="text-h4 text-center pt-6">Inscription</v-card-title>

      <v-card-text>
        <v-alert v-if="errorMessage" type="error" density="compact" class="mb-4">
          {{ errorMessage }}
        </v-alert>

        <v-form @submit.prevent="register">
          <v-text-field
            v-model="pseudo"
            label="Pseudo"
            variant="outlined"
            prepend-inner-icon="mdi-account"
            class="mb-2"
            bg-color="#1a237e"
          ></v-text-field>

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
            class="mb-2"
            bg-color="#1a237e"
          ></v-text-field>

          <v-text-field
            v-model="confirmPassword"
            label="Confirmer le mot de passe"
            variant="outlined"
            prepend-inner-icon="mdi-lock-check"
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
            S'inscrire
          </v-btn>
        </v-form>
      </v-card-text>

      <v-card-actions class="justify-center pb-6">
        <p class="text-center">
          Déjà un compte ?
          <v-btn variant="text" color="primary" to="/login">
            Se connecter
          </v-btn>
        </p>
      </v-card-actions>
    </v-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}
</style>
