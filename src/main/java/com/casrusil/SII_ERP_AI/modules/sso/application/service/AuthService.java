package com.casrusil.SII_ERP_AI.modules.sso.application.service;

import com.casrusil.SII_ERP_AI.modules.sso.domain.event.CompanyCreatedEvent;
import com.casrusil.SII_ERP_AI.modules.sso.domain.model.Company;
import com.casrusil.SII_ERP_AI.modules.sso.domain.model.User;
import com.casrusil.SII_ERP_AI.modules.sso.domain.model.UserRole;
import com.casrusil.SII_ERP_AI.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.SII_ERP_AI.modules.sso.domain.port.out.UserRepository;
import com.casrusil.SII_ERP_AI.modules.sso.infrastructure.security.JwtTokenProvider;
import com.casrusil.SII_ERP_AI.shared.domain.event.EventPublisher;
import com.casrusil.SII_ERP_AI.shared.domain.exception.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para la autenticación y registro de usuarios.
 * 
 * <p>
 * Maneja el ciclo de vida de la seguridad: registro de nuevas empresas,
 * login de usuarios y generación de tokens JWT.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Registrar nuevas empresas y sus usuarios administradores.</li>
 * <li>Autenticar credenciales (email/password).</li>
 * <li>Generar tokens JWT para sesiones stateless.</li>
 * <li>Publicar eventos de creación de empresa (para seeding de datos).</li>
 * </ul>
 * 
 * @see JwtTokenProvider
 * @see CompanyManagementService
 * @since 1.0
 */
@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EventPublisher eventPublisher;

    public AuthService(CompanyRepository companyRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, EventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String registerCompany(String rut, String razonSocial, String adminEmail, String adminPassword) {
        if (companyRepository.findByRut(rut).isPresent()) {
            throw new DomainException("Company with RUT " + rut + " already exists") {
            };
        }
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            throw new DomainException("User with email " + adminEmail + " already exists") {
            };
        }

        Company company = Company.create(rut, razonSocial, adminEmail);
        companyRepository.save(company);

        // Publish event to trigger account seeding
        eventPublisher.publish(new CompanyCreatedEvent(company));

        String passwordHash = passwordEncoder.encode(adminPassword);
        User adminUser = User.create(adminEmail, passwordHash, UserRole.ADMIN, company.getId());
        userRepository.save(adminUser);

        return jwtTokenProvider.generateToken(adminUser.getId(), company.getId(), adminUser.getRole());
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("Invalid credentials") {
                });

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new DomainException("Invalid credentials") {
            };
        }

        if (!user.isActive()) {
            throw new DomainException("User is inactive") {
            };
        }

        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new DomainException("Company not found") {
                });

        if (!company.isActive()) {
            throw new DomainException("Company is inactive") {
            };
        }

        return jwtTokenProvider.generateToken(user.getId(), company.getId(), user.getRole());
    }
}
