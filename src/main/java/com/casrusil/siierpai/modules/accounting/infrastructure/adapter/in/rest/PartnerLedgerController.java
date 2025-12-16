package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.application.service.PartnerLedgerService;
import com.casrusil.siierpai.modules.accounting.domain.dto.PartnerMovementDTO;
import com.casrusil.siierpai.modules.accounting.domain.dto.PartnerSummaryDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.PartnerType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting/ledger/partners")
public class PartnerLedgerController {

    private final PartnerLedgerService partnerLedgerService;

    public PartnerLedgerController(PartnerLedgerService partnerLedgerService) {
        this.partnerLedgerService = partnerLedgerService;
    }

    @GetMapping
    public ResponseEntity<List<PartnerSummaryDTO>> getSummaries(
            @RequestParam(defaultValue = "CUSTOMER") PartnerType type) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(partnerLedgerService.getSummaries(companyId, type));
    }

    @GetMapping("/{rut}/movements")
    public ResponseEntity<List<PartnerMovementDTO>> getMovements(@PathVariable String rut) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(partnerLedgerService.getMovements(companyId, rut));
    }
}
