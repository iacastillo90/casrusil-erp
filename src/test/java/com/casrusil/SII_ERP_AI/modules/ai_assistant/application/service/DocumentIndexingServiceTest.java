package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.service;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIndexingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    private DocumentIndexingService documentIndexingService;

    @BeforeEach
    void setUp() {
        documentIndexingService = new DocumentIndexingService(embeddingModel, embeddingStore);
    }

    @Test
    void shouldIndexInvoiceSuccessfully() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = createTestInvoice(companyId);
        Embedding embedding = new Embedding(new float[] { 0.1f, 0.2f });

        when(embeddingModel.embed(any(TextSegment.class)))
                .thenReturn(dev.langchain4j.model.output.Response.from(embedding));

        // When
        documentIndexingService.indexInvoice(invoice);

        // Then
        ArgumentCaptor<TextSegment> segmentCaptor = ArgumentCaptor.forClass(TextSegment.class);
        verify(embeddingStore).add(eq(embedding), segmentCaptor.capture());

        TextSegment capturedSegment = segmentCaptor.getValue();
        assertEquals(companyId.value().toString(), capturedSegment.metadata().getString("companyId"));
        assertEquals("invoice", capturedSegment.metadata().getString("type"));
    }

    @Test
    void shouldSearchAndFilterByCompany() {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        String query = "factura";
        Embedding queryEmbedding = new Embedding(new float[] { 0.1f, 0.2f });

        when(embeddingModel.embed(query)).thenReturn(dev.langchain4j.model.output.Response.from(queryEmbedding));

        // Mock search results
        TextSegment segment1 = TextSegment.from("Result 1",
                new dev.langchain4j.data.document.Metadata().add("companyId", companyId.value().toString()));
        TextSegment segment2 = TextSegment.from("Result 2",
                new dev.langchain4j.data.document.Metadata().add("companyId", "other-company-id"));

        EmbeddingMatch<TextSegment> match1 = new EmbeddingMatch<>(0.9, "id1", new Embedding(new float[] { 0.1f }),
                segment1);
        EmbeddingMatch<TextSegment> match2 = new EmbeddingMatch<>(0.8, "id2", new Embedding(new float[] { 0.1f }),
                segment2);

        when(embeddingStore.findRelevant(queryEmbedding, 10)).thenReturn(List.of(match1, match2));

        // When
        List<TextSegment> results = documentIndexingService.search(query, companyId, 10);

        // Then
        assertEquals(1, results.size());
        assertEquals("Result 1", results.get(0).text());
    }

    private Invoice createTestInvoice(CompanyId companyId) {
        return Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());
    }
}
