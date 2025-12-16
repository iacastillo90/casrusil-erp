package com.casrusil.siierpai.shared.infrastructure.mail;

import org.springframework.stereotype.Component;

/**
 * Proveedor de plantillas de correo HTML.
 * Utiliza Java Text Blocks para mantener el HTML legible en el código.
 */
@Component
public class EmailTemplateProvider {

    private static final String BASE_STYLE = """
            <style>
                body { font-family: 'Inter', sans-serif; color: #333; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 8px; }
                .header { text-align: center; margin-bottom: 30px; }
                .logo { max-height: 60px; }
                .button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; }
                .footer { margin-top: 30px; font-size: 0.8em; color: #777; text-align: center; border-top: 1px solid #eee; padding-top: 20px; }
            </style>
            """;

    public String getWelcomeTemplate(String userName, String companyName) {
        return """
                <html>
                <head>%s</head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>¡Bienvenido a SII ERP AI!</h2>
                        </div>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Tu cuenta ha sido creada exitosamente y ya formas parte del equipo de <strong>%s</strong>.</p>
                        <p>Estamos emocionados de tenerte a bordo. Ahora puedes acceder a todas las herramientas de gestión financiera y tributaria potenciadas por IA.</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">Ir al Dashboard</a>
                        </div>
                        <div class="footer">
                            <p>© 2025 SII ERP AI. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(BASE_STYLE, userName, companyName, "http://localhost:3000"); // TODO: Use env var for URL
    }

    public String getInvitationTemplate(String inviteLink, String companyName, String companyRut, String logoUrl,
            String inviterName) {
        String logoHtml = (logoUrl != null && !logoUrl.isBlank())
                ? "<img src='" + logoUrl + "' alt='" + companyName + "' class='logo'>"
                : "<h1>" + companyName + "</h1>";

        return """
                <html>
                <head>%s</head>
                <body>
                    <div class="container">
                        <div class="header">
                            %s
                        </div>
                        <p>Hola,</p>
                        <p><strong>%s</strong> (RUT: %s) te ha invitado a unirte a su equipo en SII ERP AI.</p>
                        <p>Esta invitación fue enviada por <strong>%s</strong>.</p>
                        <p>Para aceptar la invitación y configurar tu cuenta, haz clic en el siguiente botón:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">Aceptar Invitación</a>
                        </div>
                        <p class="small">Este enlace expirará en 24 horas.</p>
                        <div class="footer">
                            <p>Si no esperabas esta invitación, puedes ignorar este correo.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(BASE_STYLE, logoHtml, companyName, companyRut, inviterName, inviteLink);
    }

    public String getWeeklyReportTemplate(String period) {
        return """
                <html>
                <head>%s</head>
                <body>
                    <div class="container">
                         <div class="header">
                            <h2>Reporte Semanal</h2>
                        </div>
                        <p>Adjunto encontrarás el reporte de movimientos para el periodo: <strong>%s</strong>.</p>
                        <p>Resumen de actividad:</p>
                        <ul>
                            <li>Ventas procesadas</li>
                            <li>Compras registradas</li>
                            <li>Balance preliminar</li>
                        </ul>
                        <div class="footer">
                            <p>Generado automáticamente por SII ERP AI.</p>
                            <p>Este es un reporte automático, por favor no respondas a este correo.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(BASE_STYLE, period);
    }
}
