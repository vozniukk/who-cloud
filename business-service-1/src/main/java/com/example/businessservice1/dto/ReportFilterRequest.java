package com.example.businessservice1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO для фильтрации отчетов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterRequest {
    
    /**
     * Дата начала периода
     */
    private LocalDate dateFrom;
    
    /**
     * Дата окончания периода
     */
    private LocalDate dateTo;
    
    /**
     * Список ID категорий для фильтрации
     */
    private List<Long> categoryIds;
    
    /**
     * Список ID статусов для фильтрации
     */
    private List<Long> statusIds;
    
    /**
     * Список ID материально-ответственных для фильтрации
     */
    private List<Long> custodianIds;
    
    /**
     * Список ID типов контрактов для фильтрации
     */
    private List<Long> contractTypeIds;
    
    /**
     * Поисковый запрос (по инвентарному номеру, серийному номеру, производителю)
     */
    private String searchQuery;
    
    /**
     * Включать ли историю в отчет
     */
    private Boolean includeHistory;
}
