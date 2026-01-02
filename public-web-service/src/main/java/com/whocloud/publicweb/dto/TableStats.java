package com.whocloud.publicweb.dto;

public class TableStats {
    private String tableName;
    private Long recordCount;

    public TableStats() {
    }

    public TableStats(String tableName, Long recordCount) {
        this.tableName = tableName;
        this.recordCount = recordCount;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }
}
