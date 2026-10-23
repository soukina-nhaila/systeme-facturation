-- =====================================================
-- Script de création complète pour Système de Facturation
-- À exécuter dans phpMyAdmin
-- =====================================================

-- 1. CRÉATION DE LA BASE DE DONNÉES
DROP DATABASE IF EXISTS facturation;
CREATE DATABASE facturation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE facturation;

-- 2. CRÉATION DE L'UTILISATEUR (optionnel si vous utilisez root)
DROP USER IF EXISTS 'facturation_user'@'localhost';
CREATE USER 'facturation_user'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON facturation.* TO 'facturation_user'@'localhost';
FLUSH PRIVILEGES;

-- 3. CRÉATION DES TABLES (JPA les crée automatiquement, mais voici la structure pour référence)

-- Table clients
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    code_postal VARCHAR(10),
    ville VARCHAR(100),
    pays VARCHAR(100),
    numero_tva VARCHAR(50),
    INDEX idx_email (email),
    INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table produits
CREATE TABLE IF NOT EXISTS produits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(100) NOT NULL,
    designation VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    prix_unitaire_ht DECIMAL(10,2) NOT NULL,
    taux_tva DECIMAL(5,2) NOT NULL DEFAULT 20.00,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    unite VARCHAR(50) DEFAULT 'Unité',
    INDEX idx_reference (reference),
    INDEX idx_designation (designation),
    INDEX idx_actif (actif)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table factures
CREATE TABLE IF NOT EXISTS factures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_facture VARCHAR(50) NOT NULL UNIQUE,
    date_facture DATE NOT NULL,
    date_echeance DATE NOT NULL,
    client_id BIGINT NOT NULL,
    montant_ht DECIMAL(10,2),
    montant_tva DECIMAL(10,2),
    montant_ttc DECIMAL(10,2),
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
    remarques VARCHAR(1000),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    INDEX idx_numero (numero_facture),
    INDEX idx_client (client_id),
    INDEX idx_date (date_facture),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table lignes_facture
CREATE TABLE IF NOT EXISTS lignes_facture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facture_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    quantite DECIMAL(10,2) NOT NULL,
    prix_unitaire_ht DECIMAL(10,2) NOT NULL,
    taux_tva DECIMAL(5,2) NOT NULL,
    remise DECIMAL(10,2) DEFAULT 0.00,
    montant_ht DECIMAL(10,2),
    montant_tva DECIMAL(10,2),
    montant_ttc DECIMAL(10,2),
    FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produits(id),
    INDEX idx_facture (facture_id),
    INDEX idx_produit (produit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. INSERTION DE DONNÉES DE TEST

-- Insertion de clients
INSERT INTO clients (nom, prenom, email, telephone, adresse, code_postal, ville, pays, numero_tva) VALUES
('Dupont', 'Jean', 'jean.dupont@email.com', '0601020304', '12 Rue de la Paix', '75001', 'Paris', 'France', 'FR12345678901'),
('Martin', 'Sophie', 'sophie.martin@email.com', '0602030405', '45 Avenue des Champs-Élysées', '69001', 'Lyon', 'France', 'FR98765432109'),
('Bernard', 'Pierre', 'pierre.bernard@email.com', '0603040506', '78 Boulevard Victor Hugo', '13001', 'Marseille', 'France', 'FR11223344556'),
('Dubois', 'Marie', 'marie.dubois@email.com', '0604050607', '23 Rue du Commerce', '33000', 'Bordeaux', 'France', 'FR66778899001'),
('Laurent', 'Paul', 'paul.laurent@email.com', '0605060708', '56 Place de la République', '31000', 'Toulouse', 'France', 'FR55443322110'),
('Petit', 'Julie', 'julie.petit@email.com', '0606070809', '89 Avenue Montaigne', '06000', 'Nice', 'France', 'FR99887766554'),
('Roux', 'Thomas', 'thomas.roux@email.com', '0607080910', '34 Rue de Rivoli', '44000', 'Nantes', 'France', 'FR33445566778'),
('Moreau', 'Isabelle', 'isabelle.moreau@email.com', '0608091011', '67 Boulevard Haussmann', '67000', 'Strasbourg', 'France', 'FR22334455667');

-- Insertion de produits
INSERT INTO produits (reference, designation, description, prix_unitaire_ht, taux_tva, actif, unite) VALUES
('PROD001', 'Ordinateur Portable Dell XPS 15', 'Laptop professionnel haute performance - 16GB RAM, 512GB SSD', 1200.00, 20.00, 1, 'Unité'),
('PROD002', 'Licence Office 365 Business', 'Suite bureautique complète Microsoft - Abonnement annuel', 99.99, 20.00, 1, 'Licence'),
('PROD003', 'Souris sans fil Logitech MX Master 3', 'Souris ergonomique professionnelle sans fil', 89.90, 20.00, 1, 'Unité'),
('PROD004', 'Écran Dell UltraSharp 27 pouces 4K', 'Moniteur professionnel UHD avec support réglable', 450.00, 20.00, 1, 'Unité'),
('PROD005', 'Clavier mécanique Cherry MX', 'Clavier professionnel rétroéclairé RGB', 120.00, 20.00, 1, 'Unité'),
('PROD006', 'Support technique - 1 heure', 'Assistance technique informatique à distance ou sur site', 80.00, 20.00, 1, 'Heure'),
('PROD007', 'Formation Windows 11 - Journée', 'Formation complète Windows 11 pour entreprise', 500.00, 20.00, 1, 'Jour'),
('PROD008', 'Webcam Logitech C920 HD Pro', 'Caméra Full HD 1080p pour visioconférence', 79.99, 20.00, 1, 'Unité'),
('PROD009', 'Casque audio Jabra Evolve2 65', 'Casque professionnel sans fil avec micro antibruit', 199.00, 20.00, 1, 'Unité'),
('PROD010', 'Station d''accueil USB-C', 'Hub multiport pour laptop - HDMI, USB 3.0, Ethernet', 150.00, 20.00, 1, 'Unité'),
('PROD011', 'Disque dur externe 2TB', 'Disque dur externe USB 3.0 pour sauvegarde', 89.00, 20.00, 1, 'Unité'),
('PROD012', 'Routeur WiFi 6 TP-Link', 'Routeur sans fil haute performance WiFi 6', 129.99, 20.00, 1, 'Unité'),
('PROD013', 'Imprimante laser HP LaserJet Pro', 'Imprimante laser monochrome réseau', 249.00, 20.00, 1, 'Unité'),
('PROD014', 'Pack antivirus Kaspersky 5 postes', 'Protection antivirus pour 5 ordinateurs - 1 an', 59.99, 20.00, 1, 'Licence'),
('PROD015', 'Câble HDMI 2.1 - 2m', 'Câble HDMI haute vitesse 8K compatible', 19.99, 20.00, 1, 'Unité');

-- 5. CRÉATION DE QUELQUES FACTURES DE TEST (optionnel)

-- Facture 1 pour Jean Dupont
INSERT INTO factures (numero_facture, date_facture, date_echeance, client_id, montant_ht, montant_tva, montant_ttc, statut, remarques) 
VALUES ('FACT-202402-0001', '2024-02-01', '2024-03-01', 1, 2469.90, 493.98, 2963.88, 'VALIDEE', 'Commande matériel bureau');

INSERT INTO lignes_facture (facture_id, produit_id, quantite, prix_unitaire_ht, taux_tva, remise, montant_ht, montant_tva, montant_ttc)
VALUES 
(1, 1, 2, 1200.00, 20.00, 5.00, 2280.00, 456.00, 2736.00),
(1, 3, 2, 89.90, 20.00, 0.00, 179.80, 35.96, 215.76);

-- Facture 2 pour Sophie Martin
INSERT INTO factures (numero_facture, date_facture, date_echeance, client_id, montant_ht, montant_tva, montant_ttc, statut, remarques) 
VALUES ('FACT-202402-0002', '2024-02-05', '2024-03-05', 2, 699.99, 140.00, 839.99, 'PAYEE', 'Formation et licences');

INSERT INTO lignes_facture (facture_id, produit_id, quantite, prix_unitaire_ht, taux_tva, remise, montant_ht, montant_tva, montant_ttc)
VALUES 
(2, 7, 1, 500.00, 20.00, 0.00, 500.00, 100.00, 600.00),
(2, 2, 2, 99.99, 20.00, 0.00, 199.98, 39.996, 239.976);

-- Facture 3 pour Pierre Bernard
INSERT INTO factures (numero_facture, date_facture, date_echeance, client_id, montant_ht, montant_tva, montant_ttc, statut, remarques) 
VALUES ('FACT-202402-0003', '2024-02-08', '2024-03-08', 3, 920.00, 184.00, 1104.00, 'ENVOYEE', 'Équipement informatique');

INSERT INTO lignes_facture (facture_id, produit_id, quantite, prix_unitaire_ht, taux_tva, remise, montant_ht, montant_tva, montant_ttc)
VALUES 
(3, 4, 2, 450.00, 20.00, 0.00, 900.00, 180.00, 1080.00),
(3, 15, 1, 19.99, 20.00, 0.00, 19.99, 3.998, 23.988);

-- 6. VÉRIFICATIONS

-- Vérifier les tables créées
SHOW TABLES;

-- Compter les enregistrements
SELECT 'Clients' AS Table_Name, COUNT(*) AS Nombre FROM clients
UNION ALL
SELECT 'Produits', COUNT(*) FROM produits
UNION ALL
SELECT 'Factures', COUNT(*) FROM factures
UNION ALL
SELECT 'Lignes de facture', COUNT(*) FROM lignes_facture;

-- Afficher quelques données
SELECT * FROM clients LIMIT 5;
SELECT * FROM produits LIMIT 5;
SELECT * FROM factures;

-- Vérifier le chiffre d'affaires
SELECT 
    SUM(montant_ttc) AS 'CA Total',
    AVG(montant_ttc) AS 'Montant Moyen',
    COUNT(*) AS 'Nombre de Factures'
FROM factures 
WHERE statut IN ('VALIDEE', 'ENVOYEE', 'PAYEE');

-- =====================================================
-- FIN DU SCRIPT
-- La base de données est prête à être utilisée !
-- =====================================================
