package com.portfolio.controller;

import com.portfolio.service.BootstrapSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bootstrap controller for triggering initial portfolio sync when empty.
 * Public CORS-enabled endpoint for frontend to call during app initialization.
 */
@RestController
@RequestMapping("/api/bootstrap")
@RequiredArgsConstructor
@Tag(name = "Bootstrap API", description = "Bootstrap sync operations for empty portfolio")
@CrossOrigin(origins = {"http://localhost:4200", "https://portfolio.bernarduriza.dev"})
@Slf4j
public class BootstrapController {
    
    private final BootstrapSyncService bootstrapSyncService;
    
    @PostMapping("/sync-if-empty")
    @Operation(
        summary = "Trigger bootstrap sync if portfolio is empty",
        description = "Fire-and-forget endpoint that triggers two-phase sync (ingest + curate) if no portfolio projects exist. Implements cooldown to prevent spam.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Bootstrap sync result",
                content = @Content(schema = @Schema(implementation = BootstrapSyncService.BootstrapSyncResult.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<BootstrapSyncService.BootstrapSyncResult> syncIfEmpty() {
        try {
            log.debug("Bootstrap sync request received");
            BootstrapSyncService.BootstrapSyncResult result = bootstrapSyncService.maybeTrigger();
            
            if (result.triggered()) {
                log.info("Bootstrap sync triggered: {}", result.reason());
            } else {
                log.debug("Bootstrap sync not triggered: {}", result.reason());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error during bootstrap sync request", e);
            return ResponseEntity.internalServerError()
                    .body(new BootstrapSyncService.BootstrapSyncResult(false, "server-error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    @Operation(
        summary = "Get bootstrap sync status",
        description = "Get current bootstrap sync status for monitoring and debugging"
    )
    public ResponseEntity<BootstrapSyncService.BootstrapStatus> getStatus() {
        try {
            BootstrapSyncService.BootstrapStatus status = bootstrapSyncService.getStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting bootstrap status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}