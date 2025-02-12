package com.marvel.marveljourney.service;

import com.marvel.marveljourney.dto.VerificationRequest;
import com.marvel.marveljourney.exception.ErrorCode;
import com.marvel.marveljourney.exception.UserNotFoundException;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.model.User.VerificationCode;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.security.VerificationCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public void sendVerificationCode(String email) {
        String verificationCode = VerificationCodeUtil.generateCode();
        updateVerificationCode(email, verificationCode, Instant.now());
        emailService.sendVerificationEmail(email, verificationCode);
    }

    public void updateVerificationCode(String email, String code, Instant createdAt) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(createdAt);
        user.setVerificationCode(verificationCode);
        userRepository.save(user);
    }

    public Map<String, String> verifyEmail(VerificationRequest verificationRequest) {
        User userExist = userRepository.findByEmail(verificationRequest.getEmail()).orElse(null);

        if (userExist == null) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        VerificationCode verificationCode = userExist.getVerificationCode();

        if (verificationCode == null || !verificationCode.getCode().equals(verificationRequest.getCode())) {
            throw new IllegalArgumentException(ErrorCode.INVALID_VERIFICATION_CODE.getMessage());
        }

        Instant now = Instant.now();
        Instant codeCreationTime = verificationCode.getCreatedAt();
        Duration duration = Duration.between(codeCreationTime, now);

        if (duration.toMinutes() > 5) {
            throw new IllegalArgumentException(ErrorCode.EXPIRED_VERIFICATION_CODE.getMessage());
        }

        userRepository.verifyEmail(verificationRequest.getEmail());
        return Map.of("message", "Email successfully verified.");
    }
}
