-- =====================================================================================
-- init.sql - HallowNote / Hackaton
-- Compatible MySQL 8.4
-- Objectif: schéma + super folder + triggers + seed data (idempotent via INSERT IGNORE)
-- =====================================================================================

-- 0) Encodage
SET NAMES utf8mb4;

-- 1) DB
CREATE DATABASE IF NOT EXISTS hackaton
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE hackaton;

-- =====================================================================================
-- (Optionnel) reset complet si vous le voulez
-- =====================================================================================
-- SET FOREIGN_KEY_CHECKS = 0;
-- DROP TABLE IF EXISTS notes;
-- DROP TABLE IF EXISTS app_settings;
-- DROP TABLE IF EXISTS folders;
-- SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================================
-- 2) Table folders
-- =====================================================================================
CREATE TABLE IF NOT EXISTS folders
(
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name       VARCHAR(255)    NOT NULL,
  parent_id  BIGINT UNSIGNED NULL,
  created_at DATETIME        NOT NULL,
  updated_at DATETIME        NOT NULL,

  CONSTRAINT pk_folders PRIMARY KEY (id),

  CONSTRAINT fk_folders_parent
    FOREIGN KEY (parent_id) REFERENCES folders (id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,

  CONSTRAINT chk_folders_name_not_empty CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE = InnoDB;

CREATE INDEX idx_folders_parent     ON folders (parent_id);
CREATE INDEX idx_folders_updated_at ON folders (updated_at);
CREATE INDEX idx_folders_created_at ON folders (created_at);

-- =====================================================================================
-- 3) Table notes
-- =====================================================================================
CREATE TABLE IF NOT EXISTS notes
(
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  folder_id  BIGINT UNSIGNED NOT NULL,
  title      VARCHAR(255)    NOT NULL,
  content    MEDIUMTEXT,

  size_bytes BIGINT UNSIGNED NOT NULL,
  line_count INT UNSIGNED    NOT NULL,
  word_count INT UNSIGNED    NOT NULL,
  char_count INT UNSIGNED    NOT NULL,

  created_at DATETIME        NOT NULL,
  updated_at DATETIME        NOT NULL,

  CONSTRAINT pk_notes PRIMARY KEY (id),

  CONSTRAINT fk_notes_folder
    FOREIGN KEY (folder_id) REFERENCES folders (id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,

  CONSTRAINT chk_notes_title_not_empty CHECK (CHAR_LENGTH(TRIM(title)) > 0),
  CONSTRAINT chk_notes_counts_nonneg CHECK (
    size_bytes >= 0 AND line_count >= 0 AND word_count >= 0 AND char_count >= 0
  )
) ENGINE = InnoDB;

CREATE INDEX idx_notes_folder     ON notes (folder_id);
CREATE INDEX idx_notes_updated_at ON notes (updated_at);
CREATE INDEX idx_notes_created_at ON notes (created_at);

-- =====================================================================================
-- 4) Table app_settings
-- =====================================================================================
CREATE TABLE IF NOT EXISTS app_settings
(
  id                   TINYINT UNSIGNED NOT NULL CHECK (id = 1),
  super_root_folder_id BIGINT UNSIGNED  NOT NULL,

  CONSTRAINT pk_app_settings PRIMARY KEY (id),

  CONSTRAINT fk_app_settings_super_root
    FOREIGN KEY (super_root_folder_id) REFERENCES folders (id)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
) ENGINE = InnoDB;

-- =====================================================================================
-- 5) Super folder + app_settings (id deterministe)
-- =====================================================================================
START TRANSACTION;

INSERT IGNORE INTO folders (id, name, parent_id, created_at, updated_at)
VALUES (1, '__SUPER__', NULL, NOW(), NOW());

ALTER TABLE folders AUTO_INCREMENT = 2;

SET @super_id := 1;

INSERT INTO app_settings (id, super_root_folder_id)
VALUES (1, @super_id)
ON DUPLICATE KEY UPDATE super_root_folder_id = VALUES(super_root_folder_id);

COMMIT;

-- =====================================================================================
-- 6) Triggers
-- =====================================================================================
DROP TRIGGER IF EXISTS trg_folders_enforce_single_null_parent_insert;
DROP TRIGGER IF EXISTS trg_folders_enforce_single_null_parent_update;
DROP TRIGGER IF EXISTS trg_app_settings_super_root_must_be_root;

DELIMITER $$

CREATE TRIGGER trg_folders_enforce_single_null_parent_insert
BEFORE INSERT ON folders
FOR EACH ROW
BEGIN
  DECLARE super_id BIGINT UNSIGNED;

  SELECT super_root_folder_id INTO super_id
  FROM app_settings
  WHERE id = 1;

  IF NEW.parent_id IS NULL AND NEW.id <> super_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Seul le Super Dossier peut avoir parent_id = NULL.';
  END IF;
END$$

CREATE TRIGGER trg_folders_enforce_single_null_parent_update
BEFORE UPDATE ON folders
FOR EACH ROW
BEGIN
  DECLARE super_id BIGINT UNSIGNED;

  SELECT super_root_folder_id INTO super_id
  FROM app_settings
  WHERE id = 1;

  IF NEW.parent_id IS NULL AND NEW.id <> super_id THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Seul le Super Dossier peut avoir parent_id = NULL.';
  END IF;
END$$

CREATE TRIGGER trg_app_settings_super_root_must_be_root
BEFORE UPDATE ON app_settings
FOR EACH ROW
BEGIN
  DECLARE parent_of_new BIGINT UNSIGNED;

  SELECT parent_id INTO parent_of_new
  FROM folders
  WHERE id = NEW.super_root_folder_id;

  IF parent_of_new IS NOT NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Le Super Dossier doit etre un dossier racine (parent_id NULL).';
  END IF;
END$$

DELIMITER ;

-- =====================================================================================
-- 7) Donnees de demonstration (base)
-- =====================================================================================
START TRANSACTION;

INSERT INTO folders (id, name, parent_id, created_at, updated_at)
VALUES (2, 'Projets', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), parent_id = VALUES(parent_id);

INSERT INTO folders (id, name, parent_id, created_at, updated_at)
VALUES (3, 'Hackathon', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), parent_id = VALUES(parent_id);

INSERT INTO notes (id, folder_id, title, content,
                   size_bytes, line_count, word_count, char_count,
                   created_at, updated_at)
VALUES (1, 3, 'Spec technique', 'Texte...',
        1024, 12, 180, 980,
        NOW(), NOW())
ON DUPLICATE KEY UPDATE
  folder_id  = VALUES(folder_id),
  title      = VALUES(title),
  content    = VALUES(content),
  size_bytes = VALUES(size_bytes),
  line_count = VALUES(line_count),
  word_count = VALUES(word_count),
  char_count = VALUES(char_count),
  updated_at = VALUES(updated_at);

COMMIT;

-- =====================================================================================
-- 8) Seed demo additionnel (issus de ton dump) - IDs fixes 4..11 et notes 2..12
--    Format copy/paste safe: contenus en une ligne avec \n
-- =====================================================================================
START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

INSERT IGNORE INTO folders (id, name, parent_id, created_at, updated_at) VALUES
(4,'Projet',2,'2026-01-22 16:14:59','2026-01-22 16:14:59'),
(5,'JeuVideo',2,'2026-01-22 16:15:11','2026-01-22 16:15:11'),
(6,'Techno. Mobile',2,'2026-01-22 16:15:50','2026-01-22 16:15:50'),
(7,'Frontend',4,'2026-01-22 16:18:26','2026-01-22 16:18:26'),
(8,'Backend',4,'2026-01-22 16:18:39','2026-01-22 16:18:39'),
(9,'Flutter',6,'2026-01-22 16:24:35','2026-01-22 16:24:35'),
(10,'Consignes',3,'2026-01-22 16:30:38','2026-01-22 16:30:52'),
(11,'Repartition',10,'2026-01-22 16:34:21','2026-01-22 16:34:21');

ALTER TABLE folders AUTO_INCREMENT = 12;

INSERT IGNORE INTO notes
(id, folder_id, title, content, size_bytes, line_count, word_count, char_count, created_at, updated_at)
VALUES
(2,1,'Lorem Ipsum',
'# SECTION 1\n\n# Lorem Ipsum - Introduction\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit.  \nSuspendisse et felis ac nibh blandit pulvinar.  \nNullam euismod **massa non felis pharetra**, vitae cursus *nunc gravida*.\n\n## Sous-section 1.1\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit.  \nMauris pulvinar gravida dui, vitae facilisis sem imperdiet ut.  \nDonec nec sapien ac ipsum tempor volutpat.\n\n### Liste\n\n*   element 1\n*   element 2\n*   element 3\n\n### Citation\n\n> Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n\n### Code\n\n    console.log(\"Hello Markdown!\");\n\n***\n\n# SECTION 2\n\n# Chapitre 2 - Developpement\n\nPhasellus congue urna vel laoreet viverra.  \nDuis bibendum risus et ligula dignissim, nec aliquet sapien pharetra.  \nMauris at justo ac tellus fermentum posuere.\n\n## Liste imbriquee\n\n*   Niveau 1\n    *   Niveau 2\n        *   Niveau 3\n\n***\n\n# SECTION 3\n\n# Chapitre 3 - Volume de texte\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit.  \nSed varius augue vitae turpis pulvinar, vel scelerisque dui consequat.  \nInteger hendrerit bibendum fermentum.  \nPraesent eu lorem ac massa tempor lobortis.  \nDonec interdum posuere ligula, eget suscipit neque convallis id.\n\n*(Bloc repete pour remplir des pages)*\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit.  \nLorem ipsum dolor sit amet, consectetur adipiscing elit.  \nLorem ipsum dolor sit amet, consectetur adipiscing elit.\n\n***\n\n# SECTION 4\n\n# Chapitre 4 - Plus de Lorem Ipsum\n\nMorbi sed felis in eros varius placerat in eu libero.  \nVivamus luctus urna vitae justo hendrerit, vel iaculis lacus pretium.\n\n    Lorem ipsum dolor sit amet,\n    consectetur adipiscing elit.\n    Sed do eiusmod tempor incididunt\n    ut labore et dolore magna aliqua.\n\n***\n\n# SECTION 5\n\n# Conclusion\n\nSed feugiat risus volutpat pellentesque consequat.  \nNam scelerisque, velit eget maximus varius, lorem justo tempus neque, vel suscipit sem ipsum non velit.\n\nMerci d''utiliser ce Lorem Ipsum pour tester ton export PDF
\n\n***\n',
2724,70,387,2153,'2026-01-22 16:11:50','2026-01-22 16:14:28'),

(3,4,'Description generale',
'# Readify - Application de gestion de bibliotheque\n\n**Readify** est une application web full-stack de gestion de bibliotheque, concue pour faciliter la consultation, l''emprunt et la gestion des livres.\n',
1332,18,191,1042,'2026-01-22 16:18:47','2026-01-22 16:23:55'),

(4,8,'Description','Back-end Readify (ASP.NET Core)\n',1861,33,265,1452,'2026-01-22 16:21:13','2026-01-22 16:22:59'),
(5,7,'Description','Front-end Readify (Angular)\n',923,17,130,718,'2026-01-22 16:23:32','2026-01-22 16:23:39'),
(6,9,'Flutter','Description projet Flutter\n',837,6,115,671,'2026-01-22 16:24:43','2026-01-22 16:28:23'),
(7,5,'Description','NoSiege - description\n',1868,24,278,1416,'2026-01-22 16:29:42','2026-01-22 16:29:48'),
(8,10,'Contexte','HallowNotes - contexte\n',1186,9,179,928,'2026-01-22 16:33:20','2026-01-22 16:33:50'),
(9,11,'Repartition des taches','Repartition des taches\n',2492,25,362,1924,'2026-01-22 16:34:39','2026-01-22 16:36:26'),
(10,6,'Zzz','',0,1,0,0,'2026-01-22 16:40:02','2026-01-22 16:40:02'),
(11,1,'ZZzz','',0,1,0,0,'2026-01-22 16:40:14','2026-01-22 16:40:14'),
(12,1,'ZZzz (1)','',0,1,0,0,'2026-01-22 16:40:31','2026-01-22 16:40:31');

ALTER TABLE notes AUTO_INCREMENT = 13;

SET FOREIGN_KEY_CHECKS = 1;

COMMIT;