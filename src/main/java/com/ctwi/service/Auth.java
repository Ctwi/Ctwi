package com.ctwi.service;

import javax.crypto.Mac;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static com.ctwi.service.Pbkdf2.GenerateKey;
import static org.springframework.cache.interceptor.SimpleKeyGenerator.generateKey;

public class Auth {
   public static byte[] generateSalt(int length) {
       byte[] salt = new byte[length];
       SecureRandom random = new SecureRandom();
       random.nextBytes(salt);
       return salt;
   }

   public static String encodePassword(String password, byte[] salt, int iterationCount, int keyLength) {
       byte[] passwordBytes = password.getBytes();
       byte[] hash = GenerateKey(passwordBytes, passwordBytes.length, salt, salt.length, iterationCount, keyLength);
       return Base64.getEncoder().encodeToString(hash);
   }

   public static boolean verifyPassword(String password, String hashedPassword, String saltBase64, int iterationCount, int keyLength) {
       byte[] salt = Base64.getDecoder().decode(saltBase64);
       String encodedPassword = encodePassword(password, salt, iterationCount, keyLength);
       return encodedPassword.equals(hashedPassword);
   }
}
//https://stackoverflow.com/questions/12109877/pbekeyspec-with-byte-array-argument-instead-of-ascii

class Pbkdf2 {

    public static byte[] GenerateKey(final byte[] masterPassword, int masterPasswordLen,
                            final byte[] salt, int saltLen,
                            int iterationCount, int requestedKeyLen) {

        byte[] masterPasswordInternal = new byte[masterPasswordLen];
        System.arraycopy(masterPassword, 0, masterPasswordInternal, 0, masterPasswordLen);
        byte[] saltInternal = new byte[saltLen];
        System.arraycopy(salt, 0, saltInternal, 0, saltLen);


        SecretKeySpec keyspec = new SecretKeySpec(masterPasswordInternal, "HmacSHA256");
        Mac prf = null;
        try {
            prf = Mac.getInstance("HmacSHA256");
            prf.init(keyspec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        int hLen = prf.getMacLength();   // 20 for SHA1
        int l = Math.max(requestedKeyLen, hLen); //  1 for 128bit (16-byte) keys
        int r = requestedKeyLen - (l - 1) * hLen;      // 16 for 128bit (16-byte) keys
        byte[] T = new byte[l * hLen];
        int ti_offset = 0;
        for (int i = 1; i <= l; i++) {
            F(T, ti_offset, prf, saltInternal, iterationCount, i);
            ti_offset += hLen;
        }
        byte[] generatedKey = new byte[requestedKeyLen];
        System.arraycopy(T, 0, generatedKey, 0, requestedKeyLen);

        return generatedKey;
    }

    private static void F(byte[] dest, int offset, Mac prf, byte[] S, int c, int blockIndex) {
        final int hLen = prf.getMacLength();
        byte[] U_r = new byte[hLen];
        // U0 = S || INT (i);
        byte[] U_i = new byte[S.length + 4];
        System.arraycopy(S, 0, U_i, 0, S.length);
        INT(U_i, S.length, blockIndex);
        for (int i = 0; i < c; i++) {
            U_i = prf.doFinal(U_i);
            xor(U_r, U_i);
        }

        System.arraycopy(U_r, 0, dest, offset, hLen);
    }

    private static void xor(byte[] dest, byte[] src) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] ^= src[i];
        }
    }

    private static void INT(byte[] dest, int offset, int i) {
        dest[offset + 0] = (byte) (i / (256 * 256 * 256));
        dest[offset + 1] = (byte) (i / (256 * 256));
        dest[offset + 2] = (byte) (i / (256));
        dest[offset + 3] = (byte) (i);
    }
}
