package com.assurance.assurance_springboot.model;

/**
 * Modes de paiement. Stockés en VARCHAR(32) dans {@code paiements.mode_paiement}
 * (colonne nullable).
 */
public enum ModePaiement {
    CARTE,
    VIREMENT,
    PRELEVEMENT,
    ESPECES,
    CHEQUE,
    AUTRE
}
