package com.skillswap.service;

import com.skillswap.dto.LoginRequest;
import com.skillswap.dto.LoginResponse;
import com.skillswap.dto.RegisterRequest;
import com.skillswap.dto.VerifyOtpRequest;
import com.skillswap.model.User;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.dev-show-otp:false}")
    private boolean devShowOtp;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    public String register(RegisterRequest req) {
        User existing = userRepository.findByEmail(req.getEmail()).orElse(null);
        if (existing != null && existing.isEnabled()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = (existing != null) ? existing : new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(false);

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            emailService.sendOtp(user.getEmail(), otp);
        } catch (Exception e) {
            if (!devShowOtp) {
                throw new IllegalArgumentException("Could not send the verification email. Please try again later.");
            }
        }
        return devShowOtp ? otp : null;
    }

    public void verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Account already verified");
        }
        if (user.getOtp() == null || !user.getOtp().equals(req.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired. Please register again.");
        }

        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Please verify your email first");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}