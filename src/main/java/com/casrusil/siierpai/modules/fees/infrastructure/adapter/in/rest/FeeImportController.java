package com.casrusil.siierpai.modules.fees.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fees")
public class FeeImportController {

    private final FeeReceiptService feeReceiptService;

    public FeeImportController(FeeReceiptService feeReceiptService) {
        this.feeReceiptService = feeReceiptService;
    }

    @PostMapping("/import")
    public ResponseEntity<FeeReceiptService.ImportResult> importFees(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") UUID companyId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new FeeReceiptService.ImportResult(0, 0, "File is empty"));
        }

        try {
            FeeReceiptService.ImportResult result = feeReceiptService.importFromCsv(file.getInputStream(),
                    new CompanyId(companyId));
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new FeeReceiptService.ImportResult(0, 0, "Error processing file: " + e.getMessage()));
        }
    }
}
