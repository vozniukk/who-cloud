package com.whocloud.publicweb.service;

import com.whocloud.publicweb.dto.DatabaseStats;
import com.whocloud.publicweb.dto.TableStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DatabaseStatsService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseStatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DatabaseStats getDatabaseStats() {
        // Query to get all user tables with their row counts
        String query = """
            SELECT 
                t.table_name,
                COALESCE(s.n_live_tup, 0) as record_count
            FROM information_schema.tables t
            LEFT JOIN pg_stat_user_tables s ON t.table_name = s.relname
            WHERE t.table_schema = 'public'
                AND t.table_type = 'BASE TABLE'
            ORDER BY t.table_name
        """;

        List<TableStats> tables = jdbcTemplate.query(query, (rs, rowNum) ->
            new TableStats(
                rs.getString("table_name"),
                rs.getLong("record_count")
            )
        );

        return new DatabaseStats(
            tables.size(),
            tables,
            LocalDateTime.now()
        );
    }
}
