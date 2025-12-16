package com.casrusil.siierpai.modules.partners.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.partners.domain.model.Partner;
import com.casrusil.siierpai.modules.partners.domain.model.PartnerType;
import com.casrusil.siierpai.modules.partners.domain.port.out.PartnerRepository;
import com.casrusil.siierpai.modules.partners.infrastructure.persistence.entity.PartnerEntity;
import com.casrusil.siierpai.modules.partners.infrastructure.persistence.repository.PartnerJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PartnerJpaAdapter implements PartnerRepository {

    private final PartnerJpaRepository jpaRepository;

    public PartnerJpaAdapter(PartnerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Partner save(Partner partner) {
        PartnerEntity entity = toEntity(partner);
        PartnerEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Partner> findByCompanyIdAndRut(CompanyId companyId, String rut) {
        return jpaRepository.findByCompanyIdAndRut(companyId.value(), rut)
                .map(this::toDomain);
    }

    private PartnerEntity toEntity(Partner partner) {
        Set<String> types = partner.getTypes().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new PartnerEntity(
                partner.getId(),
                partner.getCompanyId().value(),
                partner.getRut(),
                partner.getName(),
                types);
    }

    private Partner toDomain(PartnerEntity entity) {
        Set<PartnerType> types = entity.getTypes().stream()
                .map(PartnerType::valueOf)
                .collect(Collectors.toSet());
        return new Partner(
                entity.getId(),
                new CompanyId(entity.getCompanyId()),
                entity.getRut(),
                entity.getName(),
                types);
    }
}
