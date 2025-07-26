package com.uthon.cocotomo.controller;

import com.uthon.cocotomo.dto.*;
import com.uthon.cocotomo.service.EmailVerificationService;
import com.uthon.cocotomo.service.UserService;
import com.uthon.cocotomo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증·인가", description = "인증 관련 API")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입 시 이메일 인증을 위한 코드를 발송합니다.")
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody EmailSendRequest request) {
        try {
            emailVerificationService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok("인증 코드가 발송되었습니다. 5분 내에 입력해주세요.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 이메일 인증 코드를 확인합니다.")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationRequest request) {
        boolean isVerified = emailVerificationService.verifyEmail(request.getEmail(), request.getVerificationCode());
        
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다. 이제 회원가입을 진행하세요.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 유효하지 않거나 만료되었습니다.");
        }
    }

    @Operation(summary = "회원가입", description = "먼저 이메일 인증을 해야합니다.")
    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@RequestBody LoginRequest signUpRequest) {
        if (!emailVerificationService.isEmailVerified(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("이메일 인증을 먼저 완료해주세요.");
        }
        
        if (userService.findByEmail(signUpRequest.getEmail()) != null) {
            return ResponseEntity.badRequest().body("이미 등록된 이메일입니다.");
        }
        
        userService.createUser(signUpRequest.getEmail(), signUpRequest.getPassword());
        userService.verifyUserEmail(signUpRequest.getEmail());
        
        return ResponseEntity.ok("회원가입이 완료되었습니다. 로그인해주세요.");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername()));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("작동중임");
    }
}