package com.casrusil.siierpai.modules.accounting.domain.port.out;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
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
 * @see com.casrusil.siierpai.modules.accounting.infrastructure.adapter.out.persistence.AccountingEntryJpaAdapter
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

        /**
         * Busca los movimientos de una cuenta específica para el Libro Mayor (General
         * Ledger).
         *
         * @param companyId   ID de la empresa
         * @param accountCode Código de la cuenta
         * @param from        Fecha inicio
         * @param to          Fecha fin
         * @return Lista de movimientos planos (sin saldo calculado)
         */
        java.util.List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement> findMovementsByAccount(
                        CompanyId companyId, String accountCode, java.time.LocalDate from, java.time.LocalDate to);

        /**
         * Busca asientos para el Estado de Resultados (Clases 4, 5, 6).
         *
         * @param companyId ID de la empresa
         * @param from      Fecha inicio
         * @param to        Fecha fin
         * @param classes   Lista de prefijos de cuenta (ej: 4, 5, 6)
         * @return Lista de asientos que contienen líneas de esas clases
         */
        java.util.List<AccountingEntry> findInPeriodForClasses(
                        CompanyId companyId, java.time.LocalDate from, java.time.LocalDate to, List<Integer> classes);

        /**
         * Elimina asientos por tipo de referencia y rango de fechas.
         * Útil para limpieza de datos masiva.
         */
        void deleteByReferenceTypeAndPeriod(CompanyId companyId, String referenceType, java.time.LocalDate from,
                        java.time.LocalDate to);
}
