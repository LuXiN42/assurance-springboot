package com.assurance.assurance_springboot.model;

/**
 * Rôles applicatifs. Stockés en VARCHAR(32) dans la table {@code utilisateurs}
 * (colonne {@code role}) via {@code @Enumerated(EnumType.STRING)}.
 */
public enum Role {
    CLIENT,
    AGENT,
    ADMIN
}
