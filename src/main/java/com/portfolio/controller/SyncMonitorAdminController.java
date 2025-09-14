package com.portfolio.controller;

import com.portfolio.service.SyncMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

    /**
     * Fetch sync logs. When an offset is provided, only logs with id greater than offset are returned.
     */
    @GetMapping("/log")
    public ResponseEntity<List<Map<String, Object>>> getLogs(@RequestParam(name = "offset", required = false) Long offset) {
        List<SyncMonitorService.LogEntry> entries =
                offset == null ? syncMonitorService.getAllLogs() : syncMonitorService.getLogsSince(offset);

        List<Map<String, Object>> body = entries.stream()
                .map(e -> Map.of(
                        "id", e.getId(),
                        "timestamp", ISO.format(e.getTimestamp()),
                        "level", e.getLevel(),
                        "message", e.getMessage()
                ))
                .toList();

        return ResponseEntity.ok(body);
    }
}

