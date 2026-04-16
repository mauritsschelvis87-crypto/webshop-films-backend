package homecinema.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostgresConstraintMigrationRunner implements CommandLineRunner {

    private static final String UPDATE_FILM_REGION_CHECK = """
            DO $$
            BEGIN
                IF EXISTS (
                    SELECT 1
                    FROM pg_constraint
                    WHERE conname = 'film_region_check'
                ) THEN
                    ALTER TABLE film DROP CONSTRAINT film_region_check;
                END IF;

                ALTER TABLE film
                ADD CONSTRAINT film_region_check
                CHECK (region IN ('A', 'B', 'FREE'));
            END $$;
            """;

    private static final String UPDATE_USER_FILM_RATING_COLUMN = """
            DO $$
            BEGIN
                IF EXISTS (
                    SELECT 1
                    FROM information_schema.tables
                    WHERE table_name = 'user_film_ratings'
                ) THEN
                    ALTER TABLE user_film_ratings
                    ALTER COLUMN rating TYPE numeric(2,1);
                END IF;
            END $$;
            """;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public PostgresConstraintMigrationRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!isPostgreSql()) {
            return;
        }

        jdbcTemplate.execute(UPDATE_FILM_REGION_CHECK);
        jdbcTemplate.execute(UPDATE_USER_FILM_RATING_COLUMN);
    }

    private boolean isPostgreSql() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        }
    }
}
