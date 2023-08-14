package team6.sobun.global.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {

    public static void main(String[] args) {
        // 16바이트(128비트) 길이의 랜덤한 AES 키 생성
        byte[] aesKey = generateRandomKey(16);

        // 생성된 AES 키를 Base64로 인코딩하여 출력
        String base64Key = Base64.getEncoder().encodeToString(aesKey);
        System.out.println("Generated AES Key: " + base64Key);
    }

    private static byte[] generateRandomKey(int keyLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[keyLength];
        secureRandom.nextBytes(key);
        return key;
    }
}
