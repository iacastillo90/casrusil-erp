package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.audit;

import com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceAuditService {

    private final Javers javers;

    public InvoiceAuditService(Javers javers) {
        this.javers = javers;
    }

    public List<Change> getInvoiceChanges(UUID invoiceId) {
        return javers.findChanges(QueryBuilder.byInstanceId(invoiceId, InvoiceEntity.class).build());
    }

    public String getInvoiceHistoryPrettyPrint(UUID invoiceId) {
        List<Change> changes = getInvoiceChanges(invoiceId);
        return javers.getJsonConverter().toJson(changes);
    }
}
