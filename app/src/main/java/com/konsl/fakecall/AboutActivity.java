package com.konsl.fakecall;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Arrays;

import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;
import dev.oneuiproject.oneui.layout.AppInfoLayout;

public class AboutActivity extends AppCompatActivity {

    private AppInfoLayout appInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        appInfoLayout = findViewById(R.id.appInfoLayout);
        appInfoLayout.setStatus(AppInfoLayout.NOT_UPDATEABLE);

        appInfoLayout.setMainButtonClickListener(new AppInfoLayout.OnClickListener() {
            @Override
            public void onUpdateClicked(View v) {

            }

            @Override
            public void onRetryClicked(View v) {
                doUpdateCheck();
            }
        });

        doUpdateCheck();
    }

    private void doUpdateCheck() {
        appInfoLayout.setStatus(AppInfoLayout.LOADING);

        Handler handler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                String currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

                try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet("https://api.github.com/repos/" + getString(R.string.github_repo) + "/releases");

                    try (final CloseableHttpResponse response = httpClient.execute(request)) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_PARTIAL_CONTENT ||
                                statusCode == HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION) {
                            JsonArray json = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonArray();

                            if (json.size() == 0)
                                handler.post(() -> appInfoLayout.setStatus(AppInfoLayout.NO_UPDATE));
                            else {
                                String newVersion = json.get(0).getAsJsonObject().get("tag_name").getAsString();
                                if (newVersion.startsWith("v"))
                                    newVersion = newVersion.substring(1);

                                if (compareVersions(newVersion, currentVersion) > 0)
                                    handler.post(() -> appInfoLayout.setStatus(AppInfoLayout.UPDATE_AVAILABLE));
                                else
                                    handler.post(() -> appInfoLayout.setStatus(AppInfoLayout.NO_UPDATE));
                            }
                        }
                    }
                }

            } catch (PackageManager.NameNotFoundException | IOException e) {
                handler.post(() -> appInfoLayout.setStatus(AppInfoLayout.NO_CONNECTION));
            }
        }).start();
    }

    public void showOpenSourceLicenses(View v) {
        //startActivity(new Intent(this, OpenSourceLicensesActivity.class));
    }

    public void openGithub(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://github.com/" + getString(R.string.github_repo)));
        startActivity(i);
    }

    public static int compareVersions(String v1, String v2) {
        int[] v1Parsed = Arrays.stream(v1.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int[] v2Parsed = Arrays.stream(v2.split("\\.")).mapToInt(Integer::parseInt).toArray();

        for (int i = 0; i < Math.min(v1Parsed.length, v2Parsed.length); i++) {
            int cmpResult = Integer.compare(v1Parsed[i], v2Parsed[i]);
            if (cmpResult != 0) return cmpResult;
        }

        return Integer.compare(v1Parsed.length, v2Parsed.length);
    }
}