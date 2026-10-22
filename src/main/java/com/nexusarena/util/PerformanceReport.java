package com.nexusarena.util;

import org.hibernate.Session;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import java.util.logging.Logger;

/**
 * Générateur de rapport de performance basé sur l'API Statistics d'Hibernate (Étape 7 du TP).
 */
public class PerformanceReport {

    private static final Logger LOGGER = Logger.getLogger(PerformanceReport.class.getName());
    private final EntityManager em;

    public PerformanceReport(EntityManager em) {
        this.em = em;
    }

    public void printReport() {
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();

        StringBuilder sb = new StringBuilder();
        sb.append("\n=======================================================\n");
        sb.append("   RAPPORT D'ANALYSE DE PERFORMANCE - NEXUS ARENA\n");
        sb.append("=======================================================\n");
        sb.append(String.format("► Requêtes exécutées           : %d\n", stats.getQueryExecutionCount()));
        sb.append(String.format("► PreparedStatements préparés  : %d\n", stats.getPrepareStatementCount()));
        sb.append(String.format("► Requête la plus lente        : %s (%d ms)\n",
                stats.getQueryExecutionMaxTimeQueryString() != null ? stats.getQueryExecutionMaxTimeQueryString() : "N/A",
                stats.getQueryExecutionMaxTime()));
        sb.append("-------------------------------------------------------\n");
        sb.append(" [CACHE DE SECOND NIVEAU - L2 CACHE]\n");
        sb.append(String.format("► Cache L2 Hits (succès)       : %d\n", stats.getSecondLevelCacheHitCount()));
        sb.append(String.format("► Cache L2 Misses (échecs)     : %d\n", stats.getSecondLevelCacheMissCount()));
        sb.append(String.format("► Cache L2 Puts (insertions)   : %d\n", stats.getSecondLevelCachePutCount()));

        double totalL2Requests = stats.getSecondLevelCacheHitCount() + stats.getSecondLevelCacheMissCount();
        double hitRatio = totalL2Requests > 0 ? (stats.getSecondLevelCacheHitCount() * 100.0 / totalL2Requests) : 0.0;
        sb.append(String.format("► Taux d'efficacité L2 (Hit %%) : %.2f %%\n", hitRatio));

        sb.append("-------------------------------------------------------\n");
        sb.append(" [CACHE DE REQUÊTES - QUERY CACHE]\n");
        sb.append(String.format("► Query Cache Hits             : %d\n", stats.getQueryCacheHitCount()));
        sb.append(String.format("► Query Cache Misses           : %d\n", stats.getQueryCacheMissCount()));
        sb.append(String.format("► Query Cache Puts             : %d\n", stats.getQueryCachePutCount()));
        sb.append("-------------------------------------------------------\n");
        sb.append(" [TRANSACTIONS & SESSIONS]\n");
        sb.append(String.format("► Transactions validées        : %d\n", stats.getSuccessfulTransactionCount()));
        sb.append(String.format("► Total transactions           : %d\n", stats.getTransactionCount()));
        sb.append(String.format("► Sessions ouvertes            : %d\n", stats.getSessionOpenCount()));
        sb.append(String.format("► Sessions fermées             : %d\n", stats.getSessionCloseCount()));

        // Détail par région si disponible
        for (String regionName : stats.getSecondLevelCacheRegionNames()) {
            SecondLevelCacheStatistics regionStats = stats.getSecondLevelCacheStatistics(regionName);
            if (regionStats != null && (regionStats.getHitCount() > 0 || regionStats.getPutCount() > 0)) {
                sb.append(String.format("   • Région [%s] -> Hits: %d, Misses: %d, Puts: %d\n",
                        regionName, regionStats.getHitCount(), regionStats.getMissCount(), regionStats.getPutCount()));
            }
        }
        sb.append("=======================================================\n");

        System.out.println(sb.toString());
        LOGGER.info("Rapport de performance généré.");
    }
}
