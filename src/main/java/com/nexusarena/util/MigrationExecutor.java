package com.nexusarena.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exécuteur JDBC pour automatiser les scripts de migration SQL (Étape 6 du TP).
 * Supporte les scripts multi-lignes et multi-instructions.
 */
public class MigrationExecutor {

    private static final Logger LOGGER = Logger.getLogger(MigrationExecutor.class.getName());

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public MigrationExecutor(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration(String scriptPath) throws Exception {
        LOGGER.info(() -> "Démarrage de la migration SQL : " + scriptPath);

        InputStream is = getClass().getClassLoader().getResourceAsStream(scriptPath);
        if (is == null) {
            throw new IllegalArgumentException("Fichier de migration introuvable dans le classpath : " + scriptPath);
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            conn.setAutoCommit(false);

            StringBuilder sqlBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignorer les commentaires et lignes vides
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                sqlBuffer.append(line).append(" ");
                if (trimmed.endsWith(";")) {
                    String sqlStatement = sqlBuffer.toString().replace(";", "").trim();
                    if (!sqlStatement.isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            LOGGER.info(() -> "Exécution SQL : " + sqlStatement);
                            stmt.execute(sqlStatement);
                        }
                    }
                    sqlBuffer.setLength(0);
                }
            }

            conn.commit();
            LOGGER.info("Migration exécutée et validée avec succès !");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Échec de l'exécution de la migration SQL", e);
            throw e;
        }
    }
}
