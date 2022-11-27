package com.konsl.fakecall;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import com.konsl.fakecall.call.FakeConnectionService;

public class MainApplication extends Application {

    private static PhoneAccountHandle phoneAccountHandle = null;
    private static TelecomManager telecomManager = null;

    public static PhoneAccountHandle getPhoneAccountHandle(Context ctx) {
        if (phoneAccountHandle == null)
            phoneAccountHandle = new PhoneAccountHandle(
                    new ComponentName(ctx, FakeConnectionService.class),
                    getApplicationName(ctx));

        return phoneAccountHandle;
    }

    public static TelecomManager getTelecomManager(Context ctx) {
        if (telecomManager == null)
            telecomManager = ctx.getSystemService(TelecomManager.class);

        return telecomManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerPhoneAccount(this);
    }

    private void registerPhoneAccount(Context ctx) {
        getTelecomManager(ctx)
                .registerPhoneAccount(PhoneAccount.builder(
                                getPhoneAccountHandle(ctx),
                                getApplicationName(ctx))
                        .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                        .build());
    }

    private static String getApplicationName(Context ctx) {
        ApplicationInfo applicationInfo = ctx.getApplicationInfo();
        int stringId = applicationInfo.labelRes;

        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : ctx.getString(stringId);
    }
}
