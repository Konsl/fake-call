package com.konsl.fakecall;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.konsl.fakecall.history.AppDatabase;
import com.konsl.fakecall.history.HistoryEntry;

import java.time.LocalDateTime;

public class StartCallWorker extends Worker {
    public static final String INPUT_PHONE_NUMBER = "INPUT_PHONE_NUMBER";

    public StartCallWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        TelecomManager telecomManager = MainApplication.getTelecomManager(getApplicationContext());
        PhoneAccountHandle phoneAccountHandle = MainApplication.getPhoneAccountHandle(getApplicationContext());

        String phoneNumber = getInputData().getString(INPUT_PHONE_NUMBER);

        Bundle extras = new Bundle();
        Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, phoneNumber, null);
        extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);

        telecomManager.addNewIncomingCall(phoneAccountHandle, extras);

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.phoneNumber = phoneNumber;
        historyEntry.time = LocalDateTime.now();

        AppDatabase.getDatabase(getApplicationContext())
                .historyDao().append(historyEntry);

        return Result.success();
    }
}
