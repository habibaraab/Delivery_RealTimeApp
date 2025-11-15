package com.spring.DeliveryApp.auth.Service;

import com.spring.DeliveryApp.auth.Dto.AuthenticationResponse;
import com.spring.DeliveryApp.auth.Dto.LoginRequest;
import com.spring.DeliveryApp.auth.Dto.Mapper.UserMapper;
import com.spring.DeliveryApp.auth.Dto.UserRequestDto;
import com.spring.DeliveryApp.auth.Entity.Token;
import com.spring.DeliveryApp.auth.Entity.User;
import com.spring.DeliveryApp.auth.Enum.TokenType;
import com.spring.DeliveryApp.auth.Repository.TokenRepository;
import com.spring.DeliveryApp.auth.Repository.UserRepository;
import com.spring.DeliveryApp.auth.Security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserRequestDto request) {
        User user = userMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        String accessToken = jwtService.generateToken(savedUser, claims);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveUserToken(savedUser, accessToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getName(), request.getPassword())
        );

        User user = userRepository.findUserByName(request.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        String accessToken = jwtService.generateToken(user, claims);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        User user = userRepository.findUserByName(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (jwtService.isRefreshTokenValid(refreshToken, user)) {
            Map<String, Object> claims = new HashMap<>();
            String newAccessToken = jwtService.generateToken(user, claims);

            revokeAllUserTokens(user);
            saveUserToken(user, newAccessToken);

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    private void revokeAllUserTokens(User user) {
        var validateUserToken = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validateUserToken.isEmpty()) {
            return;
        }
        validateUserToken.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validateUserToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}