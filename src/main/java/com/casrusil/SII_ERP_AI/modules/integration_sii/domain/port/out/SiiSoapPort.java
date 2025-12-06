package com.casrusil.SII_ERP_AI.modules.integration_sii.domain.port.out;

import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.RcvData;
import com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model.SiiToken;
import java.util.List;

public interface SiiSoapPort {
    String getSeed();

    String getToken(String signedSeed);

    List<RcvData> getRcv(SiiToken token, String rutEmpresa, String period, boolean isPurchase);
}
