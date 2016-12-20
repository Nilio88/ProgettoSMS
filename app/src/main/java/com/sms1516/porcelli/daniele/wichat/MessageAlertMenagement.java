package com.sms1516.porcelli.daniele.wichat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

/**
 * Created by Giancosimo on 05/11/2016.
 */
public class MessageAlertMenagement {

    private  ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private  Context context;
    public static final String FINDING_DEVICE = "Finding device.";
    private String LOG_TAG = "MessageAlertMenagement";

    public MessageAlertMenagement(Context c){
        this.context = c;
    }

    public MessageAlertMenagement() {

    }

    /**
     * Il metodo serve per far partire un progressDialog durante la scansione dei dispositivi
     * vicini.
     * @param msg il messaggio da visualizzare
     */
    public  void onInitiateDiscovery(String msg) {
        progressDialog = ProgressDialog.show(context, "Press back to cancel", msg, true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                });
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public  void closeProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    /**
     * Il metodo serve per far comparire una finestra di dialogo che chiede all'utente se vuole connettersi
     * con il dispositivo da lui selezionato.
     * @param name il nome del dispositivo
     * @return

    public Dialog onCreateDialog(String name) {
        Log.d(LOG_TAG, "Finestra di dialogo");
        builder = new AlertDialog.Builder(context)
                .setTitle("Connettersi a questo dispositivo?")
                .setMessage("Name: "+ name + "\nMAC: " + mac)
                .setCancelable(false)
                .setPositiveButton("Connetti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceActionListener.connect(mac, v, holder);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "Connessione annullata");
                    }
                });
        return builder.create();
    }
     */
}
