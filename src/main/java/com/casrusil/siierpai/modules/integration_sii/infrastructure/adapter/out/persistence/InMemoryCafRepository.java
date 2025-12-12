package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCafRepository implements CafRepository {

    private final Map<CompanyId, List<Caf>> store = new ConcurrentHashMap<>();

    @Override
    public void save(CompanyId companyId, Caf caf) {
        store.computeIfAbsent(companyId, k -> Collections.synchronizedList(new ArrayList<>())).add(caf);
    }

    @Override
    public Optional<Caf> findActiveForFolio(CompanyId companyId, String tipoDte, Long folio) {
        List<Caf> cafs = store.get(companyId);
        if (cafs == null) {
            return Optional.empty();
        }
        synchronized (cafs) {
            return cafs.stream()
                    .filter(c -> c.tipoDte().equals(tipoDte))
                    .filter(c -> c.containsFolio(folio))
                    .findFirst();
        }
    }
}
