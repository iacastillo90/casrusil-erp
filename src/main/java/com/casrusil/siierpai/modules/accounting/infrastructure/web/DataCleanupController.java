package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.DataCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounting/cleanup")
public class DataCleanupController {

    private final DataCleanupService cleanupService;

    public DataCleanupController(DataCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @PostMapping("/november-rcv")
    public ResponseEntity<String> cleanupNovemberRcv() {
        cleanupService.cleanupNovemberRcv();
        return ResponseEntity.ok("Successfully cleaned up November RCV entries.");
    }
}
