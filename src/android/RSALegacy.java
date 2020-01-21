package com.crypho.plugins;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

public class RSALegacy extends AbstractRSA {

    @Override
    public boolean isEntryAvailable(String alias) {
        try {
            return loadKey(Cipher.ENCRYPT_MODE, alias) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public AlgorithmParameterSpec getInitParams(Context ctx, String alias, Integer userAuthenticationValidityDuration) throws Exception {
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, CERT_VALID_YEARS);

        return new KeyPairGeneratorSpec.Builder(ctx)
            .setAlias(alias)
            .setSubject(new X500Principal(String.format("CN=%s, OU=%s", alias, ctx.getPackageName())))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(Calendar.getInstance().getTime())
            .setEndDate(notAfter.getTime())
            .setEncryptionRequired()
            .setKeySize(2048)
            .setKeyType(getRSAKey())
            .build();
    }
}