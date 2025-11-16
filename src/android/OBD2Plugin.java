package com.alphaodb2.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * OBD2 Cordova Plugin
 * Main plugin class that handles JavaScript to Native bridge
 */
public class OBD2Plugin extends CordovaPlugin {

    private static final String TAG = "OBD2Plugin";
    private OBD2Manager obd2Manager;
    private BluetoothManager bluetoothManager;
    private CallbackContext monitoringCallback;

    @Override
    public void pluginInitialize() {
        super.pluginInitialize();
        obd2Manager = new OBD2Manager(cordova.getActivity());
        bluetoothManager = new BluetoothManager(cordova.getActivity());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        // Check Bluetooth permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasBluetoothPermissions()) {
                requestBluetoothPermissions(callbackContext);
                return true;
            }
        }

        switch (action) {
            // Connection Management
            case "scanDevices":
                scanDevices(callbackContext);
                return true;
            case "connect":
                connect(args.getString(0), callbackContext);
                return true;
            case "disconnect":
                disconnect(callbackContext);
                return true;
            case "isConnected":
                isConnected(callbackContext);
                return true;
            case "initialize":
                initialize(callbackContext);
                return true;

            // Engine Parameters
            case "getEngineRPM":
                getEngineRPM(callbackContext);
                return true;
            case "getSpeed":
                getSpeed(callbackContext);
                return true;
            case "getCoolantTemperature":
                getCoolantTemperature(callbackContext);
                return true;
            case "getEngineLoad":
                getEngineLoad(callbackContext);
                return true;
            case "getThrottlePosition":
                getThrottlePosition(callbackContext);
                return true;
            case "getIntakeAirTemperature":
                getIntakeAirTemperature(callbackContext);
                return true;
            case "getMAFAirFlowRate":
                getMAFAirFlowRate(callbackContext);
                return true;
            case "getFuelLevel":
                getFuelLevel(callbackContext);
                return true;
            case "getFuelPressure":
                getFuelPressure(callbackContext);
                return true;
            case "getIntakeManifoldPressure":
                getIntakeManifoldPressure(callbackContext);
                return true;
            case "getTimingAdvance":
                getTimingAdvance(callbackContext);
                return true;
            case "getEngineRuntime":
                getEngineRuntime(callbackContext);
                return true;
            case "getDistanceSinceMIL":
                getDistanceSinceMIL(callbackContext);
                return true;
            case "getBarometricPressure":
                getBarometricPressure(callbackContext);
                return true;
            case "getControlModuleVoltage":
                getControlModuleVoltage(callbackContext);
                return true;
            case "getAmbientAirTemperature":
                getAmbientAirTemperature(callbackContext);
                return true;
            case "getEngineOilTemperature":
                getEngineOilTemperature(callbackContext);
                return true;
            case "getFuelInjectionTiming":
                getFuelInjectionTiming(callbackContext);
                return true;
            case "getFuelRate":
                getFuelRate(callbackContext);
                return true;

            // Oxygen Sensors
            case "getO2Sensor1Voltage":
                getO2Sensor1Voltage(callbackContext);
                return true;
            case "getO2Sensor2Voltage":
                getO2Sensor2Voltage(callbackContext);
                return true;

            // Diagnostic Trouble Codes
            case "getDTCs":
                getDTCs(callbackContext);
                return true;
            case "getPendingDTCs":
                getPendingDTCs(callbackContext);
                return true;
            case "clearDTCs":
                clearDTCs(callbackContext);
                return true;
            case "getDTCCount":
                getDTCCount(callbackContext);
                return true;

            // Vehicle Information
            case "getVIN":
                getVIN(callbackContext);
                return true;
            case "getCalibrationID":
                getCalibrationID(callbackContext);
                return true;
            case "getECUName":
                getECUName(callbackContext);
                return true;

            // Advanced Features
            case "getMultiplePIDs":
                getMultiplePIDs(args.getJSONArray(0), callbackContext);
                return true;
            case "sendCustomCommand":
                sendCustomCommand(args.getString(0), callbackContext);
                return true;
            case "getSupportedPIDs":
                getSupportedPIDs(callbackContext);
                return true;
            case "getVehicleSnapshot":
                getVehicleSnapshot(callbackContext);
                return true;
            case "startMonitoring":
                startMonitoring(args.getJSONArray(0), args.getInt(1), callbackContext);
                return true;
            case "stopMonitoring":
                stopMonitoring(callbackContext);
                return true;
            case "getFreezeFrameData":
                getFreezeFrameData(callbackContext);
                return true;
            case "getFuelSystemStatus":
                getFuelSystemStatus(callbackContext);
                return true;
            case "getOBDStandards":
                getOBDStandards(callbackContext);
                return true;

            default:
                return false;
        }
    }

    // ========== CONNECTION MANAGEMENT ==========

    private void scanDevices(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONArray devices = bluetoothManager.scanDevices();
                callbackContext.success(devices);
            } catch (Exception e) {
                callbackContext.error("Failed to scan devices: " + e.getMessage());
            }
        });
    }

    private void connect(String deviceAddress, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                boolean success = obd2Manager.connect(deviceAddress);
                if (success) {
                    callbackContext.success("Connected successfully");
                } else {
                    callbackContext.error("Connection failed");
                }
            } catch (Exception e) {
                callbackContext.error("Connection error: " + e.getMessage());
            }
        });
    }

    private void disconnect(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                obd2Manager.disconnect();
                callbackContext.success("Disconnected");
            } catch (Exception e) {
                callbackContext.error("Disconnect error: " + e.getMessage());
            }
        });
    }

    private void isConnected(CallbackContext callbackContext) {
        try {
            boolean connected = obd2Manager.isConnected();
            JSONObject result = new JSONObject();
            result.put("connected", connected);
            callbackContext.success(result);
        } catch (JSONException e) {
            callbackContext.error("Error checking connection: " + e.getMessage());
        }
    }

    private void initialize(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                boolean success = obd2Manager.initialize();
                if (success) {
                    callbackContext.success("Initialized successfully");
                } else {
                    callbackContext.error("Initialization failed");
                }
            } catch (Exception e) {
                callbackContext.error("Initialization error: " + e.getMessage());
            }
        });
    }

    // ========== ENGINE PARAMETERS ==========

    private void getEngineRPM(CallbackContext callbackContext) {
        getOBDData(OBD2Command.ENGINE_RPM, "rpm", callbackContext);
    }

    private void getSpeed(CallbackContext callbackContext) {
        getOBDData(OBD2Command.VEHICLE_SPEED, "speed", callbackContext);
    }

    private void getCoolantTemperature(CallbackContext callbackContext) {
        getOBDData(OBD2Command.COOLANT_TEMP, "temperature", callbackContext);
    }

    private void getEngineLoad(CallbackContext callbackContext) {
        getOBDData(OBD2Command.ENGINE_LOAD, "load", callbackContext);
    }

    private void getThrottlePosition(CallbackContext callbackContext) {
        getOBDData(OBD2Command.THROTTLE_POS, "position", callbackContext);
    }

    private void getIntakeAirTemperature(CallbackContext callbackContext) {
        getOBDData(OBD2Command.INTAKE_TEMP, "temperature", callbackContext);
    }

    private void getMAFAirFlowRate(CallbackContext callbackContext) {
        getOBDData(OBD2Command.MAF_FLOW, "rate", callbackContext);
    }

    private void getFuelLevel(CallbackContext callbackContext) {
        getOBDData(OBD2Command.FUEL_LEVEL, "level", callbackContext);
    }

    private void getFuelPressure(CallbackContext callbackContext) {
        getOBDData(OBD2Command.FUEL_PRESSURE, "pressure", callbackContext);
    }

    private void getIntakeManifoldPressure(CallbackContext callbackContext) {
        getOBDData(OBD2Command.INTAKE_PRESSURE, "pressure", callbackContext);
    }

    private void getTimingAdvance(CallbackContext callbackContext) {
        getOBDData(OBD2Command.TIMING_ADVANCE, "advance", callbackContext);
    }

    private void getEngineRuntime(CallbackContext callbackContext) {
        getOBDData(OBD2Command.RUNTIME, "runtime", callbackContext);
    }

    private void getDistanceSinceMIL(CallbackContext callbackContext) {
        getOBDData(OBD2Command.DISTANCE_MIL, "distance", callbackContext);
    }

    private void getBarometricPressure(CallbackContext callbackContext) {
        getOBDData(OBD2Command.BAROMETRIC_PRESSURE, "pressure", callbackContext);
    }

    private void getControlModuleVoltage(CallbackContext callbackContext) {
        getOBDData(OBD2Command.CONTROL_VOLTAGE, "voltage", callbackContext);
    }

    private void getAmbientAirTemperature(CallbackContext callbackContext) {
        getOBDData(OBD2Command.AMBIENT_TEMP, "temperature", callbackContext);
    }

    private void getEngineOilTemperature(CallbackContext callbackContext) {
        getOBDData(OBD2Command.OIL_TEMP, "temperature", callbackContext);
    }

    private void getFuelInjectionTiming(CallbackContext callbackContext) {
        getOBDData(OBD2Command.FUEL_INJECTION_TIMING, "timing", callbackContext);
    }

    private void getFuelRate(CallbackContext callbackContext) {
        getOBDData(OBD2Command.FUEL_RATE, "rate", callbackContext);
    }

    // ========== OXYGEN SENSORS ==========

    private void getO2Sensor1Voltage(CallbackContext callbackContext) {
        getOBDData(OBD2Command.O2_SENSOR_1, "voltage", callbackContext);
    }

    private void getO2Sensor2Voltage(CallbackContext callbackContext) {
        getOBDData(OBD2Command.O2_SENSOR_2, "voltage", callbackContext);
    }

    // ========== DIAGNOSTIC TROUBLE CODES ==========

    private void getDTCs(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONArray dtcs = obd2Manager.getDTCs();
                callbackContext.success(dtcs);
            } catch (Exception e) {
                callbackContext.error("Error getting DTCs: " + e.getMessage());
            }
        });
    }

    private void getPendingDTCs(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONArray dtcs = obd2Manager.getPendingDTCs();
                callbackContext.success(dtcs);
            } catch (Exception e) {
                callbackContext.error("Error getting pending DTCs: " + e.getMessage());
            }
        });
    }

    private void clearDTCs(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                boolean success = obd2Manager.clearDTCs();
                if (success) {
                    callbackContext.success("DTCs cleared");
                } else {
                    callbackContext.error("Failed to clear DTCs");
                }
            } catch (Exception e) {
                callbackContext.error("Error clearing DTCs: " + e.getMessage());
            }
        });
    }

    private void getDTCCount(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                int count = obd2Manager.getDTCCount();
                JSONObject result = new JSONObject();
                result.put("count", count);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Error getting DTC count: " + e.getMessage());
            }
        });
    }

    // ========== VEHICLE INFORMATION ==========

    private void getVIN(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                String vin = obd2Manager.getVIN();
                JSONObject result = new JSONObject();
                result.put("vin", vin);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Error getting VIN: " + e.getMessage());
            }
        });
    }

    private void getCalibrationID(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                String calibrationId = obd2Manager.getCalibrationID();
                JSONObject result = new JSONObject();
                result.put("calibrationId", calibrationId);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Error getting calibration ID: " + e.getMessage());
            }
        });
    }

    private void getECUName(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                String ecuName = obd2Manager.getECUName();
                JSONObject result = new JSONObject();
                result.put("ecuName", ecuName);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Error getting ECU name: " + e.getMessage());
            }
        });
    }

    // ========== ADVANCED FEATURES ==========

    private void getMultiplePIDs(JSONArray pids, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONObject results = obd2Manager.getMultiplePIDs(pids);
                callbackContext.success(results);
            } catch (Exception e) {
                callbackContext.error("Error getting multiple PIDs: " + e.getMessage());
            }
        });
    }

    private void sendCustomCommand(String command, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                String response = obd2Manager.sendCommand(command);
                JSONObject result = new JSONObject();
                result.put("command", command);
                result.put("response", response);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Error sending command: " + e.getMessage());
            }
        });
    }

    private void getSupportedPIDs(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONArray supportedPIDs = obd2Manager.getSupportedPIDs();
                callbackContext.success(supportedPIDs);
            } catch (Exception e) {
                callbackContext.error("Error getting supported PIDs: " + e.getMessage());
            }
        });
    }

    private void getVehicleSnapshot(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONObject snapshot = obd2Manager.getVehicleSnapshot();
                callbackContext.success(snapshot);
            } catch (Exception e) {
                callbackContext.error("Error getting vehicle snapshot: " + e.getMessage());
            }
        });
    }

    private void startMonitoring(JSONArray pids, int interval, CallbackContext callbackContext) {
        monitoringCallback = callbackContext;
        cordova.getThreadPool().execute(() -> {
            try {
                obd2Manager.startMonitoring(pids, interval, new OBD2Manager.MonitoringCallback() {
                    @Override
                    public void onData(JSONObject data) {
                        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                        result.setKeepCallback(true);
                        monitoringCallback.sendPluginResult(result);
                    }

                    @Override
                    public void onError(String error) {
                        PluginResult result = new PluginResult(PluginResult.Status.ERROR, error);
                        result.setKeepCallback(true);
                        monitoringCallback.sendPluginResult(result);
                    }
                });
            } catch (Exception e) {
                callbackContext.error("Error starting monitoring: " + e.getMessage());
            }
        });
    }

    private void stopMonitoring(CallbackContext callbackContext) {
        try {
            obd2Manager.stopMonitoring();
            monitoringCallback = null;
            callbackContext.success("Monitoring stopped");
        } catch (Exception e) {
            callbackContext.error("Error stopping monitoring: " + e.getMessage());
        }
    }

    private void getFreezeFrameData(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONObject freezeFrame = obd2Manager.getFreezeFrameData();
                callbackContext.success(freezeFrame);
            } catch (Exception e) {
                callbackContext.error("Error getting freeze frame data: " + e.getMessage());
            }
        });
    }

    private void getFuelSystemStatus(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONObject status = obd2Manager.getFuelSystemStatus();
                callbackContext.success(status);
            } catch (Exception e) {
                callbackContext.error("Error getting fuel system status: " + e.getMessage());
            }
        });
    }

    private void getOBDStandards(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                JSONObject standards = obd2Manager.getOBDStandards();
                callbackContext.success(standards);
            } catch (Exception e) {
                callbackContext.error("Error getting OBD standards: " + e.getMessage());
            }
        });
    }

    // ========== HELPER METHODS ==========

    private void getOBDData(String command, String key, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                double value = obd2Manager.getOBDValue(command);
                JSONObject result = new JSONObject();
                result.put(key, value);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Error getting OBD data: " + e.getMessage());
            }
        });
    }

    // ========== PERMISSIONS ==========

    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return cordova.hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
                   cordova.hasPermission(Manifest.permission.BLUETOOTH_SCAN);
        }
        return true;
    }

    private void requestBluetoothPermissions(CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
            cordova.requestPermissions(this, 0, permissions);
        }
    }

    @Override
    public void onDestroy() {
        if (obd2Manager != null) {
            obd2Manager.disconnect();
        }
        super.onDestroy();
    }
}
