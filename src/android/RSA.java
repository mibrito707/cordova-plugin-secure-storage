package com.crypho.plugins;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Calendar;

import javax.crypto.Cipher;

public class RSA extends AbstractRSA {

    @TargetApi(Build.VERSION_CODES.M)
    public boolean isEntryAvailable(String alias) {
        try {
            Key privateKey = loadKey(Cipher.DECRYPT_MODE, alias);
            if (privateKey == null) {
                return false;
            }
            KeyFactory factory = KeyFactory.getInstance(privateKey.getAlgorithm(), KEYSTORE_PROVIDER);
            KeyInfo keyInfo = factory.getKeySpec(privateKey, KeyInfo.class);
            return keyInfo.isInsideSecureHardware();
        } catch (Exception e) {
            Log.i(TAG, "Checking encryption keys failed.", e);
            return false;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public AlgorithmParameterSpec getInitParams(Context ctx, String alias, Integer userAuthenticationValidityDuration) {
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, CERT_VALID_YEARS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                .setCertificateNotBefore(Calendar.getInstance().getTime())
                .setCertificateNotAfter(notAfter.getTime())
                .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(userAuthenticationValidityDuration)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .build();
        }
        return null;
    }
}