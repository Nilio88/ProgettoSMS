package com.sms1516.porcelli.daniele.wichat;

import java.io.Serializable;

/**
 * Questa interfaccia verrà usata dal servizio WiChatService
 * per astrarre l'attività che si registrerà come listener dei
 * messaggi ricevuti tramite il network service discovery.
 *
 * @author Daniele Porcelli
 */
public interface MessagesListener extends Serializable {

    /**
     * Metodo di callback invocato quando viene ricevuto
     * un nuovo messaggio.
     *
     * @param message Il messaggio ricevuto.
     */
    public void onMessageReceived(Message message);

    /**
     * Metodo accessore che permette di ottenere il destinatario
     * dei messaggi inviati dall'activity che implementa questa
     * interfaccia.
     *
     * @return Il nome del destinatario dei messaggi.
     */
    public String getRecipient();

    /**
     * Metodo di callback invocato quando il dispositivo remoto
     * con cui si sta comunicando si è disconnesso (fuori portata,
     * Wi-Fi spento o dispositivo spento).
     */
    public void onContactDisconnected();
}