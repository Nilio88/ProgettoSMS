package com.sms1516.porcelli.daniele.wichat;

/**
 * Questa interfaccia verrà usata dal fragment ContactsFragment
 * per astrarre sull'activity che si registrerà per ottenere
 * la notifica della selezione di un contatto dalla lista di contatti
 * presente nel fragment.
 *
 * @author Daniele Porcelli
 */
public interface ContactSelectedListener {

    /**
     * Metodo di callback che notifica il contatto selezionato dalla lista
     * presente nel fragment.
     *
     * @param contact Il contatto selezionato dalla lista del fragment, rappresentato
     *                dal suo indirizzo MAC.
     */
    public void onContactSelected(String contact);
}
