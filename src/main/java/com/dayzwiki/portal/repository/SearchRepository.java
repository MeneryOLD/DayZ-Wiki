package com.dayzwiki.portal.repository;

import com.dayzwiki.portal.dto.api.Order;
import com.dayzwiki.portal.dto.api.SearchApi;
import com.dayzwiki.portal.dto.api.SourceApi;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class SearchRepository {

    private static final Set<String> SORTABLE = Set.of("name", "english_name", "section", "source_table");

    @PersistenceContext
    private EntityManager entityManager;

    public List<SearchApi> search(String searchText, String type, Integer page, Integer size, List<Order> orders) {
        StringBuilder queryStr = new StringBuilder(
                "SELECT DISTINCT name, english_name, section, url, description, source_table, image " +
                        "FROM full_text_search " +
                        "WHERE (LOWER(name) LIKE :searchText " +
                        "OR LOWER(alt_name) LIKE :searchText " +
                        "OR LOWER(english_name) LIKE :searchText " +
                        "OR LOWER(section) LIKE :searchText)"
        );

        if (type != null && !type.isEmpty()) {
            queryStr.append(" AND LOWER(section) = :type");
        }

        // Sort fields come from the request body, so only allow known columns and directions (prevents SQL injection).
        String orderClause = orders == null ? "" : orders.stream()
                .filter(order -> order.getField() != null && SORTABLE.contains(order.getField().toLowerCase(Locale.ROOT)))
                .map(order -> order.getField().toLowerCase(Locale.ROOT) + ("desc".equalsIgnoreCase(order.getDirection()) ? " DESC" : " ASC"))
                .collect(Collectors.joining(", "));
        queryStr.append(orderClause.isEmpty() ? " ORDER BY LENGTH(name), name" : " ORDER BY " + orderClause);

        Query query = entityManager.createNativeQuery(queryStr.toString());
        query.setParameter("searchText", "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%");

        if (type != null && !type.isEmpty()) {
            query.setParameter("type", type.trim().toLowerCase(Locale.ROOT));
        }

        if (size != null && size > 0) {
            query.setMaxResults(size);
        } else {
            size = 10;
        }
        if (page != null && page >= 0) {
            query.setFirstResult(page * size);
        }

        List<Object[]> result = query.getResultList();
        return result.stream().map(row -> new SearchApi(
                row[0].toString(), // name
                row[1].toString(), // englishName
                row[2].toString(), // section
                row[3].toString(), // url
                shortDescription(row[4]), // description
                new SourceApi(row[5].toString(), null), // source
                row[6] // image path, may be null
        )).collect(Collectors.toList());
    }

    public int getCount(String searchText, String type) {
        StringBuilder queryStr = new StringBuilder(
                "SELECT COUNT(DISTINCT name) " +
                        "FROM full_text_search " +
                        "WHERE (LOWER(name) LIKE :searchText " +
                        "OR LOWER(alt_name) LIKE :searchText " +
                        "OR LOWER(english_name) LIKE :searchText " +
                        "OR LOWER(section) LIKE :searchText)"
        );

        if (type != null && !type.isEmpty()) {
            queryStr.append(" AND LOWER(section) = :type");
        }

        Query query = entityManager.createNativeQuery(queryStr.toString());
        query.setParameter("searchText", "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%");

        if (type != null && !type.isEmpty()) {
            query.setParameter("type", type.trim().toLowerCase(Locale.ROOT));
        }

        return ((Number) query.getSingleResult()).intValue();
    }

    private String shortDescription(Object description) {
        if (description == null) {
            return null;
        }
        String text = description.toString().replace("&nbsp;", " ").replaceAll("</(.+?)><(\\w)", "</$1> <$2");
        if (text.length() > 200) {
            text = String.format("%s...", text.substring(0, 200).trim()).replaceAll("\\s+", " ").replaceAll("\\.{4,}", "...");
        }
        return text;
    }

}