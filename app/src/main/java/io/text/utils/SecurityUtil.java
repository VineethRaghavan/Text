package io.text.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import io.text.BuildConfig;

public class SecurityUtil {
    private static final String KEYSTORE_ALIAS = BuildConfig.KEYSTORE_ALIAS;
    private static final String TAG = "Security";

    public KeyPair getKeyPair() {
        KeyPair keyPair = null;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            if (!ks.containsAlias(KEYSTORE_ALIAS)) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC,
                        "AndroidKeyStore"
                );

                KeyGenParameterSpec parameterSpec = new KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .build();
                kpg.initialize(parameterSpec);

                keyPair = kpg.generateKeyPair();
            } else {
                KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) ks.getEntry(KEYSTORE_ALIAS, null);
                keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | KeyStoreException | CertificateException | IOException | UnrecoverableEntryException | UnsupportedOperationException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return keyPair;
    }

    public String getPublicKey() {
        KeyPair keyPair = getKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        return new String(Base64.encode(publicKey.getEncoded(), Base64.DEFAULT));
    }

    public PrivateKey getPrivateKey() {
        KeyPair keyPair = getKeyPair();
        return keyPair.getPrivate();
    }

}