package smart_city.util;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecurityUtil {

    private static final String SECRET =
            System.getenv().getOrDefault("BROKER_PASSWORD", "secret1234567890"); // 16+ chars

    // ================= HMAC =================
    public static String generateHmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }

    public static boolean verifyHmac(String data, String received) throws Exception {
        return generateHmac(data).equals(received);
    }

    // ================= AES =================
    public static String encrypt(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET.substring(0,16).getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encrypted) throws Exception {
        SecretKeySpec key = new SecretKeySpec(SECRET.substring(0,16).getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }
}