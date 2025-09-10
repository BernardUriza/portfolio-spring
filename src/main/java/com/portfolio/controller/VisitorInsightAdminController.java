/**
 * Creado por Bernard Orozco
 * Admin controller for visitor insights management
 */
package com.portfolio.controller;

import com.portfolio.model.VisitorInsight;
import com.portfolio.repository.VisitorInsightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.io.StringWriter;
import java.io.PrintWriter;

@RestController
@RequestMapping("/api/admin/insights")
@CrossOrigin(origins = {"http://localhost:4200", "https://localhost:4200"})
public class VisitorInsightAdminController {
    
    @Autowired
    private VisitorInsightRepository insightRepository;
    
    @GetMapping
    public ResponseEntity<Page<VisitorInsight>> getInsights(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Boolean hasContact,
            Pageable pageable) {
        
        Page<VisitorInsight> insights = insightRepository.findWithFilters(dateFrom, dateTo, minDuration, hasContact, pageable);
        return ResponseEntity.ok(insights);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VisitorInsight> getInsight(@PathVariable Long id) {
        return insightRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/export.csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Boolean hasContact,
            Pageable pageable) {
        
        Page<VisitorInsight> insights = insightRepository.findWithFilters(dateFrom, dateTo, minDuration, hasContact, pageable);
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        // CSV Header
        writer.println("ID,Session ID,Started At,Ended At,Duration (s),Pages Visited,Projects Viewed,Has Contact,AI Conclusion");
        
        // CSV Data
        insights.getContent().forEach(insight -> {
            writer.printf("%d,\"%s\",%s,%s,%d,%d,\"%s\",%s,\"%s\"%n",
                insight.getId(),
                insight.getSessionId(),
                insight.getStartedAt(),
                insight.getEndedAt() != null ? insight.getEndedAt() : "",
                insight.getDurationSeconds() != null ? insight.getDurationSeconds() : 0,
                insight.getPagesVisited(),
                String.join(";", insight.getProjectsViewed()),
                insight.getContactMessageId() != null ? "Yes" : "No",
                escapeCSV(insight.getAiConclusion())
            );
        });
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "visitor_insights.csv");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(stringWriter.toString());
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}