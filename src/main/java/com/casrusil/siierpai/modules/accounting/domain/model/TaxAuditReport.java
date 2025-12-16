package com.casrusil.siierpai.modules.accounting.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TaxAuditReport(
                @JsonProperty("summary") AuditSummary summary,
                @JsonProperty("discrepancies") List<DiscrepancyDetail> discrepancies) {
        public record AuditSummary(
                        @JsonProperty("totalIvaSii") BigDecimal totalIvaSii,
                        @JsonProperty("totalIvaErp") BigDecimal totalIvaErp,
                        @JsonProperty("difference") BigDecimal difference,
                        @JsonProperty("totalInvoicesSii") int totalInvoicesSii,
                        @JsonProperty("invoicesWithIssues") int invoicesWithIssues) {
        }

        public record DiscrepancyDetail(
                        @JsonProperty("type") String type,

                        // ✅ CORRECCIÓN CRÍTICA: Formatear fecha como String ISO (YYYY-MM-DD)
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @JsonProperty("date") LocalDate date,

                        @JsonProperty("counterpart") String counterpart,
                        @JsonProperty("folio") long folio,
                        @JsonProperty("amountSii") BigDecimal amountSii,
                        @JsonProperty("amountErp") BigDecimal amountErp,
                        @JsonProperty("status") String status) {
        }
}
