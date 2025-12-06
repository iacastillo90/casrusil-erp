package com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.out.soap;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.RcvData;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.out.SiiSoapPort;
import com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.parser.RcvXmlParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiiSoapAdapter implements SiiSoapPort {

    private final SiiAuthSoapClient authClient;
    private final SiiRcvSoapClient rcvClient;
    private final RcvXmlParser rcvParser;

    public SiiSoapAdapter(SiiAuthSoapClient authClient, SiiRcvSoapClient rcvClient, RcvXmlParser rcvParser) {
        this.authClient = authClient;
        this.rcvClient = rcvClient;
        this.rcvParser = rcvParser;
    }

    @Override
    public String getSeed() {
        return authClient.getSeed();
    }

    @Override
    public String getToken(String signedSeed) {
        return authClient.getToken(signedSeed);
    }

    @Override
    public List<RcvData> getRcv(SiiToken token, String rutEmpresa, String period, boolean isPurchase) {
        String xml = rcvClient.downloadRcv(token.token(), rutEmpresa, period, isPurchase);
        return rcvParser.parse(xml);
    }
}
