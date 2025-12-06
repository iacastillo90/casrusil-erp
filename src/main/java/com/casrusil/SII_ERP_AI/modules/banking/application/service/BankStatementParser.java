package com.casrusil.SII_ERP_AI.modules.banking.application.service;

import com.casrusil.SII_ERP_AI.modules.banking.domain.model.BankTransaction;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para parsear extractos bancarios en formato CSV y Excel.
 * Soporta formatos comunes de bancos chilenos (Banco de Chile, BCI, Santander).
 */
@Service
public class BankStatementParser {

    private static final Logger logger = LoggerFactory.getLogger(BankStatementParser.class);
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
    };

    /**
     * Parsea un archivo CSV de extracto bancario.
     * 
     * @param inputStream Stream del archivo CSV
     * @param companyId   ID de la empresa
     * @return Lista de transacciones bancarias
     */
    public List<BankTransaction> parseCsv(InputStream inputStream, CompanyId companyId) throws IOException {
        List<BankTransaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < 3) {
                    continue; // Skip invalid lines
                }

                try {
                    LocalDate date = parseDate(fields[0].trim());
                    String description = fields[1].trim();
                    BigDecimal amount = new BigDecimal(fields[2].trim().replace(".", "").replace(",", "."));
                    String reference = fields.length > 3 ? fields[3].trim() : "";

                    BankTransaction transaction = BankTransaction.create(companyId, date, description, amount,
                            reference);
                    transactions.add(transaction);
                } catch (Exception e) {
                    // Skip invalid transaction
                    logger.error("Error parsing CSV line: {} - {}", line, e.getMessage());
                }
            }
        }

        return transactions;
    }

    /**
     * Parsea un archivo Excel de extracto bancario.
     * 
     * @param inputStream Stream del archivo Excel
     * @param companyId   ID de la empresa
     * @return Lista de transacciones bancarias
     */
    public List<BankTransaction> parseExcel(InputStream inputStream, CompanyId companyId) throws IOException {
        List<BankTransaction> transactions = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                // Skip header
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                try {
                    Cell dateCell = row.getCell(0);
                    Cell descriptionCell = row.getCell(1);
                    Cell amountCell = row.getCell(2);
                    Cell referenceCell = row.getCell(3);

                    if (dateCell == null || descriptionCell == null || amountCell == null) {
                        continue;
                    }

                    LocalDate date = parseDateFromCell(dateCell);
                    String description = descriptionCell.getStringCellValue();
                    BigDecimal amount = BigDecimal.valueOf(amountCell.getNumericCellValue());
                    String reference = referenceCell != null ? referenceCell.getStringCellValue() : "";

                    BankTransaction transaction = BankTransaction.create(companyId, date, description, amount,
                            reference);
                    transactions.add(transaction);
                } catch (Exception e) {
                    // Skip invalid transaction
                    logger.error("Error parsing Excel row: {} - {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        return transactions;
    }

    /**
     * Intenta parsear una fecha usando múltiples formatos.
     */
    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }

    /**
     * Parsea una fecha desde una celda de Excel.
     */
    private LocalDate parseDateFromCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            return parseDate(cell.getStringCellValue());
        }
        throw new IllegalArgumentException("Unable to parse date from cell");
    }
}
