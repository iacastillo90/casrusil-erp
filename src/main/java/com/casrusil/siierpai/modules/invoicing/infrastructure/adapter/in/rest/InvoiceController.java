package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.service.InvoiceDispatchService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de facturas electrónicas.
 * 
 * <p>
 * Permite crear, listar y enviar facturas por correo electrónico.
 * Es el punto de entrada principal para la facturación.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/invoices}: Crear nueva factura.</li>
 * <li>{@code GET /api/v1/invoices}: Listar facturas de la empresa.</li>
 * <li>{@code POST /api/v1/invoices/{id}/send}: Enviar factura por email.</li>
 * </ul>
 * 
 * @see CreateInvoiceUseCase
 * @see SearchInvoicesUseCase
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

        private final CreateInvoiceUseCase createInvoiceUseCase;
        private final SearchInvoicesUseCase searchInvoicesUseCase;
        private final InvoiceDispatchService invoiceDispatchService;

        public InvoiceController(CreateInvoiceUseCase createInvoiceUseCase,
                        SearchInvoicesUseCase searchInvoicesUseCase,
                        InvoiceDispatchService invoiceDispatchService) {
                this.createInvoiceUseCase = createInvoiceUseCase;
                this.searchInvoicesUseCase = searchInvoicesUseCase;
                this.invoiceDispatchService = invoiceDispatchService;
        }

        @PostMapping
        public ResponseEntity<Invoice> createInvoice(@RequestBody CreateInvoiceRequest request) {
                CompanyId companyId = CompanyContext.requireCompanyId();
                Invoice invoice = Invoice.create(
                                companyId,
                                InvoiceType.fromCode(request.tipoDte()),
                                request.folio(),
                                request.rutEmisor(),
                                request.razonSocialEmisor(),
                                request.fechaEmision(),
                                request.montoTotal(),
                                request.montoNeto(),
                                request.montoIva(),
                                request.lines().stream().map(this::mapLine).collect(Collectors.toList()));

                return ResponseEntity.ok(createInvoiceUseCase.createInvoice(invoice));
        }

        @GetMapping
        public ResponseEntity<List<Invoice>> getInvoices() {
                CompanyId companyId = CompanyContext.requireCompanyId();
                return ResponseEntity.ok(searchInvoicesUseCase.getInvoicesByCompany(companyId));
        }

        @PostMapping("/{id}/send")
        public ResponseEntity<Void> sendInvoice(@PathVariable UUID id, @RequestParam String email) {
                invoiceDispatchService.dispatchInvoice(id, email);
                return ResponseEntity.ok().build();
        }

        private InvoiceLine mapLine(InvoiceLineRequest line) {
                return new InvoiceLine(1, line.description(), null, line.quantity(), line.unitPrice(),
                                line.totalAmount(), "UN");
        }

        public record CreateInvoiceRequest(
                        Integer tipoDte,
                        Long folio,
                        String rutEmisor,
                        String razonSocialEmisor,
                        LocalDate fechaEmision,
                        BigDecimal montoTotal,
                        BigDecimal montoNeto,
                        BigDecimal montoIva,
                        List<InvoiceLineRequest> lines) {
        }

        public record InvoiceLineRequest(
                        String description,
                        BigDecimal quantity,
                        BigDecimal unitPrice,
                        BigDecimal totalAmount) {
        }
}
