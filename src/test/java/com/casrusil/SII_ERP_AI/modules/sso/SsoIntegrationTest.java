package com.casrusil.SII_ERP_AI.modules.sso;

import com.casrusil.SII_ERP_AI.modules.sso.infrastructure.web.AuthController;
import com.casrusil.SII_ERP_AI.modules.sso.infrastructure.web.CompanyController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public class SsoIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.adapter.in.rest.InvoiceController invoiceController;

        @MockBean
        private com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.adapter.in.rest.AccountingController accountingController;

        @MockBean
        private com.casrusil.SII_ERP_AI.modules.ai_assistant.infrastructure.adapter.in.rest.AiAssistantController aiAssistantController;

        @MockBean
        private com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.in.rest.SiiIntegrationController siiIntegrationController;

        @MockBean
        private com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.out.soap.SiiAuthSoapClient siiAuthSoapClient;

        @MockBean
        private com.casrusil.SII_ERP_AI.modules.integration_sii.infrastructure.adapter.out.soap.SiiRcvSoapClient siiRcvSoapClient;

        @Test
        void testRegisterAndLoginFlow() throws Exception {
                // 1. Register Company + Admin
                AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest(
                                "76123456-7",
                                "Test Company SpA",
                                "admin@test.com",
                                "password123");

                MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andReturn();

                String token = objectMapper.readValue(registerResult.getResponse().getContentAsString(), Map.class)
                                .get("token")
                                .toString();

                // 2. Login (Verify credentials)
                AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(
                                "admin@test.com",
                                "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists());

                // 3. Access Protected Endpoint (Update Profile)
                CompanyController.UpdateCompanyRequest updateRequest = new CompanyController.UpdateCompanyRequest(
                                "Updated Company SpA",
                                "contact@updated.com");

                mockMvc.perform(put("/api/v1/companies/me")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.razonSocial").value("Updated Company SpA"))
                                .andExpect(jsonPath("$.email").value("contact@updated.com"));
        }
}
