package com.casrusil.SII_ERP_AI.modules.accounting.application.listener;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.seed.ChileanChartOfAccountsSeeder;
import com.casrusil.SII_ERP_AI.modules.sso.domain.event.CompanyCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que inicializa el plan de cuentas contable cuando se crea una
 * empresa.
 * 
 * <p>
 * Escucha eventos {@link CompanyCreatedEvent} y crea automáticamente el plan de
 * cuentas
 * contable chileno estándar para la nueva empresa.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Escuchar {@link CompanyCreatedEvent} de forma asíncrona</li>
 * <li>Crear plan de cuentas chileno estándar</li>
 * <li>Inicializar cuentas de activo, pasivo, patrimonio, ingresos y gastos</li>
 * </ul>
 * 
 * <h2>Flujo:</h2>
 * <ol>
 * <li>Empresa creada → {@link CompanyCreatedEvent} publicado</li>
 * <li>Listener captura evento (asíncrono)</li>
 * <li>Seeder crea ~50 cuentas contables estándar</li>
 * <li>Empresa lista para operar</li>
 * </ol>
 * 
 * <h2>Cuentas creadas (ejemplos):</h2>
 * <ul>
 * <li>110101 - Caja</li>
 * <li>110201 - Banco</li>
 * <li>210101 - Proveedores</li>
 * <li>510101 - Gastos de Alimentación</li>
 * <li>410101 - Ingresos por Ventas</li>
 * </ul>
 * 
 * @see CompanyCreatedEvent
 * @see ChileanChartOfAccountsSeeder
 * @since 1.0
 */
@Component
public class CompanyCreatedListener {

    private final AccountRepository accountRepository;

    public CompanyCreatedListener(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Async
    @EventListener
    public void handle(CompanyCreatedEvent event) {
        // Automatically seed Chilean Chart of Accounts for the new company
        ChileanChartOfAccountsSeeder.seedAccountsForCompany(
                event.company().getId(),
                accountRepository);
    }
}
