package com.konsl.fakecall;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.picker.widget.SeslNumberPicker;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.konsl.fakecall.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

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

        binding.testBtn.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactSelectorLauncher.launch(i);
        });
    }

    private void onContactSelected(String phoneNumber) {
        SeslNumberPicker timerPicker = new SeslNumberPicker(this);
        timerPicker.setMinValue(0);
        timerPicker.setMaxValue(12);
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
                .setPositiveButton(android.R.string.ok, (btn, which) -> WorkManager.getInstance(this).enqueue(
                        new OneTimeWorkRequest.Builder(StartCallWorker.class)
                                .setInitialDelay(Interval.getInterval(timerPicker.getValue()))
                                .setInputData(new Data.Builder()
                                        .putString(StartCallWorker.INPUT_PHONE_NUMBER, phoneNumber)
                                        .build())
                                .build()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
