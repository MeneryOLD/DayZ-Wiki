package com.dayzwiki.portal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Widens columns that older versions of the app created too small. Hibernate's ddl-auto=update adds
 * new columns but never changes existing ones, so post text was capped at 255 characters and image
 * columns could reject normal photos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaUpgrade implements ApplicationRunner {

    private record Wanted(String table, String column, List<String> okTypes, String definition) {}

    private static final List<Wanted> COLUMNS = List.of(
            new Wanted("posts", "content", List.of("text", "mediumtext", "longtext"), "TEXT"),
            new Wanted("post_images", "image", List.of("longblob"), "LONGBLOB NOT NULL"),
            new Wanted("user_avatars", "avatar", List.of("longblob"), "LONGBLOB NOT NULL"),
            new Wanted("user_banners", "banner", List.of("longblob"), "LONGBLOB NOT NULL"));

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        for (Wanted w : COLUMNS) {
            try {
                List<String> types = jdbc.queryForList(
                        "select data_type from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
                        String.class, w.table(), w.column());
                if (!types.isEmpty() && !w.okTypes().contains(types.get(0).toLowerCase())) {
                    jdbc.execute("alter table " + w.table() + " modify " + w.column() + " " + w.definition());
                    log.info("Widened {}.{} from {} to {}", w.table(), w.column(), types.get(0), w.definition());
                }
            } catch (RuntimeException e) {
                log.warn("Could not check column {}.{}: {}", w.table(), w.column(), e.getMessage());
            }
        }
    }
}
