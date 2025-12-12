package com.casrusil.siierpai.modules.accounting.infrastructure.seed;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seed data for Chilean Chart of Accounts (Plan de Cuentas Chileno)
 * Based on standard accounting practices in Chile
 */
@Configuration
public class ChileanChartOfAccountsSeeder {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
                        .getLogger(ChileanChartOfAccountsSeeder.class);

        @Bean
        public CommandLineRunner seedChileanAccounts(AccountRepository accountRepository,
                        com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository companyRepository) {
                return args -> {
                        log.info("Seeding Chilean Chart of Accounts for all companies...");

                        companyRepository.findAll().forEach(company -> {
                                try {
                                        seedAccountsForCompany(company.getId(), accountRepository);
                                } catch (Exception e) {
                                        log.error("Failed to seed accounts for company: " + company.getId(), e);
                                }
                        });

                        log.info("Seeding completed.");
                };
        }

        /**
         * Creates standard Chilean accounts for a given company
         */
        public static void seedAccountsForCompany(CompanyId companyId, AccountRepository accountRepository) {
                // ACTIVOS (Assets)
                createAccount(accountRepository, companyId, "1", "ACTIVO", AccountType.ASSET, "Activos totales");
                createAccount(accountRepository, companyId, "11", "ACTIVO CIRCULANTE", AccountType.ASSET,
                                "Activos corrientes");
                createAccount(accountRepository, companyId, "1101", "Caja", AccountType.ASSET, "Efectivo en caja");
                createAccount(accountRepository, companyId, "110101", "Caja Moneda Nacional", AccountType.ASSET,
                                "Efectivo en pesos chilenos");
                createAccount(accountRepository, companyId, "1102", "Banco", AccountType.ASSET, "Cuentas bancarias");
                createAccount(accountRepository, companyId, "110201", "Banco Cuenta Corriente", AccountType.ASSET,
                                "Cuenta corriente bancaria");
                createAccount(accountRepository, companyId, "1105", "Clientes", AccountType.ASSET,
                                "Cuentas por cobrar a clientes");
                createAccount(accountRepository, companyId, "110501", "Clientes Nacionales", AccountType.ASSET,
                                "Cuentas por cobrar clientes nacionales");
                createAccount(accountRepository, companyId, "1108", "IVA Crédito Fiscal", AccountType.ASSET,
                                "IVA por recuperar");
                createAccount(accountRepository, companyId, "110801", "IVA Crédito Fiscal del Mes", AccountType.ASSET,
                                "IVA crédito fiscal del período");

                // PASIVOS (Liabilities)
                createAccount(accountRepository, companyId, "2", "PASIVO", AccountType.LIABILITY, "Pasivos totales");
                createAccount(accountRepository, companyId, "21", "PASIVO CIRCULANTE", AccountType.LIABILITY,
                                "Pasivos corrientes");
                createAccount(accountRepository, companyId, "2101", "Proveedores", AccountType.LIABILITY,
                                "Cuentas por pagar a proveedores");
                createAccount(accountRepository, companyId, "210101", "Proveedores Nacionales", AccountType.LIABILITY,
                                "Cuentas por pagar proveedores nacionales");
                createAccount(accountRepository, companyId, "2104", "IVA Débito Fiscal", AccountType.LIABILITY,
                                "IVA por pagar");
                createAccount(accountRepository, companyId, "210401", "IVA Débito Fiscal del Mes",
                                AccountType.LIABILITY,
                                "IVA débito fiscal del período");
                createAccount(accountRepository, companyId, "2105", "Retenciones", AccountType.LIABILITY,
                                "Retenciones de impuestos");
                createAccount(accountRepository, companyId, "210501", "Retenciones de Segunda Categoría",
                                AccountType.LIABILITY,
                                "Retenciones de trabajadores dependientes");
                createAccount(accountRepository, companyId, "2106", "Provisiones", AccountType.LIABILITY,
                                "Provisiones diversas");
                createAccount(accountRepository, companyId, "210601", "Provisión Vacaciones", AccountType.LIABILITY,
                                "Provisión para vacaciones del personal");

                // PATRIMONIO (Equity)
                createAccount(accountRepository, companyId, "3", "PATRIMONIO", AccountType.EQUITY, "Patrimonio total");
                createAccount(accountRepository, companyId, "31", "CAPITAL", AccountType.EQUITY, "Capital social");
                createAccount(accountRepository, companyId, "3101", "Capital", AccountType.EQUITY,
                                "Capital aportado por socios");
                createAccount(accountRepository, companyId, "310101", "Capital Pagado", AccountType.EQUITY,
                                "Capital efectivamente pagado");
                createAccount(accountRepository, companyId, "33", "RESULTADOS", AccountType.EQUITY,
                                "Resultados acumulados");
                createAccount(accountRepository, companyId, "3301", "Utilidades Acumuladas", AccountType.EQUITY,
                                "Utilidades de ejercicios anteriores");
                createAccount(accountRepository, companyId, "3302", "Utilidad del Ejercicio", AccountType.EQUITY,
                                "Resultado del período actual");

                // INGRESOS (Revenue)
                createAccount(accountRepository, companyId, "4", "INGRESOS", AccountType.REVENUE, "Ingresos totales");
                createAccount(accountRepository, companyId, "41", "INGRESOS OPERACIONALES", AccountType.REVENUE,
                                "Ingresos de la actividad principal");
                createAccount(accountRepository, companyId, "4101", "Ventas", AccountType.REVENUE,
                                "Ventas de bienes y servicios");
                createAccount(accountRepository, companyId, "410101", "Ventas Nacionales", AccountType.REVENUE,
                                "Ventas en el mercado nacional");
                createAccount(accountRepository, companyId, "410102", "Ventas Exentas", AccountType.REVENUE,
                                "Ventas exentas de IVA");
                createAccount(accountRepository, companyId, "42", "INGRESOS NO OPERACIONALES", AccountType.REVENUE,
                                "Otros ingresos");
                createAccount(accountRepository, companyId, "4201", "Otros Ingresos", AccountType.REVENUE,
                                "Ingresos diversos");

                // EGRESOS/COSTOS (Expenses)
                createAccount(accountRepository, companyId, "5", "COSTOS Y GASTOS", AccountType.EXPENSE,
                                "Costos y gastos totales");
                createAccount(accountRepository, companyId, "51", "COSTO DE VENTAS", AccountType.EXPENSE,
                                "Costo de los productos vendidos");
                createAccount(accountRepository, companyId, "5101", "Costo de Ventas", AccountType.EXPENSE,
                                "Costo directo de ventas");
                createAccount(accountRepository, companyId, "510101", "Costo de Ventas General", AccountType.EXPENSE,
                                "Costo de ventas general");
                createAccount(accountRepository, companyId, "52", "GASTOS DE ADMINISTRACIÓN", AccountType.EXPENSE,
                                "Gastos administrativos");
                createAccount(accountRepository, companyId, "5201", "Remuneraciones", AccountType.EXPENSE,
                                "Sueldos y salarios");
                createAccount(accountRepository, companyId, "520101", "Sueldos", AccountType.EXPENSE,
                                "Sueldos del personal");
                createAccount(accountRepository, companyId, "5202", "Honorarios", AccountType.EXPENSE,
                                "Honorarios profesionales");
                createAccount(accountRepository, companyId, "5203", "Arriendos", AccountType.EXPENSE,
                                "Gastos de arriendo");
                createAccount(accountRepository, companyId, "5204", "Servicios Básicos", AccountType.EXPENSE,
                                "Luz, agua, gas, etc.");
                createAccount(accountRepository, companyId, "520401", "Electricidad", AccountType.EXPENSE,
                                "Consumo eléctrico");
                createAccount(accountRepository, companyId, "520402", "Agua", AccountType.EXPENSE, "Consumo de agua");
                createAccount(accountRepository, companyId, "520403", "Teléfono e Internet", AccountType.EXPENSE,
                                "Servicios de comunicación");
                createAccount(accountRepository, companyId, "53", "GASTOS DE VENTAS", AccountType.EXPENSE,
                                "Gastos relacionados con ventas");
                createAccount(accountRepository, companyId, "5301", "Publicidad", AccountType.EXPENSE,
                                "Gastos de marketing y publicidad");
                createAccount(accountRepository, companyId, "5302", "Comisiones de Venta", AccountType.EXPENSE,
                                "Comisiones a vendedores");

                log.info("Chilean Chart of Accounts seeded successfully for company: {}", companyId.value());
        }

        private static void createAccount(AccountRepository repository, CompanyId companyId,
                        String code, String name, AccountType type, String description) {
                // Check if account already exists
                if (repository.findByCode(companyId, code).isEmpty()) {
                        Account account = new Account(companyId, code, name, type, description);
                        repository.save(account);
                }
        }
}
