package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class Pkcs12HandlerTest {

    private Pkcs12Handler pkcs12Handler;
    private Path tempP12File;
    private String password = "password";

    // Generated dummy PKCS12 (password: password)
    private static final String DUMMY_P12_BASE64 = "MIIKgAIBAzCCCioGCSqGSIb3DQEHAaCCChsEggoXMIIKEzCCBaoGCSqGSIb3DQEH" +
            "AaCCBZsEggWXMIIFkzCCBY8GCyqGSIb3DQEMCgECoIIFQDCCBTwwZgYJKoZIhvcN" +
            "AQUNMFkwOAYJKoZIhvcNAQUMMCsEFOxpEbpqjVBWRviPu71SYZ8x30QZAgInEAIB" +
            "IDAMBggqhkiG9w0CCQUAMB0GCWCGSAFlAwQBKgQQL8ncZyOaUdzCkVYwVUegRQSC" +
            "BNCxjhf4dms914JYrR8uwAt+1JfndfnjH8hxKzEcaKJIdcxR9OzsMQw1Fhgovsjj" +
            "Rrhv6ORz67XJIqi+oNgY5dDj2T/lQdnFC2a5jfzmMqbt7n+ZPn2FD+JKkBlz7L6n" +
            "cvzayRcmy2zBtHKHuy7FjOHXQcUH/Fcjg1iRbCF5YqALOjkw+iI0wRypVSUffuJX" +
            "7RzdAalxd40OKWVSCr8aagMuUgD2mumdVo0tDPIKeVExAKc1eqyKBVkagQMXY0Rd" +
            "ukuHtdCpXjbg+iA4nhI9Zr1wNcP7lWFz9Ku6YugoJtGokYiIMA33E+1XgaAv5n/f" +
            "Cq8+nsGjWrYl9BVig49x+3cXWKQ6PcgeUYGNpruwTfA0Ww6Owef2FLDg0UdVUncr" +
            "asnMudUQiGyMVXIfWj2lN/o8nF5tdvAUgEnzixS9muQwUQybNDCmFvIQ6z0y62IQ" +
            "39mCw6T6dtjVf8pYGotRZLc33q5nD8x4H90bsZ/Q7R0TISpzPWFNzw4Bl20gnv8/" +
            "qZ/qhPmd+ToQe92D3CSV5F9gIidXb6ymSI4flxV3t4ASq3Tw49z1vIaf9C1Ui210" +
            "Ylt87U43KZhyjm26pAQ5/v9Qtpg90Tid3g/97GqFa8rvJK3c9GjPyefOiM+T+IHs" +
            "Ct2govvZVjIVt0ya5FvTddl9MHlPBSlHBj+YLQxGLUHir9S7CRW9+vOZU4/IiKvZ" +
            "hF09NkFmH8Cqzw06I44DJK05AjpmJkn3TdfjFfMMRb6HmHLKOGYSNpNHB7PFvM4j" +
            "yOovVV8aiUHIYrEitYwjnRfmJvrBnsdfa0UO3b7evubr50MkbPS9S4uAiQqranRn" +
            "wCe4l0jsCLmau8oPu7yu4Sc6RILQb+I/xGndyApet6ah3H2CzcQSU0k96WFrT0ch" +
            "6RHfTd+zHYMNnk+5SuCXbFpxoUFom5gbShw36XfL3IMUQ+aQ8WiJjbab4PZba1aU" +
            "YZF/blXbr1X/iIE8wcGQk+18Nq5XKgEjdKNbE5pIPiEIy4N0JygPXb9mCM2yZeeK" +
            "tcKMVQ7+mWEgsN6lPyZGQHwU0F9/Z7svTtbHqQJVwu4M9Lpvavi5RmtMWeybfs1m" +
            "dJXaqhQx9MLIATvj3kjOtewBqJK4K0x9gKdyApKvej3vuIhDJCzT9v8GRATuPgXL" +
            "BkevWRxKS8Cg6o98+c65o+EwrDmSLJAJLkoKZE0aTW0fcPGA8S0jS6fkDmEfSoG1" +
            "hKCzT/lH7Sqh9vpitd4LtkhnXPhD4sm0owv+SkP/P9BYgkbydXsZ5i2gMFKQk74I" +
            "rLVnQpFu1XN6VYRN7SimluVKAyPSCvxdx/2AKqKiNuo20cYCI+hDB1tCJwRHtWdZ" +
            "ciJZzOCgrdJ1cJw9WzmQ2uoWYG6VNCg3sRoZ4w8yY4+qTKeSwRRCjC/bp5bz8ady" +
            "zsp1Dux8DjF3GLcmVlJ75XPs8DjLag10bxu26iZWQpwmAFKYf2QUlrtXIrhOTbBQ" +
            "TgFi66Nppyg0ds+mflFD2h1AJl4UhLKMCTN2mLwLjQiXvyU/20PHAl/lZWeDMRJU" +
            "23ISNLI5ZLECLVbyW5ycoFxjlw7wxJAiOBBsDjVjFTOqRqQ7e6pWxW2Zag1ZwEZd" +
            "CrzTL0KnJOnhRoPSQ0er1Sv3GlR9cPUN9uVMjbcY6vWdcTE8MBcGCSqGSIb3DQEJ" +
            "FDEKHggAdABlAHMAdDAhBgkqhkiG9w0BCRUxFAQSVGltZSAxNzY0NzMxMjM2ODgy" +
            "MIIEYQYJKoZIhvcNAQcGoIIEUjCCBE4CAQAwggRHBgkqhkiG9w0BBwEwZgYJKoZI" +
            "hvcNAQUNMFkwOAYJKoZIhvcNAQUMMCsEFE4feQsIhKIPTEoMpuI7udAVLpLZAgIn" +
            "EAIBIDAMBggqhkiG9w0CCQUAMB0GCWCGSAFlAwQBKgQQl8wy6Alc5BuncYWFb4h8" +
            "YoCCA9CDrWxqljnYPZP2toAAa6SkDzbWVzvRTLFlY3sKWkSnRszFtPkZ5rnQ2ZT3" +
            "FnJLCzw07Tu11RKddSFispPpASprW+bjYyaiRHP4JzjORW7kE4c61OmSxfreCfFk" +
            "d5LfKN1Nte72doDeyjf1qtr9dh4eJFmE++ZpjEx1WXmPE87FDvXspzb1cMBZG+Sd" +
            "Eke4bfutgeGV7rz5V6q/eMk6SLlN12YukN6o5GWnSJ6umO6P67EVQtVygDsEs2Er" +
            "LbNd94kFXOG7LALepSABUuOjOSRmjme2chkfN5c9cji71Ln7nUW91P9l4XkgC0MT" +
            "7SPJNnu+xAn3f1/0Yxu0S9wUXZKpVQbPSrTs9fzKLjS8lxG2st2xuXYCprqFbHQa" +
            "OglYvuXZ5ova9unisWm27kRkAPnKetTcFXsE0AF0hNcRt/CcnIi6QdU7+M6k8aQU" +
            "+0lOZK+PcP/VqhhiiQd9X243CLRVFzk+d3GwWBtV5+OiL03E+YbBcKT+RbUT2hvJ" +
            "/kfcti8svqctrewtzI16kY8bdDpr3HxjYVC507OxXAZwF6KsblGAp5LCYEhyONLi" +
            "R9tZMdz3b/kBcHpB/IyeTB7bk19DfLQF24hhwWGaIJ6rqDSh8CP2x/5ekxexF3m/" +
            "h0yC6IbU7Ztc4VZXeZaoAemXuC0nki+V4Hkf4V6lkTZ9Zy37J0Wrb0PTawferZBX" +
            "dxwczG3H2b8nKxbVnaWtH49ClHbWjSPUHGQrtrDYvxHBkk6JCXcN+PZ8XoOwVfWj" +
            "pVOrWE658h8m9gMKpArvg5ZJZkifnSM6WAepT13dM3cAOon78cOnAnnC9c3Qc9cW" +
            "iK7VU6ti9BccbmUEB0pWyG9VTnDhN2GPqtjL+31Qf9gz7mKRGR3tfnZFiurEdp+" +
            "ivPU9GTom4EdthIRlZN3FhsOu7wZlhpFjA13AjX4tz3uvNhJQLNQEz7WTb2fWNVr" +
            "4BrIzxh4lvpJZyxgJp0nV2TWud11H3VcwVoplliypxBtHEoUWFZoYknSz/bOG3us" +
            "W9cau6JhxZqFZdIw4flfgtALm+oVq7x4ZhSjGsQV6zb/8vMiLhpPOcfsHKiHEX3G" +
            "G9uZM4GirYgCZeNclxBNlocKzTs3xNM3XgXNalfpdXoFZTozuf7dqJWeCbQ9fy9K" +
            "sYgIzUj/3lEUveJ2XDgEj7Ejw+q+qpimL/tva6TgIR4HE6XVRZXxnygO7fkL8+Qo" +
            "4pLJU0+RL4TUIcGpqdcpi6OUp1ITIZ5jg7wHohF6N/SId5hfm21Bjv3OkuowjqU5" +
            "uAr84UR7o8DPq0p0swukVRCIKaXeME0wMTANBglghkgBZQMEAgEFAAQg+BaY5jjJ" +
            "VkrvuyTOD6WzForlAR7Isgup0KwdmtDmI48EFMSxYzhRk5uDmUTehkUlUGKUrMkQ" +
            "AgInEA==";

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        pkcs12Handler = new Pkcs12Handler();
        tempP12File = tempDir.resolve("test-cert.p12");
    }

    @Test
    void loadCertificate_ShouldLoadValidCertificate() {
        byte[] p12Bytes = Base64.getDecoder().decode(DUMMY_P12_BASE64);
        ByteArrayInputStream is = new ByteArrayInputStream(p12Bytes);

        SiiCertificate certificate = pkcs12Handler.loadCertificate(is, password);

        assertNotNull(certificate);
        assertNotNull(certificate.certificate());
        assertNotNull(certificate.privateKey());
        // RUT extraction logic in Pkcs12Handler is currently a placeholder
        // "UNKNOWN-RUT"
        assertEquals("UNKNOWN-RUT", certificate.rut());
    }

    @Test
    void loadCertificate_ShouldThrowException_WhenPasswordIsIncorrect() {
        byte[] p12Bytes = Base64.getDecoder().decode(DUMMY_P12_BASE64);
        ByteArrayInputStream is = new ByteArrayInputStream(p12Bytes);

        assertThrows(RuntimeException.class, () -> {
            pkcs12Handler.loadCertificate(is, "wrongpassword");
        });
    }

    @Test
    void loadCertificate_ShouldThrowException_WhenFileDoesNotExist() {
        assertThrows(RuntimeException.class, () -> {
            pkcs12Handler.loadCertificate("non-existent-file.p12", password);
        });
    }
}
