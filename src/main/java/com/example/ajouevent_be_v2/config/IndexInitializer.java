package com.example.ajouevent_be_v2.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class IndexInitializer implements ApplicationRunner {

    private static final int MYSQL_ER_DUP_KEYNAME = 1061;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("create-indexes.sql");
        String sql;
        try (InputStream is = resource.getInputStream()) {
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int created = 0;
        int skipped = 0;
        for (String segment : sql.split(";")) {
            String statement = stripComments(segment).trim();
            if (statement.isEmpty()) {
                continue;
            }
            try {
                jdbcTemplate.execute(statement);
                created++;
            } catch (DataAccessException e) {
                if (isDuplicateKeyName(e)) {
                    skipped++;
                } else {
                    throw e;
                }
            }
        }
        log.info("Index initialization complete — created: {}, already existed: {}", created, skipped);
    }

    private String stripComments(String segment) {
        return Arrays.stream(segment.split("\n"))
            .filter(line -> !line.trim().startsWith("--"))
            .collect(Collectors.joining("\n"));
    }

    private boolean isDuplicateKeyName(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLException sqlEx && sqlEx.getErrorCode() == MYSQL_ER_DUP_KEYNAME) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
