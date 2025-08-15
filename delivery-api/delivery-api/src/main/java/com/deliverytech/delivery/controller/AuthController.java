package com.deliverytech.delivery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverytech.delivery.dto.request.LoginRequest;
import com.deliverytech.delivery.dto.request.RegisterRequest;
import com.deliverytech.delivery.dto.response.ApiResponseDTO;
import com.deliverytech.delivery.model.Role;
import com.deliverytech.delivery.model.Usuario;
import com.deliverytech.delivery.repository.UsuarioRepository;
import com.deliverytech.delivery.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Operações de autenticação e autorização")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String MSG_USUARIO_REGISTRADO = "Usuário registrado com sucesso";
    private static final String MSG_LOGIN_REALIZADO = "Login realizado com sucesso";
    private static final String MSG_EMAIL_JA_CADASTRADO = "Email já cadastrado";
    private static final String MSG_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final com.deliverytech.delivery.metrics.BusinessMetricsService metricsService;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuário",
               description = "Registra um novo usuário no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<ApiResponseDTO<String>> register(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do usuário a ser registrado",
                required = true
            ) RegisterRequest request) {
    MDC.put("correlationId", java.util.UUID.randomUUID().toString());
    logger.info("Registro de usuário iniciado: {} | correlationId={}", request.getEmail(), MDC.get("correlationId"));
        
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Tentativa de registro com email já cadastrado: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error(MSG_EMAIL_JA_CADASTRADO));
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .nome(request.getNome())
                .role(request.getRole() != null ? request.getRole() : Role.CLIENTE)
                .ativo(true)
                .restauranteId(request.getRestauranteId())
                .build();

    usuarioRepository.save(usuario);
    metricsService.atualizarUsuariosAtivos(1);
    logger.debug("Usuário salvo com ID {}", usuario.getId());
    String token = jwtUtil.generateToken(User.withUsername(usuario.getEmail()).password(usuario.getSenha()).authorities("ROLE_" + usuario.getRole().name()).build(), usuario);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponseDTO.success(token, MSG_USUARIO_REGISTRADO));
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login",
               description = "Autentica um usuário no sistema e retorna um token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<ApiResponseDTO<String>> login(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciais de login",
                required = true
            ) LoginRequest request) {
    MDC.put("correlationId", java.util.UUID.randomUUID().toString());
    logger.info("Tentativa de login para usuário: {} | correlationId={}", request.getEmail(), MDC.get("correlationId"));
        
    try {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException(MSG_USUARIO_NAO_ENCONTRADO));
        metricsService.atualizarUsuariosAtivos(1);
        String token = jwtUtil.generateToken(User.withUsername(usuario.getEmail()).password(usuario.getSenha()).authorities("ROLE_" + usuario.getRole().name()).build(), usuario);
        logger.debug("Login realizado com sucesso para usuário: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponseDTO.success(token, MSG_LOGIN_REALIZADO));
    } catch (Exception e) {
        logger.error("Erro no login para usuário {}: {}", request.getEmail(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDTO.error("Credenciais inválidas"));
    }
    }
}
