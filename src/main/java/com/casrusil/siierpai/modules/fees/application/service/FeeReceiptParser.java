package com.casrusil.siierpai.modules.fees.application.service;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeeReceiptParser {

    private static final Logger logger = LoggerFactory.getLogger(FeeReceiptParser.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Parsea un archivo CSV de Boletas de Honorarios Recibidas.
     * Soporta diferentes formatos de encabezado típicos del SII.
     */
    public List<FeeReceipt> parse(InputStream inputStream, CompanyId companyId) throws Exception {
        // Configuramos formato flexible
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        List<FeeReceipt> feeReceipts = new ArrayList<>();

        // SII suele usar ISO-8859-1
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                CSVParser csvParser = new CSVParser(reader, format)) {

            for (CSVRecord record : csvParser) {
                try {
                    // Validar si es una línea válida (a veces hay totales al final)
                    if (!record.isConsistent() || record.size() < 5) {
                        continue;
                    }

                    // Intentamos mapear columnas por nombres comunes
                    // Folio
                    Long folio = parseLong(getWithFallback(record, "N° Boleta", "Folio", "N°"));

                    // Estado (solo procesamos Vigentes)
                    String estado = getWithFallback(record, "Estado", "Est.");
                    if (estado != null && estado.contains("Anul")) {
                        // Podríamos importarlas marcadas como nulas
                    }
                    boolean isNullified = estado != null && estado.toUpperCase().contains("ANUL");

                    // Rut Emisor (Prestador)
                    String issuerRut = getWithFallback(record, "Rut Emisor", "Rut Prestador", "Rut");
                    String issuerName = getWithFallback(record, "Razon Social", "Nombre Prestador", "Nombre");

                    // Fecha
                    String dateStr = getWithFallback(record, "Fecha Boleta", "Fecha Docto", "Fecha");
                    LocalDate date = parseDate(dateStr);

                    // Montos
                    BigDecimal grossAmount = parseAmount(
                            getWithFallback(record, "Monto Bruto", "Honorario Bruto", "Bruto"));
                    BigDecimal retentionAmount = parseAmount(
                            getWithFallback(record, "Retencion", "Ret. Impto.", "Retención"));
                    BigDecimal netAmount = parseAmount(
                            getWithFallback(record, "Monto Liquido", "Honorario Liquido", "Liquido"));

                    // Como son RECIBIDAS, el receptor es mi empresa (companyId)
                    // Pero necesitamos el RUT de mi empresa.
                    // Como el parser solo recibe CompanyId, asumimos que el servicio orquestador
                    // pondrá el RUT correcto si se necesitara persistir string.
                    // En el modelo FeeReceipt guardamos issuerRut (prestador) y receiverRut
                    // (nosotros).
                    // Para receiverRut, usaremos "SELF" o lo dejaremos pendiente, pero el Entity
                    // tiene receiverRut.
                    // Mejor pasar companyRut al parser o dejarlo vacio y llenarlo despues.
                    // Simplificaremos usando el ID.

                    FeeReceipt receipt = FeeReceipt.create(
                            companyId,
                            folio,
                            issuerRut,
                            "SELF",
                            issuerName,
                            date,
                            grossAmount,
                            retentionAmount,
                            netAmount);

                    if (isNullified) {
                        // Re-crear con Status NULLIFIED
                        receipt = new FeeReceipt(receipt.getId(), receipt.getCompanyId(), receipt.getFolio(),
                                receipt.getIssuerRut(), receipt.getReceiverRut(), receipt.getIssuerName(),
                                receipt.getIssueDate(), receipt.getGrossAmount(), receipt.getRetentionAmount(),
                                receipt.getNetAmount(), FeeReceipt.Status.NULLIFIED);
                    }

                    feeReceipts.add(receipt);

                } catch (Exception e) {
                    logger.warn("Skipping row {}: {}", record.getRecordNumber(), e.getMessage());
                }
            }
        }
        return feeReceipts;
    }

    private String getWithFallback(CSVRecord record, String... headers) {
        for (String header : headers) {
            try {
                if (record.isMapped(header)) {
                    return record.get(header);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        // Fallback: try default headers if map fails or scan headers
        // Just return null if not found
        return null;
    }

    private Long parseLong(String value) {
        if (value == null)
            return 0L;
        return Long.parseLong(value.trim());
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.trim().isEmpty())
            return BigDecimal.ZERO;
        return new BigDecimal(value.trim().replace(".", "").replace(",", "."));
    }

    private LocalDate parseDate(String value) {
        if (value == null)
            return LocalDate.now();
        // A veces viene "dd/MM/yyyy HH:mm:ss"
        String cleanValue = value.split(" ")[0];
        return LocalDate.parse(cleanValue, DATE_FORMATTER);
    }
}
