package com.nexusarena.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entité représentant un terrain de padel, un terrain de futsal,
 * un booth d'e-sport ou un simulateur VR.
 * Cache de second niveau activé (READ_WRITE).
 */
@Entity
@Table(name = "espaces_arena")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EspaceArena implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_espace", nullable = false, length = 30)
    private TypeEspace typeEspace;

    @Column(nullable = false)
    private int capacite;

    @Column(name = "tarif_horaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifHoraire;

    @Column(length = 255)
    private String description;

    @Column(name = "equipement_inclus", length = 255)
    private String equipementInclus;

    @Column(nullable = false)
    private boolean actif = true;

    @Version
    private int version;

    public EspaceArena() {
    }

    public EspaceArena(String nom, TypeEspace typeEspace, int capacite, BigDecimal tarifHoraire, String description) {
        this.nom = nom;
        this.typeEspace = typeEspace;
        this.capacite = capacite;
        this.tarifHoraire = tarifHoraire;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public TypeEspace getTypeEspace() {
        return typeEspace;
    }

    public void setTypeEspace(TypeEspace typeEspace) {
        this.typeEspace = typeEspace;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public BigDecimal getTarifHoraire() {
        return tarifHoraire;
    }

    public void setTarifHoraire(BigDecimal tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEquipementInclus() {
        return equipementInclus;
    }

    public void setEquipementInclus(String equipementInclus) {
        this.equipementInclus = equipementInclus;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EspaceArena that = (EspaceArena) o;
        return Objects.equals(nom, that.nom) && typeEspace == that.typeEspace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom, typeEspace);
    }

    @Override
    public String toString() {
        return "EspaceArena{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", typeEspace=" + typeEspace +
                ", capacite=" + capacite +
                ", tarifHoraire=" + tarifHoraire + "€" +
                ", version=" + version +
                '}';
    }
}
