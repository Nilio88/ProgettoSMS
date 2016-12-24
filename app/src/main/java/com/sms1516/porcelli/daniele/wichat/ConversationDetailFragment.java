package com.sms1516.porcelli.daniele.wichat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sms1516.porcelli.daniele.wichat.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single Conversation detail screen.
 * This fragment is either contained in a {@link ConversationListActivity}
 * in two-pane mode (on tablets) or a {@link ConversationDetailActivity}
 * on handsets.
 */
public class ConversationDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_POSITION = "item_position";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.Device mItem;

    private ChatRoomRecyclerViewAdapter chatRoomRecyclerViewAdapter = null;
    private MessagesStore mMessagesStore;
    private String mThisDeviceMacAddress;
    private SharedPreferences mMessagesReceivedPrefs;
    private static final String LOG_TAG = ConversationDetailFragment.class.getName();
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mMessagesReceiver;
    private String mContactMacAddress;
    private EditText mMessageEditText;
    private IntentFilter mIntentFilter;
    private int mNumMessaggi;   //Memorizza il numero di messaggi caricati dal file di cronologia di conversazione.
    public static final int SENDER = 0;
    public static final int RECEIVER = 1;
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;
    LinearLayout linearLayoutChat;
    private Snackbar snackbar;
    private static final String KEY_NUM_MESSAGGI_CRONOLOGIA = "num_messaggio_cronologia";
    private static final String KEY_CONTACT_MAC = "contact_mac";
    private static final String KEY_THIS_DEVICE_MAC = "this_device_mac";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMessagesStore = MessagesStore.getInstance();
        mMessagesReceivedPrefs = this.getActivity().getSharedPreferences(CostantKeys.RECEIVED_MESSAGES_PREFS, Context.MODE_PRIVATE);
         if (getArguments().containsKey(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_MAC)) {
            // Load the dummy content specified by the fragment
            // arguments.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(CostantKeys.ACTION_START_CONVERSATION_ACTIVITY_EXTRA_MAC));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.mac);
            }
        }

        if(savedInstanceState == null) {

            mContactMacAddress = mItem.mac;
            Log.i(LOG_TAG, "Indirizzo MAC ottenuto da ConversationListActivity: " + mContactMacAddress);

            Log.i(LOG_TAG, "Recupero l'indirizzo MAC del dispositivo.");
            WifiManager wifiManager = (WifiManager) this.getActivity().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            mThisDeviceMacAddress = info.getMacAddress().toLowerCase();

            Log.i(LOG_TAG, "Indirizzo MAC recuperato: " + mThisDeviceMacAddress);
        }   else {

            //Inserisci qui il codice per recuperare i dati salvati dall'activity
            //tramite il metodo onSaveInstanceState()

            mNumMessaggi = savedInstanceState.getInt(KEY_NUM_MESSAGGI_CRONOLOGIA);
            mThisDeviceMacAddress = savedInstanceState.getString(KEY_THIS_DEVICE_MAC);
            mContactMacAddress = savedInstanceState.getString(KEY_CONTACT_MAC);
        }

        Log.i(LOG_TAG, "In onCreate() mNumMessaggi = " + mNumMessaggi);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this.getActivity());
        mMessagesReceiver = new MessagesReceiver();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CostantKeys.ACTION_SEND_MESSAGE);
        mIntentFilter.addAction(CostantKeys.ACTION_CONTACT_DISCONNECTED);
        mIntentFilter.addAction(CostantKeys.ACTION_CONTACT_AVAILABILITY);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(LOG_TAG, "Sono in onResume() di ConversationDetailFragment.");

        mLocalBroadcastManager.registerReceiver(mMessagesReceiver, mIntentFilter);

        //Recupera la cronologia dei messaggi inviati e ricevuti da questo contatto.
        List<Message> messaggi = mMessagesStore.loadMessagesList(mContactMacAddress);

        WiChatService.registerMessagesListener(this.getContext());

        //Inserisce i messaggi nell'adapter.
        for (int i = mNumMessaggi; i < messaggi.size(); i++) {
            Message.addItem(messaggi.get(i));
            mNumMessaggi++;
        }

        chatRoomRecyclerViewAdapter.notifyDataSetChanged();
        if(chatRoomRecyclerViewAdapter.getItemCount() > 1) {
            //scrolling alla fine della recyclerview
            //recyclerView.getLayoutManager().scrollToPosition(chatRoomRecyclerViewAdapter.getItemCount());
            recyclerView.scrollToPosition(chatRoomRecyclerViewAdapter.getItemCount() - 1);
        }

        Log.i(LOG_TAG, "In onResume() mNumMessaggi = " + mNumMessaggi);

        //Memorizza nelle Shared Preferences il numero dei messaggi letti dal file della cronologia.
        SharedPreferences.Editor editor = mMessagesReceivedPrefs.edit();
        editor.putInt(mContactMacAddress, mNumMessaggi);
        editor.apply();

        //Controlla se il contatto con cui sta comunicando è ancora disponibile
        WiChatService.checkContactAvailable(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.conversation_detail, container, false);
        mMessageEditText = (EditText) rootView.findViewById(R.id.message_et);
        snackbar = Snackbar.make(rootView.findViewById(R.id.coordinatorLayoutChat),
                "Contatto disconnesso!", Snackbar.LENGTH_INDEFINITE);
        linearLayoutChat = (LinearLayout) rootView.findViewById(R.id.linearLayoutChat);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.messages_recycler_view);
        assert recyclerView != null;
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        setupRecyclerView((RecyclerView) recyclerView);

        mMessageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatRoomRecyclerViewAdapter.getItemCount() > 1) {
                    //scrolling alla fine della recyclerview
                    Log.i(LOG_TAG, "Ha cliccato l'EditText");
                    recyclerView.smoothScrollToPosition(chatRoomRecyclerViewAdapter.getItemCount() - 1);
                }
            }
        });
        // Show the dummy content as text in a TextView.
     //   if (mItem != null) {
       //     ((TextView) rootView.findViewById(R.id.conversation_detail)).setText(mItem.mac);
       // }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        ImageButton imgBtnSendMessages = (ImageButton) getActivity().findViewById(R.id.imgBtnSendMessages);
        imgBtnSendMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             sendMessage();
            }
        });
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        chatRoomRecyclerViewAdapter=new ChatRoomRecyclerViewAdapter(Message.ITEMS);
        recyclerView.setAdapter(chatRoomRecyclerViewAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "Sono in onPause() di ConversationActivity.");

        Log.i(LOG_TAG, "In onPause() mNumMessaggi = " + mNumMessaggi);

        List<Message> messaggiNuovi = new ArrayList<>();

        WiChatService.unRegisterMessagesListener(this.getContext());

        //Salva i nuovi messaggi ricevuti nel file della cronologia
        for (int i = mNumMessaggi; i < Message.ITEMS.size(); i++) {

            //Aggiunge il messaggio alla lista di messaggi da salvare.
            //ATTENZIONE: Io ho messo come mittente mContactMacAddress ad ogni messaggio,
            //ma dobbiamo mettere il giusto mittente di ogni messaggio presente nell'adapter.
            messaggiNuovi.add(Message.ITEMS.get(i));
            mNumMessaggi++;
        }

        Log.i(LOG_TAG, "In onPause() dopo il salvataggio dei messaggi, mNumMessaggi = " + mNumMessaggi);

        if (messaggiNuovi.size() > 0)
            mMessagesStore.saveMessagesList(mContactMacAddress, messaggiNuovi);

        mLocalBroadcastManager.unregisterReceiver(mMessagesReceiver);

        //Memorizza nelle Shared Preferences il numero dei messaggi letti dal file della cronologia.
        SharedPreferences.Editor editor = mMessagesReceivedPrefs.edit();
        editor.putInt(mContactMacAddress, mNumMessaggi);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Svuota la memoria dei messaggi
        Message.deleteAllMessages();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_NUM_MESSAGGI_CRONOLOGIA, mNumMessaggi);
        outState.putString(KEY_CONTACT_MAC, mContactMacAddress);
        outState.putString(KEY_THIS_DEVICE_MAC, mThisDeviceMacAddress);
    }

    public void sendMessage() {
        String testo = mMessageEditText.getText().toString();

        if (!testo.isEmpty()) {
            //Visualizza il messaggio appena composto nella lista dei messaggi
            //Crea un'istanza di Message e la invia al Service
            Message messaggio = new Message(mThisDeviceMacAddress, testo);
            Message.addItem(messaggio);
            chatRoomRecyclerViewAdapter.notifyDataSetChanged();
            if(chatRoomRecyclerViewAdapter.getItemCount() > 1) {
                //scrolling alla fine della recyclerview
                recyclerView.smoothScrollToPosition(chatRoomRecyclerViewAdapter.getItemCount() - 1);
            }
            mMessageEditText.setText("");
            Log.i(LOG_TAG, "Messaggio inviato al Service.");
            WiChatService.sendMessage(this.getContext(), messaggio);
        }
    }

    public class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ViewHolder> {

        private final List<Message> mValues;

        public ChatRoomRecyclerViewAdapter(List<Message> items) {
            this.mValues = items;
        }

        @Override
        public ChatRoomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = null;

            //ViewType serve per capire dove inserire il messaggio di chat:
            // a destra per il mittente, a sinistra per il destinatario

            Log.e("VIEWTYPW", "VIEWTYPE" + viewType);
            if(viewType == SENDER) {
                // self message
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_item_self, parent, false);
            } else if(viewType == RECEIVER) {
                // others message
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_item_other, parent, false);
            }
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ChatRoomRecyclerViewAdapter.ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.message.setText(mValues.get(position).getText());
        }

        @Override
        public int getItemViewType(int position) {
            Message message = mValues.get(position);
            if(message.getSender().equals(mThisDeviceMacAddress)){
                return SENDER;
            } else {
                return RECEIVER;
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView message;
            public Message mItem;
            public ViewHolder(View view) {
                super(view);
                mView = view;
                message = (TextView) view.findViewById(R.id.message);
            }
        }
    }

    private class MessagesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(CostantKeys.ACTION_SEND_MESSAGE)) {

                //È stato ricevuto un messaggio: visualizzalo nella recyclerView.
                Log.i(LOG_TAG, "Ricevuto un messaggio, ora lo visualizzo.");

                Message messaggioRicevuto = (Message) intent.getSerializableExtra(CostantKeys.ACTION_SEND_MESSAGE_EXTRA);
                Message.addItem(messaggioRicevuto);

                chatRoomRecyclerViewAdapter.notifyDataSetChanged();
                if(chatRoomRecyclerViewAdapter.getItemCount() > 1) {
                    //scrolling alla fine della recyclerview
                    recyclerView.smoothScrollToPosition(chatRoomRecyclerViewAdapter.getItemCount() - 1);
                }
            }
            else if (action.equals(CostantKeys.ACTION_CONTACT_DISCONNECTED)) {
                Log.i(LOG_TAG, "Il contatto con cui si sta comunicando si è disconnesso.");

                //Il contatto con cui si sta comunicando risulta essersi disconnesso
                //(per via di diversi motivi: si è allontanato troppo, ha disattivato il Wi-Fi,
                //ha spento il dispositivo, etc...).

                String disconnectedDevice = intent.getStringExtra(CostantKeys.ACTION_CONTACT_DISCONNECTED_FOR_CONTACTS_EXTRA);
                //L'intent ACTION_CONTACT_DISCONNECTED non contiene alcun indirizzo MAC.

                String macDisconnected = disconnectedDevice;
                int min = disconnectedDevice.length();
                for(DummyContent.Device d : DummyContent.ITEMS) {
                    int similarity = Utils.getSimilarity(disconnectedDevice, d.mac);
                    if(similarity < min) {
                        min = similarity;
                        macDisconnected = d.mac;
                    }
                }
                DummyContent.changeStateConnection(macDisconnected, DummyContent.Device.DISCONNECTED);
                Toast.makeText(context, "Contatto disconnesso", Toast.LENGTH_LONG).show();
                linearLayoutChat.setVisibility(View.GONE);
                snackbar.show();

                //Non sono ancora certo su cosa fare in questo caso. Vedi un po' tu, Giancosimo,
                //come trattare questa situazione. Chiudere l'activity/fragment è una possibile soluzione,
                //ma è alquanto brusca...

                //finish();
            }
            else if (action.equals(CostantKeys.ACTION_CONTACT_AVAILABILITY)) {
                Log.i(LOG_TAG, "Ricevuta la risposta alla richiesta di disponibilità del contatto.");

                //È arrivata la risposta da parte del Service riguardo alla richiesta
                //di controllare se il contatto sia ancora disponibile.
                //Estrae la risposta dall'intent.
                boolean connesso = intent.getBooleanExtra(CostantKeys.ACTION_CONTACT_AVAILABILITY_EXTRA, false);

                //Così come per ACTION_CONTACT_DISCONNECTED, vedi un po' cosa fare.
                //Ancora una volta, chiudere l'activity/fragment può essere una soluzione ma è
                //alquanto brusca...
                if (!connesso) {

                    //Non c'è bisogno di recuperare l'indirizzo MAC del dispositivo disconnesso poiché il Wi-Fi Direct
                    //permette la connessione con un solo dispositivo alla volta.
                    //String disconnectedDevice = intent.getStringExtra(CostantKeys.ACTION_CONTACT_DISCONNECTED_FOR_CONTACTS_EXTRA);

                    //ATTENZIONE: Qui si verifica un NullPointerException.
                    /*String macDisconnected = disconnectedDevice;
                    int min = disconnectedDevice.length();
                    for(DummyContent.Device d : DummyContent.ITEMS) {
                        int similarity = Utils.getSimilarity(disconnectedDevice, d.mac);
                        if(similarity < min) {
                            min = similarity;
                            macDisconnected = d.mac;
                        }
                    }*/

                    //Nel codice sottostante ho sostituito le occorrenze di macDisconnected con mContactMacAddress
                    DummyContent.changeStateConnection(mContactMacAddress, DummyContent.Device.DISCONNECTED);
                    Toast.makeText(context, "Il contatto non è più connesso", Toast.LENGTH_LONG).show();
                    linearLayoutChat.setVisibility(View.GONE);
                    snackbar.show();
                    Log.i(LOG_TAG, "Il contatto non è più connesso.");
                   // finish();
                }

            }
        }
    }
}
