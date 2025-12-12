package com.casrusil.siierpai.modules.integration_sii.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.in.DownloadRcvUseCase;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiSoapPort;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aplicación para la descarga del Registro de Compras y Ventas
 * (RCV).
 * 
 * <p>
 * Orquesta la comunicación con el SII para obtener los registros detallados de
 * facturas recibidas y emitidas en un periodo tributario.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Validar la vigencia del token de autenticación.</li>
 * <li>Descargar registro de compras (para declarar IVA Crédito).</li>
 * <li>Descargar registro de ventas (para declarar IVA Débito).</li>
 * </ul>
 * 
 * @see DownloadRcvUseCase
 * @see RcvData
 * @since 1.0
 */
@Service
public class SiiRcvService implements DownloadRcvUseCase {

    private final SiiSoapPort siiSoapPort;

    public SiiRcvService(SiiSoapPort siiSoapPort) {
        this.siiSoapPort = siiSoapPort;
    }

    @Override
    public List<RcvData> downloadPurchaseRegister(SiiToken token, CompanyId companyId, String rutEmpresa,
            String period) {
        if (!token.isValid()) {
            throw new IllegalArgumentException("Token is expired or invalid");
        }
        return siiSoapPort.getRcv(token, rutEmpresa, period, true);
    }

    @Override
    public List<RcvData> downloadSalesRegister(SiiToken token, CompanyId companyId, String rutEmpresa, String period) {
        if (!token.isValid()) {
            throw new IllegalArgumentException("Token is expired or invalid");
        }
        return siiSoapPort.getRcv(token, rutEmpresa, period, false);
    }
}
