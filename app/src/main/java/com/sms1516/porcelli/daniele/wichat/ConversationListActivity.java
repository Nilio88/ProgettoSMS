package com.sms1516.porcelli.daniele.wichat;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sms1516.porcelli.daniele.wichat.dummy.DummyContent;

import java.util.List;
import android.os.Handler;

/**
 * An activity representing a list of Contacts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ConversationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ConversationListActivity extends AppCompatActivity

{

    private static final String KEY_NOME_CONTATTO = "nome_contatto";
    private static final String KEY_INDIRIZZO_MAC = "indirizzo_mac";
    private static final String KEY_NUM_MESSAGGI = "num_messaggi";
    private static final String KEY_CONNESSO = "connesso";
    private static final String KEY_FIRSTRUN = "first_run";

    transient private TextView noDeviceText;
    transient private TextView messageDetail;
    transient private FloatingActionButton fab;
    private  Context context = ConversationListActivity.this;
    private transient SimpleItemRecyclerViewAdapter simpleItemRecyclerViewAdapter = null;
    private IntentFilter mIntentFilter;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ContactsMessagesReceiver mContactsMessagesReceiver;
    private MessagesStore mMessagesStore;
    private SharedPreferences mMessagesReceivedPrefs;    //Memorizzerà, per ciascun dispositivo rilevato, il numero di messaggi scambiati con esso.
    private boolean mFirstRun = true;
    public  MessageAlertMenagement messageAlertMenagement;
    private View viewSelected = null;
    ProgressBar progressBar;
    private NotificationManager notificationManager;
    private String connectedTo; //Memorizza l'indirizzo MAC del dispositivo remoto con il quale si è già connessi
    Snackbar snackbar;
    private int posizione = 0;

    //Costanti e variabili per la chiusura della progressBar dopo un determinato tempo
    private static final long MAX_WAIT_PROGRESS_BAR = 10000L; //Il tempo in cui la PB sarà chiusa non avendo rilevato nessun dispositivo
    private static final int CLOSE_PROGRESS_BAR = 1;
    private long mStartTime;
    private boolean mIsDone = false;
    private Tools tools;
    private Dialog dialog;
    private ConversationDetailFragment fragment = null;
    //Costante per il Log
    private static final String LOG_TAG = ConversationListActivity.class.getName();

    //Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Log.i(LOG_TAG, "Sono in onCreate() della MainActivity.");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Imposta la transizione animata tra activity se quest'app viene
        //eseguita su un dispositivo Android 5.0 o più recenti.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementExitTransition(new ChangeBounds());
        }

        tools = new Tools();
        noDeviceText = (TextView) findViewById(R.id.textEmptyList);
        messageDetail = (TextView) findViewById(R.id.messageDetail);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fabRefreshItem);
        View recyclerView = findViewById(R.id.conversation_list);
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayoutMain),
                R.string.msg_wifi_turned_off, Snackbar.LENGTH_INDEFINITE);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        //Metodo, per ottimizzare le performance della recyclerView, che calcola la dimensione
        //delle View una sola volta, essendo in questo caso sempre la stessa.
        ((RecyclerView) recyclerView).setHasFixedSize(true);

        //Aggiunto un ItemDecoration alla recyclerView per separare ogni contatto con una linea
        ((RecyclerView) recyclerView).addItemDecoration(new LineItemDecoration());

        registerForContextMenu(recyclerView);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mContactsMessagesReceiver = new ContactsMessagesReceiver();
        mMessagesReceivedPrefs = getSharedPreferences(CostantKeys.RECEIVED_MESSAGES_PREFS, MODE_PRIVATE);



        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CostantKeys.ACTION_SEND_CONTACT);
        mIntentFilter.addAction(CostantKeys.ACTION_CONTACT_DISCONNECTED_FOR_CONTACTS);
        mIntentFilter.addAction(CostantKeys.ACTION_SEND_MESSAGE_FOR_CONTACTS);
        mIntentFilter.addAction(CostantKeys.ACTION_CONTACT_NOT_AVAILABLE);
        mIntentFilter.addAction(CostantKeys.ACTION_CONNECTED_TO_DEVICE);
        mIntentFilter.addAction(CostantKeys.ACTION_SEND_DISCONNECT_REQUEST);
        mIntentFilter.addAction(CostantKeys.ACTION_CONNECTION_RECEIVED);
        mIntentFilter.addAction(CostantKeys.ACTION_CONTACT_CONNECTED);
        mIntentFilter.addAction(CostantKeys.ACTION_CONNECTION_REFUSED);
        mIntentFilter.addAction(CostantKeys.ACTION_DISCONNECT_SUCCESSFUL);
        mIntentFilter.addAction(CostantKeys.ACTION_WIFI_TURNED_OFF);
        mIntentFilter.addAction(CostantKeys.ACTION_WIFI_TURNED_ON);
        mIntentFilter.addAction(CostantKeys.ACTION_STILL_CONNECTING);
        mIntentFilter.addAction(CostantKeys.ACTION_REBOOT_WIFI);

        if (savedInstanceState != null) {
            mFirstRun = savedInstanceState.getBoolean(KEY_FIRSTRUN);
            fragment = (ConversationDetailFragment) savedInstanceState.getSerializable("FRAGMENT");
            removeFragment();
        }
        else {
            //Cancella i dispositivi rilevati e memorizzati in DummyContent.
            //Questo serve per risolvere il bug che mostra la textView noDeviceText
            //al riavvio di WiChat dopo che ha rilevato almeno un dispositivo remoto.
            DummyContent.ITEM_MAP.clear();
            DummyContent.ITEMS.clear();
        }

        //Avvia WiChatService se non è in esecuzione.
        if (mFirstRun) {
            Class wiChatServiceClass = WiChatService.class;

            if (!isMyServiceRunning(wiChatServiceClass)) {

                //Inizializza il MessagesStore.
                MessagesStore.initialize(this);

                //Avvia WiChatService.
                Intent startWiChatServiceIntent = new Intent(this, wiChatServiceClass);
                MessagesStore.initialize(this);
                startService(startWiChatServiceIntent);
            }
        }

        //Recupera l'istanza di MessagesStore.
        mMessagesStore = MessagesStore.getInstance();

        fab.setVisibility(View.VISIBLE);
        if (findViewById(R.id.conversation_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            noDeviceText.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            fab = (FloatingActionButton) findViewById(R.id.fabRefreshItemW900);
            mTwoPane = true;
        }

        if(!WiChatService.mWifiState) {
            snackbar.show();
            fab.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //Aggiona la lista dei contatti rilevati
                    //Cancella prima la lista dei contatti rilevati.
                    DummyContent.ITEMS.clear();
                    DummyContent.ITEM_MAP.clear();
                    posizione = 0;
                    //  mConnesso.setText("");

                    //Avvia la ricerca dei nuovi contatti
                    progressBar.setVisibility(View.VISIBLE);
                    mIsDone = false;
                    simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                    startTimeProgressBar();
                    WiChatService.discoverServices(context);

                if(DummyContent.ITEMS.isEmpty() && mTwoPane) {
                    messageDetail.setText(R.string.text_empty);
                    noDeviceText.setVisibility(View.GONE);
                } else if(!DummyContent.ITEMS.isEmpty() && !mTwoPane) {
                    noDeviceText.setVisibility(View.GONE);
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "Sono in onStart() di MainActivity.");
        mLocalBroadcastManager.registerReceiver(mContactsMessagesReceiver, mIntentFilter);

        WiChatService.registerContactsListener(this);

        if (mFirstRun) {
            //WiChatService.whoIsConnected(this);
            mFirstRun = false;
        }
        WiChatService.whoIsConnected(this);

        //Invia la richiesta a WiChatService per sapere se il dispositivo si sta connettendo
        //con uno remoto.
        WiChatService.isConnecting(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Cancella le notifiche ricevute nella status bar mentre l'activity era inattiva.
        notificationManager.cancelAll();

        if(DummyContent.ITEMS.isEmpty() && mTwoPane) {
            messageDetail.setText(R.string.text_empty);
            noDeviceText.setVisibility(View.GONE);
        } else if(!DummyContent.ITEMS.isEmpty() && !mTwoPane) {
            noDeviceText.setVisibility(View.GONE);
        }
        simpleItemRecyclerViewAdapter.notifyDataSetChanged();
        Log.i(LOG_TAG, "Sono in onResume() di MainActivity.");
    }

    @Override public void onRestart() {
        super.onRestart();

        Log.i(LOG_TAG, "Sono in onRestart() di MainActivity.");

        if(DummyContent.ITEMS.isEmpty() && mTwoPane) {
            messageDetail.setText(R.string.text_empty);
            noDeviceText.setVisibility(View.GONE);
        } else if(!DummyContent.ITEMS.isEmpty() && !mTwoPane) {
            noDeviceText.setVisibility(View.GONE);
        }
        simpleItemRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "Sono in onPause() di MainActivity.");
        //noDeviceText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "Sono in onStop() di MainActivity.");
        WiChatService.unRegisterContactsListener(this);

        mLocalBroadcastManager.unregisterReceiver(mContactsMessagesReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "Sono in onDestroy() di MainActivity.");

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FIRSTRUN, mFirstRun);
        outState.putSerializable("FRAGMENT", fragment);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        simpleItemRecyclerViewAdapter=new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS);
        recyclerView.setAdapter(simpleItemRecyclerViewAdapter);
    }

    /**
     * Questa classe ha la responsabilità di accedere ai dati e quindi di creare le corrispondenti View,
     * che in questo caso sono rappresentati da un ViewHolder.
     */
    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.Device> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.Device> items) {
            mValues = items;
        }

        /**
         * Questo metodo è responsabile della creazione del particolare ViewHolder.
         *
         * @param parent   indica il riferimento alla ViewGroup in cui la View creata
         *                 dovrà essere inserita dopo l'operazione di inflate.
         * @param viewType indica il tipo di view nel caso in cui vi fosse la necessità
         *                 di creare dei ViewHolder diversi per tipi diversi di dati.
         * @return
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.conversation_list_content, parent, false);
            return new ViewHolder(view);

        }

        /**
         * Questo metodo è responsabile dell'operazione di bind
         *
         * @param holder   è il particolare ViewHolder
         * @param position è la posizione relativa al dato da mostrare
         */
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.mItem = mValues.get(position);
            holder.mIdView.setText("" + (position + 1));
            holder.mContactView.setText(mValues.get(position).name);

            if(mValues.get(position).connected) {
                //stateConnection.setVisibility(View.VISIBLE);
                holder.mStateConnection.setText(mValues.get(position).CONNECTED_TEXT);
                holder.mlistContent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.contact_connected_color));
            } else {
                holder.mStateConnection.setText("");
                holder.mlistContent.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.activity_background));
            }

            if(mValues.get(position).unreadCount > DummyContent.Device.COUNT_DEFAULT) {
                holder.mUnreadCount.setVisibility(View.VISIBLE);
                holder.mUnreadCount.setText("" + mValues.get(position).unreadCount);
            } else {
                holder.mUnreadCount.setVisibility(View.INVISIBLE);
                holder.mUnreadCount.setText("");
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        startConversation(holder.mItem.mac);
                    } else {
                        Context context = v.getContext();
                        viewSelected = holder.mView;
                      //  Intent intent = new Intent(context, ConversationDetailActivity.class);
                      //  intent.putExtra(ConversationDetailFragment.ARG_ITEM_POSITION, holder.mItem.mac);
                        startConversation(holder.mItem.mac);
                    }
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
        }

        /**
         * Questo metodo restituisce il numero di informazioni disponibili.
         *
         * @return
         */
        @Override
        public int getItemCount() {
            return mValues.size();
        }

        /**
         * Questa classe serve per gestire i riferimenti alle varie View che verranno fornite dall'Adapter.
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContactView;
            public final TextView mStateConnection;
            public final TextView mUnreadCount;
            public final RelativeLayout mlistContent;

            public DummyContent.Device mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContactView = (TextView) view.findViewById(R.id.contact);
                mStateConnection = (TextView) view.findViewById(R.id.state_connection);
                mUnreadCount = (TextView) view.findViewById(R.id.num_news_messages);
                mlistContent = (RelativeLayout) view.findViewById(R.id.list_content);
                view.setOnCreateContextMenuListener(this);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContactView.getText() + "'";
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuItem menuItem = menu.add("Cancella messaggi");
                menuItem.setOnMenuItemClickListener(this);
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                cancellaMessaggi(mView, mItem.mac);
                return false;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.disconnect_mitem:
                WiChatService.disconnect(this);

                //Rimuovi la stringa "connesso" dalla TextView connesso_tv
                if(connectedTo != null && !DummyContent.ITEMS.isEmpty()) {
                    DummyContent.changeStateConnection(connectedTo, DummyContent.Device.DISCONNECTED);
                    if(mTwoPane) {
                        removeFragment();
                    }
                    simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                }

                //Messaggio da visualizzare nel Detail Fragment se l'app sta girando su un tablet in landscape
                if (mTwoPane) {
                    messageDetail.setText(R.string.message_detail);
                }

                return true;

            /*case R.id.refresh_service:
                //Riavvia il Service. Questo è necessario
                //poiché le API su cui si basa l'app
                //(WifiP2pManager) non sono completamente
                //affidabili (contengono bug).
                stopService(new Intent(ConversationListActivity.this, WiChatService.class));
                startService(new Intent(ConversationListActivity.this, WiChatService.class));
                return true;*/

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openDetailFragment(String indirizzoMAC) {
        Log.i(LOG_TAG, "Sto per aggiungere il fragment di conversazione.");

        //Azzero il numero dei messaggi ricevuti dal contatto non ancora letti.
        DummyContent.updateUnreadCount(indirizzoMAC, DummyContent.Device.COUNT_DEFAULT);
        simpleItemRecyclerViewAdapter.notifyDataSetChanged();

        Bundle arguments = new Bundle();
        arguments.putString(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_MAC, indirizzoMAC);
        fragment = new ConversationDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.conversation_detail_container, fragment)
                .commit();


    }

    private void removeFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(fragment != null) {
            Log.i(LOG_TAG, "Il fragment non è nullo, quindi lo rimuovo");
            transaction.remove(fragment);
            transaction.commit();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            fragment = null;
        }
    }
    /**
     * Questo metodo non fa altro che avviare l'activity
     * per la conversazione con il dispositivo remoto.
     */
    private void openConversationActivity(View commonView, String nomeContatto, String indirizzoMAC) {
        Log.i(LOG_TAG, "Sto per aprire l'activity di conversazione.");

        //Azzero il numero dei messaggi ricevuti dal contatto non ancora letti.

        DummyContent.updateUnreadCount(indirizzoMAC, DummyContent.Device.COUNT_DEFAULT);
        simpleItemRecyclerViewAdapter.notifyDataSetChanged();

        //Crea l'intent per avviare l'activity di conversazione
        Intent intent = new Intent(this,ConversationDetailActivity.class );
        intent.putExtra(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_NAME, nomeContatto);
        intent.putExtra(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_MAC, indirizzoMAC);

        //Controlla se la view in comune tra le activity è stata passata in questo metodo
        if (commonView != null) {

            //È stato passata la View che le due activity hanno in comune, quindi l'applicazione
            //è in esecuzione su un dispositivo con Android con API Level maggiore o uguale a 21.
            //Di conseguenza avvia l'activity di conversazione con una transizione animata.
            Bundle bundle = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                bundle = ActivityOptions.makeSceneTransitionAnimation(this, commonView, getResources().getString(R.string.contact_transition_name)).toBundle();
            }
            startActivity(intent, bundle);
        }

        else {

            //Avvia l'activity di conversazione senza transizione animata
            startActivity(intent);
        }
    }

    /**
     * Questo metodo verrà invocato ogni volta che si
     * clicca sul contatto con cui si intende comunicare.
     */
    public void startConversation(String macAddress) {
        //Inserisci il codice che recupera l'indirizzo MAC del contatto
        //che è stato cliccato nella recyclerView. Poiché questa classe visualizza solo il
        //dispositivo rilevato più di recente, per memorizzare il suo
        //indirizzo MAC basta solo una variabile (macAddress).
        Log.i(LOG_TAG, "Click sul contatto con cui comunicare.");

        //Inserisci qui il codice per avviare la progress bar
        if (dialog == null || !dialog.isShowing()) {
            dialog = tools.launchRingDialog(context, "Connessione in corso...");
            dialog.show();
        }

        if (macAddress != null)
            WiChatService.connectToClient(this, macAddress);
    }

    /**
     * Metodo invocato dal bottone per cancellare la cronologia dei messaggi
     * del contatto.
     *
     * @param view Il bottone premuto.
     */
    public void cancellaMessaggi(View view, String macAddress) {
        if (macAddress != null) {
            Log.i(LOG_TAG, "Premuto tasto per la cancellazione della cronologia dei messaggi.");

            Message.deleteAllMessages();
            WiChatService.deleteMessages(this, macAddress);

            //Cancella il numero di messaggi ricevuti dal contatto e letti dalle
            //Shared Preferences.
            SharedPreferences.Editor editor = mMessagesReceivedPrefs.edit();
            editor.putInt(macAddress, 0);
            Log.i(LOG_TAG, "Sto cancellando il numero dei messaggi letti dalle shared preferences sotto la chiave: " + macAddress);
            editor.apply();
        }
    }

    private class ContactsMessagesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(CostantKeys.ACTION_WIFI_TURNED_ON)) {

                Log.i(LOG_TAG, "Il dispositivo ha il WI-FI acceso.");
                snackbar.dismiss();

                fab.setVisibility(View.VISIBLE);

            } else if(action.equals(CostantKeys.ACTION_WIFI_TURNED_OFF)) {

                Log.i(LOG_TAG, "Il dispositivo ha il WI-FI spento.");
                progressBar.setVisibility(View.GONE);

                fab.setVisibility(View.INVISIBLE);
                snackbar.show();

                DummyContent.ITEMS.clear();
                DummyContent.ITEM_MAP.clear();
                posizione = 0;
                simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                if(!mTwoPane) {
                    noDeviceText.setVisibility(View.VISIBLE);
                }
            } else if (action.equals(CostantKeys.ACTION_SEND_CONTACT)) {
                if(progressBar.isShown()) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.i(LOG_TAG, "Rilevato un nuovo contatto, ora lo aggiungo.");
                WifiP2pDevice device = intent.getParcelableExtra(CostantKeys.ACTION_SEND_CONTACT_EXTRA);

                if(!isDeviceDetected(DummyContent.ITEMS, device.deviceAddress)) {
                    posizione++;
                    DummyContent.addItem(DummyContent.createDummyItem(posizione, device.deviceAddress,
                            device.deviceName, DummyContent.Device.DISCONNECTED, DummyContent.Device.COUNT_DEFAULT));

                    Log.i(LOG_TAG, "Indirizzo MAC del dispositivo rilevato: " + device.deviceAddress);

                    noDeviceText.setVisibility(View.GONE);

                    //Controlla se il contatto appena rilevato è già connesso con
                    //il nostro dispositivo.
                    if (connectedTo != null && Utils.isMacSimilar(connectedTo, device.deviceAddress)) {

                        //Segnala che il dispositivo remoto appena rilevato è connesso
                        //con quello nostro.

                        DummyContent.changeStateConnection(device.deviceAddress, DummyContent.Device.CONNECTED);

                        //Assegna a connectedTo l'indirizzo MAC del dispositivo già connesso.
                        //Questo permette rappresentare il dispositivo remoto con l'indirizzo MAC
                        //rilevato dal NSD.
                        connectedTo = device.deviceAddress;
                        simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                    }

                    //Messaggio da visualizzare nel Detail Fragment se l'app sta girando su un tablet in landscape
                    if (mTwoPane) {
                        messageDetail.setText(R.string.message_detail);
                    }

                    //Controlla se ci sono messaggi ricevuti da questo contatto ma non ancora letti.
                    /*Log.i(LOG_TAG, "Recupero i messaggi ricevuti e letti nelle shared preferences sotto la chiave: " + device.deviceAddress);

                    int numMessaggi = mMessagesReceivedPrefs.getInt(device.deviceAddress, 0);
                    Log.i(LOG_TAG, "Numero di messaggi ricevuti da questo contatto e letti: " + numMessaggi);

                    int messaggiCronologia = mMessagesStore.getMessagesCount(device.deviceAddress);
                    Log.i(LOG_TAG, "Numero di messaggi presenti nel messagesStore per questo contatto: " + messaggiCronologia);

                    int nuoviMessaggi = messaggiCronologia - numMessaggi;
                    Log.i(LOG_TAG, "Messaggi non ancora letti da questo contatto: " + nuoviMessaggi);

                    if (nuoviMessaggi > 0) {
                        DummyContent.updateUnreadCount(device.deviceAddress, nuoviMessaggi);
                    }
                    simpleItemRecyclerViewAdapter.notifyDataSetChanged();*/
                }

                //Controlla se ci sono messaggi ricevuti da questo contatto ma non ancora letti.
                updateUnreadMessages(device.deviceAddress);


            } else if (action.equals(CostantKeys.ACTION_CONTACT_NOT_AVAILABLE)) {

                //Il dispositivo a cui si sta cercando di connettersi non è più raggiungibile
                Log.i(LOG_TAG, "Il dispositivo con cui si vuole comunicare non è più disponibile.");

                //Inserisci qui il codice per chiudere al progress bar
                tools.closeRingDialog(dialog);
                //Recupera l'indirizzo MAC del dispositivo con cui non si è riusciti a connettere
                String notAvailableDevice = intent.getStringExtra(CostantKeys.ACTION_CONTACT_NOT_AVAILABLE_EXTRA);

                for(int i = 0; i < DummyContent.ITEMS.size(); i++) {
                    Log.i("ITEM -->", "ITEM " + i + ": " + DummyContent.ITEMS.get(i));
                }
                Log.i("MAC -->", "MAC: " + notAvailableDevice);

                //Inserisci qui il codice per eliminare dalla recyclerView il dispositivo con
                //l'indirizzo MAC memorizzato in notAvailableDevice
                if(notAvailableDevice != null) {
                    String nomeContatto = (DummyContent.ITEM_MAP.get(notAvailableDevice)).name;
                    Toast.makeText(ConversationListActivity.this, nomeContatto + " non è più disponibile.", Toast.LENGTH_LONG);
                    DummyContent.removeItem(notAvailableDevice);
                    posizione--;
                }
                //Viene visualizzato un avviso nel caso in cui nessun dispositivo venisse rilevato
                //sia in un tablet in landscape, sia in uno smartphone
                if(DummyContent.ITEMS.isEmpty() && mTwoPane) {
                    messageDetail.setText(R.string.text_empty);
                    noDeviceText.setVisibility(View.GONE);
                } else if(DummyContent.ITEMS.isEmpty() && !mTwoPane) {
                    noDeviceText.setVisibility(View.VISIBLE);
                }

                simpleItemRecyclerViewAdapter.notifyDataSetChanged();

            }

            else if (action.equals(CostantKeys.ACTION_STILL_CONNECTING)) {

                //Controlla se il dispositivo sta ancora eseguendo la connessione
                //con il dispositivo remoto.
                boolean stillConnecting = intent.getBooleanExtra(CostantKeys.ACTION_STILL_CONNECTING_EXTRA, false);

                if (stillConnecting) {

                    //Mostra la progressDialog
                    if (dialog == null || !dialog.isShowing())
                    dialog = tools.launchRingDialog(ConversationListActivity.this, "Connessione in corso...");
                    dialog.show();
                }
                else {

                    //Chiudi la progressDialog
                    if (dialog != null && dialog.isShowing()) {
                        tools.closeRingDialog(dialog);
                    }

                    //Aggiorna lo stato della connessione con il dispositivo remoto.
                    if (connectedTo != null) {
                        if (DummyContent.ITEMS.size() > 0)
                            DummyContent.changeStateConnection(connectedTo, DummyContent.Device.CONNECTED);
                    }
                    else {
                        //Il contatto si è disconnesso mentre WiChat era in onStop().
                        //Aggiorna il contatto: toglie "connesso" dall'item della recyclerView.
                        for (DummyContent.Device device: DummyContent.ITEMS) {

                            if (device.connected) {
                                //Rimuovi "connesso".
                                DummyContent.changeStateConnection(device.mac, DummyContent.Device.DISCONNECTED);

                                //Poiché può essere connesso solo un dispositivo alla volta,
                                //non c'è più bisogno di scandire la lista ITEMS e il ciclo può
                                //interrompersi qui.
                                break;
                            }
                        }
                    }
                    simpleItemRecyclerViewAdapter.notifyDataSetChanged();

                    //Avvia la ricerca dei dispositivi nelle vicinanze.
                    WiChatService.discoverServices(context);
                    Log.i(LOG_TAG, "Avviata la scansione dei dispositivi.");
                }
            }

            else if (action.equals(CostantKeys.ACTION_CONNECTED_TO_DEVICE)) {

                //La connessione con il dispositivo remoto è stata stabilita con successo o era già stata stabilita
                Log.i(LOG_TAG, "Il dispositivo remoto è disponibile per comunicare.");

                String mac = intent.getStringExtra(CostantKeys.ACTION_CONNECTED_TO_DEVICE_EXTRA);

                //Inserisci qui il codice per indicare nella recyclerView il contatto
                //con cui si è appena connessi.
                //(Nel mio caso, semplicemente aggiungo la stringa "(Connesso)" ma tu
                //crea qualcosa di più visivo, come cambiare il suo sfondo in blu...)

                tools.closeRingDialog(dialog);
                //ATTENZIONE: Ho appena scoperto che questo codice, oltre ad essere inutile, crea anche un bug
                //e quindi verrà commentato. Sotto di esso verrà scritto il codice più idoneo al
                //compito (userò solo Utils.isMacSimilar() ).
                //Nota: devi scandire l'intera recyclerView e confrontare ciascun indirizzo MAC
                //con quello recuperato dall'intent tramite la funzione Utils.getSimilarity().
                //Memorizza ogni risultato che ottieni dal confronto con ciascun indirizzo MAC
                //nella recyclerView e indica come contatto connesso quello che ha ottenuto il
                //risultato più basso.
                //Poiché io visualizzo solo il contatto rilevato più di recente, mi arrangio con
                //la funzione Utils.isMacSimilar().

                /*String macConnected = mac;
                int min = mac.length();
                for(DummyContent.Device d : DummyContent.ITEMS) {
                    int similarity = Utils.getSimilarity(mac, d.mac);
                    if(similarity < min) {
                        min = similarity;
                        macConnected = d.mac;
                    }
                }*/

                boolean trovato = false;
                String macConnected = mac;
                for (DummyContent.Device device: DummyContent.ITEMS) {
                    if (Utils.isMacSimilar(device.mac, mac)) {
                        trovato = true;
                        macConnected = device.mac;
                        break;
                    }
                }

                if (trovato)
                    DummyContent.changeStateConnection(macConnected, DummyContent.Device.CONNECTED);

                connectedTo = macConnected;
                simpleItemRecyclerViewAdapter.notifyDataSetChanged();


                if(!mTwoPane) {
                    //Inserisci qui il codice per ottenere il nome del contatto e la sua View
                    //dalla recyclerView sfruttando l'indirizzo MAC appena estratto dall'intent
                    //(la variabile locale "mac").

                    String nomeContatto = DummyContent.ITEM_MAP.get(macConnected).name;

                    //Avvio l'activity di conversazione.
                    openConversationActivity(viewSelected, nomeContatto, connectedTo);
                } else if(mTwoPane) {
                    messageDetail.setText("");
                    openDetailFragment(connectedTo);
                }
            } else if (action.equals(CostantKeys.ACTION_CONTACT_DISCONNECTED_FOR_CONTACTS)) {
                Log.i(LOG_TAG, "Un contatto si è disconnesso.");
                //Un contatto con cui si aveva una connessione stabilita con esso non è più reperibile
                //(si è allontanato troppo, ha disattivato il Wi-Fi, ha spento il dispositivo, etc...).
                String disconnectedDevice = intent.getStringExtra(CostantKeys.ACTION_CONTACT_DISCONNECTED_FOR_CONTACTS_EXTRA);

                //Inserisci qui il codice per rimuovere dalla recyclerView il dispositivo che si è appena
                //disconnesso (usando l'indirizzo MAC memorizzato in disconnectedDevice).

                String macDisconnected = disconnectedDevice;
                int min = disconnectedDevice.length();
                for(DummyContent.Device d : DummyContent.ITEMS) {
                    int similarity = Utils.getSimilarity(disconnectedDevice, d.mac);
                    if(similarity < min) {
                        min = similarity;
                        macDisconnected = d.mac;
                    }
                }

                if(!DummyContent.ITEMS.isEmpty()) {
                    DummyContent.removeItem(macDisconnected);
                }
                posizione--;
                connectedTo = null;

                //Viene visualizzato un avviso nel caso in cui nessun dispositivo venisse rilevato
                //sia in un tablet in landscape, sia in uno smartphone
                if(DummyContent.ITEMS.isEmpty() && mTwoPane) {
                    messageDetail.setText(R.string.text_empty);
                    noDeviceText.setVisibility(View.GONE);
                } else if(DummyContent.ITEMS.isEmpty() && !mTwoPane) {
                    noDeviceText.setVisibility(View.VISIBLE);
                }
                simpleItemRecyclerViewAdapter.notifyDataSetChanged();

            } else if (action.equals(CostantKeys.ACTION_SEND_MESSAGE_FOR_CONTACTS)) {
                Log.i(LOG_TAG, "Ricevuto un messaggio in MainActivity.");

                //Un contatto ha inviato un messaggio a questo dispositivo ma l'activity di
                //conversazione non era attiva.

                //Recupera il messaggio
                Message message = (Message) intent.getSerializableExtra(CostantKeys.ACTION_SEND_MESSAGE_EXTRA);

                //Inserisci qui il codice che, in base al mittente del messaggio (che è l'indirizzo MAC
                //del dispositivo ottenuto invocando il metodo getSender() della classe Message),
                //incrementa il numero di messaggi non  letti ricevuti dal mittente visualizzato
                //nel recyclerView. Per identificare la View del recyclerView da modificare, serviti
                //del metodo Utils.getSimilarity() e confronta la similarità degli indirizzi  MAC
                //del recyclerView con quello presente nel messaggio ricevuto. Quello che ottiene
                //il punteggio più basso è, molto probabilmente, il contatto da incrementare i messaggi
                //non ancora letti.
                // Non è indispensabile: se ci riusciamo a implementare questa
                //caratteristica è bene, altrimenti non importa.

                String macMittente = message.getSender();
                int min = macMittente.length();
                for(DummyContent.Device d : DummyContent.ITEMS) {
                    int similarity = Utils.getSimilarity(macMittente, d.mac);
                    if(similarity < min) {
                        min = similarity;
                        macMittente = d.mac;
                    }
                }
                DummyContent.updateUnreadCount(macMittente, DummyContent.ITEM_MAP.get(macMittente).unreadCount + 1);
                simpleItemRecyclerViewAdapter.notifyDataSetChanged();

            } else if(action.equals(CostantKeys.ACTION_SEND_DISCONNECT_REQUEST)) {

                Log.i(LOG_TAG, "Richiesta di connessione con un'altro dispositivo.");
                //Recupera l'indirizzo MAC del dispositivo remoto con cui bisogna disconnettersi
                final String deviceToDisconnect = intent.getStringExtra(CostantKeys.ACTION_SEND_DISCONNECT_REQUEST_EXTRA);
                final String deviceToConnect = intent.getStringExtra(CostantKeys.ACTION_CONNECTED_TO_DEVICE_EXTRA);
                //Qui bisogna far apparire una finestra di dialogo che indica che per connettersi con il contatto
                //selezionato, bisogna prima disconnettersi dal contatto attuale. Se l'utente preme "Si", allora
                //viene mandata la richiesta di disconnessione al Service, altrimenti tutto rimane come è.

                AlertDialog.Builder builder = tools.createAlertDialog(ConversationListActivity.this.context, ContextCompat.getDrawable(context, R.drawable.disconnect_icon_24px), getString(R.string.title_disconnection_alert_dialog)
                        , getString(R.string.msg_disconnection_alert_dialog));

                builder.setPositiveButton(getString(R.string.positive_button)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Chiude la progressDialog
                                if (ConversationListActivity.this.dialog != null && ConversationListActivity.this.dialog.isShowing()) {
                                    tools.closeRingDialog(ConversationListActivity.this.dialog);
                                    Log.i(LOG_TAG, "ProgressDialog chiusa.");
                                }

                                WiChatService.disconnect(getApplicationContext());

                                //Rimuovi la stringa "connesso" dalla TextView connesso_tv
                                if(deviceToDisconnect != null && !DummyContent.ITEMS.isEmpty()) {
                                    DummyContent.changeStateConnection(deviceToDisconnect, DummyContent.Device.DISCONNECTED);
                                    simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                                }

                                //Messaggio da visualizzare nel Detail Fragment se l'app sta girando su un tablet in landscape
                                if (mTwoPane) {
                                    messageDetail.setText(R.string.message_detail);
                                }
                            }
                        });
                builder.setNegativeButton(getString(R.string.negative_button)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Chiudi la progressDialog se è aperta
                                Log.i(LOG_TAG, "Hai cliccato su No. Chiudo la progressDialog.");
                                if (ConversationListActivity.this.dialog != null && ConversationListActivity.this.dialog.isShowing()) {
                                    tools.closeRingDialog(ConversationListActivity.this.dialog);
                                    Log.i(LOG_TAG, "ProgressDialog chiusa.");
                                }
                            }
                        });
                builder.create().show();
            } else if (action.equals(CostantKeys.ACTION_CONNECTION_RECEIVED)) {

                //Questo intent indica la riuscita connessione di un dispositivo remoto
                //al nostro dispositivo. Quindi segnala nel recyclerView quale dispositivo remoto
                //si è connesso al nostro confrontando gli indirizzi MAC del recyclerView con
                //quello presente nell'intent ricevuto.
                String remoteDevice = intent.getStringExtra(CostantKeys.ACTION_CONNECTION_RECEIVED_EXTRA);
                Log.e(LOG_TAG, "REMOTE DEVICE --> " + remoteDevice);

                tools.closeRingDialog(dialog);

                //Ora confronta l'indirizzo MAC apppena ricavato con ciascun indirizzo MAC
                //presente nel recyclerView tramite la funzione Utils.getSimilarity(). Quindi
                //segnala come connesso il dispositivo nel recyclerView che ha ottenuto il risultato
                //più basso.
                if(remoteDevice != null) {
                    String macConnected = remoteDevice;
                    int min = remoteDevice.length();
                    for (DummyContent.Device d : DummyContent.ITEMS) {
                        int similarity = Utils.getSimilarity(remoteDevice, d.mac);
                        if (similarity < min) {
                            min = similarity;
                            macConnected = d.mac;
                        }
                    }

                    if(!DummyContent.ITEMS.isEmpty()) {
                        DummyContent.changeStateConnection(macConnected, DummyContent.Device.CONNECTED);
                        connectedTo = macConnected;
                    }
                    simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                }
            } else if (action.equals(CostantKeys.ACTION_CONTACT_CONNECTED)) {

                //Questo intent viene inviato dalla classe Service per informare l'activity
                //quale contatto risulta essere connesso a questo dispositivo tramite il Wi-Fi Direct.
                connectedTo = intent.getStringExtra(CostantKeys.ACTION_CONTACT_CONNECTED_EXTRA);
                progressBar.setVisibility(View.VISIBLE);
                mIsDone = false;

                if (connectedTo != null) {
                    //Nota: questo codice può portare dei bug. Verrà commentato e sostituito.
                    /*int min = connectedTo.length();
                    for (DummyContent.Device d : DummyContent.ITEMS) {
                        int similarity = Utils.getSimilarity(connectedTo, d.mac);
                        if (similarity < min) {
                            min = similarity;
                            connectedTo = d.mac;
                        }
                    }*/
                    for (DummyContent.Device device: DummyContent.ITEMS) {
                        if (Utils.isMacSimilar(connectedTo, device.mac))
                            connectedTo = device.mac;
                    }
                }

                startTimeProgressBar();
                //WiChatService.discoverServices(context);

            }  else if (action.equals(CostantKeys.ACTION_CONNECTION_REFUSED)) {

                //Il contatto con cui si vuole conversare non ha accettato la richiesta
                //di connessione Wi-Fi Direct. Avvisa l'utente.
                //Inserisci qui il codice che interrompe la progress bar e visualizza il messaggio
                //che comunica il rifiuto della connessione (preferibilmente in una dialogue).
                Log.i(LOG_TAG, "Ho ricevuto l'intent ACTION_CONNECTION_REFUSED.");
                tools.closeRingDialog(dialog);
                Toast.makeText(context, "Connessione rifiutata.", Toast.LENGTH_SHORT).show();

                //Ritorna a cercare i dispositivi nelle vicinanze.
                progressBar.setVisibility(View.VISIBLE);
                mIsDone = false;
                startTimeProgressBar();
                WiChatService.discoverServices(context);

            }  else if (action.equals(CostantKeys.ACTION_DISCONNECT_SUCCESSFUL)) {

                //Intent ricevuto dopo aver premuto su "Disconnetti" se la disconnessione
                //è avvenuta con successo.
                connectedTo = null;
            }
            else if (action.equals(CostantKeys.ACTION_REBOOT_WIFI)) {

                //Intent ricevuto quando il Wi-Fi P2P Manager della classe WiChatService
                //segnala un errore durante una delle sue operazioni (principalmente durante
                //l'esecuzione di discoverServices() ).

                //Inserisci qui il codice che mostra una alertDialog che invita l'utente a
                //riavviare il Wi-Fi del proprio dispositivo nella schermata delle impostazioni.
            }
        }
    }

    /**
     * Il metodo serve per controllare se un contatto rilevato esiste già nell'array collegato con l'Adaptor
     * @param array collegato con l'Adaptor
     * @param mac da controllare se esiste già nell'array
     * @return true se il mac è stato trovato nell'array
     */
    private boolean isDeviceDetected(List<DummyContent.Device> array, String mac) {
        boolean detected = false;
        for(int i = 0; i  < array.size(); i++){
            if(mac.equalsIgnoreCase(array.get(i).mac)) {
                detected = true;
            }
        }
        return detected;
    }

    // Metodo per far chiudere la progressBar dopo un certo tempo se non è stato trovato alcun dispositivo.
    private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case CLOSE_PROGRESS_BAR:
                long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                if (elapsedTime >= MAX_WAIT_PROGRESS_BAR && !mIsDone) {
                    Log.i(LOG_TAG, "Tempo scaduto per la ProgressBar");
                    mIsDone = true;
                    if(progressBar.isShown()) {
                        Log.i(LOG_TAG, "ProgressBar terminata");
                        progressBar.setVisibility(View.GONE);
                    }

                    //Mostra la textView indicante che non è stato trovato alcun
                    //dispositivo se la lista di DummyContent è vuota
                    if (DummyContent.ITEMS.isEmpty()) {
                        noDeviceText.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }
};

    // Metodo per far partire il conteggio del tempo di durata massima della progressBar all'handleMessage.
    private void startTimeProgressBar() {
        mStartTime = SystemClock.uptimeMillis();
        final android.os.Message closeProgressBar = mHandler.obtainMessage(CLOSE_PROGRESS_BAR);
        mHandler.sendMessageAtTime(closeProgressBar, mStartTime + MAX_WAIT_PROGRESS_BAR);
    }

    /**
     * Aggiorna il numero di messaggi ricevuti e non ancora letti da parte
     * del dispositivo remoto.
     *
     * @param device L'indirizzo MAC del dispositivo remoto di cui si vuole verificare
     *               la presenza di messaggi ricevuti e non ancora letti.
     */
    private void updateUnreadMessages(String device) {
        //Controlla se ci sono messaggi ricevuti da questo contatto ma non ancora letti.
        Log.i(LOG_TAG, "Recupero i messaggi ricevuti e letti nelle shared preferences sotto la chiave: " + device);

        int numMessaggi = mMessagesReceivedPrefs.getInt(device, 0);
        Log.i(LOG_TAG, "Numero di messaggi ricevuti da questo contatto e letti: " + numMessaggi);

        int messaggiCronologia = mMessagesStore.getMessagesCount(device);
        Log.i(LOG_TAG, "Numero di messaggi presenti nel messagesStore per questo contatto: " + messaggiCronologia);

        int nuoviMessaggi = messaggiCronologia - numMessaggi;
        Log.i(LOG_TAG, "Messaggi non ancora letti da questo contatto: " + nuoviMessaggi);

        if (nuoviMessaggi > 0) {
            DummyContent.updateUnreadCount(device, nuoviMessaggi);
        }
        simpleItemRecyclerViewAdapter.notifyDataSetChanged();
    }

}
