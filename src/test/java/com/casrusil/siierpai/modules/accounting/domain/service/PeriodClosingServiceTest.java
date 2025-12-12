package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClosedPeriodRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodClosingServiceTest {

    @Mock
    private ClosedPeriodRepository closedPeriodRepository;

    @Mock
    private AccountingEntryRepository accountingEntryRepository;

    @Mock
    private AccountingEntryService accountingEntryService;

    private PeriodClosingService periodClosingService;

    @BeforeEach
    void setUp() {
        periodClosingService = new PeriodClosingService(
                closedPeriodRepository,
                accountingEntryRepository,
                accountingEntryService);
    }

    @Test
    void shouldClosePeriod() {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        UserId userId = new UserId(UUID.randomUUID());
        YearMonth period = YearMonth.of(2023, 10);

        when(closedPeriodRepository.exists(companyId, period)).thenReturn(false);
        when(accountingEntryRepository.findByCompanyId(companyId)).thenReturn(Collections.emptyList());
        when(closedPeriodRepository.save(any(ClosedPeriod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClosedPeriod result = periodClosingService.closePeriod(companyId, period, userId);

        assertNotNull(result);
        assertEquals(period, result.getPeriod());
        verify(closedPeriodRepository).save(any(ClosedPeriod.class));
    }

    @Test
    void shouldValidateOpenPeriod() {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        YearMonth period = YearMonth.of(2023, 10);

        when(closedPeriodRepository.exists(companyId, period)).thenReturn(false);

        assertDoesNotThrow(() -> periodClosingService.validatePeriodOpen(companyId, period));
    }

    @Test
    void shouldThrowExceptionWhenPeriodIsClosed() {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        YearMonth period = YearMonth.of(2023, 10);

        when(closedPeriodRepository.exists(companyId, period)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> periodClosingService.validatePeriodOpen(companyId, period));
    }
}
