package com.example.businessservice1.service;

import com.example.businessservice1.dto.*;
import com.whocloud.business.service1.entity.*;
import com.whocloud.business.service1.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для генерации отчетов
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentHistoryRepository historyRepository;
    private final CustodianRepository custodianRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Генерация отчета по оборудованию
     */
    public List<EquipmentReportResponse> generateEquipmentReport(ReportFilterRequest filter) {
        log.info("Generating equipment report with filter: {}", filter);

        Specification<Equipment> spec = buildEquipmentSpecification(filter);
        List<Equipment> equipment = equipmentRepository.findAll(spec, Sort.by("inventoryNumber"));

        return equipment.stream()
                .map(this::mapToEquipmentReport)
                .collect(Collectors.toList());
    }

    /**
     * Генерация отчета по движению оборудования
     */
    public List<MovementReportResponse> generateMovementReport(ReportFilterRequest filter) {
        log.info("Generating movement report with filter: {}", filter);

        Specification<EquipmentHistory> spec = buildHistorySpecification(filter);
        List<EquipmentHistory> history = historyRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "timestamp"));

        return history.stream()
                .map(this::mapToMovementReport)
                .collect(Collectors.toList());
    }

    /**
     * Генерация отчета по материально-ответственным лицам
     */
    public List<CustodianReportResponse> generateCustodianReport(ReportFilterRequest filter) {
        log.info("Generating custodian report with filter: {}", filter);

        List<Custodian> custodians;
        if (filter != null && filter.getCustodianIds() != null && !filter.getCustodianIds().isEmpty()) {
            custodians = custodianRepository.findAllById(filter.getCustodianIds());
        } else {
            custodians = custodianRepository.findAll(Sort.by("lastName", "firstName"));
        }

        return custodians.stream()
                .map(this::mapToCustodianReport)
                .collect(Collectors.toList());
    }

    /**
     * Экспорт отчета по оборудованию в CSV
     */
    public byte[] exportEquipmentToCSV(ReportFilterRequest filter) throws IOException {
        log.info("Exporting equipment report to CSV");
        List<EquipmentReportResponse> data = generateEquipmentReport(filter);

        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("Inventory Number,Serial Number,Manufacturer,Category,Status,Current Owner,Position,Release Date,Commissioning Date\n");

        // Data
        for (EquipmentReportResponse item : data) {
            csv.append(escapeCsv(item.getInventoryNumber())).append(",");
            csv.append(escapeCsv(item.getSerialNumber())).append(",");
            csv.append(escapeCsv(item.getManufacturer())).append(",");
            csv.append(escapeCsv(item.getCategoryName())).append(",");
            csv.append(escapeCsv(item.getStatusName())).append(",");
            csv.append(escapeCsv(item.getCurrentOwnerName())).append(",");
            csv.append(escapeCsv(item.getCurrentOwnerPosition())).append(",");
            csv.append(item.getReleaseDate() != null ? item.getReleaseDate().format(DATE_FORMATTER) : "").append(",");
            csv.append(item.getCommissioningDate() != null ? item.getCommissioningDate().format(DATE_FORMATTER) : "");
            csv.append("\n");
        }

        return csv.toString().getBytes("UTF-8");
    }

    /**
     * Экспорт отчета по оборудованию в Excel
     */
    public byte[] exportEquipmentToExcel(ReportFilterRequest filter) throws IOException {
        log.info("Exporting equipment report to Excel");
        List<EquipmentReportResponse> data = generateEquipmentReport(filter);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Equipment Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Inventory Number", "Serial Number", "Manufacturer", "Category", "Status", 
                               "Current Owner", "Position", "Release Date", "Commissioning Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (EquipmentReportResponse item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getInventoryNumber());
                row.createCell(1).setCellValue(item.getSerialNumber());
                row.createCell(2).setCellValue(item.getManufacturer());
                row.createCell(3).setCellValue(item.getCategoryName());
                row.createCell(4).setCellValue(item.getStatusName());
                row.createCell(5).setCellValue(item.getCurrentOwnerName() != null ? item.getCurrentOwnerName() : "Warehouse");
                row.createCell(6).setCellValue(item.getCurrentOwnerPosition() != null ? item.getCurrentOwnerPosition() : "");
                row.createCell(7).setCellValue(item.getReleaseDate() != null ? item.getReleaseDate().format(DATE_FORMATTER) : "");
                row.createCell(8).setCellValue(item.getCommissioningDate() != null ? item.getCommissioningDate().format(DATE_FORMATTER) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Экспорт отчета по движению в CSV
     */
    public byte[] exportMovementToCSV(ReportFilterRequest filter) throws IOException {
        log.info("Exporting movement report to CSV");
        List<MovementReportResponse> data = generateMovementReport(filter);

        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("Date,Inventory Number,Serial Number,Manufacturer,Category,From Custodian,To Custodian,Details\n");

        // Data
        for (MovementReportResponse item : data) {
            csv.append(item.getChangeDate() != null ? item.getChangeDate().format(DATETIME_FORMATTER) : "").append(",");
            csv.append(escapeCsv(item.getInventoryNumber())).append(",");
            csv.append(escapeCsv(item.getSerialNumber())).append(",");
            csv.append(escapeCsv(item.getManufacturer())).append(",");
            csv.append(escapeCsv(item.getCategoryName())).append(",");
            csv.append(escapeCsv(item.getFromCustodianName())).append(",");
            csv.append(escapeCsv(item.getToCustodianName())).append(",");
            csv.append(escapeCsv(item.getDetails()));
            csv.append("\n");
        }

        return csv.toString().getBytes("UTF-8");
    }

    /**
     * Экспорт отчета по МОЛ в Excel
     */
    public byte[] exportCustodiansToExcel(ReportFilterRequest filter) throws IOException {
        log.info("Exporting custodians report to Excel");
        List<CustodianReportResponse> data = generateCustodianReport(filter);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Custodians Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Number", "Last Name", "First Name", "Position", 
                               "Status", "Total Equipment", "Hire Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (CustodianReportResponse item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getIdentificationNumber());
                row.createCell(1).setCellValue(item.getLastName());
                row.createCell(2).setCellValue(item.getFirstName());
                row.createCell(3).setCellValue(item.getPosition());
                row.createCell(4).setCellValue(item.getStatusName());
                row.createCell(5).setCellValue(item.getEquipmentCount() != null ? item.getEquipmentCount() : 0);
                row.createCell(6).setCellValue(item.getHireDate() != null ? item.getHireDate().format(DATE_FORMATTER) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ========== Private helper methods ==========

    /**
     * Построение спецификации для фильтрации оборудования
     */
    private Specification<Equipment> buildEquipmentSpecification(ReportFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filter.getCategoryIds()));
            }

            if (filter.getStatusIds() != null && !filter.getStatusIds().isEmpty()) {
                predicates.add(root.get("status").get("id").in(filter.getStatusIds()));
            }

            if (filter.getCustodianIds() != null && !filter.getCustodianIds().isEmpty()) {
                predicates.add(root.get("currentCustodian").get("id").in(filter.getCustodianIds()));
            }

            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("commissioningDate"), filter.getDateFrom()));
            }

            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("commissioningDate"), filter.getDateTo()));
            }

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearchQuery().toLowerCase() + "%";
                Predicate inventorySearch = cb.like(cb.lower(root.get("inventoryNumber")), searchPattern);
                Predicate serialSearch = cb.like(cb.lower(root.get("serialNumber")), searchPattern);
                Predicate manufacturerSearch = cb.like(cb.lower(root.get("manufacturer")), searchPattern);
                predicates.add(cb.or(inventorySearch, serialSearch, manufacturerSearch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Построение спецификации для фильтрации истории
     */
    private Specification<EquipmentHistory> buildHistorySpecification(ReportFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.getDateFrom().atStartOfDay()));
            }

            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.getDateTo().atTime(23, 59, 59)));
            }

            if (filter.getCustodianIds() != null && !filter.getCustodianIds().isEmpty()) {
                Predicate fromCustodian = root.get("fromCustodianId").in(filter.getCustodianIds());
                Predicate toCustodian = root.get("toCustodianId").in(filter.getCustodianIds());
                predicates.add(cb.or(fromCustodian, toCustodian));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Маппинг Equipment в EquipmentReportResponse
     */
    private EquipmentReportResponse mapToEquipmentReport(Equipment equipment) {
        List<EquipmentHistory> history = historyRepository.findByEquipmentIdOrderByTimestampDesc(equipment.getId());

        return EquipmentReportResponse.builder()
                .id(equipment.getId())
                .inventoryNumber(equipment.getInventoryNumber())
                .serialNumber(equipment.getSerialNumber())
                .manufacturer(equipment.getManufacturer())
                .releaseDate(equipment.getReleaseDate())
                .commissioningDate(equipment.getCommissioningDate())
                .initialCost(null) // Not available in Equipment entity
                .currency(null) // Not available in Equipment entity
                .statusName(equipment.getStatus() != null ? equipment.getStatus().getName() : null)
                .statusTranslations(equipment.getStatus() != null ? equipment.getStatus().getTranslations() : null)
                .categoryName(equipment.getCategory() != null ? equipment.getCategory().getName() : null)
                .categoryTranslations(equipment.getCategory() != null ? equipment.getCategory().getTranslations() : null)
                .contractTypeName(null) // Not available in Equipment entity
                .contractTypeTranslations(null) // Not available in Equipment entity
                .currentOwnerName(equipment.getCurrentCustodian() != null ? 
                        equipment.getCurrentCustodian().getFirstName() + " " + equipment.getCurrentCustodian().getLastName() : "Warehouse")
                .currentOwnerPosition(equipment.getCurrentCustodian() != null ? equipment.getCurrentCustodian().getPosition() : null)
                .currentOwnerIdentificationNumber(equipment.getCurrentCustodian() != null ? 
                        equipment.getCurrentCustodian().getIdentificationNumber() : null)
                .transfersCount(history.size())
                .lastTransferDate(!history.isEmpty() ? history.get(0).getTimestamp() : null)
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }

    /**
     * Маппинг EquipmentHistory в MovementReportResponse
     */
    private MovementReportResponse mapToMovementReport(EquipmentHistory history) {
        Equipment equipment = history.getEquipment();
        Custodian fromCustodian = history.getFromCustodianId() != null ? 
                custodianRepository.findById(history.getFromCustodianId()).orElse(null) : null;
        Custodian toCustodian = history.getToCustodianId() != null ? 
                custodianRepository.findById(history.getToCustodianId()).orElse(null) : null;
        
        return MovementReportResponse.builder()
                .historyId(history.getId())
                .changeDate(history.getTimestamp())
                .equipmentId(equipment.getId())
                .inventoryNumber(equipment.getInventoryNumber())
                .serialNumber(equipment.getSerialNumber())
                .manufacturer(equipment.getManufacturer())
                .categoryName(equipment.getCategory() != null ? equipment.getCategory().getName() : null)
                .fromCustodianName(fromCustodian != null ? 
                        fromCustodian.getFirstName() + " " + fromCustodian.getLastName() : "Warehouse")
                .fromCustodianPosition(fromCustodian != null ? fromCustodian.getPosition() : null)
                .toCustodianName(toCustodian != null ? 
                        toCustodian.getFirstName() + " " + toCustodian.getLastName() : "Warehouse")
                .toCustodianPosition(toCustodian != null ? toCustodian.getPosition() : null)
                .oldStatusName(null) // Not available in EquipmentHistory
                .newStatusName(null) // Not available in EquipmentHistory
                .details(history.getDetails())
                .build();
    }

    /**
     * Маппинг Custodian в CustodianReportResponse
     */
    private CustodianReportResponse mapToCustodianReport(Custodian custodian) {
        Long custodianId = custodian.getId();
        
        // Подсчет оборудования
        List<Equipment> equipment = equipmentRepository.findByCurrentCustodianId(custodianId);
        int totalCount = equipment.size();

        return CustodianReportResponse.builder()
                .id(custodian.getId())
                .authUserId(custodian.getAuthUserId())
                .firstName(custodian.getFirstName())
                .lastName(custodian.getLastName())
                .identificationNumber(custodian.getIdentificationNumber())
                .position(custodian.getPosition())
                .hireDate(custodian.getHireDate())
                .statusName(custodian.getStatus() != null ? custodian.getStatus().getName() : null)
                .equipmentCount(totalCount)
                .activeEquipmentCount(null) // Would require status filtering
                .inRepairEquipmentCount(null) // Would require status filtering
                .retiredEquipmentCount(null) // Would require status filtering
                .createdAt(custodian.getCreatedAt())
                .updatedAt(custodian.getUpdatedAt())
                .build();
    }

    /**
     * Escape CSV values to prevent CSV injection
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
