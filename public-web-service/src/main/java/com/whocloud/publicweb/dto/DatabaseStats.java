package com.whocloud.publicweb.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DatabaseStats {
    private Integer totalTables;
    private List<TableStats> tables;
    private LocalDateTime timestamp;

    public DatabaseStats() {
    }

    public DatabaseStats(Integer totalTables, List<TableStats> tables, LocalDateTime timestamp) {
        this.totalTables = totalTables;
        this.tables = tables;
        this.timestamp = timestamp;
    }

    public Integer getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(Integer totalTables) {
        this.totalTables = totalTables;
    }

    public List<TableStats> getTables() {
        return tables;
    }

    public void setTables(List<TableStats> tables) {
        this.tables = tables;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
