package com.nexusarena.test;

import com.nexusarena.model.*;
import com.nexusarena.service.ReservationService;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scénarios de tests automatisés JUnit 5 pour Nexus Arena (Étape 4 du TP).
 * Valide les réservations, le cache L2, la détection de conflits et le verrouillage optimiste.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationArenaTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    public static void initFactory() {
        emf = Persistence.createEntityManagerFactory("ReservationPU");
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    public void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @AfterAll
    public static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test 1 : Création réussie d'un membre, d'un espace et d'une réservation")
    public void testCreateReservation() {
        em.getTransaction().begin();

        Membre membre = new Membre("Karim Gaming", "karim.gamer@nexusarena.com", "0699887766", "PRO");
        em.persist(membre);

        EspaceArena espace = new EspaceArena("Booth E-Sport Solo 01", TypeEspace.ESPORT_BOOTH, 1,
                BigDecimal.valueOf(15.00), "PC RTX 4080");
        em.persist(espace);

        LocalDateTime debut = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0);
        LocalDateTime fin = debut.plusHours(2);

        ReservationArena reservation = new ReservationArena(membre, espace, debut, fin);
        reservation.setMontantTotal(BigDecimal.valueOf(30.00));
        em.persist(reservation);

        em.getTransaction().commit();

        assertNotNull(reservation.getId(), "L'ID de réservation devrait être généré");
        assertEquals(0, reservation.getVersion(), "La version initiale pour l'optimistic lock doit être 0");
        assertEquals("Karim Gaming", reservation.getMembre().getNom());
    }

    @Test
    @Order(2)
    @DisplayName("Test 2 : Validation du Cache de Second Niveau (L2 Cache Hit)")
    public void testSecondLevelCacheHit() {
        // 1. Insertion d'une entité dans une transaction
        em.getTransaction().begin();
        EspaceArena espace = new EspaceArena("Simulateur Pimax VR", TypeEspace.SIMULATEUR_VR, 2,
                BigDecimal.valueOf(50.00), "Cockpit GT3");
        em.persist(espace);
        em.getTransaction().commit();
        Long espaceId = espace.getId();
        em.close(); // Fermeture de la session 1 -> le cache L1 est détruit

        // Récupérer les statistiques Hibernate
        Session session = emf.createEntityManager().unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        long hitsAvant = stats.getSecondLevelCacheHitCount();

        // 2. Première lecture dans Session A -> alimente le cache L2
        EntityManager emA = emf.createEntityManager();
        EspaceArena loadedA = emA.find(EspaceArena.class, espaceId);
        assertNotNull(loadedA);
        emA.close();

        // 3. Deuxième lecture dans Session B -> doit être servie par le cache de second niveau !
        EntityManager emB = emf.createEntityManager();
        EspaceArena loadedB = emB.find(EspaceArena.class, espaceId);
        assertNotNull(loadedB);
        emB.close();

        long hitsApres = stats.getSecondLevelCacheHitCount();
        assertTrue(hitsApres > hitsAvant, "Le cache L2 aurait dû enregistrer au moins un Hit supplémentaire !");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3 : Détection de conflit de créneau (chevauchement)")
    public void testConflictSlotDetection() {
        em.getTransaction().begin();
        Membre m = new Membre("Julien Test", "julien.test@nexusarena.com", "0600112233", "ROOKIE");
        em.persist(m);
        EspaceArena court = new EspaceArena("Court Padel Indoor 3", TypeEspace.COURT_PADEL, 4,
                BigDecimal.valueOf(40.00), "Court rapide");
        em.persist(court);
        em.getTransaction().commit();

        ReservationService service = new ReservationService(em);
        LocalDateTime debut1 = LocalDateTime.now().plusDays(10).withHour(14).withMinute(0);
        LocalDateTime fin1 = debut1.plusHours(2); // 14h00 à 16h00

        // 1ère réservation réussie
        service.reserverEspace(m.getId(), court.getId(), debut1, fin1, "Réservation initiale");

        // 2ème réservation qui chevauche (15h00 à 17h00) -> doit échouer
        LocalDateTime debut2 = LocalDateTime.now().plusDays(10).withHour(15).withMinute(0);
        LocalDateTime fin2 = debut2.plusHours(2);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.reserverEspace(m.getId(), court.getId(), debut2, fin2, "Réservation conflictuelle");
        });

        assertTrue(exception.getMessage().contains("indisponible"), "L'exception doit indiquer l'indisponibilité du créneau");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4 : Détection de collision concurrente avec Optimistic Locking (@Version)")
    public void testOptimisticLockingConflict() {
        // Création initiale
        em.getTransaction().begin();
        EspaceArena espace = new EspaceArena("Arène Futsal B", TypeEspace.FUTSAL_ARENA, 10,
                BigDecimal.valueOf(75.00), "Terrain annexe");
        em.persist(espace);
        em.getTransaction().commit();
        Long id = espace.getId();

        // Transaction 1 et Transaction 2 lisent la même entité simultanément
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        EspaceArena espaceSession1 = em1.find(EspaceArena.class, id);
        EspaceArena espaceSession2 = em2.find(EspaceArena.class, id);

        assertEquals(espaceSession1.getVersion(), espaceSession2.getVersion());

        // Transaction 1 modifie et commit -> version passe de 0 à 1
        em1.getTransaction().begin();
        espaceSession1.setTarifHoraire(BigDecimal.valueOf(85.00));
        em1.getTransaction().commit();
        em1.close();

        // Transaction 2 tente de modifier la donnée périmée -> le commit doit échouer
        em2.getTransaction().begin();
        espaceSession2.setTarifHoraire(BigDecimal.valueOf(90.00));

        Exception exception = assertThrows(Exception.class, () -> {
            em2.getTransaction().commit();
        });

        boolean isOptimisticLock = (exception instanceof OptimisticLockException) ||
                (exception.getCause() instanceof OptimisticLockException) ||
                (exception.getCause() != null && exception.getCause().getCause() instanceof OptimisticLockException);

        assertTrue(isOptimisticLock, "L'erreur de commit doit être causée par un conflit OptimisticLockException");

        if (em2.getTransaction().isActive()) {
            em2.getTransaction().rollback();
        }
        em2.close();
    }
}
