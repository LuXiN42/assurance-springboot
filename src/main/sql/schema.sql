-- ============================================================================
-- schema.sql : schéma de la base `assurance_db` (MySQL 8 / MariaDB 10)
-- Projet J2EE Assurance, compatible avec Hibernate hbm2ddl = update compatible.
-- ============================================================================
-- Généré à partir des entités JPA du package com.assurance.model :
--   Utilisateur, Contrat, TypeAssurance, Garantie, Sinistre, Paiement,
--   Commentaire, PieceJustificative
-- + tables de jointure ManyToMany : contrat_garanties, type_assurance_garanties.
--
-- Enums stockés en STRING via @Enumerated(EnumType.STRING) :
--   Role             : CLIENT, AGENT, ADMIN
--   StatutContrat    : EN_ATTENTE, ACTIF, SUSPENDU, EXPIRE, RESILIE
--   StatutSinistre   : DECLARE, EN_ANALYSE, ACCEPTE, REFUSE, INDEMNISE
--   StatutPaiement   : EN_ATTENTE, EFFECTUE, ECHOUE, ANNULE
--   ModePaiement     : CARTE, VIREMENT, PRELEVEMENT, ESPECES, CHEQUE, AUTRE
--
-- Moteur / charset : InnoDB + utf8mb4 (cohérent avec Hibernate MySQL8Dialect).
-- ============================================================================

-- Suppression préalable des tables dans l'ordre INVERSE des dépendances FK,
-- pour permettre une ré-exécution propre du script.
DROP TABLE IF EXISTS pieces_justificatives;
DROP TABLE IF EXISTS commentaires;
DROP TABLE IF EXISTS paiements;
DROP TABLE IF EXISTS sinistres;
DROP TABLE IF EXISTS contrat_garanties;
DROP TABLE IF EXISTS type_assurance_garanties;
DROP TABLE IF EXISTS contrats;
DROP TABLE IF EXISTS garanties;
DROP TABLE IF EXISTS types_assurance;
DROP TABLE IF EXISTS utilisateurs;

-- ============================================================================
-- 1. utilisateurs
-- ============================================================================
CREATE TABLE utilisateurs (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    email              VARCHAR(255)    NOT NULL,
    mot_de_passe_hash  VARCHAR(255)    NOT NULL,
    role               VARCHAR(32)     NOT NULL,
    nom                VARCHAR(100)    NULL,
    prenom             VARCHAR(100)    NULL,
    telephone          VARCHAR(20)     NULL,
    adresse            VARCHAR(255)    NULL,
    date_inscription   DATE            NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_utilisateur_email (email),
    CONSTRAINT chk_utilisateur_role
        CHECK (role IN ('CLIENT','AGENT','ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 2. types_assurance
-- ============================================================================
CREATE TABLE types_assurance (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(150)    NOT NULL,
    description VARCHAR(2000)   NULL,
    prix_base   DECIMAL(12,2)   NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 3. garanties
-- ============================================================================
CREATE TABLE garanties (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(150)    NOT NULL,
    description VARCHAR(2000)   NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 4. type_assurance_garanties (jointure M:N TypeAssurance <-> Garantie)
-- ============================================================================
CREATE TABLE type_assurance_garanties (
    type_assurance_id BIGINT NOT NULL,
    garantie_id       BIGINT NOT NULL,
    PRIMARY KEY (type_assurance_id, garantie_id),
    CONSTRAINT fk_tag_type FOREIGN KEY (type_assurance_id)
        REFERENCES types_assurance (id) ON DELETE CASCADE,
    CONSTRAINT fk_tag_gar  FOREIGN KEY (garantie_id)
        REFERENCES garanties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 5. contrats
-- ============================================================================
CREATE TABLE contrats (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    numero_contrat      VARCHAR(80)   NOT NULL,
    date_debut          DATE          NOT NULL,
    date_fin            DATE          NULL,
    prime               DECIMAL(12,2) NOT NULL,
    statut              VARCHAR(32)   NOT NULL,
    client_id           BIGINT        NOT NULL,
    type_assurance_id   BIGINT        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_contrat_numero (numero_contrat),
    KEY idx_contrat_client (client_id),
    KEY idx_contrat_type   (type_assurance_id),
    KEY idx_contrat_statut (statut),
    KEY idx_contrat_datedebut (date_debut),
    CONSTRAINT fk_contrat_client FOREIGN KEY (client_id)
        REFERENCES utilisateurs (id),
    CONSTRAINT fk_contrat_type   FOREIGN KEY (type_assurance_id)
        REFERENCES types_assurance (id),
    CONSTRAINT chk_contrat_statut
        CHECK (statut IN ('EN_ATTENTE','ACTIF','SUSPENDU','EXPIRE','RESILIE')),
    CONSTRAINT chk_contrat_dates
        CHECK (date_fin IS NULL OR date_fin > date_debut),
    CONSTRAINT chk_contrat_prime
        CHECK (prime > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 6. contrat_garanties (jointure M:N Contrat <-> Garantie)
-- ============================================================================
CREATE TABLE contrat_garanties (
    contrat_id  BIGINT NOT NULL,
    garantie_id BIGINT NOT NULL,
    PRIMARY KEY (contrat_id, garantie_id),
    CONSTRAINT fk_cg_contrat FOREIGN KEY (contrat_id)
        REFERENCES contrats (id) ON DELETE CASCADE,
    CONSTRAINT fk_cg_gar     FOREIGN KEY (garantie_id)
        REFERENCES garanties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 7. sinistres
-- ============================================================================
CREATE TABLE sinistres (
    id                      BIGINT         NOT NULL AUTO_INCREMENT,
    numero_sinistre         VARCHAR(80)    NOT NULL,
    `date`                  DATE           NOT NULL,
    description             VARCHAR(2000)  NOT NULL,
    montant_estime          DECIMAL(12,2)  NULL,
    type_sinistre           VARCHAR(120)   NULL,
    statut                  VARCHAR(32)    NOT NULL,
    montant_indemnisation   DECIMAL(12,2)  NULL,
    contrat_id              BIGINT         NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sinistre_numero (numero_sinistre),
    KEY idx_sinistre_contrat (contrat_id),
    KEY idx_sinistre_statut  (statut),
    KEY idx_sinistre_date    (`date`),
    CONSTRAINT fk_sinistre_contrat FOREIGN KEY (contrat_id)
        REFERENCES contrats (id),
    CONSTRAINT chk_sinistre_statut
        CHECK (statut IN ('DECLARE','EN_ANALYSE','ACCEPTE','REFUSE','INDEMNISE')),
    CONSTRAINT chk_sinistre_montant
        CHECK (montant_estime IS NULL OR montant_estime >= 0),
    CONSTRAINT chk_sinistre_indemn
        CHECK (montant_indemnisation IS NULL OR montant_indemnisation >= 0),
    CONSTRAINT chk_sinistre_indemn_statut
        CHECK ((montant_indemnisation IS NULL OR statut IN ('ACCEPTE','INDEMNISE'))
           AND (statut <> 'INDEMNISE' OR montant_indemnisation IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 8. paiements
-- ============================================================================
CREATE TABLE paiements (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    montant       DECIMAL(12,2) NOT NULL,
    `date`        DATE          NOT NULL,
    mode_paiement VARCHAR(32)   NULL,
    statut        VARCHAR(32)   NOT NULL,
    contrat_id    BIGINT        NOT NULL,
    PRIMARY KEY (id),
    KEY idx_paiement_contrat (contrat_id),
    KEY idx_paiement_statut  (statut),
    KEY idx_paiement_date    (`date`),
    CONSTRAINT fk_paiement_contrat FOREIGN KEY (contrat_id)
        REFERENCES contrats (id),
    CONSTRAINT chk_paiement_statut
        CHECK (statut IN ('EN_ATTENTE','EFFECTUE','ECHOUE','ANNULE')),
    CONSTRAINT chk_paiement_mode
        CHECK (mode_paiement IS NULL OR mode_paiement IN
               ('CARTE','VIREMENT','PRELEVEMENT','ESPECES','CHEQUE','AUTRE')),
    CONSTRAINT chk_paiement_montant
        CHECK (montant > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 9. commentaires
-- ============================================================================
CREATE TABLE commentaires (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    texte         VARCHAR(2000) NOT NULL,
    date_creation DATETIME(6)  NOT NULL,
    auteur_id     BIGINT       NOT NULL,
    sinistre_id   BIGINT       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_commentaire_auteur (auteur_id),
    KEY idx_commentaire_sinistre (sinistre_id, date_creation),
    CONSTRAINT fk_commentaire_auteur   FOREIGN KEY (auteur_id)
        REFERENCES utilisateurs (id),
    CONSTRAINT fk_commentaire_sinistre FOREIGN KEY (sinistre_id)
        REFERENCES sinistres (id),
    CONSTRAINT chk_commentaire_texte
        CHECK (CHAR_LENGTH(TRIM(texte)) >= 1 AND CHAR_LENGTH(texte) <= 2000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- 10. pieces_justificatives
-- ============================================================================
CREATE TABLE pieces_justificatives (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nom_original VARCHAR(255) NOT NULL,
    nom_stocke   VARCHAR(300) NOT NULL,
    type_mime    VARCHAR(100) NULL,
    taille       BIGINT       NOT NULL,
    date_depot   DATETIME(6)  NOT NULL,
    sinistre_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pj_nom_stocke (nom_stocke),
    KEY idx_pj_sinistre (sinistre_id),
    CONSTRAINT fk_pj_sinistre FOREIGN KEY (sinistre_id)
        REFERENCES sinistres (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================================
-- Fin du schéma. Tables créées : 10
-- ============================================================================
