-- ============================================================================
-- data.sql : jeu de démonstration de données pour l'application d'assurance.
-- Tous les comptes du jeu utilisent le mot de passe : Password123!
-- Le fichier schema.sql, distinct, supprime puis recrée la structure des tables.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM pieces_justificatives;
DELETE FROM commentaires;
DELETE FROM paiements;
DELETE FROM sinistres;
DELETE FROM contrat_garanties;
DELETE FROM type_assurance_garanties;
DELETE FROM contrats;
DELETE FROM garanties;
DELETE FROM types_assurance;
DELETE FROM utilisateurs;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 1. UTILISATEURS (1 admin, 4 agents, 25 clients)
-- IDs : admin=1, agents=2..5, clients=6..30
-- ============================================================================
INSERT INTO utilisateurs (id, email, mot_de_passe_hash, role, nom, prenom, telephone, adresse, date_inscription) VALUES
-- Admin
( 1, 'admin@assurance.fr',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'ADMIN', 'Admin', 'Système', '0100000000', '1 rue de la Compagnie, 75001 Paris', '2023-01-01'),
-- Agents
( 2, 'agent.dupont@assurance.fr',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'AGENT', 'Dupont', 'Jean-Pierre', '0612340002', '2 av. des Agents, 75002 Paris', '2023-09-01'),
( 3, 'agent.lefort@assurance.fr',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'AGENT', 'Lefort', 'Catherine', '0612340003', '3 av. des Agents, 75003 Paris', '2023-09-01'),
( 4, 'agent.mercier@assurance.fr',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'AGENT', 'Mercier', 'Antoine', '0612340004', '4 av. des Agents, 75004 Paris', '2023-09-15'),
( 5, 'agent.beaumont@assurance.fr',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'AGENT', 'Beaumont', 'Sophie', '0612340005', '5 av. des Agents, 75005 Paris', '2023-10-01'),
-- Clients (25)
( 6, 'martin.alice@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Martin', 'Alice', '0620000006', '6 rue des Clients, 75006 Paris', '2024-01-15'),
( 7, 'dubois.bruno@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Dubois', 'Bruno', '0620000007', '7 rue des Clients, 75007 Paris', '2024-01-20'),
( 8, 'bernard.chloe@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Bernard', 'Chloé', '0620000008', '8 rue des Clients, 75008 Paris', '2024-02-03'),
( 9, 'petit.david@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Petit', 'David', '0620000009', '9 rue des Clients, 75009 Paris', '2024-02-10'),
(10, 'robert.emilie@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Robert', 'Émilie', '0620000010', '10 rue des Clients, 75010 Paris', '2024-02-14'),
(11, 'richard.fabien@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Richard', 'Fabien', '0620000011', '11 rue des Clients, 75011 Paris', '2024-03-01'),
(12, 'durand.gabrielle@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Durand', 'Gabrielle', '0620000012', '12 rue des Clients, 75012 Paris', '2024-03-08'),
(13, 'moreau.hugo@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Moreau', 'Hugo', '0620000013', '13 rue des Clients, 75013 Paris', '2024-03-12'),
(14, 'laurent.isabelle@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Laurent', 'Isabelle', '0620000014', '14 rue des Clients, 75014 Paris', '2024-03-15'),
(15, 'simon.julien@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Simon', 'Julien', '0620000015', '15 rue des Clients, 75015 Paris', '2024-03-20'),
(16, 'michel.karine@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Michel', 'Karine', '0620000016', '16 rue des Clients, 75016 Paris', '2024-04-02'),
(17, 'lefebvre.luc@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Lefebvre', 'Luc', '0620000017', '17 rue des Clients, 75017 Paris', '2024-04-10'),
(18, 'leroy.marie@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Leroy', 'Marie', '0620000018', '18 rue des Clients, 75018 Paris', '2024-04-15'),
(19, 'roux.nicolas@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Roux', 'Nicolas', '0620000019', '19 rue des Clients, 75019 Paris', '2024-04-20'),
(20, 'vincent.olivia@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Vincent', 'Olivia', '0620000020', '20 rue des Clients, 75020 Paris', '2024-05-01'),
(21, 'fournier.paul@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Fournier', 'Paul', '0620000021', '21 rue des Clients, 92100 Boulogne', '2024-05-05'),
(22, 'garcia.quentin@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Garcia', 'Quentin', '0620000022', '22 rue des Clients, 92110 Clichy', '2024-05-09'),
(23, 'andre.rachel@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'André', 'Rachel', '0620000023', '23 rue des Clients, 92200 Neuilly', '2024-05-12'),
(24, 'mercier.sophie@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Mercier', 'Sophie', '0620000024', '24 rue des Clients, 92300 Levallois', '2024-05-18'),
(25, 'blanc.thomas@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Blanc', 'Thomas', '0620000025', '25 rue des Clients, 92400 Courbevoie', '2024-05-22'),
(26, 'guerin.ursula@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Guérin', 'Ursula', '0620000026', '26 rue des Clients, 92500 Rueil', '2024-06-01'),
(27, 'boyer.vincent@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Boyer', 'Vincent', '0620000027', '27 rue des Clients, 92600 Asnières', '2024-06-04'),
(28, 'garnier.william@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Garnier', 'William', '0620000028', '28 rue des Clients, 92700 Colombes', '2024-06-08'),
(29, 'chevalier.alexandre@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'Chevalier', 'Alexandre', '0620000029', '29 rue des Clients, 92800 Puteaux', '2024-06-12'),
(30, 'francois.yasmine@assurance.test',
       '$2a$10$kV8ub5mb6P1.AHH5wUJvQeyx3TI4N1yFmqZCcOHSfoJ83uiDPvlKS',
       'CLIENT', 'François', 'Yasmine', '0620000030', '30 rue des Clients, 93100 Montreuil', '2024-06-18');

-- ============================================================================
-- 2. TYPES D'ASSURANCE (5)
-- ============================================================================
INSERT INTO types_assurance (id, nom, description, prix_base) VALUES
(1, 'Automobile',    'Assurance pour véhicules particuliers et utilitaires.', 480.00),
(2, 'Habitation',    'Assurance pour logements (incendie, dégât des eaux, vol).', 320.00),
(3, 'Santé',         'Complémentaire santé individuelle et familiale.',         290.00),
(4, 'Voyage',        'Assurance des déplacements et séjours.',             250.00),
(5, 'Professionnelle', 'Assurance de responsabilité professionnelle.', 310.00);

-- ============================================================================
-- 3. GARANTIES (6)
-- ============================================================================
INSERT INTO garanties (id, nom, description) VALUES
(1, 'Responsabilité civile', 'Couverture des dommages causés à des tiers.'),
(2, 'Vol',                   'Couverture en cas de vol.'),
(3, 'Incendie',              'Couverture des dommages causés par un incendie.'),
(4, 'Accident',              'Couverture des dommages résultant d’un accident.'),
(5, 'Dégâts des eaux',       'Couverture des dommages liés aux fuites et dégâts des eaux.'),
(6, 'Assistance',            'Aide et services d’assistance à l’assuré.');

-- ============================================================================
-- 4. JOINTURE types_assurance <-> garanties
-- ============================================================================
INSERT INTO type_assurance_garanties (type_assurance_id, garantie_id) VALUES
-- Automobile : Responsabilité civile, Vol, Incendie, Accident
(1, 1), (1, 2), (1, 3), (1, 4),
-- Habitation : Responsabilité civile, Vol, Incendie, Dégâts des eaux
(2, 1), (2, 2), (2, 3), (2, 5),
-- Santé : Accident, Assistance
(3, 4), (3, 6),
-- Voyage : Assistance, Accident
(4, 6), (4, 4),
-- Professionnelle : Responsabilité civile, Incendie, Vol, Dégâts des eaux
(5, 1), (5, 3), (5, 2), (5, 5);

-- ============================================================================
-- 5. CONTRATS (~30, répartis sur 16 mois, dans tous les statuts)
-- ============================================================================
INSERT INTO contrats (id, numero_contrat, date_debut, date_fin, prime, statut, client_id, type_assurance_id) VALUES
( 1, 'CTR-2025-0001', '2025-07-15', '2026-07-15', 520.00, 'ACTIF',      6,  1),
( 2, 'CTR-2025-0002', '2025-07-22', '2025-12-31', 340.00, 'EXPIRE',     7,  2),
( 3, 'CTR-2025-0003', '2025-08-03', '2026-08-03', 310.00, 'ACTIF',      8,  3),
( 4, 'CTR-2025-0004', '2025-08-18', '2025-09-30', 460.00, 'EXPIRE',     9,  1),
( 5, 'CTR-2025-0005', '2025-09-05', '2026-09-05', 360.00, 'ACTIF',     10,  2),
( 6, 'CTR-2025-0006', '2025-09-20', '2025-11-10', 490.00, 'RESILIE',   11,  1),
( 7, 'CTR-2025-0007', '2025-10-02', '2045-10-02', 280.00, 'ACTIF',     12,  4),
( 8, 'CTR-2025-0008', '2025-10-17', '2026-10-17', 305.00, 'SUSPENDU',  13,  3),
( 9, 'CTR-2025-0009', '2025-11-04', '2026-11-04', 320.00, 'ACTIF',     14,  5),
(10, 'CTR-2025-0010', '2025-11-19', '2026-11-19', 580.00, 'ACTIF',     15,  1),
(11, 'CTR-2025-0011', '2025-12-08', '2026-12-08', 450.00, 'ACTIF',     16,  2),
(12, 'CTR-2025-0012', '2025-12-22', '2026-03-15', 380.00, 'RESILIE',   17,  3),
(13, 'CTR-2026-0013', '2026-01-06', '2027-01-06', 610.00, 'ACTIF',     18,  1),
(14, 'CTR-2026-0014', '2026-01-21', NULL,        240.00, 'EN_ATTENTE',19,  4),
(15, 'CTR-2026-0015', '2026-02-02', '2027-02-02', 720.00, 'ACTIF',     20,  2),
(16, 'CTR-2026-0016', '2026-02-15', '2027-02-15', 290.00, 'ACTIF',     21,  3),
(17, 'CTR-2026-0017', '2026-03-08', '2027-03-08', 540.00, 'ACTIF',     22,  1),
(18, 'CTR-2026-0018', '2026-03-22', '2027-03-22', 340.00, 'SUSPENDU',  23,  5),
(19, 'CTR-2026-0019', '2026-04-04', '2027-04-04', 480.00, 'ACTIF',     24,  2),
(20, 'CTR-2026-0020', '2026-04-19', '2027-04-19', 670.00, 'ACTIF',     25,  1),
(21, 'CTR-2026-0021', '2026-05-03', '2027-05-03', 410.00, 'ACTIF',     26,  3),
(22, 'CTR-2026-0022', '2026-05-18', NULL,        260.00, 'EN_ATTENTE',27,  4),
(23, 'CTR-2026-0023', '2026-06-04', '2027-06-04', 590.00, 'ACTIF',     28,  1),
(24, 'CTR-2026-0024', '2026-06-19', '2027-06-19', 510.00, 'ACTIF',     29,  2),
(25, 'CTR-2026-0025', '2026-07-05', '2027-07-05', 320.00, 'ACTIF',     30,  5),
(26, 'CTR-2026-0026', '2026-07-21', '2026-08-21', 440.00, 'RESILIE',    6,  2),
(27, 'CTR-2026-0027', '2026-08-02', '2027-08-02', 560.00, 'ACTIF',      7,  1),
(28, 'CTR-2026-0028', '2026-08-18', '2026-09-18', 390.00, 'EXPIRE',     8,  3),
(29, 'CTR-2026-0029', '2026-09-07', '2027-09-07', 490.00, 'ACTIF',      9,  2),
(30, 'CTR-2026-0030', '2026-09-25', '2027-09-25', 620.00, 'SUSPENDU',  10,  1);

-- ============================================================================
-- 6. JOINTURE contrats <-> garanties (garanties conformes au type de chaque contrat)
-- ============================================================================
INSERT INTO contrat_garanties (contrat_id, garantie_id) VALUES
( 1, 1),
( 1, 2),
( 1, 3),
( 1, 4),
( 2, 1),
( 2, 2),
( 2, 3),
( 2, 5),
( 3, 4),
( 3, 6),
( 4, 1),
( 4, 2),
( 4, 3),
( 4, 4),
( 5, 1),
( 5, 2),
( 5, 3),
( 5, 5),
( 6, 1),
( 6, 2),
( 6, 3),
( 6, 4),
( 7, 6),
( 7, 4),
( 8, 4),
( 8, 6),
( 9, 1),
( 9, 3),
( 9, 2),
( 9, 5),
(10, 1),
(10, 2),
(10, 3),
(10, 4),
(11, 1),
(11, 2),
(11, 3),
(11, 5),
(12, 4),
(12, 6),
(13, 1),
(13, 2),
(13, 3),
(13, 4),
(14, 6),
(14, 4),
(15, 1),
(15, 2),
(15, 3),
(15, 5),
(16, 4),
(16, 6),
(17, 1),
(17, 2),
(17, 3),
(17, 4),
(18, 1),
(18, 3),
(18, 2),
(18, 5),
(19, 1),
(19, 2),
(19, 3),
(19, 5),
(20, 1),
(20, 2),
(20, 3),
(20, 4),
(21, 4),
(21, 6),
(22, 6),
(22, 4),
(23, 1),
(23, 2),
(23, 3),
(23, 4),
(24, 1),
(24, 2),
(24, 3),
(24, 5),
(25, 1),
(25, 3),
(25, 2),
(25, 5),
(26, 1),
(26, 2),
(26, 3),
(26, 5),
(27, 1),
(27, 2),
(27, 3),
(27, 4),
(28, 4),
(28, 6),
(29, 1),
(29, 2),
(29, 3),
(29, 5),
(30, 1),
(30, 2),
(30, 3),
(30, 4);

-- ============================================================================
-- 7. SINISTRES (~15, dans tous les statuts)
-- Pour ACCEPTE et INDEMNISE, montant_indemnisation renseigné.
-- ============================================================================
INSERT INTO sinistres (id, numero_sinistre, `date`, description, montant_estime, type_sinistre, statut, montant_indemnisation, contrat_id) VALUES
( 1, 'SIN-2026-0001', '2026-10-22', 'Accident de voiture, collision arrière sur parking.',     1500.00, 'Accident automobile',         'DECLARE',    NULL, 1),
( 2, 'SIN-2026-0002', '2026-10-15', 'Dégât des eaux dans la cuisine après fuite.',            3500.00, 'Dégâts des eaux',              'EN_ANALYSE', NULL, 11),
( 3, 'SIN-2026-0003', '2026-09-10', 'Frais d’hospitalisation imprévus.',                     2200.00, 'Hospitalisation',             'ACCEPTE',    2000.00, 3),
( 4, 'SIN-2026-0004', '2026-08-20', 'Vol de véhicule avec effraction.',                     8000.00, 'Vol automobile',              'INDEMNISE',  7500.00, 27),
( 5, 'SIN-2026-0005', '2026-07-30', 'Incendie d’origine électrique au salon.',             12000.00, 'Incendie habitation',         'REFUSE',     NULL, 5),
( 6, 'SIN-2026-0006', '2026-07-12', 'Assistance demandée pendant un déplacement.',        50000.00, 'Assistance voyage',                       'DECLARE',    NULL, 7),
( 7, 'SIN-2026-0007', '2026-06-18', 'Fuite ayant endommagé le plafond et les murs.',                         4200.00, 'Dégâts des eaux',       'EN_ANALYSE', NULL, 19),
( 8, 'SIN-2026-0008', '2026-05-25', 'Collision ayant endommagé la carrosserie du véhicule.', 3300.00, 'Accident automobile',               'ACCEPTE',    3000.00, 13),
( 9, 'SIN-2026-0009', '2026-04-12', 'Accident corporel du conducteur, fractures.',           9500.00, 'Accident corporel',           'INDEMNISE',  9000.00, 17),
(10, 'SIN-2026-0010', '2026-03-15', 'Cambriolage avec effraction, vols d’objets.',          2800.00, 'Cambriolage',                 'DECLARE',    NULL, 29),
(11, 'SIN-2026-0011', '2026-02-22', 'Dommage automobile déclaré hors du périmètre de la garantie.',15000.00, 'Accident automobile',       'REFUSE',     NULL, 20),
(12, 'SIN-2026-0012', '2025-12-10', 'Soins dentaires lourds, prothèse.',                    4500.00, 'Soins dentaires',             'INDEMNISE',  4200.00, 21),
(13, 'SIN-2025-0013', '2025-11-05', 'Dommage causé à un tiers lors d’une prestation professionnelle.',6800.00, 'Responsabilité civile',               'EN_ANALYSE', NULL, 25),
(14, 'SIN-2026-0014', '2026-01-15', 'Dommages latéraux suite à stationnement rue étroite.',  1900.00, 'Accident automobile',         'ACCEPTE',    1800.00, 23),
(15, 'SIN-2026-0015', '2026-10-10', 'Pneu éclaté, choc contre trottoir.',                   2500.00, 'Accident automobile',         'DECLARE',    NULL,   27);

-- ============================================================================
-- 8. PAIEMENTS (~30, dans tous les statuts, un EFFECTUE par mois sur 12 mois)
-- ============================================================================
INSERT INTO paiements (id, montant, `date`, mode_paiement, statut, contrat_id) VALUES
( 1, 320.00, '2025-10-12', 'VIREMENT',     'EFFECTUE',  7),
( 2, 320.00, '2025-11-15', 'VIREMENT',     'EFFECTUE',  9),
( 3, 580.00, '2025-11-22', 'PRELEVEMENT',  'EFFECTUE', 10),
( 4, 450.00, '2025-12-10', 'VIREMENT',     'EFFECTUE', 11),
( 5, 380.00, '2025-12-28', 'CARTE',        'EFFECTUE', 12),
( 6, 280.00, '2025-10-05', 'CARTE',        'EN_ATTENTE',8),
( 7, 610.00, '2026-01-08', 'PRELEVEMENT',  'EFFECTUE', 13),
( 8, 720.00, '2026-01-25', 'VIREMENT',     'EFFECTUE', 15),
( 9, 290.00, '2026-02-02', 'CARTE',        'ECHOUE',   16),
(10, 540.00, '2026-02-18', 'PRELEVEMENT',  'EFFECTUE', 17),
(11, 480.00, '2026-03-12', 'VIREMENT',     'EFFECTUE', 19),
(12, 670.00, '2026-03-25', 'PRELEVEMENT',  'EFFECTUE', 20),
(13, 410.00, '2026-04-08', 'VIREMENT',     'EFFECTUE', 21),
(14, 590.00, '2026-04-22', 'PRELEVEMENT',  'EFFECTUE', 23),
(15, 510.00, '2026-05-04', 'CARTE',        'EN_ATTENTE',24),
(16, 320.00, '2026-05-19', 'VIREMENT',     'EFFECTUE', 25),
(17, 220.00, '2026-05-08', 'PRELEVEMENT',  'EFFECTUE', 14),
(18, 340.00, '2026-06-09', 'VIREMENT',     'EFFECTUE', 18),
(19, 590.00, '2026-06-04', 'PRELEVEMENT',  'EFFECTUE', 23),
(20, 510.00, '2026-06-19', 'PRELEVEMENT',  'EFFECTUE', 24),
(21, 560.00, '2026-06-03', 'PRELEVEMENT',  'EFFECTUE', 27),
(22, 580.00, '2026-07-08', 'CARTE',        'ECHOUE',    1),
(23, 390.00, '2026-07-13', 'VIREMENT',     'EFFECTUE', 28),
(24, 390.00, '2026-07-25', 'PRELEVEMENT',  'EFFECTUE',  5),
(25, 250.00, '2026-08-04', 'VIREMENT',     'EFFECTUE',  7),
(26, 440.00, '2026-08-19', 'CARTE',        'ANNULE',   26),
(27, 490.00, '2026-09-02', 'VIREMENT',     'EFFECTUE', 29),
(28, 620.00, '2026-09-16', 'PRELEVEMENT',  'EFFECTUE', 30),
(29, 280.00, '2026-10-05', 'CARTE',        'EFFECTUE', 22),
(30, 580.00, '2026-10-22', 'PRELEVEMENT',  'EFFECTUE',  1);

-- ============================================================================
-- 9. COMMENTAIRES (6, écrits par les agents sur certains sinistres)
-- ============================================================================
INSERT INTO commentaires (id, texte, date_creation, auteur_id, sinistre_id) VALUES
(1, 'Bien reçu, dossier transmis à l’analyse. Pièces complémentaires attendues.',  '2026-10-22 10:15:00', 2,  1),
(2, 'Dossier complet, devis validé. Accord pour indemnisation à hauteur de 2000€.',  '2026-09-11 14:30:00', 3,  3),
(3, 'Chèque d’indemnisation envoyé au client. Dossier clos.',                         '2026-08-25 09:45:00', 4,  4),
(4, 'Pièces complémentaires demandées : photos et facture de réparation.',           '2026-06-20 11:00:00', 5,  7),
(5, 'Indemnisation de 9000€ versée. Clôture du dossier.',                             '2026-04-15 16:20:00', 2,  9),
(6, 'Montant de 1800€ validé par la direction. Procéder au paiement.',                '2026-01-17 08:50:00', 3, 14);

-- ============================================================================
-- 10. PIÈCES JUSTIFICATIVES (6, attachées à des sinistres)
-- ============================================================================
INSERT INTO pieces_justificatives (id, nom_original, nom_stocke, type_mime, taille, date_depot, sinistre_id) VALUES
(1, 'constat_amiable.pdf',       'pj_001_constat_amiable.pdf',       'application/pdf', 245678,  '2026-10-22 09:30:00',  1),
(2, 'photos_degats.zip',         'pj_002_photos_degats.zip',         'application/zip', 1245678, '2026-10-15 18:12:00',  2),
(3, 'facture_hospitalisation.pdf','pj_003_facture_hospitalisation.pdf','application/pdf', 567890,  '2026-09-08 14:00:00',  3),
(4, 'devis_reparation.pdf',      'pj_004_devis_reparation.pdf',      'application/pdf', 234567,  '2026-08-19 10:20:00',  4),
(5, 'rapport_expert.pdf',        'pj_005_rapport_expert.pdf',        'application/pdf', 789012,  '2026-04-10 11:45:00',  9),
(6, 'photo_vehicule.jpg',        'pj_006_photo_vehicule.jpg',        'image/jpeg',      456789,  '2026-01-15 17:30:00', 14);

-- ============================================================================
-- REQUÊTES DE VÉRIFICATION (StatistiquesDao)
-- ============================================================================
-- (1) Effectifs utilisateurs par rôle (attendu : ADMIN=1, AGENT=4, CLIENT=25)
-- SELECT role, COUNT(*) AS effectif
--   FROM utilisateurs
--  GROUP BY role
--  ORDER BY role;

-- (2) Évolution mensuelle des contrats sur 12 mois
-- SELECT DATE_FORMAT(date_debut, '%Y-%m') AS mois, COUNT(*) AS n
--   FROM contrats
--  WHERE date_debut >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
--  GROUP BY DATE_FORMAT(date_debut, '%Y-%m')
--  ORDER BY 1;

-- (3) Répartition des contrats par statut (attendu : tous les 5 statuts présents)
-- SELECT statut, COUNT(*) AS nb
--   FROM contrats
--  GROUP BY statut
--  ORDER BY nb DESC;

-- (4) Répartition des sinistres par statut (attendu : DECLARE, EN_ANALYSE, ACCEPTE, REFUSE, INDEMNISE)
-- SELECT statut, COUNT(*) AS nb
--   FROM sinistres
--  GROUP BY statut
--  ORDER BY nb DESC;

-- (5) CA encaissé (somme paiements statut = 'EFFECTUE')
-- SELECT COALESCE(SUM(montant),0) FROM paiements WHERE statut = 'EFFECTUE';

-- (6) Total des indemnisations versées (statut = 'INDEMNISE')
-- SELECT COALESCE(SUM(montant_indemnisation),0) FROM sinistres WHERE statut = 'INDEMNISE';

-- (7) Évolution mensuelle des paiements encaissés sur 12 mois
-- SELECT DATE_FORMAT(`date`, '%Y-%m') AS mois, COALESCE(SUM(montant),0) AS total
--   FROM paiements
--  WHERE `date` >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
--    AND statut = 'EFFECTUE'
--  GROUP BY DATE_FORMAT(`date`, '%Y-%m')
--  ORDER BY 1;
-- ============================================================================
-- FIN du jeu de données.
-- Récapitulatif : 30 utilisateurs, 5 types, 6 garanties, 30 contrats,
-- 15 sinistres, 30 paiements, 6 commentaires, 6 pièces justificatives.
-- ============================================================================
