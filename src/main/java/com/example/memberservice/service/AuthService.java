package com.example.memberservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.memberservice.dto.LoginRequest;
import com.example.memberservice.dto.LoginResponse;
import com.example.memberservice.entity.Account;
import com.example.memberservice.exception.InvalidCredentialsException;
import com.example.memberservice.repository.AccountRepository;
import com.example.memberservice.utils.JwtUtils;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }
    
    public LoginResponse login(LoginRequest loginRequest) {
        Account account = accountRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = jwtUtils.createAccessToken(account.getUsername());
        String refreshToken = jwtUtils.createRefreshToken(account.getUsername());

        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);

        return new LoginResponse(accessToken, refreshToken);
    }
}
