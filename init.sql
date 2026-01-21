-- 1) Schéma de base de données (optionnel)
-- CREATE DATABASE IF NOT EXISTS hackaton CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE hackaton;

-- 2) Table des dossiers
CREATE TABLE IF NOT EXISTS folders
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255)    NOT NULL,
    parent_id  BIGINT UNSIGNED NULL,
    created_at DATETIME        NOT NULL,
    updated_at DATETIME        NOT NULL,
    -- Optionnel: pour du tri / affichage
    -- sort_order  INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_folders PRIMARY KEY (id),

    CONSTRAINT fk_folders_parent
        FOREIGN KEY (parent_id) REFERENCES folders (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,

    -- Un dossier ne peut pas s'appeler vide
    CONSTRAINT chk_folders_name_not_empty CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE = InnoDB;

CREATE INDEX idx_folders_parent ON folders (parent_id);
CREATE INDEX idx_folders_updated_at ON folders (updated_at);
CREATE INDEX idx_folders_created_at ON folders (created_at);


-- 3) Table des notes
CREATE TABLE IF NOT EXISTS notes
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    folder_id  BIGINT UNSIGNED NOT NULL,
    title      VARCHAR(255)    NOT NULL,
    content    MEDIUMTEXT      NOT NULL,

    -- métadonnées calculées côté client :
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

CREATE INDEX idx_notes_folder ON notes (folder_id);
CREATE INDEX idx_notes_updated_at ON notes (updated_at);
CREATE INDEX idx_notes_created_at ON notes (created_at);

-- Optionnel: si tu veux une recherche plein texte (facultatif)
-- ALTER TABLE notes ADD FULLTEXT INDEX ftx_notes_title_content (title, content);


-- 4) Table de paramètres applicatifs pour le Super Dossier
-- Cette table force l'unicité d'un seul super_root_folder_id.
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

-- creation du super dossier
START TRANSACTION;

-- 1) Créer le Super Dossier (parent_id = NULL)
INSERT INTO folders (name, parent_id, created_at, updated_at)
VALUES ('__SUPER__', NULL, NOW(), NOW());

-- 2) Récupérer son id
SET @super_id = LAST_INSERT_ID();

-- 3) Initialiser app_settings (id = 1)
INSERT INTO app_settings (id, super_root_folder_id)
VALUES (1, @super_id);

COMMIT;


-- -- 5) TRIGGERS pour faire respecter les règles métier
--
-- DELIMITER $$
--
-- -- a) Empêcher la création d’une NOTE dans le Super Dossier
-- CREATE TRIGGER trg_notes_prevent_super_root_insert
--     BEFORE INSERT
--     ON notes
--     FOR EACH ROW
-- BEGIN
--     DECLARE super_id BIGINT UNSIGNED;
--     SELECT super_root_folder_id INTO super_id FROM app_settings WHERE id = 1;
--     IF NEW.folder_id = super_id THEN
--         SIGNAL SQLSTATE '45000'
--             SET MESSAGE_TEXT = 'Impossible de créer une note dans le Super Dossier.';
--     END IF;
-- END$$

-- CREATE TRIGGER trg_notes_prevent_super_root_update
--     BEFORE UPDATE
--     ON notes
--     FOR EACH ROW
-- BEGIN
--     DECLARE super_id BIGINT UNSIGNED;
--     SELECT super_root_folder_id INTO super_id FROM app_settings WHERE id = 1;
--     IF NEW.folder_id = super_id THEN
--         SIGNAL SQLSTATE '45000'
--             SET MESSAGE_TEXT = 'Impossible de déplacer une note vers le Super Dossier.';
--     END IF;
-- END$$
--

-- b) Garantir qu’un seul dossier est "root réel" (parent_id IS NULL) et que c’est le Super Dossier
-- (i) À l’INSERT : si parent_id IS NULL, l'id doit être celui stocké dans app_settings (mais à l'insert, l'id est AUTO_INCREMENT, donc on contrôle plutôt que personne d'autre ne soit NULL)
CREATE TRIGGER trg_folders_enforce_single_null_parent_insert
    BEFORE INSERT
    ON folders
    FOR EACH ROW
BEGIN
    DECLARE super_id BIGINT UNSIGNED;
    DECLARE existing_null_parent_count INT;

    -- Si on insère un dossier sans parent, on vérifie que c’est pour créer le Super Dossier
    IF NEW.parent_id IS NULL THEN
        -- Il ne doit pas exister déjà un dossier sans parent
        SELECT COUNT(*) INTO existing_null_parent_count FROM folders WHERE parent_id IS NULL;
        IF existing_null_parent_count > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Un seul dossier racine (Super Dossier) est autorisé.';
        END IF;
    END IF;
END$$



-- (ii) À l’UPDATE : empêcher de mettre parent_id=NULL pour autre chose que le Super Dossier
CREATE TRIGGER trg_folders_enforce_single_null_parent_update
    BEFORE UPDATE
    ON folders
    FOR EACH ROW
BEGIN
    DECLARE super_id BIGINT UNSIGNED;
    SELECT super_root_folder_id INTO super_id FROM app_settings WHERE id = 1;

    IF NEW.parent_id IS NULL AND NEW.id <> super_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Seul le Super Dossier peut avoir parent_id = NULL.';
    END IF;
END$$

-- c) Empêcher de remplacer le Super Dossier par un dossier qui a un parent (doit être root)
CREATE TRIGGER trg_app_settings_super_root_must_be_root
    BEFORE UPDATE
    ON app_settings
    FOR EACH ROW
BEGIN
    DECLARE parent_of_new BIGINT UNSIGNED;
    SELECT parent_id INTO parent_of_new FROM folders WHERE id = NEW.super_root_folder_id;

    IF parent_of_new IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Le Super Dossier doit être un dossier racine (parent_id NULL).';
    END IF;
END$$

DELIMITER ;


-- -------------------------------------------------------------------------------------------------------
-- Dossier affiché à la racine (en réalité enfant du Super Dossier)
INSERT INTO folders (name, parent_id, created_at, updated_at)
VALUES ('Projets', @super_id, NOW(), NOW());

SET @projets_id = LAST_INSERT_ID();

-- Sous-dossier "Hackathon"
INSERT INTO folders (name, parent_id, created_at, updated_at)
VALUES ('Hackathon', @projets_id, NOW(), NOW());

SET @hackathon_id = LAST_INSERT_ID();

-- Note dans Hackathon
INSERT INTO notes (folder_id, title, content,
                   size_bytes, line_count, word_count, char_count,
                   created_at, updated_at)
VALUES (@hackathon_id,
        'Spec technique',
        'Texte...',
        1024, 12, 180, 980,
        NOW(), NOW());
