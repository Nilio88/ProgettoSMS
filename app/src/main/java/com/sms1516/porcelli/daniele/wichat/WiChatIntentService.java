package com.sms1516.porcelli.daniele.wichat;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


/**
 * Nota: questo servizio è stato creato per primo, ma ho scoperto
 * che non fa al caso nostro (infatti serve un servizio che esegua
 * più richieste contemporaneamente). Lo teniamo per sicurezza: lo possiamo
 * usare più in là se ne avremo bisogno.
 *
 * Questo servizio rappresenta il cuore dell'applicazione.
 * Esso ha la responsabilità di attivare il network service discovery
 * per la comunicazione tra due singoli interlocutori e, opzionalmente,
 * attivare un network service discovery per un gruppo.
 * Questo servizio inoltre si mette in ascolto di servizi forniti
 * da altri dispositivi limitrofi e, se tali servizi sono uguali a
 * quelli richiesti dall'applicazione WiChat, i rispettivi dispositivi
 * verranno aggiunti nella lista dei contatti della MainActivity.
 * L'invio e la ricezione dei messaggi avverranno, anch'essi, in
 * questo servizio.
 *
 * @author Daniele Porcelli
 */
public class WiChatIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.sms1516.porcelli.daniele.wichat.action.FOO";
    private static final String ACTION_BAZ = "com.sms1516.porcelli.daniele.wichat.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.sms1516.porcelli.daniele.wichat.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.sms1516.porcelli.daniele.wichat.extra.PARAM2";

    public WiChatIntentService() {
        super("WiChatService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, WiChatIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, WiChatIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
