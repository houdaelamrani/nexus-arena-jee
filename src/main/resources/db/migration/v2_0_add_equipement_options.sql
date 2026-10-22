-- ====================================================================
-- Nexus Arena - Migration Version 2.0
-- Ajout d'options d'équipements et d'optimisations d'indexation
-- ====================================================================

-- 1. Ajout de la colonne des options d'équipement (ex: raquettes carbone, simulateur haptique)
ALTER TABLE espaces_arena ADD COLUMN IF NOT EXISTS options_equipement VARCHAR(255);

-- 2. Ajout de la colonne pour consignes et notes techniques
ALTER TABLE espaces_arena ADD COLUMN IF NOT EXISTS notes_techniques VARCHAR(255);

-- 3. Création d'un index de recherche rapide sur le téléphone des membres
CREATE INDEX IF NOT EXISTS idx_membre_telephone ON membres(telephone);
