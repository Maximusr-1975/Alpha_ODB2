package com.alphaodb2.cordova;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OBD2 Manager - Handles OBD2 communication via Bluetooth
 */
public class OBD2Manager {

    private static final String TAG = "OBD2Manager";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int TIMEOUT = 5000;

    private Context context;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnected = false;
    private boolean isMonitoring = false;
    private Handler monitoringHandler;

    public OBD2Manager(Context context) {
        this.context = context;
        this.monitoringHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Connect to OBD2 device
     */
    public boolean connect(String deviceAddress) {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null || !adapter.isEnabled()) {
                Log.e(TAG, "Bluetooth is not available or not enabled");
                return false;
            }

            BluetoothDevice device = adapter.getRemoteDevice(deviceAddress);
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);

            adapter.cancelDiscovery();
            socket.connect();

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            isConnected = true;

            Log.d(TAG, "Connected to OBD2 device: " + deviceAddress);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Connection failed: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Disconnect from OBD2 device
     */
    public void disconnect() {
        isConnected = false;
        stopMonitoring();

        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage());
        }

        inputStream = null;
        outputStream = null;
        socket = null;
    }

    /**
     * Check if connected to OBD2 device
     */
    public boolean isConnected() {
        return isConnected && socket != null && socket.isConnected();
    }

    /**
     * Initialize OBD2 connection with AT commands
     */
    public boolean initialize() {
        try {
            // Reset all
            sendRawCommand("ATZ");
            Thread.sleep(1000);

            // Echo off
            sendRawCommand("ATE0");
            Thread.sleep(100);

            // Linefeed off
            sendRawCommand("ATL0");
            Thread.sleep(100);

            // Spaces off
            sendRawCommand("ATS0");
            Thread.sleep(100);

            // Headers off
            sendRawCommand("ATH0");
            Thread.sleep(100);

            // Set protocol to auto
            sendRawCommand("ATSP0");
            Thread.sleep(100);

            // Verify connection
            String response = sendCommand("0100");
            return response != null && !response.contains("ERROR");

        } catch (Exception e) {
            Log.e(TAG, "Initialization failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send OBD2 command and get response
     */
    public String sendCommand(String command) {
        if (!isConnected()) {
            Log.e(TAG, "Not connected to OBD2 device");
            return null;
        }

        try {
            // Clear input buffer
            while (inputStream.available() > 0) {
                inputStream.read();
            }

            // Send command
            String cmd = command + "\r";
            outputStream.write(cmd.getBytes());
            outputStream.flush();

            // Read response
            StringBuilder response = new StringBuilder();
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < TIMEOUT) {
                if (inputStream.available() > 0) {
                    int data = inputStream.read();
                    if (data == -1) break;

                    char c = (char) data;
                    if (c == '>') break; // ELM327 prompt
                    response.append(c);
                }
            }

            String result = response.toString().trim();
            result = result.replaceAll("\\s+", ""); // Remove spaces

            Log.d(TAG, "Command: " + command + " Response: " + result);
            return result;

        } catch (IOException e) {
            Log.e(TAG, "Error sending command: " + e.getMessage());
            return null;
        }
    }

    /**
     * Send raw AT command
     */
    private void sendRawCommand(String command) throws IOException {
        String cmd = command + "\r";
        outputStream.write(cmd.getBytes());
        outputStream.flush();
        Thread.sleep(100);

        // Clear response
        while (inputStream.available() > 0) {
            inputStream.read();
        }
    }

    /**
     * Get OBD value for a specific command
     */
    public double getOBDValue(String command) throws Exception {
        String response = sendCommand(command);
        if (response == null || response.contains("ERROR") || response.contains("NODATA")) {
            throw new Exception("Invalid response: " + response);
        }

        return OBD2Command.parseResponse(command, response);
    }

    /**
     * Get multiple PIDs at once
     */
    public JSONObject getMultiplePIDs(JSONArray pids) throws JSONException {
        JSONObject results = new JSONObject();

        for (int i = 0; i < pids.length(); i++) {
            String pid = pids.getString(i);
            try {
                double value = getOBDValue(pid);
                results.put(pid, value);
            } catch (Exception e) {
                results.put(pid, JSONObject.NULL);
            }
        }

        return results;
    }

    /**
     * Get Diagnostic Trouble Codes
     */
    public JSONArray getDTCs() throws JSONException {
        JSONArray dtcArray = new JSONArray();
        String response = sendCommand("03");

        if (response != null && !response.contains("NODATA")) {
            List<String> dtcs = parseDTCs(response);
            for (String dtc : dtcs) {
                dtcArray.put(dtc);
            }
        }

        return dtcArray;
    }

    /**
     * Get Pending DTCs
     */
    public JSONArray getPendingDTCs() throws JSONException {
        JSONArray dtcArray = new JSONArray();
        String response = sendCommand("07");

        if (response != null && !response.contains("NODATA")) {
            List<String> dtcs = parseDTCs(response);
            for (String dtc : dtcs) {
                dtcArray.put(dtc);
            }
        }

        return dtcArray;
    }

    /**
     * Clear DTCs
     */
    public boolean clearDTCs() {
        String response = sendCommand("04");
        return response != null && !response.contains("ERROR");
    }

    /**
     * Get DTC count
     */
    public int getDTCCount() {
        try {
            String response = sendCommand("0101");
            if (response != null && response.length() >= 6) {
                String countHex = response.substring(4, 6);
                return Integer.parseInt(countHex, 16) & 0x7F;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting DTC count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Parse DTC codes from response
     */
    private List<String> parseDTCs(String response) {
        List<String> dtcs = new ArrayList<>();

        try {
            response = response.replaceAll("43|47", ""); // Remove response headers

            for (int i = 0; i < response.length(); i += 4) {
                if (i + 4 <= response.length()) {
                    String code = response.substring(i, i + 4);
                    if (!code.equals("0000")) {
                        dtcs.add(formatDTC(code));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing DTCs: " + e.getMessage());
        }

        return dtcs;
    }

    /**
     * Format DTC code
     */
    private String formatDTC(String hexCode) {
        char[] prefixes = {'P', 'C', 'B', 'U'};
        int firstByte = Integer.parseInt(hexCode.substring(0, 2), 16);
        int prefixIndex = (firstByte >> 6) & 0x03;

        char prefix = prefixes[prefixIndex];
        int digit1 = (firstByte >> 4) & 0x03;
        int digit2 = firstByte & 0x0F;
        String lastTwo = hexCode.substring(2);

        return String.format("%c%d%d%s", prefix, digit1, digit2, lastTwo);
    }

    /**
     * Get VIN
     */
    public String getVIN() {
        String response = sendCommand("0902");
        if (response != null && !response.contains("NODATA")) {
            return parseVIN(response);
        }
        return "Unknown";
    }

    /**
     * Parse VIN from response
     */
    private String parseVIN(String response) {
        try {
            response = response.replaceAll("49020.", "");
            StringBuilder vin = new StringBuilder();

            for (int i = 0; i < response.length(); i += 2) {
                if (i + 2 <= response.length()) {
                    String hex = response.substring(i, i + 2);
                    int decimal = Integer.parseInt(hex, 16);
                    if (decimal >= 32 && decimal <= 126) {
                        vin.append((char) decimal);
                    }
                }
            }

            return vin.toString().trim();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing VIN: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Get Calibration ID
     */
    public String getCalibrationID() {
        String response = sendCommand("0904");
        if (response != null && !response.contains("NODATA")) {
            return parseASCII(response);
        }
        return "Unknown";
    }

    /**
     * Get ECU Name
     */
    public String getECUName() {
        String response = sendCommand("090A");
        if (response != null && !response.contains("NODATA")) {
            return parseASCII(response);
        }
        return "Unknown";
    }

    /**
     * Parse ASCII from hex response
     */
    private String parseASCII(String response) {
        try {
            response = response.replaceAll("49..", "");
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < response.length(); i += 2) {
                if (i + 2 <= response.length()) {
                    String hex = response.substring(i, i + 2);
                    int decimal = Integer.parseInt(hex, 16);
                    if (decimal >= 32 && decimal <= 126) {
                        result.append((char) decimal);
                    }
                }
            }

            return result.toString().trim();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Get supported PIDs
     */
    public JSONArray getSupportedPIDs() throws JSONException {
        JSONArray supported = new JSONArray();

        // Check PIDs 01-20
        String response = sendCommand("0100");
        if (response != null) {
            parseSupportedPIDs(response, 0x01, supported);
        }

        // Check PIDs 21-40
        response = sendCommand("0120");
        if (response != null) {
            parseSupportedPIDs(response, 0x21, supported);
        }

        // Check PIDs 41-60
        response = sendCommand("0140");
        if (response != null) {
            parseSupportedPIDs(response, 0x41, supported);
        }

        return supported;
    }

    /**
     * Parse supported PIDs from response
     */
    private void parseSupportedPIDs(String response, int baseID, JSONArray supported) throws JSONException {
        try {
            response = response.replaceAll("41..", "");
            if (response.length() >= 8) {
                long bitmap = Long.parseLong(response.substring(0, 8), 16);

                for (int i = 0; i < 32; i++) {
                    if ((bitmap & (1L << (31 - i))) != 0) {
                        String pid = String.format("01%02X", baseID + i);
                        supported.put(pid);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing supported PIDs: " + e.getMessage());
        }
    }

    /**
     * Get comprehensive vehicle snapshot
     */
    public JSONObject getVehicleSnapshot() throws JSONException {
        JSONObject snapshot = new JSONObject();

        try {
            snapshot.put("rpm", getOBDValue(OBD2Command.ENGINE_RPM));
        } catch (Exception e) { snapshot.put("rpm", JSONObject.NULL); }

        try {
            snapshot.put("speed", getOBDValue(OBD2Command.VEHICLE_SPEED));
        } catch (Exception e) { snapshot.put("speed", JSONObject.NULL); }

        try {
            snapshot.put("coolantTemp", getOBDValue(OBD2Command.COOLANT_TEMP));
        } catch (Exception e) { snapshot.put("coolantTemp", JSONObject.NULL); }

        try {
            snapshot.put("engineLoad", getOBDValue(OBD2Command.ENGINE_LOAD));
        } catch (Exception e) { snapshot.put("engineLoad", JSONObject.NULL); }

        try {
            snapshot.put("throttle", getOBDValue(OBD2Command.THROTTLE_POS));
        } catch (Exception e) { snapshot.put("throttle", JSONObject.NULL); }

        try {
            snapshot.put("fuelLevel", getOBDValue(OBD2Command.FUEL_LEVEL));
        } catch (Exception e) { snapshot.put("fuelLevel", JSONObject.NULL); }

        snapshot.put("timestamp", System.currentTimeMillis());

        return snapshot;
    }

    /**
     * Monitoring callback interface
     */
    public interface MonitoringCallback {
        void onData(JSONObject data);
        void onError(String error);
    }

    /**
     * Start real-time monitoring
     */
    public void startMonitoring(JSONArray pids, int interval, MonitoringCallback callback) {
        isMonitoring = true;

        monitoringHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isMonitoring) return;

                try {
                    JSONObject data = getMultiplePIDs(pids);
                    data.put("timestamp", System.currentTimeMillis());
                    callback.onData(data);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }

                monitoringHandler.postDelayed(this, interval);
            }
        });
    }

    /**
     * Stop monitoring
     */
    public void stopMonitoring() {
        isMonitoring = false;
        monitoringHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Get freeze frame data
     */
    public JSONObject getFreezeFrameData() throws JSONException {
        JSONObject freezeFrame = new JSONObject();
        String response = sendCommand("0202");

        if (response != null && !response.contains("NODATA")) {
            freezeFrame.put("raw", response);
            // Parse freeze frame data here
        }

        return freezeFrame;
    }

    /**
     * Get fuel system status
     */
    public JSONObject getFuelSystemStatus() throws JSONException {
        JSONObject status = new JSONObject();
        String response = sendCommand("0103");

        if (response != null && response.length() >= 6) {
            String statusHex = response.substring(4, 6);
            int statusCode = Integer.parseInt(statusHex, 16);

            status.put("code", statusCode);
            status.put("description", getFuelSystemDescription(statusCode));
        }

        return status;
    }

    /**
     * Get fuel system status description
     */
    private String getFuelSystemDescription(int code) {
        switch (code) {
            case 1: return "Open loop due to insufficient engine temperature";
            case 2: return "Closed loop, using oxygen sensor feedback";
            case 4: return "Open loop due to engine load OR fuel cut due to deceleration";
            case 8: return "Open loop due to system failure";
            case 16: return "Closed loop, using at least one oxygen sensor but fault";
            default: return "Unknown";
        }
    }

    /**
     * Get OBD standards
     */
    public JSONObject getOBDStandards() throws JSONException {
        JSONObject standards = new JSONObject();
        String response = sendCommand("011C");

        if (response != null && response.length() >= 6) {
            String standardHex = response.substring(4, 6);
            int standardCode = Integer.parseInt(standardHex, 16);

            standards.put("code", standardCode);
            standards.put("description", getOBDStandardDescription(standardCode));
        }

        return standards;
    }

    /**
     * Get OBD standard description
     */
    private String getOBDStandardDescription(int code) {
        switch (code) {
            case 1: return "OBD-II as defined by CARB";
            case 2: return "OBD as defined by EPA";
            case 3: return "OBD and OBD-II";
            case 4: return "OBD-I";
            case 5: return "Not OBD compliant";
            case 6: return "EOBD (Europe)";
            case 7: return "EOBD and OBD-II";
            case 8: return "EOBD and OBD";
            case 9: return "EOBD, OBD and OBD II";
            case 10: return "JOBD (Japan)";
            case 11: return "JOBD and OBD II";
            case 12: return "JOBD and EOBD";
            case 13: return "JOBD, EOBD, and OBD II";
            default: return "Unknown";
        }
    }
}
