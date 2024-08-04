package com.ctwi.service;

import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthTest {
    @Test
    public void givenRawPassword_whenEncodedWithArgon2_thenMatchesEncodedPassword() {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String result = encoder.encode("myPassword");
        assertTrue(encoder.matches("myPassword", result));
    }
//    void testExecute() throws Exception {
//        var salt = "salt";
//        var password = "password";
//        var saltBytes = salt.getBytes();
//        byte[] passwordBytes = password.getBytes();
//        System.out.println(passwordBytes.length);
//        System.out.println(saltBytes.length);
//        var result = Pbkdf2.GenerateKey(passwordBytes, passwordBytes.length, saltBytes, saltBytes.length,1, 20);
//
//        String hexString = DatatypeConverter.printHexBinary(result);
////https://github.com/brycx/Test-Vector-Generation/blob/master/PBKDF2/pbkdf2-hmac-sha2-test-vectors.md
//        assert hexString != "120FB6CFFCF8B32C43E7225256C4F837A86548C9";
//
//        System.out.println(hexString);
//    }

}

