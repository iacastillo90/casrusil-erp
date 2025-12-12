package com.casrusil.siierpai.modules.banking.domain.port.out;

import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para persistencia de transacciones bancarias.
 * 
 * <p>
 * Almacena transacciones importadas desde extractos bancarios (CSV/Excel)
 * para su posterior reconciliación con asientos contables.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Persistir transacciones bancarias importadas</li>
 * <li>Filtrar transacciones por rango de fechas</li>
 * <li>Identificar transacciones no reconciliadas</li>
 * </ul>
 * 
 * @see BankTransaction
 * @see com.casrusil.siierpai.modules.banking.application.service.ReconciliationService
 * @since 1.0
 */
public interface BankTransactionRepository {
    void save(BankTransaction transaction);

    List<BankTransaction> findByCompanyId(CompanyId companyId);

    List<BankTransaction> findByCompanyIdAndDateRange(CompanyId companyId, LocalDate startDate, LocalDate endDate);

    List<BankTransaction> findUnreconciledByCompanyId(CompanyId companyId);

    BankTransaction findById(UUID id);
}
