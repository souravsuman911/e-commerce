package com.demo.authservice.Controller;

import com.demo.authservice.dto.JwtResponse;
import com.demo.authservice.dto.LoginRequest;
import com.demo.authservice.dto.RegisterRequest;
import com.demo.authservice.dto.TokenRequest;
import com.demo.authservice.dto.TokenResponse;
import com.demo.authservice.entity.Role;
import com.demo.authservice.entity.User;
import com.demo.authservice.repository.IRoleRepository;
import com.demo.authservice.repository.IUserRepository;
import com.demo.authservice.security.JwtUtils;
import com.demo.authservice.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IRoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest){
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is not available");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email already exists");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        Set<String> payloadRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (payloadRoles == null) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
            roles.add(userRole);
        } else {
            payloadRoles.forEach(role -> {
                    switch (role.toLowerCase()) {
                        case "admin":
                            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                            roles.add(adminRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByName("ROLE_USER")
                                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                            roles.add(userRole);
                    }
                }
            );
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error : User not found"));

        String jwt = jwtUtils.generateJwtToken(user);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles));
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest request) {
        // simple validation (later: store in DB or config)
        if (!"payment-service".equals(request.getClientId())
                || !"secret123".equals(request.getClientSecret())) {
            return ResponseEntity.status(403).build();
        }

        String token = jwtUtils.generateToken(request.getClientId(), "SERVICE");
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
