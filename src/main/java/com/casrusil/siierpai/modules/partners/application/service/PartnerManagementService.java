package com.casrusil.siierpai.modules.partners.application.service;

import com.casrusil.siierpai.modules.partners.domain.model.Partner;
import com.casrusil.siierpai.modules.partners.domain.model.PartnerType;
import com.casrusil.siierpai.modules.partners.domain.port.out.PartnerRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnerManagementService {

    private final PartnerRepository partnerRepository;

    public PartnerManagementService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    /**
     * Registers or updates a partner based on transaction data.
     * Uses propagation REQUIRES_NEW to ensure partner creation survives even if the
     * calling transaction rolls back (optional, but good for logs),
     * or standard REQUIRED. Let's use REQUIRED to be part of the atomic import
     * transaction.
     */
    @Transactional
    public void registerPartner(CompanyId companyId, String rut, String name, PartnerType type) {
        if (rut == null || rut.trim().isEmpty())
            return;

        Partner partner = partnerRepository.findByCompanyIdAndRut(companyId, rut)
                .orElseGet(() -> Partner.create(companyId, rut, name));

        // Update name if we have a better one now and didn't before
        if (name != null && !name.trim().isEmpty() && !name.equals("Unknown") && !name.equals("Sin Nombre")) {
            // Logic: Always update name? Or only if current is empty?
            // "Updated info is usually better info".
            partner.updateName(name);
        }

        partner.addType(type);
        partnerRepository.save(partner);
    }
}
