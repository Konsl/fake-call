package com.konsl.fakecall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import dev.oneuiproject.oneui.layout.AppInfoLayout;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AppInfoLayout appInfoLayout = findViewById(R.id.appInfoLayout);
        appInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);
    }

    private void doUpdateCheck() {
        // https://api.github.com/repos/Konsl/youtube-pitch-control/releases
    }

    public void showOpenSourceLicenses(View v) {
        //startActivity(new Intent(this, OpenSourceLicensesActivity.class));
    }

    public void openGithub(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://github.com/" + getString(R.string.github_repo)));
        startActivity(i);
    }
}