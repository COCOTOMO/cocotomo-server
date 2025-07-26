package com.uthon.cocotomo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    
    private static final String EMAIL_CODE_PREFIX = "email_code:";
    private static final String EMAIL_VERIFIED_PREFIX = "email_verified:";
    private static final long CODE_EXPIRATION_MINUTES = 5;

    public void sendVerificationCode(String email) {
        String verificationCode = generateVerificationCode();
        String key = EMAIL_CODE_PREFIX + email;
        
        redisTemplate.opsForValue().set(key, verificationCode, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        
        emailService.sendVerificationEmail(email, verificationCode);
        
        log.info("Verification code sent to email: {}", email);
    }

    public boolean verifyEmail(String email, String verificationCode) {
        String key = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            return false;
        }
        
        redisTemplate.delete(key);
        markEmailAsVerified(email);
        
        log.info("Email verified successfully: {}", email);
        return true;
    }

    public void markEmailAsVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(key, "verified", 24, TimeUnit.HOURS);
    }

    public boolean isEmailVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        String verified = redisTemplate.opsForValue().get(key);
        return "verified".equals(verified);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}