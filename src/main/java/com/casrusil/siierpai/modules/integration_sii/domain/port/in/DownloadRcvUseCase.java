package com.casrusil.siierpai.modules.integration_sii.domain.port.in;

import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import java.util.List;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

/**
 * Caso de uso para descargar registros de compra y venta (RCV) desde el SII.
 * 
 * <p>
 * Este contrato define las operaciones de sincronización con el Registro de
 * Compras y Ventas del SII (Servicio de Impuestos Internos de Chile). Permite
 * obtener todas las facturas electrónicas emitidas y recibidas por una empresa.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Conectar con el servicio SOAP del SII</li>
 * <li>Descargar registros de compras (facturas recibidas)</li>
 * <li>Descargar registros de ventas (facturas emitidas)</li>
 * <li>Parsear XML de respuesta del SII</li>
 * <li>Publicar
 * {@link com.casrusil.siierpai.modules.integration_sii.domain.event.DtesDownloadedEvent}</li>
 * </ul>
 * 
 * <h2>Flujo de sincronización:</h2>
 * <ol>
 * <li>Autenticar con SII ({@link AuthenticateSiiUseCase})</li>
 * <li>Obtener token válido</li>
 * <li>Descargar RCV para período específico (ej: "2025-12")</li>
 * <li>Parsear XML y crear entidades
 * {@link com.casrusil.siierpai.modules.invoicing.domain.model.Invoice}</li>
 * </ol>
 * 
 * <h2>Ejemplo de uso:</h2>
 * 
 * <pre>{@code
 * // 1. Autenticar
 * SiiToken token = authenticateSiiUseCase.authenticate(companyId, certificate);
 * 
 * // 2. Descargar compras
 * List<RcvData> purchases = downloadRcvUseCase.downloadPurchaseRegister(
 *         token,
 *         companyId,
 *         "76.123.456-7",
 *         "2025-12");
 * 
 * // 3. Descargar ventas
 * List<RcvData> sales = downloadRcvUseCase.downloadSalesRegister(
 *         token,
 *         companyId,
 *         "76.123.456-7",
 *         "2025-12");
 * }</pre>
 * 
 * @see RcvData
 * @see SiiToken
 * @see AuthenticateSiiUseCase
 * @see com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.soap.SiiRcvSoapClient
 * @since 1.0
 */
public interface DownloadRcvUseCase {

    /**
     * Descarga el registro de compras (facturas recibidas) desde el SII.
     * 
     * <p>
     * Obtiene todas las facturas que la empresa ha recibido de sus proveedores
     * en el período especificado. Estas facturas generan IVA Crédito Fiscal.
     * 
     * @param token      Token de autenticación del SII (debe estar vigente)
     * @param companyId  ID de la empresa en el sistema
     * @param rutEmpresa RUT de la empresa en formato chileno (ej: "76.123.456-7")
     * @param period     Período en formato AAAA-MM (ej: "2025-12")
     * @return Lista de datos de facturas recibidas
     * @throws IllegalStateException si el token está expirado
     * @throws RuntimeException      si el SII rechaza la solicitud
     */
    List<RcvData> downloadPurchaseRegister(SiiToken token, CompanyId companyId, String rutEmpresa, String period);

    /**
     * Descarga el registro de ventas (facturas emitidas) desde el SII.
     * 
     * <p>
     * Obtiene todas las facturas que la empresa ha emitido a sus clientes
     * en el período especificado. Estas facturas generan IVA Débito Fiscal.
     * 
     * @param token      Token de autenticación del SII (debe estar vigente)
     * @param companyId  ID de la empresa en el sistema
     * @param rutEmpresa RUT de la empresa en formato chileno (ej: "76.123.456-7")
     * @param period     Período en formato AAAA-MM (ej: "2025-12")
     * @return Lista de datos de facturas emitidas
     * @throws IllegalStateException si el token está expirado
     * @throws RuntimeException      si el SII rechaza la solicitud
     */
    List<RcvData> downloadSalesRegister(SiiToken token, CompanyId companyId, String rutEmpresa, String period);
}
