package com.casrusil.siierpai.modules.invoicing.domain.port.in;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;

/**
 * Caso de uso para buscar facturas.
 */
public interface SearchInvoicesUseCase {
    /**
     * Busca facturas por empresa.
     *
     * @param companyId El ID de la empresa.
     * @return Lista de facturas.
     */
    List<Invoice> getInvoicesByCompany(CompanyId companyId);
}
