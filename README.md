# Time Tracking Application

## Description
Une application universelle de suivi du temps, conçue pour s'adapter à différents contextes d'utilisation (professionnel, éducatif, personnel). L'application permet de suivre manuellement le temps passé sur diverses tâches non automatisables et offre une visualisation claire des données recueillies.

## Structure du projet
Ce projet est organisé comme un monorepo contenant à la fois le frontend et le backend:

```
TimeTracking/
├── backend/                  → Module backend Spring Boot
│   ├── src/                  → Code source Java
│   └── pom.xml               → Configuration Maven du backend
├── frontend/                 → Module frontend Vue.js
│   ├── src/                  → Code source Vue.js
│   ├── node_modules/         → Dépendances npm (générées)
│   ├── package.json          → Dépendances et scripts npm
│   └── pom.xml               → Configuration Maven du frontend
├── .mvn/                     → Configuration Maven Wrapper
├── mvnw & mvnw.cmd           → Scripts Maven Wrapper
├── pom.xml                   → Configuration Maven parent
└── Procfile                  → Configuration pour déploiement cloud
```

## Fonctionnalités principales

### Pour les utilisateurs
- Création de feuilles de temps personnalisées
- Saisie manuelle du temps passé sur différentes tâches
- Consultation d'un tableau de bord personnel avec statistiques
- Rejoindre des groupes via codes d'invitation
- Exportation des données (CSV, PDF)
- Interface mobile-friendly pour une saisie rapide en déplacement

### Pour les responsables de groupe
- Création et gestion de groupes
- Attribution de rôles et permissions
- Paramétrage des feuilles de temps
- Visualisation des statistiques globales et par membre
- Génération de rapports d'activité

## Architecture technique
- **Frontend**: Vue.js avec Vuetify pour une interface responsive et moderne
- **Backend**: Spring Boot avec Spring Data JPA pour la gestion des données
- **Build System**: Maven pour la gestion des dépendances et le build du projet
- **Base de données**: H2 (en développement) avec une structure relationnelle optimisée
- **Architecture**: Application conçue selon le pattern MVC

## Prérequis
- Java 17 ou supérieur
- Maven (fourni via wrapper) ou IntelliJ IDEA

## Installation et exécution

### Cloner le projet
```bash
git clone https://github.com/Maksew/TimeTracking
```

## Build complet du projet

À la racine du projet, exécutez:
```bash
mvn clean install
```

Cette commande va :
- Installer node et npm automatiquement
- Construire le frontend avec les outils Vue.js
- Copier les fichiers frontend dans le backend
- Construire le backend

## Exécuter l'application complète

```bash
mvn --projects backend spring-boot:run
```

L'application fullstack est accessible sur :  <http://localhost:8989/>

Vous avez également accès à [la console d'administration H2](http://localhost:8989/h2-console) 
et à l'[API swagger](http://localhost:8989/swagger-ui/index.html)

### Développement Frontend
Pour faciliter le développement du frontend, on peut lancer le serveur de développement, qui "rafraîchit" automatiquement le front-end à chaque changement dans le code ! Pour cela (dans un nouveau terminal), se positionner dans le répertoire `frontend` et lancer :

```bash
cd frontend
npm run dev
```

## Déployer sur un cloud

[![Deploy to Koyeb](https://www.koyeb.com/static/images/deploy/button.svg)](https://app.koyeb.com/deploy?name=monorepo&repository=bastide%2Fmonorepo&branch=master&instance_type=free)

