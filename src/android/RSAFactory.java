package com.crypho.plugins;

import android.os.Build;

public class RSAFactory {
    public static AbstractRSA getRSA() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new RSALegacy();
        }
        return new RSA();
    }
}
