package com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import java.util.List;

/**
 * Repositorio para persistencia de asientos contables.
 * 
 * <p>
 * Define el contrato de persistencia para entidades {@link AccountingEntry}.
 * Los asientos son generados automáticamente desde facturas o creados
 * manualmente.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Persistir asientos contables</li>
 * <li>Recuperar asientos por empresa (multi-tenancy)</li>
 * <li>Soportar auditoría con JaVers</li>
 * </ul>
 * 
 * @see AccountingEntry
 * @see com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.adapter.out.persistence.AccountingEntryJpaAdapter
 * @since 1.0
 */
public interface AccountingEntryRepository {

    /**
     * Persiste un asiento contable.
     * 
     * <p>
     * Los asientos son inmutables una vez creados. Para correcciones,
     * se debe crear un nuevo asiento de ajuste.
     * 
     * @param entry El asiento a persistir
     */
    void save(AccountingEntry entry);

    /**
     * Lista todos los asientos de una empresa.
     * 
     * @param companyId ID de la empresa
     * @return Lista de asientos contables
     */
    List<AccountingEntry> findByCompanyId(CompanyId companyId);
}
