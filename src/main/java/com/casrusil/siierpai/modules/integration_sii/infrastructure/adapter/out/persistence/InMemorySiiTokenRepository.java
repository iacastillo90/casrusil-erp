package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of SiiTokenRepository.
 * Thread-safe for multi-tenant access.
 * 
 * Note: Tokens are stored in memory and will be lost on application restart.
 * For production, consider persisting to database or distributed cache.
 */
@Repository
public class InMemorySiiTokenRepository implements SiiTokenRepository {

    private final Map<CompanyId, SiiToken> tokenStore = new ConcurrentHashMap<>();

    @Override
    public void save(CompanyId companyId, SiiToken token) {
        tokenStore.put(companyId, token);
    }

    @Override
    public Optional<SiiToken> findByCompanyId(CompanyId companyId) {
        return Optional.ofNullable(tokenStore.get(companyId));
    }

    @Override
    public Map<CompanyId, SiiToken> findAll() {
        return Map.copyOf(tokenStore);
    }

    @Override
    public void delete(CompanyId companyId) {
        tokenStore.remove(companyId);
    }
}
