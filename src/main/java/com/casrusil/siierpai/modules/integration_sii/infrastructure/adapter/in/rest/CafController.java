package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.parser.CafParser;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Controlador REST para la gestión de CAF (Código de Autorización de Folios).
 * 
 * <p>
 * Permite la carga de archivos XML de folios autorizados por el SII.
 * Estos archivos son necesarios para emitir facturas electrónicas válidas.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/sii/caf}: Cargar archivo CAF XML.</li>
 * </ul>
 * 
 * @see CafParser
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/sii/caf")
public class CafController {

    private final CafParser cafParser;
    private final CafRepository cafRepository;

    public CafController(CafParser cafParser, CafRepository cafRepository) {
        this.cafParser = cafParser;
        this.cafRepository = cafRepository;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> uploadCaf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") UUID companyId) {

        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.ISO_8859_1); // SII uses ISO-8859-1
            Caf caf = cafParser.parse(xmlContent);

            cafRepository.save(new CompanyId(companyId), caf);

            return ResponseEntity
                    .ok("CAF uploaded successfully. Range: " + caf.rangoDesde() + " - " + caf.rangoHasta());

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing CAF: " + e.getMessage());
        }
    }
}
