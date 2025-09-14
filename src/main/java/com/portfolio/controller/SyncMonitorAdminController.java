package com.portfolio.controller;

import com.portfolio.service.SyncMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin endpoints to expose sync progress logs for the floating CLI panel.
 */
@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncMonitorAdminController {

    private final SyncMonitorService syncMonitorService;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    /** Simple DTO for admin sync logs */
    public record AdminSyncLogEntry(long id, String timestamp, String level, String message) { }

    /**
     * Fetch sync logs. When an offset is provided, only logs with id greater than offset are returned.
     */
    @GetMapping("/log")
    public ResponseEntity<List<AdminSyncLogEntry>> getLogs(@RequestParam(name = "offset", required = false) Long offset) {
        List<SyncMonitorService.LogEntry> entries =
                offset == null ? syncMonitorService.getAllLogs() : syncMonitorService.getLogsSince(offset);

        List<AdminSyncLogEntry> body = entries.stream()
                .map(e -> new AdminSyncLogEntry(
                        e.getId(),
                        ISO.format(e.getTimestamp()),
                        e.getLevel(),
                        e.getMessage()
                ))
                .toList();

        return ResponseEntity.ok(body);
    }
}
