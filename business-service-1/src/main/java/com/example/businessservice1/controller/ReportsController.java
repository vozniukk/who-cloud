package com.example.businessservice1.controller;

import com.example.businessservice1.dto.*;
import com.example.businessservice1.service.ReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST контроллер для работы с отчетами
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Получить отчет по оборудованию (JSON)
     * GET /api/reports/equipment
     */
    @GetMapping("/equipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EquipmentReportResponse>> getEquipmentReport(
            @ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/equipment with filter: {}", filter);
        List<EquipmentReportResponse> report = reportsService.generateEquipmentReport(filter);
        return ResponseEntity.ok(report);
    }

    /**
     * Экспорт отчета по оборудованию в CSV
     * GET /api/reports/equipment/export/csv
     */
    @GetMapping("/equipment/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportEquipmentToCSV(@ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/equipment/export/csv");
        try {
            byte[] csvData = reportsService.exportEquipmentToCSV(filter);
            String filename = "equipment_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting equipment report to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Экспорт отчета по оборудованию в Excel
     * GET /api/reports/equipment/export/excel
     */
    @GetMapping("/equipment/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportEquipmentToExcel(@ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/equipment/export/excel");
        try {
            byte[] excelData = reportsService.exportEquipmentToExcel(filter);
            String filename = "equipment_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting equipment report to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить отчет по движению оборудования (JSON)
     * GET /api/reports/movement
     */
    @GetMapping("/movement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<MovementReportResponse>> getMovementReport(
            @ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/movement with filter: {}", filter);
        List<MovementReportResponse> report = reportsService.generateMovementReport(filter);
        return ResponseEntity.ok(report);
    }

    /**
     * Экспорт отчета по движению в CSV
     * GET /api/reports/movement/export/csv
     */
    @GetMapping("/movement/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportMovementToCSV(@ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/movement/export/csv");
        try {
            byte[] csvData = reportsService.exportMovementToCSV(filter);
            String filename = "movement_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting movement report to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить отчет по материально-ответственным лицам (JSON)
     * GET /api/reports/custodians
     */
    @GetMapping("/custodians")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<CustodianReportResponse>> getCustodianReport(
            @ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/custodians with filter: {}", filter);
        List<CustodianReportResponse> report = reportsService.generateCustodianReport(filter);
        return ResponseEntity.ok(report);
    }

    /**
     * Экспорт отчета по МОЛ в Excel
     * GET /api/reports/custodians/export/excel
     */
    @GetMapping("/custodians/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportCustodiansToExcel(@ModelAttribute ReportFilterRequest filter) {
        log.info("GET /api/reports/custodians/export/excel");
        try {
            byte[] excelData = reportsService.exportCustodiansToExcel(filter);
            String filename = "custodians_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting custodians report to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить статистику для дашборда
     * GET /api/reports/dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        log.info("GET /api/reports/dashboard");
        // TODO: Implement dashboard statistics aggregation
        return ResponseEntity.ok(DashboardStatsResponse.builder()
                .message("Dashboard statistics - to be implemented")
                .build());
    }

    /**
     * DTO для статистики дашборда (placeholder)
     */
    @lombok.Data
    @lombok.Builder
    private static class DashboardStatsResponse {
        private String message;
    }
}
