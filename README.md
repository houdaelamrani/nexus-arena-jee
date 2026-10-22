# 🏟️ Nexus Arena — TP Capstone JEE (JPA & Hibernate)

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Hibernate](https://img.shields.io/badge/Hibernate_ORM-5.6.15-59666C?style=for-the-badge&logo=hibernate&logoColor=white)](https://hibernate.org/)
[![JPA](https://img.shields.io/badge/JPA-2.2_Spec-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/technologies/persistence-jsp.html)
[![H2 Database](https://img.shields.io/badge/H2_Database-2.2-003B57?style=for-the-badge&logo=h2&logoColor=white)](https://www.h2database.com/)
[![Ehcache](https://img.shields.io/badge/Ehcache-3.10-D22128?style=for-the-badge&logo=redhat&logoColor=white)](https://www.ehcache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5.9.3-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

> **Projet Capstone JEE pur (sans framework Spring)** implémentant un système complet et performant de gestion et de réservation pour **Nexus Arena**, un complexe nouvelle génération mêlant infrastructures sportives (courts de Padel panoramiques, terrains indoor de Futsal) et installations e-sport professionnelles (booths 5v5 gaming haute performance, simulateurs dynamiques SimRacing VR).

---

## 📑 Table des Matières
- [Contexte Métier](#-contexte-métier)
- [Architecture & Arborescence](#-architecture--arborescence)
- [Les 11 Étapes du TP Réalisées](#-les-11-étapes-du-tp-réalisées)
  - [1. Modèle de données & Optimistic Locking](#1-modèle-de-données--optimistic-locking)
  - [2. Cache de second niveau (Ehcache 3 / JCache)](#2-cache-de-second-niveau-ehcache-3--jcache)
  - [3. Jeu de données dynamique et réaliste](#3-jeu-de-données-dynamique-et-réaliste)
  - [4. Scénarios de tests automatisés (JUnit 5)](#4-scénarios-de-tests-automatisés-junit-5)
  - [5 & 6. Migration SQL et exécuteur JDBC](#5--6-migration-sql-et-exécuteur-jdbc)
  - [7. Rapport de performance Hibernate Statistics](#7-rapport-de-performance-hibernate-statistics)
  - [8. Application principale orchestratrice](#8-application-principale-orchestratrice)
  - [9. Requêtes avancées : JPQL & Criteria API](#9-requêtes-avancées--jpql--criteria-api)
  - [10. Transactions & Isolation](#10-transactions--isolation)
  - [11. Journalisation JEE](#11-journalisation-jee)
- [Prérequis & Installation](#-prérequis--installation)
- [Exécution](#-exécution)
- [Exemple de Rapport de Performance](#-exemple-de-rapport-de-performance)
- [Auteur](#-auteur)

---

## 🎯 Contexte Métier

Le complexe **Nexus Arena** met à disposition des adhérents plusieurs types d'espaces :
- **Courts de Padel panoramiques** (éclairage LED anti-éblouissement, gazon WPT, vitres 12mm sans montant).
- **Arènes E-Sport 5v5** (booths insonorisés, PC RTX 4090, écrans 360Hz, fibre symétrique dédiée).
- **Simulateurs de pilotage VR SimRacing** (cockpit dynamique D-BOX à 4 vérins hydrauliques, volant Fanatec Direct Drive, casque VR haute fidélité).
- **Terrains de Futsal indoor** (parquet amortissant compétition et caméras pour rediffusion automatique).

Chaque réservation contrôle les conflits d'horaires, calcule le montant selon la tarification horaire, et supporte la concurrence d'accès grâce au verrouillage optimiste.

---

## 🏗 Architecture & Arborescence

```text
nexus-arena-jee/
├── pom.xml                                   # Dépendances Maven (Hibernate 5.6, JCache, Ehcache 3, H2, JUnit 5)
├── README.md                                 # Documentation du projet
├── src/
│   ├── main/
│   │   ├── java/com/nexusarena/
│   │   │   ├── MainApp.java                  # Point d'entrée orchestrant le cycle de vie complet
│   │   │   ├── model/
│   │   │   │   ├── TypeEspace.java           # Énumération des types d'espaces (PADEL, ESPORT, etc.)
│   │   │   │   ├── StatutReservation.java    # Énumération des statuts (CONFIRMEE, ANNULEE, etc.)
│   │   │   │   ├── Membre.java               # Entité adhérent (@Entity, @Cacheable, @Version)
│   │   │   │   ├── EspaceArena.java          # Entité infrastructure (@Entity, @Cacheable, @Version)
│   │   │   │   └── ReservationArena.java     # Entité réservation (relations LAZY, @Version)
│   │   │   ├── service/
│   │   │   │   └── ReservationService.java   # Logique métier, JPQL, Criteria API, gestion rollback
│   │   │   └── util/
│   │   │       ├── DataInitializer.java      # Insertion des données de démonstration
│   │   │       ├── MigrationExecutor.java    # Exécution JDBC directe de scripts SQL de migration
│   │   │       └── PerformanceReport.java    # Collecte et affichage des métriques Hibernate
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── persistence.xml           # Configuration JPA RESOURCE_LOCAL, dialecte H2, cache L2
│   │       ├── ehcache.xml                   # Régions de mémoire cache L2 et query cache
│   │       └── db/migration/
│   │           └── v2_0_add_equipement_options.sql # Script SQL de mise à jour du schéma
│   └── test/
│       └── java/com/nexusarena/test/
│           └── ReservationArenaTest.java     # 4 Scénarios de tests JUnit 5 validés à 100%
```

---

## 🚀 Les 11 Étapes du TP Réalisées

### 1. Modèle de données & Optimistic Locking
- **`Membre`** : Représente les utilisateurs avec un système de fidélité (`ROOKIE`, `PRO`, `VIP`), email unique et contraintes `@Column(nullable = false)`.
- **`EspaceArena`** : Représente les terrains ou booths avec tarif horaire décimal (`BigDecimal`), capacité et statut actif.
- **`ReservationArena`** : Relations `@ManyToOne(fetch = FetchType.LAZY)` pour éviter les chargements EAGER inutiles.
- **Optimistic Locking** : Toutes les entités intègrent `@Version private int version;` pour prévenir les pertes de mise à jour concurrentes sans verrouiller la base.

### 2. Cache de second niveau (Ehcache 3 / JCache)
- Configuration activée dans `persistence.xml` :
  ```xml
  <property name="hibernate.cache.use_second_level_cache" value="true"/>
  <property name="hibernate.cache.region.factory_class" value="org.hibernate.cache.jcache.JCacheRegionFactory"/>
  <property name="hibernate.javax.cache.provider" value="org.ehcache.jsr107.EhcacheCachingProvider"/>
  <property name="hibernate.cache.use_query_cache" value="true"/>
  ```
- Entités annotées `@Cacheable` et `@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)`.
- Fichier `ehcache.xml` dédié définissant les pools de stockage heap et TTL pour les entités et le cache de requêtes.

### 3. Jeu de données dynamique et réaliste
- La classe `DataInitializer` utilise des dates dynamiques relatives (`LocalDateTime.now().plusDays(...)`).
- Initialise 4 profils de membres, 5 infrastructures complètes (Padel, E-Sport, SimRacing, Futsal) et 3 réservations types.

### 4. Scénarios de tests automatisés (JUnit 5)
La classe `ReservationArenaTest` valide rigoureusement :
1. **Création CRUD** : Persistance et validation des identifiants et associations.
2. **Validation du Cache L2** : Vérifie que le rechargement d'une entité dans une deuxième session est servi par le cache sans émettre de requête SQL (`hitsAprès > hitsAvant`).
3. **Détection de conflits** : Vérifie qu'une tentative de réservation sur un créneau déjà occupé lève une exception métier.
4. **Collision Optimiste** : Simule deux sessions modifiant simultanément la même entité et valide la levée de l'`OptimisticLockException` (encapsulée par `RollbackException`).

### 5 & 6. Migration SQL et exécuteur JDBC
- Script SQL autonome `v2_0_add_equipement_options.sql` ajoutant des colonnes d'équipements (`options_equipement`, `notes_techniques`) et un index `idx_membre_telephone`.
- Classe `MigrationExecutor` exécutant le script par JDBC en amont du cycle JPA, avec découpage des instructions SQL et gestion transactionnelle.

### 7. Rapport de performance Hibernate Statistics
- Activation via `<property name="hibernate.generate_statistics" value="true"/>`.
- Classe `PerformanceReport` exploitant `session.getSessionFactory().getStatistics()` pour afficher le ratio de hit L2, le temps de requête maximal, les transactions et le détail par région de cache.

### 8. Application principale orchestratrice
- `MainApp` assemble et exécute automatiquement :
  1. La migration JDBC v2.0
  2. L'initialisation du jeu de données
  3. La démonstration en direct du cache L2
  4. La recherche par Criteria API
  5. La réservation via le service métier
  6. Le rapport de performance global

### 9. Requêtes avancées : JPQL & Criteria API
- **JPQL paramétré** (sécurisé contre les injections SQL) pour la détection de chevauchement de plages horaires :
  ```sql
  SELECT COUNT(r) FROM ReservationArena r
  WHERE r.espace.id = :espaceId AND r.statut <> :annule
    AND r.dateDebut < :fin AND r.dateFin > :debut
  ```
- **Criteria API** : Construction dynamique de requêtes multi-critères (filtrage par type d'espace, capacité minimale et budget maximum avec tri ascendant).

### 10. Transactions & Isolation
- Transactions JPA `RESOURCE_LOCAL` gérées rigoureusement avec blocs `try / catch / rollback`.
- Détection et capture propre des exceptions `OptimisticLockException`.

### 11. Journalisation JEE
- Utilisation de `java.util.logging.Logger` avec messages contextuels structurés (`INFO`, `WARNING`, `SEVERE`).

---

## 💻 Prérequis & Installation

- **JDK** : Version 11 ou supérieure (testé et validé avec JDK 17 et JDK 23)
- **Maven** : Version 3.8+
- **Git**

Cloner le dépôt :
```bash
git clone https://github.com/houdaelamrani/nexus-arena-jee.git
cd nexus-arena-jee
```

---

## ⚡ Exécution

### 1. Lancer les tests unitaires
```bash
mvn clean test
```
*Résultat attendu : 4 tests exécutés avec succès (0 échec, 0 erreur).*

### 2. Lancer l'application complète
```bash
mvn exec:java
```

---

## 📊 Exemple de Rapport de Performance

```text
=======================================================
   RAPPORT D'ANALYSE DE PERFORMANCE - NEXUS ARENA
=======================================================
► Requêtes exécutées           : 2
► PreparedStatements préparés  : 16
► Requête la plus lente        : SELECT COUNT(r) FROM ReservationArena r ... (6 ms)
-------------------------------------------------------
 [CACHE DE SECOND NIVEAU - L2 CACHE]
► Cache L2 Hits (succès)       : 1
► Cache L2 Misses (échecs)     : 1
► Cache L2 Puts (insertions)   : 1
► Taux d'efficacité L2 (Hit %) : 50.00 %
-------------------------------------------------------
 [CACHE DE REQUÊTES - QUERY CACHE]
► Query Cache Hits             : 0
► Query Cache Misses           : 1
► Query Cache Puts             : 1
-------------------------------------------------------
 [TRANSACTIONS & SESSIONS]
► Transactions validées        : 2
► Total transactions           : 2
► Sessions ouvertes            : 3
► Sessions fermées             : 2
   • Région [com.nexusarena.model.EspaceArena] -> Hits: 1, Misses: 1, Puts: 1
=======================================================
```

---

## 👩‍💻 Auteur

**Houda Elamrani**  
- GitHub : [@houdaelamrani](https://github.com/houdaelamrani)  
- Projet : [nexus-arena-jee](https://github.com/houdaelamrani/nexus-arena-jee)
