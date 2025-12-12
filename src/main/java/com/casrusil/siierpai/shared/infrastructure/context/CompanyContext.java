package com.casrusil.siierpai.shared.infrastructure.context;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

/**
 * Contexto de empresa para multi-tenancy usando Java 21 ScopedValue.
 * 
 * <p>
 * Proporciona aislamiento de datos entre empresas (tenants) de forma
 * thread-safe
 * y eficiente usando la nueva API ScopedValue de Java 21. Cada operación se
 * ejecuta
 * en el contexto de una empresa específica, garantizando que los datos no se
 * mezclen.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Almacenar el {@link CompanyId} actual en un ScopedValue</li>
 * <li>Proporcionar acceso thread-safe al CompanyId</li>
 * <li>Ejecutar operaciones en el contexto de una empresa específica</li>
 * <li>Garantizar aislamiento de datos entre tenants</li>
 * </ul>
 * 
 * <h2>Ventajas de ScopedValue sobre ThreadLocal:</h2>
 * <ul>
 * <li>Inmutable - No puede cambiar una vez establecido</li>
 * <li>Compatible con Virtual Threads - No hay memory leaks</li>
 * <li>Más eficiente - Menor overhead que ThreadLocal</li>
 * <li>Seguro - No requiere cleanup manual</li>
 * </ul>
 * 
 * <h2>Uso en el sistema:</h2>
 * <p>
 * Todos los repositorios JPA usan este contexto para filtrar automáticamente
 * las consultas por empresa. Los schedulers y listeners lo usan para ejecutar
 * operaciones en el contexto correcto.
 * 
 * <h2>Ejemplo de uso:</h2>
 * 
 * <pre>{@code
 * // En un scheduler que procesa todas las empresas
 * List<Company> companies = companyRepository.findAll();
 * for (Company company : companies) {
 *     CompanyContext.runInCompanyContext(company.getId(), () -> {
 *         // Todo el código aquí se ejecuta en el contexto de esta empresa
 *         List<Invoice> invoices = invoiceRepository.findAll();
 *         // invoices solo contiene facturas de esta empresa
 *     });
 * }
 * 
 * // En un filtro JPA
 * CompanyId currentCompany = CompanyContext.requireCompanyId();
 * query.setParameter("companyId", currentCompany);
 * }</pre>
 * 
 * @see CompanyId
 * @see com.casrusil.siierpai.modules.ai_assistant.application.scheduler.FinancialAdvisorScheduler
 * @since 1.0
 */
public class CompanyContext {
    public static final ScopedValue<CompanyId> COMPANY_ID = ScopedValue.newInstance();

    public static CompanyId getCompanyId() {
        return COMPANY_ID.isBound() ? COMPANY_ID.get() : null;
    }

    public static CompanyId requireCompanyId() {
        CompanyId id = getCompanyId();
        if (id == null) {
            throw new IllegalStateException("No company context found");
        }
        return id;
    }

    public static void runInCompanyContext(CompanyId companyId, Runnable action) {
        ScopedValue.where(COMPANY_ID, companyId).run(action);
    }
}
