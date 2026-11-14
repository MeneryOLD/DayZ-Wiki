package com.dayzwiki.portal.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entry counts per wiki section, used by the home page dashboard and the sidebar.
 */
@RestController
@RequestMapping("/api/v1/wiki")
public class WikiStatsController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/stats")
    @SuppressWarnings("unchecked")
    public Map<String, Long> getStats() {
        List<Object[]> rows = entityManager
                .createNativeQuery("SELECT source_table, COUNT(*) FROM full_text_search GROUP BY source_table")
                .getResultList();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : rows) {
            counts.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private static final Set<String> MAPS = Set.of("Chernarus", "Livonia", "Sakhal", "Nasdara");

    /**
     * Per-map loot summary: how many entries of each section spawn on the map, and which spawn only there.
     */
    @GetMapping("/maps/{map}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getMapStats(@PathVariable String map) {
        String name = MAPS.stream().filter(m -> m.equalsIgnoreCase(map)).findFirst().orElse(null);
        if (name == null) {
            return ResponseEntity.notFound().build();
        }
        List<Object[]> countRows = entityManager
                .createNativeQuery("SELECT source_table, COUNT(*) FROM full_text_search WHERE maps LIKE :like GROUP BY source_table")
                .setParameter("like", "%" + name + "%")
                .getResultList();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : countRows) {
            counts.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        List<Object[]> onlyRows = entityManager
                .createNativeQuery("SELECT english_name, url, image, source_table, section FROM full_text_search WHERE maps = :map ORDER BY source_table, english_name")
                .setParameter("map", name)
                .getResultList();
        List<Map<String, Object>> exclusive = new ArrayList<>();
        for (Object[] row : onlyRows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("englishName", row[0]);
            item.put("url", row[1]);
            item.put("image", row[2]);
            item.put("source", row[3]);
            item.put("section", row[4]);
            exclusive.add(item);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("map", name);
        body.put("counts", counts);
        body.put("exclusive", exclusive);
        return ResponseEntity.ok(body);
    }
}
