package com.casrusil.siierpai.modules.accounting.infrastructure.parser;

import com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BalanceSheetParser {

    // Chilean locale for number parsing (1.000.000,00)
    private static final Locale CHILE = new Locale("es", "CL");

    public List<OpeningBalanceService.OpeningBalanceItem> parseTsv(InputStream inputStream) throws IOException {
        List<OpeningBalanceService.OpeningBalanceItem> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean headersReached = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Check for headers line to start processing after it
                // Headers might look like: "CUENTA" "DESCRIPCIÓN" "DEUDOR" "ACREEDOR"
                if (!headersReached) {
                    if (line.toUpperCase().contains("CUENTA") && line.toUpperCase().contains("DEUDOR")) {
                        headersReached = true;
                    }
                    continue;
                }

                // Parse data lines
                // Format: AccountCode \t Description \t Debit \t Credit
                String[] parts = line.split("\t");
                if (parts.length >= 4) {
                    try {
                        String accountCode = parts[0].trim();
                        String description = parts[1].trim();

                        // Parse Debit (Idx 2) and Credit (Idx 3)
                        BigDecimal debit = parseAmount(parts[2]);
                        BigDecimal credit = parseAmount(parts[3]);

                        // Skip lines with zero movement or unrelated totals
                        if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
                            continue;
                        }

                        // Basic validation: must have account code
                        if (accountCode.isEmpty() || !accountCode.matches("^[0-9.]+$")) {
                            continue;
                        }

                        items.add(
                                new OpeningBalanceService.OpeningBalanceItem(accountCode, description, debit, credit));

                    } catch (Exception e) {
                        // Log parsing error but continue
                        org.slf4j.LoggerFactory.getLogger(BalanceSheetParser.class).error("Skipping malformed line: {}",
                                line, e);
                    }
                }
            }
        }

        return items;
    }

    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // Remove dots and replace comma with dot for standard BigDecimal parsing ?
            // Or use NumberFormat.
            // Example: "2.636.114" -> 2636114
            String cleanAmount = amountStr.trim().replace(".", "").replace(",", ".");
            return new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
