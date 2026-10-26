package com.assurance.assurance_springboot.model;

/**
 * Statuts d'un contrat. Stockés en VARCHAR(32) dans {@code contrats.statut}.
 */
public enum StatutContrat {
    EN_ATTENTE,
    ACTIF,
    SUSPENDU,
    EXPIRE,
    RESILIE
}
