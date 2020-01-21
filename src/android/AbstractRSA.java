package com.crypho.plugins;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyProperties;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;

public abstract class AbstractRSA {
    protected static final String TAG = "SecureStorage";
    static final Integer CERT_VALID_YEARS = 100;
    static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private final Cipher CIPHER = getCipher();


    abstract AlgorithmParameterSpec getInitParams(Context ctx, String alias, Integer userAuthenticationValidityDuration) throws Exception;

    boolean encryptionKeysAvailable(String alias) {
        return isEntryAvailable(alias);
    }

    String getRSAKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return KeyProperties.KEY_ALGORITHM_RSA;
        }
        return "RSA";
    }

    private Cipher getCipher() {
        try {
            return Cipher.getInstance("RSA/ECB/PKCS1Padding");
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] runCipher(int cipherMode, String alias, byte[] buf) throws Exception {
        Key key = loadKey(cipherMode, alias);
        assert CIPHER != null;
        synchronized (CIPHER) {
            CIPHER.init(cipherMode, key);
            return CIPHER.doFinal(buf);
        }
    }

    public void createKeyPair(Context ctx, String alias, Integer userAuthenticationValidityDuration) throws Exception {
        AlgorithmParameterSpec spec = getInitParams(ctx, alias, userAuthenticationValidityDuration);
        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(getRSAKey(), KEYSTORE_PROVIDER);
        kpGenerator.initialize(spec);
        kpGenerator.generateKeyPair();
    }

    public byte[] encrypt(byte[] buf, String alias) throws Exception {
        return runCipher(Cipher.ENCRYPT_MODE, alias, buf);
    }


    public byte[] decrypt(byte[] buf, String alias) throws Exception {
        return runCipher(Cipher.DECRYPT_MODE, alias, buf);
    }

    protected abstract boolean isEntryAvailable(String alias);

    Key loadKey(int cipherMode, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null, null);

        if (!keyStore.containsAlias(alias)) {
            throw new Exception("KeyStore doesn't contain alias: " + alias);
        }

        Key key;
        switch (cipherMode) {
            case Cipher.ENCRYPT_MODE:
                key = keyStore.getCertificate(alias).getPublicKey();
                if (key == null) {
                    throw new Exception("Failed to load the public key for " + alias);
                }
                break;
            case Cipher.DECRYPT_MODE:
                key = keyStore.getKey(alias, null);
                if (key == null) {
                    throw new Exception("Failed to load the private key for " + alias);
                }
                break;
            default:
                throw new Exception("Invalid cipher mode parameter");
        }
        return key;
    }

    boolean userAuthenticationRequired(String alias) {
        try {
            // Do a quick encrypt/decrypt test
            byte[] encrypted = encrypt(alias.getBytes(), alias);
            decrypt(encrypted, alias);
            return false;
        } catch (InvalidKeyException noAuthEx) {
            return true;
        } catch (Exception e) {
            // Other
            return false;
        }
    }
}

