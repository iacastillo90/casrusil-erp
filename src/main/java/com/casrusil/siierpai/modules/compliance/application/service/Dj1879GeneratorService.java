package com.casrusil.siierpai.modules.compliance.application.service;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class Dj1879GeneratorService {

    private final FeeReceiptRepository feeReceiptRepository;
    private final CompanyRepository companyRepository;

    public Dj1879GeneratorService(FeeReceiptRepository feeReceiptRepository, CompanyRepository companyRepository) {
        this.feeReceiptRepository = feeReceiptRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Genera el archivo plano para la DJ 1879.
     * formato simplificado según estructura SII (Header, Detalle, Resumen).
     */
    public byte[] generateDj1879(CompanyId companyId, int year) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        List<FeeReceipt> receipts = feeReceiptRepository.findByCompanyIdAndYear(companyId, year);

        StringBuilder sb = new StringBuilder();

        // --- SECTION A: HEADER ---
        // Tipo Registro (01) + RUT Declarante + Año + ...
        // Ejemplo simplificado: "01" + RUT_FULL + AÑO
        sb.append("A").append(";");
        sb.append(year).append(";");
        sb.append(company.getRut()).append(";");
        sb.append(company.getRazonSocial()).append(";");
        sb.append("DJ1879").append("\r\n");

        // --- SECTION B: DETAILS (Agrupado por Receptor de Renta / Prestador) ---
        Map<String, List<FeeReceipt>> byIssuer = receipts.stream()
                .collect(Collectors.groupingBy(FeeReceipt::getIssuerRut));

        // Factores de Actualización (IPC). Simplificación: 1.0 para todos los meses.
        // En prod real, esto debe venir de una tabla de indicadores económicos.
        BigDecimal[] ipcFactors = new BigDecimal[12];
        for (int i = 0; i < 12; i++)
            ipcFactors[i] = BigDecimal.ONE;

        long totalRetencionActualizada = 0;
        long totalBrutoActualizado = 0;

        for (Map.Entry<String, List<FeeReceipt>> entry : byIssuer.entrySet()) {
            String rutPrestador = entry.getKey();
            List<FeeReceipt> prestadorReceipts = entry.getValue();

            BigDecimal totalBruto = BigDecimal.ZERO; // Nominal
            BigDecimal totalRetencion = BigDecimal.ZERO; // Nominal

            // Calculo Anual
            for (FeeReceipt f : prestadorReceipts) {
                totalBruto = totalBruto.add(f.getGrossAmount());
                totalRetencion = totalRetencion.add(f.getRetentionAmount());
            }

            // Registro Detalle (Tipo 1)
            // RUT Prestador ; Bruto ; Retencion
            sb.append("B").append(";");
            sb.append(rutPrestador).append(";");
            sb.append(totalBruto.longValue()).append(";");
            sb.append(totalRetencion.longValue()).append("\r\n");

            totalBrutoActualizado += totalBruto.longValue();
            totalRetencionActualizada += totalRetencion.longValue();
        }

        // --- SECTION C: SUMMARY ---
        // Totales de control
        sb.append("C").append(";");
        sb.append(byIssuer.size()).append(";"); // Cantidad informados
        sb.append(totalBrutoActualizado).append(";");
        sb.append(totalRetencionActualizada).append("\r\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8); // SII suele usar ISO-8859-1, pero UTF-8 es más seguro
                                                               // internamente. Convertir al servir.
    }
}
