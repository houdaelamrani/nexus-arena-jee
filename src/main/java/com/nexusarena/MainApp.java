package com.nexusarena;

import com.nexusarena.model.EspaceArena;
import com.nexusarena.model.TypeEspace;
import com.nexusarena.service.ReservationService;
import com.nexusarena.util.DataInitializer;
import com.nexusarena.util.MigrationExecutor;
import com.nexusarena.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Application principale orchestrant le cycle de vie complet de l'application Nexus Arena (Étape 8 du TP).
 */
public class MainApp {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Démarrage de l'application Nexus Arena Capstone...");

        // 1. Initialisation de la fabrique JPA
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReservationPU");
        EntityManager em = emf.createEntityManager();

        try {
            // 2. Étape 6 : Exécution de la migration SQL de mise à jour v2.0
            MigrationExecutor migrationExecutor = new MigrationExecutor(
                    "jdbc:h2:mem:nexus_arena_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                    "sa",
                    ""
            );
            migrationExecutor.executeMigration("db/migration/v2_0_add_equipement_options.sql");

            // 3. Étape 3 : Initialisation des données de test
            DataInitializer initializer = new DataInitializer(em);
            initializer.initData();

            // 4. Étape 2 : Démonstration du Cache de Second Niveau (L2 Cache)
            demonstrationCacheSecondNiveau(emf);

            // 5. Étape 9 : Requête Criteria API pour rechercher les espaces Padel disponibles
            ReservationService reservationService = new ReservationService(em);
            List<EspaceArena> courts = reservationService.rechercherEspacesCriteria(
                    TypeEspace.COURT_PADEL,
                    4,
                    BigDecimal.valueOf(50.00)
            );
            System.out.println("\n--- RÉSULTAT CRITERIA API (Courts Padel <= 50€/h) ---");
            for (EspaceArena court : courts) {
                System.out.println("  • " + court.getNom() + " | " + court.getTarifHoraire() + " €/h | " + court.getEquipementInclus());
            }

            // 6. Étape 10 : Nouvelle réservation via le service métier
            LocalDateTime demainMatin = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
            LocalDateTime demainMidi = demainMatin.plusHours(2);
            reservationService.reserverEspace(1L, 1L, demainMatin, demainMidi, "Match Padel amical VIP");

            // 7. Étape 7 : Génération du rapport de performance
            PerformanceReport performanceReport = new PerformanceReport(em);
            performanceReport.printReport();

        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'exécution de l'application : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) em.close();
            if (emf != null && emf.isOpen()) emf.close();
            LOGGER.info("Application Nexus Arena arrêtée proprement.");
        }
    }

    /**
     * Démontre l'accès au cache L2 :
     * Chargement dans une première session, fermeture, puis rechargement dans une nouvelle session.
     */
    private static void demonstrationCacheSecondNiveau(EntityManagerFactory emf) {
        System.out.println("\n--- DÉMONSTRATION DU CACHE L2 (SECOND-LEVEL CACHE) ---");

        // Session 1 : Chargement depuis la base H2 -> mise en cache L2
        EntityManager em1 = emf.createEntityManager();
        System.out.println("Session 1 : Chargement de l'espace #1 (Requête SQL émise)");
        EspaceArena espace1 = em1.find(EspaceArena.class, 1L);
        System.out.println("Session 1 : Trouvé -> " + espace1.getNom());
        em1.close(); // Le cache de premier niveau est vidé !

        // Session 2 : Rechargement du même ID -> Servi directement par Ehcache L2 sans SQL !
        EntityManager em2 = emf.createEntityManager();
        System.out.println("Session 2 : Rechargement de l'espace #1 (Servi par Ehcache L2, AUCUN SQL)");
        EspaceArena espace2 = em2.find(EspaceArena.class, 1L);
        System.out.println("Session 2 : Trouvé via Cache L2 -> " + espace2.getNom());
        em2.close();
    }
}
