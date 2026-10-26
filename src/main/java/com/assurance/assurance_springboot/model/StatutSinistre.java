package com.assurance.assurance_springboot.model;

/**
 * Statuts d'un sinistre. Stockés en VARCHAR(32) dans {@code sinistres.statut}.
 */
public enum StatutSinistre {
    DECLARE,
    EN_ANALYSE,
    ACCEPTE,
    REFUSE,
    INDEMNISE
}
