package com.sms1516.porcelli.daniele.wichat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.view.View;

/**
 * Questa classe serve per contenere alcune operazioni di Material Design etc..
 * Created by Giancosimo on 16/12/2016.
 */
public class Tools {



    public void notificationMsg(Context c, Class destActivity, String msg) {
        final Intent destIntent = new Intent(c, destActivity);
        final PendingIntent deleteIntent = PendingIntent.getActivity(c, 0, destIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        final Intent selectedIntent = new Intent(c, destActivity);
        final PendingIntent selectedPendingIntent = PendingIntent.getActivity(c, 0, selectedIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(c)
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setDeleteIntent(deleteIntent)
                .setContentIntent(selectedPendingIntent)
                .setContentText(msg)
                .setContentTitle("WiChat: Nuovo messaggio").build();

        // default phone settings for notifications
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;

        // show scrolling text on status bar when notification arrives
        notification.tickerText = "Nuovo messaggio ricevuto";

        notificationManager.notify(CostantKeys.NEW_MESSAGE_NOTIFICATION, notification);
    }
}
