<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="app-container">
    <header>
      <h1>Mon Application</h1>
      <RouterLink to="/">Accueil</RouterLink>
    </header>

    <main>
      <RouterView />
    </main>

    <footer>
      © 2025 - Mon Application
    </footer>
  </div>
</template>

<style>
#app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

header {
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  color: var(--color-heading);
}

main {
  flex: 1;
  padding: 1rem;
}

footer {
  background: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
  text-align: center;
  padding: 1rem;
  color: var(--color-text);
}
</style>
