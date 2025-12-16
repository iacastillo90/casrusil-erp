package com.casrusil.siierpai.modules.banking.application.service;

import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     */
    public List<BankTransaction> parseCsv(InputStream inputStream, CompanyId companyId) throws IOException {
        List<BankTransaction> transactions = new ArrayList<>();
        // Implementación básica para CSV si se requiere en el futuro
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            // ... lógica CSV existente o vacía por ahora ...
        }
        return transactions;
    }

    /**
     * Parsea un archivo Excel con detección inteligente y robusta de cabeceras.
     */
    public List<BankTransaction> parseExcel(InputStream inputStream, CompanyId companyId) throws IOException {
        List<BankTransaction> transactions = new ArrayList<>();

        try (InputStream bufferedStream = new BufferedInputStream(inputStream);
                Workbook workbook = WorkbookFactory.create(bufferedStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 1. Detectar Cabecera REAL (ignorando metadatos como "Fecha Desde")
            Map<String, Integer> colMap = findHeaderRow(sheet);
            if (colMap.isEmpty() || !colMap.containsKey("_ROW_INDEX")) {
                throw new IllegalArgumentException(
                        "No se encontró una cabecera válida. Buscando fila con 'Fecha' Y ('Descripción' o 'Cargo' o 'Monto').");
            }

            int startRow = colMap.get("_ROW_INDEX") + 1;

            // Usamos Integer para validar nulos antes de usar
            Integer dateIdx = colMap.get("FECHA");
            Integer descIdx = colMap.get("DESCRIPCION");
            Integer cargoIdx = colMap.get("CARGO");
            Integer abonoIdx = colMap.get("ABONO");
            Integer amountIdx = colMap.get("MONTO");
            Integer refIdx = colMap.getOrDefault("REFERENCIA", -1);

            // Validación estricta de columnas mínimas
            if (dateIdx == null)
                throw new IllegalArgumentException("Columna 'Fecha' no encontrada en la cabecera detectada.");
            if (descIdx == null && amountIdx == null && cargoIdx == null) {
                throw new IllegalArgumentException(
                        "Se detectó cabecera pero faltan columnas críticas (Descripción o Montos).");
            }
            if (descIdx == null)
                descIdx = -1; // Fallback si no hay descripción

            logger.info("Cabecera válida en fila {}. Mapping: Fecha={}, Desc={}, Cargo={}, Abono={}",
                    startRow - 1, dateIdx, descIdx, cargoIdx, abonoIdx);

            // 2. Leer Datos
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                try {
                    Cell dateCell = row.getCell(dateIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (dateCell == null)
                        continue; // Fin de datos

                    LocalDate date = parseDateFromCell(dateCell);
                    String description = (descIdx != -1) ? getCellValueAsString(row.getCell(descIdx))
                            : "Sin descripción";
                    String reference = (refIdx != -1) ? getCellValueAsString(row.getCell(refIdx)) : "";

                    BigDecimal amount = BigDecimal.ZERO;

                    // Lógica para calcular monto
                    if (cargoIdx != null && abonoIdx != null) {
                        BigDecimal cargo = getNumericValue(row.getCell(cargoIdx));
                        BigDecimal abono = getNumericValue(row.getCell(abonoIdx));

                        // Heurística: Cargo suele ser negativo o débito.
                        // Si viene negativo (tu caso), sumamos algebraicamente.
                        if (cargo.compareTo(BigDecimal.ZERO) > 0) {
                            amount = abono.subtract(cargo);
                        } else {
                            amount = abono.add(cargo);
                        }
                    } else if (amountIdx != null) {
                        amount = getNumericValue(row.getCell(amountIdx));
                    }

                    // Ignorar fila si no hay monto ni descripción relevante
                    if (amount.compareTo(BigDecimal.ZERO) == 0 && description.equals("Sin descripción"))
                        continue;

                    transactions.add(BankTransaction.create(companyId, date, description, amount, reference));

                } catch (Exception e) {
                    // Log debug para no ensuciar consola con filas vacías al final
                    logger.debug("Saltando fila {} (posiblemente fin de archivo o formato inválido): {}", i,
                            e.getMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            throw e; // Re-lanzar errores de validación para el usuario
        } catch (Exception e) {
            logger.error("Error crítico procesando Excel", e);
            throw new IOException("Error procesando archivo Excel: " + e.getMessage(), e);
        }

        return transactions;
    }

    /**
     * Busca la fila de cabecera que cumpla CRITERIOS ESTRICTOS.
     * Debe tener "FECHA" Y al menos otra columna clave.
     */
    private Map<String, Integer> findHeaderRow(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();

        // Escanear primeras 30 filas
        for (Row row : sheet) {
            if (row.getRowNum() > 30)
                break;

            Map<String, Integer> map = new HashMap<>();
            boolean hasDate = false;
            boolean hasOtherKeyCol = false;

            for (Cell cell : row) {
                String text = formatter.formatCellValue(cell).trim().toUpperCase();
                int idx = cell.getColumnIndex();

                if (text.isEmpty())
                    continue;

                if (text.equals("FECHA") || text.equals("DATE") || text.startsWith("FECHA ")) {
                    // Cuidado con "Fecha Desde" -> Solo aceptamos si luego encontramos otras
                    // columnas
                    if (text.equals("FECHA") || text.equals("DATE")) {
                        map.put("FECHA", idx);
                        hasDate = true;
                    }
                    // Si dice "Fecha contable" o similar, lo mapeamos tentativo
                    else if (text.contains("FECHA")) {
                        // Solo si es corta, para evitar textos largos
                        if (text.length() < 20) {
                            map.put("FECHA", idx);
                            hasDate = true;
                        }
                    }
                }

                else if (text.contains("DESCRIPCI") || text.contains("MOVIMIENTO") || text.contains("DETALLE")) {
                    map.put("DESCRIPCION", idx);
                    hasOtherKeyCol = true;
                } else if (text.equals("CARGO") || text.contains("GIRO") || text.contains("DEBIT")) {
                    map.put("CARGO", idx);
                    hasOtherKeyCol = true;
                } else if (text.equals("ABONO") || text.contains("DEPOSITO") || text.contains("CREDIT")) {
                    map.put("ABONO", idx);
                    hasOtherKeyCol = true;
                } else if (text.equals("MONTO") || text.equals("AMOUNT") || text.equals("SALDO")) {
                    if (!text.contains("SALDO")) {
                        map.put("MONTO", idx);
                        hasOtherKeyCol = true;
                    }
                } else if (text.contains("DOC") || text.contains("REF") || text.contains("NUMERO")) {
                    map.put("REFERENCIA", idx);
                }
            }

            // CRITERIO DE ACEPTACIÓN: Tiene Fecha Y (Descripción O Montos)
            // Esto descarta la fila "Fecha Desde" que solo tiene fechas pero no
            // montos/descripción
            if (hasDate && hasOtherKeyCol) {
                map.put("_ROW_INDEX", row.getRowNum());
                return map;
            }
        }
        return new HashMap<>();
    }

    private BigDecimal getNumericValue(Cell cell) {
        if (cell == null)
            return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty())
                return BigDecimal.ZERO;
            val = val.replace(".", "").replace(",", ".");
            try {
                return new BigDecimal(val);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private LocalDate parseDateFromCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            return parseDate(cell.getStringCellValue());
        }
        throw new IllegalArgumentException("Fecha inválida");
    }

    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
            }
        }
        throw new IllegalArgumentException("Formato fecha desconocido: " + dateStr);
    }
}
