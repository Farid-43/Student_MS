package com.example.StudentMS.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encoded = encoder.encode(rawPassword);
        System.out.println("Raw: " + rawPassword);
        System.out.println("Encoded: " + encoded);
        System.out.println("Matches: " + encoder.matches(rawPassword, encoded));
    }
}
