package com.konsl.fakecall;

import android.os.Handler;
import android.os.Looper;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;

public class FakeConnection extends Connection {
    private boolean isMuted = false;
    private boolean answered = false;
    private boolean rejected = false;

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        if(isMuted == state.isMuted())
            return;

        isMuted = state.isMuted();
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
    }

    @Override
    public void onAbort() {
        super.onAbort();
        setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        destroy();
    }

    @Override
    public void onHold() {
        super.onHold();
        setOnHold();
    }

    @Override
    public void onUnhold() {
        super.onUnhold();
        setActive();
    }

    @Override
    public void onReject() {
        super.onReject();
        _onReject();
    }

    @Override
    public void onReject(int rejectReason) {
        super.onReject(rejectReason);
        _onReject();
    }

    @Override
    public void onReject(String replyMessage) {
        super.onReject(replyMessage);
        _onReject();
    }

    private void _onReject(){
        if(rejected)
            return;

        rejected = true;

        setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        destroy();
    }

    @Override
    public void onAnswer() {
        super.onAnswer();
        _onAnswer();
    }

    @Override
    public void onAnswer(int videoState) {
        super.onAnswer(videoState);
        _onAnswer();
    }

    private void _onAnswer(){
        if(answered)
            return;

        answered = true;

        setConnectionCapabilities(getConnectionCapabilities() | Connection.CAPABILITY_HOLD);
        setAudioModeIsVoip(true);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::setActive, 1000);
    }
}
