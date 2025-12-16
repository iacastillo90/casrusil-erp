package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.dto.PartnerMovementDTO;
import com.casrusil.siierpai.modules.accounting.domain.dto.PartnerSummaryDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.PartnerType;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.PaymentStatus;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class PartnerLedgerService {

    private final InvoiceRepository invoiceRepository;

    public PartnerLedgerService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<PartnerSummaryDTO> getSummaries(CompanyId companyId, PartnerType type) {
        List<Invoice> allInvoices = invoiceRepository.findByCompanyId(companyId);

        TransactionType targetType = (type == PartnerType.CUSTOMER) ? TransactionType.SALE : TransactionType.PURCHASE;

        Map<String, List<Invoice>> groupedByRut;
        if (type == PartnerType.CUSTOMER) {
            // For customers, group by receiverRut
            groupedByRut = allInvoices.stream()
                    .filter(inv -> inv.getTransactionType() == targetType)
                    .filter(inv -> inv.getStatus() != PaymentStatus.PAID)
                    .collect(Collectors.groupingBy(Invoice::getReceiverRut));
        } else {
            // For suppliers, group by issuerRut
            groupedByRut = allInvoices.stream()
                    .filter(inv -> inv.getTransactionType() == targetType)
                    .filter(inv -> inv.getStatus() != PaymentStatus.PAID)
                    .collect(Collectors.groupingBy(Invoice::getIssuerRut));
        }

        return groupedByRut.entrySet().stream()
                .map(entry -> {
                    String rut = entry.getKey();
                    List<Invoice> invoices = entry.getValue();

                    String name = invoices.stream()
                            .filter(inv -> inv.getBusinessName() != null)
                            .map(Invoice::getBusinessName)
                            .findFirst()
                            .orElse("Desconocido");

                    BigDecimal totalDebt = invoices.stream()
                            .map(Invoice::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal overdueDebt = invoices.stream()
                            .filter(inv -> inv.getDueDate() != null && inv.getDueDate().isBefore(LocalDate.now()))
                            .map(Invoice::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int pendingInvoices = invoices.size();
                    LocalDate lastMovement = invoices.stream()
                            .map(Invoice::getDate)
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    return new PartnerSummaryDTO(rut, name, totalDebt, overdueDebt, pendingInvoices, lastMovement);
                })
                .sorted(Comparator.comparing(PartnerSummaryDTO::totalDebt).reversed())
                .toList();
    }

    public List<PartnerMovementDTO> getMovements(CompanyId companyId, String rut) {
        // Fetch all invoices involving this partner (either as issuer or receiver)
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(inv -> rut.equals(inv.getIssuerRut()) || rut.equals(inv.getReceiverRut()))
                .sorted(Comparator.comparing(Invoice::getDate))
                .toList();

        return buildMovementsSafely(invoices);
    }

    // Helper to determine if the movement is a charge (increases debt/receivable)
    // based on perspective.
    // This is complex.
    // If I am observing a Customer (rut): Sale Invoice = Positive (Charge), Payment
    // = Negative.
    // If I am observing a Supplier (rut): Purchase Invoice = Negative? Or just
    // tracked as Debt?
    // Usually Ledger shows "Balance".
    // For Customer: Positive Balance = They owe me.
    // For Supplier: Positive Balance = I owe them.
    // So:
    // If Sale Invoice (I sold to them): Amount is added.
    // If Purchase Invoice (I bought from them): Amount is added (to my debt).
    // So usually Invoice amounts are Positive in 3rd party ledger. Payments are
    // negative.

    // Simplification: Invoices are always "Charges" to the account balance.
    // (Customer Account: they owe me. Supplier Account: I owe them).
    // Payments/Credit Notes would be "Credits".
    // Since we only have Invoices here (and Credit Notes are Types), we need to
    // handle Notes.
    // Invoice (33/34) -> Positive.
    // Credit Note (61) -> Negative.

    // Wait, getMovements implementation above inside stream with AtomicReference
    // and map is RISKY if not careful.
    // But sequential stream preserves order.
    // However, map is lazy? No, toList triggers it.
    // Better to use a loop to build the list to be safe and clear.

    private List<PartnerMovementDTO> buildMovementsSafely(List<Invoice> invoices) {
        java.util.List<PartnerMovementDTO> movements = new java.util.ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;

        for (Invoice inv : invoices) {
            BigDecimal sign = BigDecimal.ONE;
            // Check for Credit Notes
            if (inv.getType().getCode() == 61) { // 61 = Credit Note
                sign = new BigDecimal("-1");
            }

            BigDecimal amount = inv.getTotalAmount().multiply(sign);
            balance = balance.add(amount);

            movements.add(new PartnerMovementDTO(
                    inv.getId().toString(),
                    inv.getDate(),
                    getReference(inv),
                    amount,
                    balance,
                    formatStatus(inv),
                    null));
        }
        return movements;
    }

    private boolean isCharge(Invoice inv, String partnerRut) {
        // Logic handled in buildMovementsSafely regarding Credit Notes.
        return true;
    }

    private String getReference(Invoice inv) {
        return inv.getType().getDescription() + " #" + inv.getFolio();
    }

    private String formatStatus(Invoice inv) {
        if (inv.getStatus() == PaymentStatus.PAID)
            return "PAGADO";
        if (inv.getDueDate() != null && inv.getDueDate().isBefore(LocalDate.now()))
            return "VENCIDO";
        return "AL DIA";
    }
}
