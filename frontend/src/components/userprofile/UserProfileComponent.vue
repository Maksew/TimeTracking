<template>
  <v-container fill-height>
    <v-card class="profile-card">
      <v-card-title class="d-flex align-center justify-space-between">
        <div v-if="isAuthenticated" class="d-flex align-center pa-2 avatar-container">
          <v-avatar size="36" color="#7986cb" class="mr-2">
            <span class="text-h6" v-if="user.pseudo">{{ user.pseudo.charAt(0).toUpperCase() }}</span>
          </v-avatar>
          <div>
            <h4 class="text-h6 font-weight-bold">{{ user.pseudo }}</h4>
            <p class="text-caption" style="opacity: 0.7; font-size: 0.875rem;">{{ user.email }}</p>
          </div>

          <!-- Botón "Modifier" en la misma línea que el avatar -->
          <v-btn v-if="!isEditing" color="purple" class="ml-auto" @click="startEditing">Modifier</v-btn>
        </div>
      </v-card-title>

      <v-card-text>
        <v-form v-if="isEditing">
          <v-row>
            <v-col cols="6">
              <v-text-field v-model="user.pseudo" label="Pseudo" variant="outlined" required />
            </v-col>
            <v-col cols="6">
              <v-text-field v-model="user.email" label="Email" variant="outlined" required />
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="6">
              <v-text-field
                v-model="user.currentPassword"
                label="Mot de passe actuel"
                :type="showPassword.current ? 'text' : 'password'"
                variant="outlined"
                prepend-inner-icon="mdi-key"
                @click:append-inner="togglePassword('current')"
                :append-inner-icon="showPassword.current ? 'mdi-eye-off' : 'mdi-eye'"
              />
            </v-col>
            <v-col cols="6">
              <v-text-field
                v-model="user.newPassword"
                label="Nouveau mot de passe"
                :type="showPassword.new ? 'text' : 'password'"
                variant="outlined"
                prepend-inner-icon="mdi-key"
                @click:append-inner="togglePassword('new')"
                :append-inner-icon="showPassword.new ? 'mdi-eye-off' : 'mdi-eye'"
              />
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="user.confirmPassword"
                label="Confirmer nouveau mot de passe"
                :type="showPassword.confirm ? 'text' : 'password'"
                variant="outlined"
                prepend-inner-icon="mdi-key"
                @click:append-inner="togglePassword('confirm')"
                :append-inner-icon="showPassword.confirm ? 'mdi-eye-off' : 'mdi-eye'"
              />
            </v-col>
          </v-row>

          <div class="d-flex justify-end mt-4">
            <v-btn variant="outlined" color="white" @click="cancelEdit">Annuler</v-btn>
            <v-btn color="purple" class="ml-2" @click="updateUserProfile">Sauvegarder</v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import { ref, reactive, onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";

export default {
  setup() {
    const authStore = useAuthStore();
    const user = reactive({
      pseudo: "",
      email: "",
      currentPassword: "",
      newPassword: "",
      confirmPassword: ""
    });

    const isEditing = ref(false); // Inicia como no editando
    const showPassword = reactive({ current: false, new: false, confirm: false });
    const isAuthenticated = ref(true);

    const apiUrl = "http://localhost:8989/api/users";
    const userId = authStore.user.id;
    const token = authStore.user.token;

    async function fetchUserData() {
      console.log("📡 Récupération des données utilisateur...");
      try {
        const response = await fetch(`${apiUrl}/${userId}`, {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` }
        });

        if (!response.ok) throw new Error(`Erreur API: ${response.status}`);

        const data = await response.json();
        Object.assign(user, data);
        console.log("✅ Données utilisateur récupérées :", data);
      } catch (error) {
        console.error("❌ Erreur lors de la récupération du profil :", error);
      }
    }

    function togglePassword(field) {
      showPassword[field] = !showPassword[field];
    }

    function cancelEdit() {
      isEditing.value = false;
      fetchUserData(); // Restaura los datos originales al cancelar
    }

    function startEditing() {
      isEditing.value = true; // Activa el modo de edición
    }

    onMounted(() => {
      fetchUserData();
    });

    return {
      user,
      isEditing,
      showPassword,
      togglePassword,
      isAuthenticated,
      updateUserProfile: () => {}, // Implementa la lógica de actualización aquí
      cancelEdit,
      startEditing
    };
  }
};
</script>

<style scoped>
.profile-card {
  max-width: 2000px;
  margin: auto;
  padding: 20px;
  background-color: #283593;
  color: white;
  height: 100%;
}

.avatar-container {
  margin-bottom: 16px;
  width: 100%;
}
</style>
