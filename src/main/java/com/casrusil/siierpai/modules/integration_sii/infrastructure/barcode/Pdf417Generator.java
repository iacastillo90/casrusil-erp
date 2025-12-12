package com.casrusil.siierpai.modules.integration_sii.infrastructure.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.Compaction;
import com.google.zxing.pdf417.encoder.Dimensions;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Generador de Códigos de Barras PDF417.
 * 
 * <p>
 * Genera la imagen del código de barras bidimensional requerido para
 * la representación impresa de los documentos electrónicos.
 * 
 * <p>
 * Utiliza el contenido del Timbre Electrónico (TED) como fuente de datos.
 * 
 * @since 1.0
 */
@Component
public class Pdf417Generator {

    public byte[] generatePdf417(String tedXml) {
        try {
            // SII requires PDF417 with specific dimensions and error correction
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.PDF417_COMPACTION, Compaction.BYTE);
            hints.put(EncodeHintType.PDF417_DIMENSIONS, new Dimensions(5, 5, 2, 60)); // Standard SII aspect ratio
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.ISO_8859_1.name());

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    tedXml,
                    BarcodeFormat.PDF_417,
                    600, // Width (pixels)
                    200, // Height (pixels) - approximate, PDF417 scales
                    hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate PDF417 barcode", e);
        }
    }
}
