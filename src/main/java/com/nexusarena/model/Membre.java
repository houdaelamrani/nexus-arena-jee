package com.nexusarena.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entité représentant un membre / adhérent du complexe sportif et e-sport.
 * Bénéficie du cache de second niveau Hibernate (READ_WRITE).
 */
@Entity
@Table(name = "membres")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Membre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 25)
    private String telephone;

    @Column(length = 30)
    private String niveauBadge = "ROOKIE"; // ROOKIE, PRO, VIP

    @Version
    private int version;

    public Membre() {
    }

    public Membre(String nom, String email, String telephone, String niveauBadge) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.niveauBadge = niveauBadge;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNiveauBadge() {
        return niveauBadge;
    }

    public void setNiveauBadge(String niveauBadge) {
        this.niveauBadge = niveauBadge;
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
        Membre membre = (Membre) o;
        return Objects.equals(email, membre.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Membre{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", niveauBadge='" + niveauBadge + '\'' +
                ", version=" + version +
                '}';
    }
}
