package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.sso.application.dto.AuthResult;
import com.casrusil.siierpai.modules.sso.domain.event.CompanyCreatedEvent;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.security.JwtTokenProvider;
import com.casrusil.siierpai.shared.domain.event.EventPublisher;
import com.casrusil.siierpai.shared.domain.exception.DomainException;
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

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
    public AuthResult registerCompany(String rut, String razonSocial, String adminEmail, String adminPassword) {
        logger.info("Intentando registrar empresa RUT: {} con Admin: {}", rut, adminEmail);

        if (companyRepository.findByRut(rut).isPresent()) {
            throw new DomainException("La empresa con RUT " + rut + " ya existe") {
            };
        }
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            throw new DomainException("El usuario con email " + adminEmail + " ya existe") {
            };
        }

        Company company = Company.create(rut, razonSocial, adminEmail);
        companyRepository.save(company);

        // Publish event to trigger account seeding
        eventPublisher.publish(new CompanyCreatedEvent(company));

        String passwordHash = passwordEncoder.encode(adminPassword);
        User adminUser = User.create(adminEmail, passwordHash, UserRole.ADMIN, company.getId());
        userRepository.save(adminUser);

        logger.info("Empresa y usuario registrados exitosamente. ID Usuario: {}", adminUser.getId().getValue());

        String token = jwtTokenProvider.generateToken(adminUser.getId(), company.getId(), adminUser.getRole());
        return new AuthResult(token, adminUser, company);
    }

    public AuthResult login(String email, String password) {
        logger.info("Intento de login para: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Login fallido: Usuario no encontrado en DB -> {}", email);
                    return new DomainException("Usuario no encontrado: " + email) {
                    };
                });

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.error("Login fallido: Contraseña incorrecta para -> {}", email);
            throw new DomainException("Contraseña incorrecta") {
            };
        }

        if (!user.isActive()) {
            throw new DomainException("El usuario está inactivo") {
            };
        }

        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new DomainException("Empresa no encontrada") {
                });

        if (!company.isActive()) {
            throw new DomainException("La empresa está inactiva") {
            };
        }

        logger.info("Login exitoso para: {}", email);

        String token = jwtTokenProvider.generateToken(user.getId(), company.getId(), user.getRole());
        return new AuthResult(token, user, company);
    }
}
