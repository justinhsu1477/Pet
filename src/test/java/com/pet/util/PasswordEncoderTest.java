package com.pet.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {

    @Test
    public void generatePasswords() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String[] passwords = {"admin123", "password123", "sitter123"};

        for (String password : passwords) {
            String encoded = passwordEncoder.encode(password);
            System.out.println("原始密碼: " + password);
            System.out.println("加密密碼: " + encoded);
            System.out.println("驗證結果: " + passwordEncoder.matches(password, encoded));
            System.out.println("---");
        }
    }
}
