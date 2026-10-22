package com.nexusarena.model;

/**
 * Types d'espaces sportifs et e-sport disponibles dans le complexe Nexus Arena.
 */
public enum TypeEspace {
    COURT_PADEL("Court de Padel panoramique avec éclairage LED"),
    FUTSAL_ARENA("Terrain indoor synthétique Futsal 5v5"),
    ESPORT_BOOTH("Station de jeu e-sport 5v5 avec PC ultra haute performance"),
    SIMULATEUR_VR("Simulateur dynamique SimRacing F1 & cockpit réalité virtuelle");

    private final String description;

    TypeEspace(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
