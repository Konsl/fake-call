package com.konsl.fakecall.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.konsl.fakecall.R;
import com.konsl.fakecall.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().setNavigationButtonAsBack();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment_host, new SettingsFragment())
                .commit();
    }
}
