# Nexus Arena - Gestion & Réservation Complexe Sportif & E-Sport (JPA / Hibernate)

Ce projet est la version personnalisée du TP Capstone JEE, structuré autour de **Nexus Arena** (terrains de Padel, arène E-Sport, simulateurs VR SimRacing, terrains Futsal indoor).

## 📋 Prérequis & Technologies
- **JDK** : 17 ou supérieur
- **Maven** : 3.8+
- **JPA / Hibernate** : 5.6.15.Final (RESOURCE_LOCAL)
- **Cache L2** : Hibernate JCache + Ehcache 3.10.8
- **Base de Données** : H2 Database embarquée autonome (`jdbc:h2:mem:nexus_arena_db`)
- **Tests** : JUnit 5 (Jupiter 5.9.3)

## 🏗️ Structure du Projet

- `com.nexusarena.model`
  - `Membre` : Adhérent avec niveau (ROOKIE, PRO, VIP), `@Cacheable` et `@Version`.
  - `EspaceArena` : Court de Padel, Arène E-Sport, Simulateur VR, etc. `@Cacheable` et `@Version`.
  - `ReservationArena` : Réservation avec relations LAZY, montant et `@Version`.
  - `TypeEspace`, `StatutReservation` : Énumérations typées.
- `com.nexusarena.service`
  - `ReservationService` : Logique métier (vérification anti-chevauchement JPQL, recherche Criteria API, gestion des exceptions).
- `com.nexusarena.util`
  - `MigrationExecutor` : Exécution de scripts SQL de migration JDBC.
  - `DataInitializer` : Initialisation dynamique des données de test.
  - `PerformanceReport` : Statistiques Hibernate (L2 Cache hits/misses, requêtes, transactions).
- `com.nexusarena.MainApp` : Classe principale orchestrant l'application complète.

## 🚀 Lancement

### Lancer les tests unitaires
```bash
mvn test
```

### Lancer l'application de démonstration
```bash
mvn exec:java
```
