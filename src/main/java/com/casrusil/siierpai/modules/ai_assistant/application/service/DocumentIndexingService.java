package com.casrusil.siierpai.modules.ai_assistant.application.service;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for indexing documents into the embedding store for RAG.
 * Converts domain objects (invoices, accounting entries) into searchable
 * embeddings.
 */
@Service
public class DocumentIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexingService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public DocumentIndexingService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * Index an invoice for RAG retrieval.
     * Creates a text representation and stores its embedding.
     */
    public void indexInvoice(Invoice invoice) {
        try {
            // Create text representation of invoice
            String text = String.format(
                    "Factura #%d del %s. Emisor: %s, Receptor: %s. " +
                            "Tipo: %s. Monto Neto: $%s, IVA: $%s, Total: $%s",
                    invoice.getFolio(),
                    invoice.getDate(),
                    invoice.getIssuerRut(),
                    invoice.getReceiverRut(),
                    invoice.getType().name(),
                    invoice.getNetAmount(),
                    invoice.getTaxAmount(),
                    invoice.getTotalAmount());

            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("companyId", invoice.getCompanyId().value().toString());
            metadata.put("invoiceId", invoice.getId().toString());
            metadata.put("folio", invoice.getFolio());
            metadata.put("type", "invoice");
            metadata.put("date", invoice.getDate().toString());

            // Create text segment
            TextSegment segment = TextSegment.from(text, new Metadata(metadata));

            // Generate embedding
            Embedding embedding = embeddingModel.embed(segment).content();

            // Store in embedding store
            embeddingStore.add(embedding, segment);

            logger.debug("Indexed invoice #{} for company {}",
                    invoice.getFolio(), invoice.getCompanyId());

        } catch (Exception e) {
            logger.error("Failed to index invoice #{}: {}",
                    invoice.getFolio(), e.getMessage(), e);
        }
    }

    /**
     * Search for relevant documents based on a query.
     * Returns text segments that match the query semantically.
     */
    public List<TextSegment> search(String query, CompanyId companyId, int maxResults) {
        try {
            // Generate embedding for query
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // Search in embedding store
            var results = embeddingStore.findRelevant(queryEmbedding, maxResults);

            // Filter by company ID and extract text segments
            return results.stream()
                    .filter(match -> {
                        String storedCompanyId = match.embedded().metadata().getString("companyId");
                        return storedCompanyId != null &&
                                storedCompanyId.equals(companyId.value().toString());
                    })
                    .map(match -> match.embedded())
                    .toList();

        } catch (Exception e) {
            logger.error("Failed to search embeddings: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get count of indexed documents for a company.
     */
    public int getIndexedCount(CompanyId companyId) {
        // Note: InMemoryEmbeddingStore doesn't provide count by metadata
        // This is a limitation of the in-memory implementation
        logger.warn("getIndexedCount not fully supported with in-memory store");
        return 0;
    }
}
