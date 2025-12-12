package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.service.CompanyManagementService;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión del perfil de empresa.
 * 
 * <p>
 * Permite a los administradores actualizar la información de su propia empresa.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code PUT /api/v1/companies/me}: Actualizar perfil de la empresa
 * actual.</li>
 * </ul>
 * 
 * @see CompanyManagementService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyManagementService companyService;

    public CompanyController(CompanyManagementService companyService) {
        this.companyService = companyService;
    }

    @PutMapping("/me")
    public ResponseEntity<Company> updateProfile(@RequestBody UpdateCompanyRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        Company updatedCompany = companyService.updateCompany(companyId, request.razonSocial(), request.email());
        return ResponseEntity.ok(updatedCompany);
    }

    public record UpdateCompanyRequest(String razonSocial, String email) {
    }
}
