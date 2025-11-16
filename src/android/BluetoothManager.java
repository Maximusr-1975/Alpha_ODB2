package com.alphaodb2.cordova;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Bluetooth Manager for OBD2 device discovery
 */
public class BluetoothManager {

    private static final String TAG = "BluetoothManager";
    private Context context;

    public BluetoothManager(Context context) {
        this.context = context;
    }

    /**
     * Scan for paired Bluetooth devices
     */
    public JSONArray scanDevices() throws JSONException {
        JSONArray devices = new JSONArray();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "Bluetooth adapter not available");
            return devices;
        }

        if (!adapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return devices;
        }

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("name", device.getName() != null ? device.getName() : "Unknown");
                deviceInfo.put("address", device.getAddress());
                deviceInfo.put("type", getDeviceType(device));
                deviceInfo.put("bondState", getBondState(device.getBondState()));

                devices.put(deviceInfo);
                Log.d(TAG, "Found device: " + device.getName() + " (" + device.getAddress() + ")");
            }
        }

        return devices;
    }

    /**
     * Get device type as string
     */
    private String getDeviceType(BluetoothDevice device) {
        int type = device.getType();
        switch (type) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                return "Classic";
            case BluetoothDevice.DEVICE_TYPE_LE:
                return "Low Energy";
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                return "Dual Mode";
            default:
                return "Unknown";
        }
    }

    /**
     * Get bond state as string
     */
    private String getBondState(int state) {
        switch (state) {
            case BluetoothDevice.BOND_BONDED:
                return "Bonded";
            case BluetoothDevice.BOND_BONDING:
                return "Bonding";
            case BluetoothDevice.BOND_NONE:
                return "Not Bonded";
            default:
                return "Unknown";
        }
    }
}
