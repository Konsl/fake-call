package com.konsl.fakecall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.picker.widget.SeslNumberPicker;

import com.konsl.fakecall.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private TelecomManager telecomManager;
    private PhoneAccountHandle handle;

    ActivityResultLauncher<Intent> contactSelectorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent resultIntent = result.getData();
                if (resultIntent == null)
                    return;

                Uri contactUri = resultIntent.getData();

                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                cursor.moveToFirst();

                int columnNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (columnNumber < 0)
                    return;

                onContactSelected(cursor.getString(columnNumber));
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.konsl.fakecall.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        handle = new PhoneAccountHandle(new ComponentName(this, FakeConnectionService.class),
                getApplicationName());
        registerPhoneAccount();

        binding.testBtn.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactSelectorLauncher.launch(i);
        });
    }

    private void registerPhoneAccount() {
        telecomManager.registerPhoneAccount(PhoneAccount.builder(handle, getApplicationName())
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                .build());
    }

    private void onContactSelected(String phoneNumber) {
        SeslNumberPicker timerPicker = new SeslNumberPicker(this);
        timerPicker.setMinValue(0);
        timerPicker.setMaxValue(5);
        timerPicker.setWrapSelectorWheel(false);
        timerPicker.setDisplayedValues(getResources().getStringArray(R.array.timer_lengths));
        timerPicker.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE)
                timerPicker.setEditTextMode(false);
            return false;
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.timer)
                .setView(timerPicker)
                .setPositiveButton(android.R.string.ok, (btn, which) -> startCall(phoneNumber))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void startCall(String phoneNumber) {
        Bundle extras = new Bundle();
        Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, phoneNumber, null);
        extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);

        telecomManager.addNewIncomingCall(handle, extras);
    }

    private String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;

        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }
}
