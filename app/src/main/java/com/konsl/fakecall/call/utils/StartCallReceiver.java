package com.konsl.fakecall.call.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.konsl.fakecall.MainApplication;
import com.konsl.fakecall.R;
import com.konsl.fakecall.history.AppDatabase;
import com.konsl.fakecall.history.HistoryEntry;

import java.time.LocalDateTime;

public class StartCallReceiver extends BroadcastReceiver {
    public static final String INPUT_PHONE_NUMBER = "INPUT_PHONE_NUMBER";

    @Override
    public void onReceive(Context context, Intent intent) {
        TelecomManager telecomManager = MainApplication.getTelecomManager(context);
        PhoneAccountHandle phoneAccountHandle = MainApplication.getPhoneAccountHandle(context);

        String phoneNumber = intent.getExtras().getString(INPUT_PHONE_NUMBER);

        Bundle extras = new Bundle();
        Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, phoneNumber, null);
        extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);

        try {
            telecomManager.addNewIncomingCall(phoneAccountHandle, extras);
        } catch (SecurityException e) {
            Log.e("StartCallReceiver", "TelecomManager.addNewIncomingCall failed", e);
        }

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.phoneNumber = phoneNumber;
        historyEntry.time = LocalDateTime.now();

        AppDatabase.getDatabase(context)
                .historyDao().append(historyEntry);
    }
}
