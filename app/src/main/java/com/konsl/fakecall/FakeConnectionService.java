package com.konsl.fakecall;

import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

public class FakeConnectionService extends ConnectionService {

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        FakeConnection connection = new FakeConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_MUTE | Connection.CAPABILITY_SUPPORT_HOLD);

        connection.setInitializing();
        connection.setDialing();
        connection.setAudioModeIsVoip(true);
        connection.setAddress(request.getAddress(), TelecomManager.PRESENTATION_ALLOWED);

        if (!Build.MANUFACTURER.equalsIgnoreCase("Samsung"))
            connection.setInitialized();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            connection.setConnectionCapabilities(connection.getConnectionCapabilities() | Connection.CAPABILITY_HOLD);
            connection.setActive();
        }, 1000);

        return connection;
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        FakeConnection connection = new FakeConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_MUTE | Connection.CAPABILITY_SUPPORT_HOLD);

        connection.setInitializing();
        connection.setAddress(request.getAddress(), TelecomManager.PRESENTATION_ALLOWED);
        connection.setRinging();
        connection.setInitialized();

        return connection;
    }
}
