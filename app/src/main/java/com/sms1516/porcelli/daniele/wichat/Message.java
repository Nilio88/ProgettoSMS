package com.sms1516.porcelli.daniele.wichat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Questa classe rappresentà l'entità messaggio.
 * Un messaggio è definito da un mittente e da
 * un testo.
 *
 * @author Daniele Porcelli
 */
public class Message implements Serializable {

    public String sender;
    public String text;

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Message> ITEMS = new ArrayList<Message>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Message> ITEM_MAP = new HashMap<String, Message>();

    /**
     * Costruttore di Message
     * @param sender Il mittente del messaggio.
     * @param text Il testo che il mittente vuole mandare al suo interlocutore.
     */
    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public static void addItem(Message item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getSender(), item);
    }

    public static void deleteAllMessages() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    /**
     * Metodo accessore che restituisce il mittente del
     * messaggio.
     *
     * @return Il mittente del messaggio.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Metodo accessore che restituisce il testo inviato
     * dal mittente.
     *
     * @return Il messaggio inviato dal mittente.
     */
    public String getText() {
        return text;
    }


}

