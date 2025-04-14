# TimeTracking - Application de Suivi du Temps

## 📝 Description
TimeTracking est une application universelle de suivi du temps, conçue pour répondre aux besoins des professionnels, éducateurs et particuliers. L’application permet de suivre manuellement le temps passé sur diverses tâches, d’exporter les rapports et d’obtenir une 
visualisation détaillée de l’activité. Les récentes mises à jour apportent notamment une meilleure gestion des groupes, l’optimisation de l’interface mobile et un déploiement simplifié en cloud et via Docker.

## 📂 Structure du projet
Ce projet est structuré en monorepo, regroupant à la fois le backend et le frontend :

```
TimeTracking/
├── backend/                  → Module backend Spring Boot (Java)
│   ├── src/                  → Code source Java
│   └── pom.xml               → Configuration Maven du backend
├── frontend/                 → Module frontend Vue.js
│   ├── src/                  → Code source Vue.js
│   ├── node_modules/         → Dépendances npm (générées)
│   ├── package.json          → Dépendances et scripts npm
│   └── pom.xml               → Configuration Maven intégrant le frontend
├── .mvn/                     → Configuration Maven Wrapper
├── mvnw & mvnw.cmd           → Scripts Maven Wrapper
├── pom.xml                   → Configuration Maven parent
├── Procfile                  → Configuration de déploiement cloud (Heroku/Koyeb)
├── koyeb.yaml                → Configuration spécifique pour Koyeb
├── Dockerfile                → Déploiement via conteneur Docker
└── README.md                 → Documentation du projet (ce fichier)
```

## ✨ Fonctionnalités principales

### Pour les utilisateurs
- Création de feuilles de temps personnalisées.
- Saisie manuelle du temps passé sur différentes tâches.
- Consultation d’un tableau de bord personnel avec statistiques détaillées.
- Rejoindre des groupes via codes d'invitation
- Exportation des données au format CSV et PDF.
- Interface mobile-friendly pour une saisie rapide en déplacement.

### Pour les responsables de groupe
- Création, gestion et configuration de groupes.
- Attribution de rôles et permissions affinées aux membres.
- Paramétrage personnalisé des feuilles de temps (par groupe ou utilisateur).
- Visualisation des statistiques globales et par membre.
- Génération automatique de rapports d'activité.

## 🔧 Architecture Technique

## Frontend

- **Framework :** Vue.js 3 avec Vuetify pour une interface responsive et moderne
- **État :** Pinia pour la gestion de l'état global
- **Routage :** Vue Router pour la navigation entre les vues
- **Visualisation :** Apex Charts pour les graphiques interactifs
- **Build et dépendances :** Gérés via npm et Maven

## Backend

- **Framework :** Spring Boot avec Spring Data JPA et Spring Security
- **Authentification :** JWT (JSON Web Tokens)
- **Base de données :**
    - **H2** en développement (structure relationnelle optimisée)
    - **PostgreSQL** en production

- **Documentation API :** Swagger UI

## Build System

- **Outil :** Utilisation de Maven (via wrapper) pour l'agrégation et la construction des deux modules

## 📋 Prérequis
- Java 21 ou supérieur (JDK)
- Maven: Inclus via le wrapper (mvnw ou mvnw.cmd)
- Node.js et npm: Pour le développement et la compilation du frontend (installés automatiquement par Maven)
- Docker (optionnel): Pour le déploiement en conteneur

## 🚀 Installation et exécution

### Cloner le projet
```bash
git clone https://github.com/Maksew/TimeTracking
cd TimeTracking
```

## Build complet du projet

À la racine du projet, exécutez:
```bash
./mvnw clean install
```

Cette commande va :
- Installer node et npm automatiquement
- Construire le frontend avec les outils Vue.js
- Copier les fichiers frontend dans le backend
- Construire le backend

## Exécuter l'application complète

```bash
./mvnw --projects backend spring-boot:run
```

L'application fullstack est accessible sur :  <http://localhost:8989/>

Vous avez également accès à [la console d'administration H2](http://localhost:8989/h2-console) 
et à l'[API swagger](http://localhost:8989/swagger-ui/index.html)

## Développement

### Développement Frontend
Pour faciliter le développement du frontend, on peut lancer le serveur de développement, qui "rafraîchit" automatiquement le front-end à chaque changement dans le code ! Pour cela (dans un nouveau terminal), se positionner dans le répertoire `frontend` et lancer :

```bash
cd frontend
npm run dev
```

Le serveur de développement sera accessible sur: http://localhost:5173/

### 📦 Déploiement via Docker

Construisez l’image Docker et lancez le conteneur :

```bash
docker build -t timetracking .
docker run -p 8989:8989 timetracking
```

### Déploiement sur Koyeb

Koyeb est une plateforme cloud qui facilite le déploiement d'applications conteneurisées.

#### Configuration pour Koyeb

L'application est déjà configurée pour Koyeb avec le fichier koyeb.yaml à la racine du projet:

```yaml
name: timetracking
service:
  app:
    dockerfile: Dockerfile
    ports:
      - port: 8989
        protocol: http
    env:
      - name: SPRING_PROFILES_ACTIVE
        value: "deploy"
      - name: JDBC_URI
        value: "jdbc:postgresql://db.pwyzzsvgwicqstwfebyb.supabase.co:5432/postgres"
      - name: DB_USERNAME
        value: "postgres"
      - name: DB_PASSWORD
        secret: SUPABASE_PASSWORD
      - name: SERVER_PORT
        value: "8989"
      - name: JAVA_TOOL_OPTIONS
        value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
      - name: SPRING_DATASOURCE_DRIVER_CLASS_NAME
        value: "org.postgresql.Driver"
      - name: SPRING_JPA_DATABASE_PLATFORM
        value: "org.hibernate.dialect.PostgreSQLDialect"
      - name: SPRING_H2_CONSOLE_ENABLED
        value: "false"
      - name: SPRING_JPA_HIBERNATE_DDL_AUTO
        value: "update"
      - name: SPRING_JPA_SHOW_SQL
        value: "false"
```
## Méthodes de déploiement

### Option 1: Déploiement automatique via bouton

[![Deploy to Koyeb](https://www.koyeb.com/static/images/deploy/button.svg)](https://app.koyeb.com/deploy?name=monorepo&repository=bastide%2Fmonorepo&branch=master&instance_type=free)

### Option 2: Utiliser Koyeb CLI

1. Ajouter vos secrets dans Koyeb:

```bash
koyeb secrets create SUPABASE_PASSWORD --value "votre-mot-de-passe-supabase"
```

2. Déployer l'application:

```bash 
koyeb app init --name timetracking --git github.com/Maksew/TimeTracking --git-branch main
```

### Option 3: Déploiement via l'interface web

1. Connectez-vous à votre compte Koyeb
2. Créez une nouvelle application
3. Sélectionnez "GitHub" comme source
4. Choisissez votre dépôt et configurez les variables d'environnement selon koyeb.yaml


## 📚 Ressources

[Documentation de Vue.js](https://vuejs.org/guide/introduction.html)

[Documentation de Sping Boot](https://docs.spring.io/spring-boot/)

[Documentation de Swagger](https://swagger.io/docs/) 

[Guide de déploiement sur Koyeb](https://www.koyeb.com/)


© 2025 TimeTracking. Tous droits réservés.



