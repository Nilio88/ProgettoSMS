package com.sms1516.porcelli.daniele.wichat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String LOG_TAG = BootReceiver.class.getName();


        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equalsIgnoreCase("android.intent.action.QUICKBOOT_POWERON")) {
            Log.i(LOG_TAG, "action: " + intent.getAction());
            //Avvia il servizio
            Intent serviceIntent = new Intent(context, WiChatService.class);
            Log.i(LOG_TAG, "Sto avviando WiChatService.");

            context.startService(serviceIntent);
            Log.i(LOG_TAG, "WiChatService avviato.");

        }
    }
}
