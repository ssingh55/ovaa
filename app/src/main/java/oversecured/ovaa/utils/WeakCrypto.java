package oversecured.ovaa.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class WeakCrypto {
    private static final String KEY_ALIAS = "secure_app_key";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits

    private static SecretKey sSecureKey;

    private WeakCrypto() {
    }

    // This method should be called once at application startup, e.g., in Application.onCreate()
    public static void initializeSecureKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null); // Load the KeyStore

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            // Generate a new key if it doesn't exist
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build();
            keyGenerator.init(keyGenParameterSpec);
            sSecureKey = keyGenerator.generateKey();
        } else {
            // Retrieve the existing key
            sSecureKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        }
    }

    public static String encrypt(String data) {
        if (sSecureKey == null) {
            // Log an error or throw an exception if the key was not initialized
            // For example: throw new IllegalStateException("Secure key not initialized.");
            return ""; // Return empty string or handle error appropriately
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // Generate a cryptographically secure random IV for each encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Initialize the cipher with the secure key and GCM parameters
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, sSecureKey, gcmSpec);

            // Encrypt the plaintext
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));

            // Combine IV and ciphertext (which includes the authentication tag in GCM)
            // for storage/transmission. The IV is prepended to the ciphertext.
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // Base64 encode the combined result
            return Base64.encodeToString(combined, Base64.NO_WRAP);

        } catch (Exception e) {
            // Log the exception for debugging purposes
            // For example: Log.e("WeakCrypto", "Encryption failed", e);
            return ""; // Return empty string or handle error appropriately
        }
    }
}
