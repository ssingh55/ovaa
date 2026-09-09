package oversecured.ovaa.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class WeakCrypto {
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALIAS = "secure_app_key";
    private static final int GCM_IV_LENGTH = 12;

    private WeakCrypto() {
    }

    private static SecretKey getOrCreateSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build());
            return keyGenerator.generateKey();
        } else {
            return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        }
    }

    public static String encrypt(String data) {
        try {
            SecretKey secureKey = getOrCreateSecretKey();
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secureKey);

            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(data.getBytes());

            byte[] encryptedData = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, encryptedData, GCM_IV_LENGTH, ciphertext.length);

            return Base64.encodeToString(encryptedData, 0);
        } catch (Exception e) {
            return "";
        }
    }
}
