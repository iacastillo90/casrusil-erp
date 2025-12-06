package com.casrusil.SII_ERP_AI.modules.accounting.application.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AuditAlert;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Servicio para validar el uso correcto de cuentas contables.
 * Previene el uso de cuentas inapropiadas según el tipo de negocio.
 */
@Service
public class AccountValidationService {

    // Cuentas que NO deben usarse en empresas de servicios
    private static final Set<String> INVENTORY_ACCOUNTS = Set.of(
            "1103", // Existencias
            "110301", // Mercaderías
            "110302", // Materias Primas
            "110303" // Productos Terminados
    );

    // Cuentas que requieren documentación especial
    private static final Set<String> SPECIAL_ACCOUNTS = Set.of(
            "2104", // Provisiones
            "210402", // Provisión Vacaciones
            "210403" // Provisión Gratificaciones
    );

    // Cuentas que no deben tener saldo negativo
    private static final Set<String> POSITIVE_ONLY_ACCOUNTS = Set.of(
            "1101", // Caja
            "1102", // Bancos
            "1103" // Existencias
    );

    /**
     * Valida un asiento contable y retorna alertas si encuentra problemas.
     */
    public List<AuditAlert> validateEntry(AccountingEntry entry, String companyBusinessType) {
        List<AuditAlert> alerts = new ArrayList<>();

        for (AccountingEntryLine line : entry.getLines()) {
            // Validar uso de cuentas de inventario en empresas de servicios
            if ("SERVICE".equals(companyBusinessType) && isInventoryAccount(line.accountCode())) {
                alerts.add(createInvalidAccountAlert(
                        entry,
                        line.accountCode(),
                        "Las empresas de servicios no deberían usar cuentas de inventario"));
            }

            // Validar cuentas especiales
            if (isSpecialAccount(line.accountCode())) {
                alerts.add(createSpecialAccountAlert(entry, line.accountCode()));
            }

            // Validar cuentas que no deben tener saldo negativo
            if (isPositiveOnlyAccount(line.accountCode())) {
                // Nota: Aquí necesitaríamos el saldo actual de la cuenta
                // Por ahora solo alertamos si hay un crédito grande
                if (line.credit().compareTo(line.debit()) > 0) {
                    alerts.add(createNegativeBalanceWarning(entry, line.accountCode()));
                }
            }
        }

        return alerts;
    }

    /**
     * Valida que un código de cuenta exista y esté bien formado.
     */
    public boolean isValidAccountCode(String accountCode) {
        if (accountCode == null || accountCode.isEmpty()) {
            return false;
        }

        // Validar formato: debe ser numérico y tener entre 4 y 6 dígitos
        if (!accountCode.matches("\\d{4,6}")) {
            return false;
        }

        // Validar que el primer dígito sea válido (1-5 para activo, pasivo, patrimonio,
        // ingresos, gastos)
        char firstDigit = accountCode.charAt(0);
        return firstDigit >= '1' && firstDigit <= '5';
    }

    private boolean isInventoryAccount(String accountCode) {
        return INVENTORY_ACCOUNTS.stream()
                .anyMatch(inv -> accountCode.startsWith(inv));
    }

    private boolean isSpecialAccount(String accountCode) {
        return SPECIAL_ACCOUNTS.stream()
                .anyMatch(special -> accountCode.startsWith(special));
    }

    private boolean isPositiveOnlyAccount(String accountCode) {
        return POSITIVE_ONLY_ACCOUNTS.stream()
                .anyMatch(positive -> accountCode.startsWith(positive));
    }

    private AuditAlert createInvalidAccountAlert(AccountingEntry entry, String accountCode, String reason) {
        String title = "Cuenta Contable Inapropiada";
        String description = String.format(
                "El asiento '%s' usa la cuenta %s. %s",
                entry.getDescription(),
                accountCode,
                reason);

        return new AuditAlert(
                AuditAlert.Type.INVALID_ACCOUNT,
                AuditAlert.Severity.WARNING,
                title,
                description,
                entry.getId().toString(),
                "Revisar si la cuenta es apropiada para este tipo de transacción.");
    }

    private AuditAlert createSpecialAccountAlert(AccountingEntry entry, String accountCode) {
        String title = "Cuenta Especial Detectada";
        String description = String.format(
                "El asiento '%s' usa la cuenta especial %s que requiere documentación adicional.",
                entry.getDescription(),
                accountCode);

        return new AuditAlert(
                AuditAlert.Type.INVALID_ACCOUNT,
                AuditAlert.Severity.INFO,
                title,
                description,
                entry.getId().toString(),
                "Asegurar que existe la documentación de respaldo apropiada.");
    }

    private AuditAlert createNegativeBalanceWarning(AccountingEntry entry, String accountCode) {
        String title = "Posible Saldo Negativo";
        String description = String.format(
                "El asiento '%s' podría generar un saldo negativo en la cuenta %s, lo cual no es permitido.",
                entry.getDescription(),
                accountCode);

        return new AuditAlert(
                AuditAlert.Type.INVALID_ACCOUNT,
                AuditAlert.Severity.WARNING,
                title,
                description,
                entry.getId().toString(),
                "Verificar el saldo de la cuenta antes de registrar el asiento.");
    }
}
