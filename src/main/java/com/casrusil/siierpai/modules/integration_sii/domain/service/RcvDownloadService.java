package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.DownloadRcvUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiSoapPort;
import org.springframework.stereotype.Service;

import java.util.List;

import com.casrusil.siierpai.modules.integration_sii.domain.event.DtesDownloadedEvent;
import com.casrusil.siierpai.shared.domain.event.EventPublisher;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

@Service
@org.springframework.context.annotation.Primary
public class RcvDownloadService implements DownloadRcvUseCase {

    private final SiiSoapPort siiSoapPort;
    private final EventPublisher eventPublisher;

    public RcvDownloadService(SiiSoapPort siiSoapPort, EventPublisher eventPublisher) {
        this.siiSoapPort = siiSoapPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<RcvData> downloadPurchaseRegister(SiiToken token, CompanyId companyId, String rutEmpresa,
            String period) {
        List<RcvData> rcvData = siiSoapPort.getRcv(token, rutEmpresa, period, true);
        eventPublisher.publish(new DtesDownloadedEvent(companyId, rcvData));
        return rcvData;
    }

    @Override
    public List<RcvData> downloadSalesRegister(SiiToken token, CompanyId companyId, String rutEmpresa, String period) {
        List<RcvData> rcvData = siiSoapPort.getRcv(token, rutEmpresa, period, false);
        eventPublisher.publish(new DtesDownloadedEvent(companyId, rcvData));
        return rcvData;
    }
}
