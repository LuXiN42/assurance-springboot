package com.assurance.assurance_springboot.model;

/**
 * Statuts d'un paiement. Stockés en VARCHAR(32) dans {@code paiements.statut}.
 */
public enum StatutPaiement {
    EN_ATTENTE,
    EFFECTUE,
    ECHOUE,
    ANNULE
}
