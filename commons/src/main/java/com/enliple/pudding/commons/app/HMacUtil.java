package com.enliple.pudding.commons.app;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class HMacUtil {
    public static byte[] deriveKey(String p, byte[] s, int i, int l) throws Exception {
        PBEKeySpec ks = new PBEKeySpec(p.toCharArray(), s, i, l);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(ks).getEncoded();
    }

    public static String hashMac(String str, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKey);

        return bytesToHex(sha256_HMAC.doFinal(str.getBytes()));
    }

    public static String encrypt(String s, String p) throws Exception {
        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");

        // Generate 160 bit Salt for Encryption Key
        byte[] esalt = new byte[20];
        r.nextBytes(esalt);
        // Generate 128 bit Encryption Key
        byte[] dek = deriveKey(p, esalt, 100000, 128);

        // Perform Encryption
        SecretKeySpec eks = new SecretKeySpec(dek, "AES");
        Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
        byte[] es = c.doFinal(s.getBytes(StandardCharsets.UTF_8));

        // Generate 160 bit Salt for HMAC Key
        byte[] hsalt = new byte[20];
        r.nextBytes(hsalt);
        // Generate 160 bit HMAC Key
        byte[] dhk = deriveKey(p, hsalt, 100000, 160);

        // Perform HMAC using SHA-256
        SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(hks);
        byte[] hmac = m.doFinal(es);

        // Construct Output as "ESALT + HSALT + CIPHERTEXT + HMAC"
        byte[] os = new byte[40 + es.length + 32];
        System.arraycopy(esalt, 0, os, 0, 20);
        System.arraycopy(hsalt, 0, os, 20, 20);
        System.arraycopy(es, 0, os, 40, es.length);
        System.arraycopy(hmac, 0, os, 40 + es.length, 32);

        // Return a Base64 Encoded String
        return new String(encodeBase64(os));
    }

    public static String decrypt(String eos, String p) throws Exception {
        // Recover our Byte Array by Base64 Decoding
        byte[] os = decodeBase64(eos.getBytes());

        // Check Minimum Length (ESALT (20) + HSALT (20) + HMAC (32))
        if (os.length > 72) {
            // Recover Elements from String
            byte[] esalt = Arrays.copyOfRange(os, 0, 20);
            byte[] hsalt = Arrays.copyOfRange(os, 20, 40);
            byte[] es = Arrays.copyOfRange(os, 40, os.length - 32);
            byte[] hmac = Arrays.copyOfRange(os, os.length - 32, os.length);

            // Regenerate HMAC key using Recovered Salt (hsalt)
            byte[] dhk = deriveKey(p, hsalt, 100000, 160);

            // Perform HMAC using SHA-256
            SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(hks);
            byte[] chmac = m.doFinal(es);

            // Compare Computed HMAC vs Recovered HMAC
            if (MessageDigest.isEqual(hmac, chmac)) {
                // HMAC Verification Passed
                // Regenerate Encryption Key using Recovered Salt (esalt)
                byte[] dek = deriveKey(p, esalt, 100000, 128);

                // Perform Decryption
                SecretKeySpec eks = new SecretKeySpec(dek, "AES");
                Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
                c.init(Cipher.DECRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
                byte[] s = c.doFinal(es);

                // Return our Decrypted String
                return new String(s, StandardCharsets.UTF_8);
            }
        }
        throw new Exception();
    }

    private static String encodeBase64(byte[] os) {
        return Base64.encodeToString(os, Base64.NO_WRAP);
    }

    private static byte[] decodeBase64(byte[] chars) {
        return Base64.decode(chars, Base64.NO_WRAP);
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
