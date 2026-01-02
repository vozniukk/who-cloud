export interface TableStats {
  tableName: string;
  recordCount: number;
}

export interface DatabaseStats {
  totalTables: number;
  tables: TableStats[];
  timestamp: string;
}
