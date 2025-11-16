package com.alphaodb2.cordova;

import android.util.Log;

/**
 * OBD2 Command definitions and response parsers
 * Contains all PID commands and their parsing logic
 */
public class OBD2Command {

    private static final String TAG = "OBD2Command";

    // Mode 01 - Show current data
    public static final String ENGINE_RPM = "010C";
    public static final String VEHICLE_SPEED = "010D";
    public static final String COOLANT_TEMP = "0105";
    public static final String ENGINE_LOAD = "0104";
    public static final String THROTTLE_POS = "0111";
    public static final String INTAKE_TEMP = "010F";
    public static final String MAF_FLOW = "0110";
    public static final String FUEL_LEVEL = "012F";
    public static final String FUEL_PRESSURE = "010A";
    public static final String INTAKE_PRESSURE = "010B";
    public static final String TIMING_ADVANCE = "010E";
    public static final String RUNTIME = "011F";
    public static final String DISTANCE_MIL = "0121";
    public static final String BAROMETRIC_PRESSURE = "0133";
    public static final String CONTROL_VOLTAGE = "0142";
    public static final String AMBIENT_TEMP = "0146";
    public static final String OIL_TEMP = "015C";
    public static final String FUEL_INJECTION_TIMING = "015D";
    public static final String FUEL_RATE = "015E";

    // Oxygen Sensors
    public static final String O2_SENSOR_1 = "0114";
    public static final String O2_SENSOR_2 = "0115";
    public static final String O2_SENSOR_3 = "0116";
    public static final String O2_SENSOR_4 = "0117";

    // Additional PIDs
    public static final String SHORT_TERM_FUEL_TRIM_1 = "0106";
    public static final String LONG_TERM_FUEL_TRIM_1 = "0107";
    public static final String SHORT_TERM_FUEL_TRIM_2 = "0108";
    public static final String LONG_TERM_FUEL_TRIM_2 = "0109";
    public static final String EVAP_VAPOR_PRESSURE = "0132";
    public static final String ABSOLUTE_LOAD = "0143";
    public static final String RELATIVE_THROTTLE = "0145";
    public static final String CATALYST_TEMP_B1S1 = "013C";
    public static final String CATALYST_TEMP_B2S1 = "013D";
    public static final String CATALYST_TEMP_B1S2 = "013E";
    public static final String CATALYST_TEMP_B2S2 = "013F";
    public static final String ABSOLUTE_THROTTLE_B = "0147";
    public static final String ABSOLUTE_THROTTLE_C = "0148";
    public static final String ACCELERATOR_POS_D = "0149";
    public static final String ACCELERATOR_POS_E = "014A";
    public static final String ACCELERATOR_POS_F = "014B";
    public static final String COMMANDED_THROTTLE = "014C";
    public static final String TIME_WITH_MIL = "014D";
    public static final String TIME_SINCE_CLEAR = "014E";
    public static final String MAX_MAF = "0150";
    public static final String FUEL_TYPE = "0151";
    public static final String ETHANOL_PERCENT = "0152";
    public static final String EVAP_PRESSURE = "0153";
    public static final String EVAP_PRESSURE_ALT = "0154";
    public static final String SHORT_TERM_O2_TRIM_1 = "0155";
    public static final String LONG_TERM_O2_TRIM_1 = "0156";
    public static final String SHORT_TERM_O2_TRIM_3 = "0157";
    public static final String LONG_TERM_O2_TRIM_3 = "0158";
    public static final String FUEL_RAIL_PRESSURE = "0159";
    public static final String RELATIVE_ACCEL_POS = "015A";
    public static final String HYBRID_BATTERY_LIFE = "015B";

    /**
     * Parse OBD2 response based on PID command
     */
    public static double parseResponse(String command, String response) throws Exception {
        try {
            // Remove echo and extract data bytes
            response = response.replaceAll(command, "").replaceAll("41", "").trim();

            switch (command) {
                // Single byte, unsigned
                case VEHICLE_SPEED:
                    return parseByteUnsigned(response, 0);

                case COOLANT_TEMP:
                case INTAKE_TEMP:
                case AMBIENT_TEMP:
                case OIL_TEMP:
                    return parseTemperature(response);

                case ENGINE_LOAD:
                case THROTTLE_POS:
                case FUEL_LEVEL:
                case RELATIVE_THROTTLE:
                case ABSOLUTE_THROTTLE_B:
                case ABSOLUTE_THROTTLE_C:
                case ACCELERATOR_POS_D:
                case ACCELERATOR_POS_E:
                case ACCELERATOR_POS_F:
                case COMMANDED_THROTTLE:
                case ETHANOL_PERCENT:
                case HYBRID_BATTERY_LIFE:
                case RELATIVE_ACCEL_POS:
                    return parsePercentage(response);

                // Two bytes
                case ENGINE_RPM:
                    return parseRPM(response);

                case MAF_FLOW:
                    return parseMAF(response);

                case FUEL_PRESSURE:
                    return parseFuelPressure(response);

                case INTAKE_PRESSURE:
                case BAROMETRIC_PRESSURE:
                    return parseByteUnsigned(response, 0);

                case TIMING_ADVANCE:
                    return parseTimingAdvance(response);

                case RUNTIME:
                case TIME_WITH_MIL:
                case TIME_SINCE_CLEAR:
                    return parseRuntime(response);

                case DISTANCE_MIL:
                    return parseTwoByteUnsigned(response);

                case CONTROL_VOLTAGE:
                    return parseVoltage(response);

                case FUEL_INJECTION_TIMING:
                    return parseFuelTiming(response);

                case FUEL_RATE:
                    return parseFuelRate(response);

                case O2_SENSOR_1:
                case O2_SENSOR_2:
                case O2_SENSOR_3:
                case O2_SENSOR_4:
                    return parseO2Voltage(response);

                case SHORT_TERM_FUEL_TRIM_1:
                case LONG_TERM_FUEL_TRIM_1:
                case SHORT_TERM_FUEL_TRIM_2:
                case LONG_TERM_FUEL_TRIM_2:
                case SHORT_TERM_O2_TRIM_1:
                case LONG_TERM_O2_TRIM_1:
                case SHORT_TERM_O2_TRIM_3:
                case LONG_TERM_O2_TRIM_3:
                    return parseFuelTrim(response);

                case EVAP_VAPOR_PRESSURE:
                case EVAP_PRESSURE:
                    return parseEvapPressure(response);

                case ABSOLUTE_LOAD:
                    return parseAbsoluteLoad(response);

                case CATALYST_TEMP_B1S1:
                case CATALYST_TEMP_B2S1:
                case CATALYST_TEMP_B1S2:
                case CATALYST_TEMP_B2S2:
                    return parseCatalystTemp(response);

                case FUEL_RAIL_PRESSURE:
                    return parseFuelRailPressure(response);

                default:
                    Log.w(TAG, "Unknown PID: " + command);
                    return 0;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response for " + command + ": " + e.getMessage());
            throw new Exception("Parse error: " + e.getMessage());
        }
    }

    /**
     * Parse single byte as unsigned integer
     */
    private static int parseByteUnsigned(String response, int offset) {
        if (response.length() < (offset + 1) * 2) {
            throw new IllegalArgumentException("Response too short");
        }
        String byteHex = response.substring(offset * 2, (offset + 1) * 2);
        return Integer.parseInt(byteHex, 16);
    }

    /**
     * Parse two bytes as unsigned integer
     */
    private static int parseTwoByteUnsigned(String response) {
        if (response.length() < 4) {
            throw new IllegalArgumentException("Response too short");
        }
        int a = parseByteUnsigned(response, 0);
        int b = parseByteUnsigned(response, 1);
        return (a * 256) + b;
    }

    /**
     * Parse temperature (°C)
     * Formula: A - 40
     */
    private static double parseTemperature(String response) {
        int a = parseByteUnsigned(response, 0);
        return a - 40.0;
    }

    /**
     * Parse percentage
     * Formula: A * 100 / 255
     */
    private static double parsePercentage(String response) {
        int a = parseByteUnsigned(response, 0);
        return (a * 100.0) / 255.0;
    }

    /**
     * Parse RPM
     * Formula: ((A * 256) + B) / 4
     */
    private static double parseRPM(String response) {
        int value = parseTwoByteUnsigned(response);
        return value / 4.0;
    }

    /**
     * Parse MAF air flow rate (g/s)
     * Formula: ((A * 256) + B) / 100
     */
    private static double parseMAF(String response) {
        int value = parseTwoByteUnsigned(response);
        return value / 100.0;
    }

    /**
     * Parse fuel pressure (kPa)
     * Formula: A * 3
     */
    private static double parseFuelPressure(String response) {
        int a = parseByteUnsigned(response, 0);
        return a * 3.0;
    }

    /**
     * Parse timing advance (degrees before TDC)
     * Formula: (A - 128) / 2
     */
    private static double parseTimingAdvance(String response) {
        int a = parseByteUnsigned(response, 0);
        return (a - 128.0) / 2.0;
    }

    /**
     * Parse runtime (seconds)
     * Formula: (A * 256) + B
     */
    private static double parseRuntime(String response) {
        return parseTwoByteUnsigned(response);
    }

    /**
     * Parse control module voltage (V)
     * Formula: ((A * 256) + B) / 1000
     */
    private static double parseVoltage(String response) {
        int value = parseTwoByteUnsigned(response);
        return value / 1000.0;
    }

    /**
     * Parse fuel injection timing (degrees)
     * Formula: (((A * 256) + B) - 26880) / 128
     */
    private static double parseFuelTiming(String response) {
        int value = parseTwoByteUnsigned(response);
        return (value - 26880.0) / 128.0;
    }

    /**
     * Parse fuel rate (L/h)
     * Formula: ((A * 256) + B) / 20
     */
    private static double parseFuelRate(String response) {
        int value = parseTwoByteUnsigned(response);
        return value / 20.0;
    }

    /**
     * Parse O2 sensor voltage (V)
     * Formula: A / 200
     */
    private static double parseO2Voltage(String response) {
        int a = parseByteUnsigned(response, 0);
        return a / 200.0;
    }

    /**
     * Parse fuel trim (%)
     * Formula: (A - 128) * 100 / 128
     */
    private static double parseFuelTrim(String response) {
        int a = parseByteUnsigned(response, 0);
        return ((a - 128.0) * 100.0) / 128.0;
    }

    /**
     * Parse evaporative system vapor pressure (Pa)
     * Formula: ((A * 256) + B) - 32767
     */
    private static double parseEvapPressure(String response) {
        int value = parseTwoByteUnsigned(response);
        return value - 32767.0;
    }

    /**
     * Parse absolute load value (%)
     * Formula: ((A * 256) + B) * 100 / 255
     */
    private static double parseAbsoluteLoad(String response) {
        int value = parseTwoByteUnsigned(response);
        return (value * 100.0) / 255.0;
    }

    /**
     * Parse catalyst temperature (°C)
     * Formula: ((A * 256) + B) / 10 - 40
     */
    private static double parseCatalystTemp(String response) {
        int value = parseTwoByteUnsigned(response);
        return (value / 10.0) - 40.0;
    }

    /**
     * Parse fuel rail pressure (kPa)
     * Formula: ((A * 256) + B) * 10
     */
    private static double parseFuelRailPressure(String response) {
        int value = parseTwoByteUnsigned(response);
        return value * 10.0;
    }
}
