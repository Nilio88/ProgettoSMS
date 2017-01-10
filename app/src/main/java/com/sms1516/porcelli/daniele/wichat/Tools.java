package com.sms1516.porcelli.daniele.wichat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.view.View;

/**
 * Questa classe serve per contenere alcune operazioni di Material Design etc..
 * Created by Giancosimo on 16/12/2016.
 */
public class Tools {

    /**
     * Il metodo serve per notificare attraverso una Notification che è stato ricevuto un messaggio.
     * @param c
     * @param destActivity
     * @param msg
     * @return
     */
    public Notification notificationMsg(Context c, Class destActivity, String msg) {
        final Intent destIntent = new Intent(c, destActivity);
        final PendingIntent deleteIntent = PendingIntent.getActivity(c, 0, destIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        final Intent selectedIntent = new Intent(c, destActivity);
        final PendingIntent selectedPendingIntent = PendingIntent.getActivity(c, 0, selectedIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

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

        return notification;
    }


    /**
     * Il metodo serve per far comparire un ProgressDialog
     * @param context
     * @param message
     * @return
     */
    public Dialog launchRingDialog(Context context, String message){
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    //Metodo per chiudere un progressDialog
    public void closeRingDialog(Dialog dialog) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public AlertDialog.Builder createAlertDialog(Context context, Drawable icon, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setIcon(icon)
                .setTitle(title)
                .setMessage(msg);
        return builder;
    }

}
