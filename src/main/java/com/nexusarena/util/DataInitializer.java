package com.nexusarena.util;

import com.nexusarena.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Initialisateur de données réalistes pour le complexe Nexus Arena (Étape 3 du TP).
 */
public class DataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());
    private final EntityManager em;

    public DataInitializer(EntityManager em) {
        this.em = em;
    }

    public void initData() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            LOGGER.info("Initialisation du jeu de données Nexus Arena...");

            // 1. Création des membres
            Membre m1 = new Membre("Alexandre Rivier", "alexandre.rivier@nexusarena.com", "0601020304", "VIP");
            Membre m2 = new Membre("Sarah Benali", "sarah.benali@nexusarena.com", "0611223344", "PRO");
            Membre m3 = new Membre("Lucas Moreau", "lucas.moreau@nexusarena.com", "0622334455", "ROOKIE");
            Membre m4 = new Membre("Emma Dubois", "emma.dubois@nexusarena.com", "0633445566", "PRO");

            em.persist(m1);
            em.persist(m2);
            em.persist(m3);
            em.persist(m4);

            // 2. Création des espaces sportifs et gaming
            EspaceArena padel1 = new EspaceArena(
                    "Court Central Padel Panoramique",
                    TypeEspace.COURT_PADEL,
                    4,
                    BigDecimal.valueOf(44.00),
                    "Gazon texturé bleu WPT avec vitres panoramiques 12mm sans montant"
            );
            padel1.setEquipementInclus("Raquettes Bullpadel Pro & Balles Head Padel Pro fournies");

            EspaceArena padel2 = new EspaceArena(
                    "Court Padel Outdoor Sunset",
                    TypeEspace.COURT_PADEL,
                    4,
                    BigDecimal.valueOf(36.00),
                    "Court extérieur haute visibilité avec éclairage LED anti-éblouissement"
            );
            padel2.setEquipementInclus("Balles Head fournies");

            EspaceArena esport1 = new EspaceArena(
                    "Arène E-Sport Apex Stage",
                    TypeEspace.ESPORT_BOOTH,
                    10,
                    BigDecimal.valueOf(65.00),
                    "Box scénique 5v5 insonorisé avec fibre 10 Gbps symétrique dédiée"
            );
            esport1.setEquipementInclus("10 PC RTX 4090 + Casques Logitech G Pro X 2 + Fauteuils Secretlab");

            EspaceArena simu1 = new EspaceArena(
                    "Simulateur HyperDrive SimRacing",
                    TypeEspace.SIMULATEUR_VR,
                    2,
                    BigDecimal.valueOf(45.00),
                    "Cockpit dynamique 4 vérins D-BOX, volant Direct Drive Fanatec, Casque VR Crystal"
            );
            simu1.setEquipementInclus("Pédalier hydraulique et gants de pilotage");

            EspaceArena futsal1 = new EspaceArena(
                    "Terrain Futsal Alpha Indoor",
                    TypeEspace.FUTSAL_ARENA,
                    10,
                    BigDecimal.valueOf(80.00),
                    "Parquet sportif amortissant homologué avec caméras de rediffusion automatique"
            );
            futsal1.setEquipementInclus("Chasubles numériques, ballons FIFA Quality Pro");

            em.persist(padel1);
            em.persist(padel2);
            em.persist(esport1);
            em.persist(simu1);
            em.persist(futsal1);

            // 3. Réservations dynamiques
            LocalDateTime maintenant = LocalDateTime.now();

            // Réservation 1 : Tournoi e-sport prévu demain soir
            ReservationArena res1 = new ReservationArena(m1, esport1, maintenant.plusDays(1).withHour(18).withMinute(0),
                    maintenant.plusDays(1).withHour(21).withMinute(0));
            res1.setMontantTotal(BigDecimal.valueOf(195.00)); // 3h * 65€
            res1.setRemarques("Tournoi interne Team Apex - Stream Twitch configuré");
            em.persist(res1);

            // Réservation 2 : Match de padel prévu dans 2 jours
            ReservationArena res2 = new ReservationArena(m2, padel1, maintenant.plusDays(2).withHour(14).withMinute(0),
                    maintenant.plusDays(2).withHour(15).withMinute(30));
            res2.setMontantTotal(BigDecimal.valueOf(66.00)); // 1.5h * 44€
            res2.setRemarques("Session entraînement niveau Pro");
            em.persist(res2);

            // Réservation 3 : Session simulateur VR
            ReservationArena res3 = new ReservationArena(m3, simu1, maintenant.plusHours(4), maintenant.plusHours(5));
            res3.setMontantTotal(BigDecimal.valueOf(45.00)); // 1h * 45€
            res3.setRemarques("Test circuit Spa-Francorchamps en F1 2024");
            em.persist(res3);

            tx.commit();
            LOGGER.info("Jeu de données Nexus Arena initialisé avec succès !");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.severe("Erreur lors de l'initialisation des données : " + e.getMessage());
            throw e;
        }
    }
}
