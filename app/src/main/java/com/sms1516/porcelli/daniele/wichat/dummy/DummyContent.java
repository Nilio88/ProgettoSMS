package com.sms1516.porcelli.daniele.wichat.dummy;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample mac for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Device> ITEMS = new ArrayList<Device>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Device> ITEM_MAP = new HashMap<String, Device>();

    public static final String LOG_TAG = DummyContent.class.getName();

   /* private static final int COUNT = 25;


    static {
        // Add some sample items.

        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }*/



    public static void addItem(Device item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.mac, item);
    }

    public static void removeItem(String mac) {
        for(int i = 0; i < ITEMS.size(); i++) {
            Log.e("ITEM -->", "ITEM " + i + ": " + ITEMS.get(i));
        }
        Log.e("MAC --> " , "REMOVE ITEM: " + mac);
        Log.e("POSIZIONE --> " , "REMOVE ITEM: " + ITEM_MAP.get(mac).position);
        int position = Integer.parseInt(ITEM_MAP.get(mac).position);
        ITEMS.remove(position - 1);
        ITEM_MAP.remove(mac);

    }

    public static Device createDummyItem(int position, String mac, String name, boolean isConnected, int unReadCount) {

        return new Device(String.valueOf(position),mac , name, isConnected, unReadCount);
    }

    public static void changeStateConnection(String mac, boolean state) {
        if(DummyContent.ITEM_MAP.containsKey(mac)) {
            Log.e("MAC -->", "CHANGE STATE CONNECTION: " + mac);
            Log.e("SIZE --> ", "ITEMS_MAP: " + ITEM_MAP.size());
            Log.e("SIZE --> ", "ITEMS: " + ITEMS.size());
            Log.e("POSIZIONE --> ", "CHANGE ITEM: " + ITEM_MAP.get(mac).position);


            Device device = ITEM_MAP.get(mac);
            int position = Integer.parseInt(device.position);

            if (device.connected) {
                ITEM_MAP.put(mac, createDummyItem(position, mac, device.name, state, device.unreadCount));
                ITEMS.set((position - 1), ITEM_MAP.get(mac));
            } else {
                ITEM_MAP.put(mac, createDummyItem(position, mac, device.name, state, device.unreadCount));
                ITEMS.set((position - 1), ITEM_MAP.get(mac));
            }
        }
    }

    public static void updateUnreadCount(String mac, int newCount) {
        Device device = ITEM_MAP.get(mac);
        int position = Integer.parseInt(device.position);
        ITEM_MAP.put(mac, createDummyItem(position, mac, device.name, device.connected, newCount));
        ITEMS.set((position - 1), ITEM_MAP.get(mac));
    }
    /*
    public static void changeStateConnection(int position, Device device) {
        if(device.connected) {
            ITEMS.set(position, createDummyItem(position, device.mac, device.name, Device.DISCONNECTED, device.unreadCount));
            ITEM_MAP.put(device.mac, ITEMS.get(position));
        } else {
            ITEMS.set(position, createDummyItem(position, device.mac, device.name, Device.CONNECTED, device.unreadCount));
            ITEM_MAP.put(device.mac, ITEMS.get(position));
        }
    }*/

    /*public static void updateUnreadCount(int position, Device device, int newCount) {
        ITEMS.set(position, createDummyItem(position, device.mac, device.name, device.connected, newCount));
        ITEM_MAP.put(device.mac, ITEMS.get(position));
    }*/

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of mac.
     */
    public static class Device {
        public final String position;
        public final String mac;
        public final boolean connected;
        public final String name;
        public final int unreadCount;
        public static final boolean CONNECTED = true;
        public static final boolean DISCONNECTED = false;
        public static final String CONNECTED_TEXT = " (Connesso)";


        public static final int COUNT_DEFAULT= 0;


        public Device(String position, String mac, String name, boolean connected,int unreadCount) {
            this.position = position;
            this.mac = mac;
            this.connected = connected;
            this.name = name;
            this.unreadCount = unreadCount;
        }

        @Override
        public String toString() {
            return mac;
        }
    }
}
