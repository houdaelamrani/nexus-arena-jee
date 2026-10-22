package com.nexusarena.service;

import com.nexusarena.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service métier de gestion des réservations (Étapes 9, 10 et 11 du TP).
 * Intègre JPQL, Criteria API, gestion des transactions RESOURCE_LOCAL,
 * détection d'OptimisticLockException et journalisation JEE.
 */
public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private final EntityManager em;

    public ReservationService(EntityManager em) {
        this.em = em;
    }

    /**
     * Crée une nouvelle réservation avec vérification des conflits de créneau et calcul de tarif.
     */
    public ReservationArena reserverEspace(Long membreId, Long espaceId, LocalDateTime debut, LocalDateTime fin, String remarques) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            LOGGER.info(() -> String.format("Tentative de réservation : Espace #%d par Membre #%d de %s à %s",
                    espaceId, membreId, debut, fin));

            if (debut == null || fin == null || !fin.isAfter(debut)) {
                throw new IllegalArgumentException("La date de fin doit être strictement après la date de début.");
            }

            // Chargement des entités
            Membre membre = em.find(Membre.class, membreId);
            if (membre == null) {
                throw new IllegalArgumentException("Membre introuvable avec l'ID : " + membreId);
            }

            EspaceArena espace = em.find(EspaceArena.class, espaceId);
            if (espace == null) {
                throw new IllegalArgumentException("Espace introuvable avec l'ID : " + espaceId);
            }

            if (!espace.isActif()) {
                throw new IllegalStateException("Cet espace est actuellement fermé pour maintenance.");
            }

            // Étape 9 : Vérification de disponibilité par JPQL
            boolean conflit = verifierConflitCreneauJPQL(espaceId, debut, fin);
            if (conflit) {
                throw new IllegalStateException("Créneau indisponible : un autre match ou session est déjà programmé !");
            }

            // Calcul du montant selon la durée et le tarif horaire
            long minutes = Duration.between(debut, fin).toMinutes();
            double heures = minutes / 60.0;
            BigDecimal montant = espace.getTarifHoraire().multiply(BigDecimal.valueOf(heures));

            ReservationArena reservation = new ReservationArena(membre, espace, debut, fin);
            reservation.setMontantTotal(montant);
            reservation.setRemarques(remarques);

            em.persist(reservation);
            tx.commit();

            LOGGER.info(() -> String.format("Réservation #%d validée avec succès pour %s (Montant: %.2f €)",
                    reservation.getId(), membre.getNom(), montant.doubleValue()));

            return reservation;
        } catch (RollbackException re) {
            if (tx.isActive()) tx.rollback();
            if (re.getCause() instanceof OptimisticLockException) {
                LOGGER.log(Level.WARNING, "Conflit d'accès concurrent détecté (OptimisticLockException) !", re.getCause());
                throw (OptimisticLockException) re.getCause();
            }
            throw re;
        } catch (OptimisticLockException ole) {
            if (tx.isActive()) tx.rollback();
            LOGGER.log(Level.WARNING, "Conflit d'accès concurrent détecté (OptimisticLockException) !", ole);
            throw ole;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la réservation : " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Étape 9 : Requête JPQL pour détecter les chevauchements de créneaux.
     */
    public boolean verifierConflitCreneauJPQL(Long espaceId, LocalDateTime debut, LocalDateTime fin) {
        String jpql = "SELECT COUNT(r) FROM ReservationArena r " +
                "WHERE r.espace.id = :espaceId " +
                "AND r.statut <> :annule " +
                "AND r.dateDebut < :fin " +
                "AND r.dateFin > :debut";

        Long count = em.createQuery(jpql, Long.class)
                .setParameter("espaceId", espaceId)
                .setParameter("annule", StatutReservation.ANNULEE)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getSingleResult();

        return count > 0;
    }

    /**
     * Étape 9 : Recherche dynamique d'espaces avec Criteria API.
     */
    public List<EspaceArena> rechercherEspacesCriteria(TypeEspace type, Integer capaciteMin, BigDecimal tarifMax) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<EspaceArena> cq = cb.createQuery(EspaceArena.class);
        Root<EspaceArena> root = cq.from(EspaceArena.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("actif")));

        if (type != null) {
            predicates.add(cb.equal(root.get("typeEspace"), type));
        }
        if (capaciteMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("capacite"), capaciteMin));
        }
        if (tarifMax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("tarifHoraire"), tarifMax));
        }

        cq.select(root).where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("tarifHoraire")));

        // Utilisation du cache de requêtes
        return em.createQuery(cq)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }
}
