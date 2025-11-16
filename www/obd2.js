/**
 * Cordova OBD2 Plugin - Comprehensive OBD2 Interface
 * Compatible with Alphanywhere and standard Cordova apps
 */

var exec = require('cordova/exec');

/**
 * OBD2 Plugin Main Object
 */
var OBD2 = {

    /**
     * Scan for available Bluetooth devices
     * @param {Function} success - Success callback with array of devices
     * @param {Function} error - Error callback
     */
    scanDevices: function(success, error) {
        exec(success, error, 'OBD2', 'scanDevices', []);
    },

    /**
     * Connect to OBD2 device
     * @param {String} deviceAddress - Bluetooth MAC address or device ID
     * @param {Function} success - Success callback
     * @param {Function} error - Error callback
     */
    connect: function(deviceAddress, success, error) {
        exec(success, error, 'OBD2', 'connect', [deviceAddress]);
    },

    /**
     * Disconnect from OBD2 device
     * @param {Function} success - Success callback
     * @param {Function} error - Error callback
     */
    disconnect: function(success, error) {
        exec(success, error, 'OBD2', 'disconnect', []);
    },

    /**
     * Check connection status
     * @param {Function} success - Success callback with connection status
     * @param {Function} error - Error callback
     */
    isConnected: function(success, error) {
        exec(success, error, 'OBD2', 'isConnected', []);
    },

    /**
     * Initialize OBD2 connection (sends AT commands)
     * @param {Function} success - Success callback
     * @param {Function} error - Error callback
     */
    initialize: function(success, error) {
        exec(success, error, 'OBD2', 'initialize', []);
    },

    // ============ ENGINE PARAMETERS ============

    /**
     * Get Engine RPM
     * @param {Function} success - Success callback with RPM value
     * @param {Function} error - Error callback
     */
    getEngineRPM: function(success, error) {
        exec(success, error, 'OBD2', 'getEngineRPM', []);
    },

    /**
     * Get Vehicle Speed (km/h)
     * @param {Function} success - Success callback with speed value
     * @param {Function} error - Error callback
     */
    getSpeed: function(success, error) {
        exec(success, error, 'OBD2', 'getSpeed', []);
    },

    /**
     * Get Engine Coolant Temperature (°C)
     * @param {Function} success - Success callback with temperature
     * @param {Function} error - Error callback
     */
    getCoolantTemperature: function(success, error) {
        exec(success, error, 'OBD2', 'getCoolantTemperature', []);
    },

    /**
     * Get Engine Load (%)
     * @param {Function} success - Success callback with load percentage
     * @param {Function} error - Error callback
     */
    getEngineLoad: function(success, error) {
        exec(success, error, 'OBD2', 'getEngineLoad', []);
    },

    /**
     * Get Throttle Position (%)
     * @param {Function} success - Success callback with throttle position
     * @param {Function} error - Error callback
     */
    getThrottlePosition: function(success, error) {
        exec(success, error, 'OBD2', 'getThrottlePosition', []);
    },

    /**
     * Get Intake Air Temperature (°C)
     * @param {Function} success - Success callback with temperature
     * @param {Function} error - Error callback
     */
    getIntakeAirTemperature: function(success, error) {
        exec(success, error, 'OBD2', 'getIntakeAirTemperature', []);
    },

    /**
     * Get MAF Air Flow Rate (g/s)
     * @param {Function} success - Success callback with MAF rate
     * @param {Function} error - Error callback
     */
    getMAFAirFlowRate: function(success, error) {
        exec(success, error, 'OBD2', 'getMAFAirFlowRate', []);
    },

    /**
     * Get Fuel Level (%)
     * @param {Function} success - Success callback with fuel level
     * @param {Function} error - Error callback
     */
    getFuelLevel: function(success, error) {
        exec(success, error, 'OBD2', 'getFuelLevel', []);
    },

    /**
     * Get Fuel Pressure (kPa)
     * @param {Function} success - Success callback with fuel pressure
     * @param {Function} error - Error callback
     */
    getFuelPressure: function(success, error) {
        exec(success, error, 'OBD2', 'getFuelPressure', []);
    },

    /**
     * Get Intake Manifold Pressure (kPa)
     * @param {Function} success - Success callback with pressure
     * @param {Function} error - Error callback
     */
    getIntakeManifoldPressure: function(success, error) {
        exec(success, error, 'OBD2', 'getIntakeManifoldPressure', []);
    },

    /**
     * Get Timing Advance (degrees)
     * @param {Function} success - Success callback with timing advance
     * @param {Function} error - Error callback
     */
    getTimingAdvance: function(success, error) {
        exec(success, error, 'OBD2', 'getTimingAdvance', []);
    },

    /**
     * Get Engine Runtime (seconds)
     * @param {Function} success - Success callback with runtime
     * @param {Function} error - Error callback
     */
    getEngineRuntime: function(success, error) {
        exec(success, error, 'OBD2', 'getEngineRuntime', []);
    },

    /**
     * Get Distance Since MIL On (km)
     * @param {Function} success - Success callback with distance
     * @param {Function} error - Error callback
     */
    getDistanceSinceMIL: function(success, error) {
        exec(success, error, 'OBD2', 'getDistanceSinceMIL', []);
    },

    /**
     * Get Barometric Pressure (kPa)
     * @param {Function} success - Success callback with pressure
     * @param {Function} error - Error callback
     */
    getBarometricPressure: function(success, error) {
        exec(success, error, 'OBD2', 'getBarometricPressure', []);
    },

    /**
     * Get Control Module Voltage (V)
     * @param {Function} success - Success callback with voltage
     * @param {Function} error - Error callback
     */
    getControlModuleVoltage: function(success, error) {
        exec(success, error, 'OBD2', 'getControlModuleVoltage', []);
    },

    /**
     * Get Ambient Air Temperature (°C)
     * @param {Function} success - Success callback with temperature
     * @param {Function} error - Error callback
     */
    getAmbientAirTemperature: function(success, error) {
        exec(success, error, 'OBD2', 'getAmbientAirTemperature', []);
    },

    /**
     * Get Engine Oil Temperature (°C)
     * @param {Function} success - Success callback with temperature
     * @param {Function} error - Error callback
     */
    getEngineOilTemperature: function(success, error) {
        exec(success, error, 'OBD2', 'getEngineOilTemperature', []);
    },

    /**
     * Get Fuel Injection Timing (degrees)
     * @param {Function} success - Success callback with timing
     * @param {Function} error - Error callback
     */
    getFuelInjectionTiming: function(success, error) {
        exec(success, error, 'OBD2', 'getFuelInjectionTiming', []);
    },

    /**
     * Get Fuel Rate (L/h)
     * @param {Function} success - Success callback with fuel rate
     * @param {Function} error - Error callback
     */
    getFuelRate: function(success, error) {
        exec(success, error, 'OBD2', 'getFuelRate', []);
    },

    // ============ OXYGEN SENSORS ============

    /**
     * Get O2 Sensor Voltage (Bank 1, Sensor 1)
     * @param {Function} success - Success callback with voltage
     * @param {Function} error - Error callback
     */
    getO2Sensor1Voltage: function(success, error) {
        exec(success, error, 'OBD2', 'getO2Sensor1Voltage', []);
    },

    /**
     * Get O2 Sensor Voltage (Bank 1, Sensor 2)
     * @param {Function} success - Success callback with voltage
     * @param {Function} error - Error callback
     */
    getO2Sensor2Voltage: function(success, error) {
        exec(success, error, 'OBD2', 'getO2Sensor2Voltage', []);
    },

    // ============ DIAGNOSTIC TROUBLE CODES (DTC) ============

    /**
     * Get Diagnostic Trouble Codes
     * @param {Function} success - Success callback with array of DTCs
     * @param {Function} error - Error callback
     */
    getDTCs: function(success, error) {
        exec(success, error, 'OBD2', 'getDTCs', []);
    },

    /**
     * Get Pending Diagnostic Trouble Codes
     * @param {Function} success - Success callback with array of pending DTCs
     * @param {Function} error - Error callback
     */
    getPendingDTCs: function(success, error) {
        exec(success, error, 'OBD2', 'getPendingDTCs', []);
    },

    /**
     * Clear Diagnostic Trouble Codes and MIL
     * @param {Function} success - Success callback
     * @param {Function} error - Error callback
     */
    clearDTCs: function(success, error) {
        exec(success, error, 'OBD2', 'clearDTCs', []);
    },

    /**
     * Get Number of DTCs
     * @param {Function} success - Success callback with DTC count
     * @param {Function} error - Error callback
     */
    getDTCCount: function(success, error) {
        exec(success, error, 'OBD2', 'getDTCCount', []);
    },

    // ============ VEHICLE INFORMATION ============

    /**
     * Get VIN (Vehicle Identification Number)
     * @param {Function} success - Success callback with VIN
     * @param {Function} error - Error callback
     */
    getVIN: function(success, error) {
        exec(success, error, 'OBD2', 'getVIN', []);
    },

    /**
     * Get Calibration ID
     * @param {Function} success - Success callback with calibration ID
     * @param {Function} error - Error callback
     */
    getCalibrationID: function(success, error) {
        exec(success, error, 'OBD2', 'getCalibrationID', []);
    },

    /**
     * Get ECU Name
     * @param {Function} success - Success callback with ECU name
     * @param {Function} error - Error callback
     */
    getECUName: function(success, error) {
        exec(success, error, 'OBD2', 'getECUName', []);
    },

    // ============ ADVANCED FEATURES ============

    /**
     * Get Multiple PIDs at once for efficiency
     * @param {Array} pids - Array of PID codes (e.g., ['010C', '010D'])
     * @param {Function} success - Success callback with object of PID results
     * @param {Function} error - Error callback
     */
    getMultiplePIDs: function(pids, success, error) {
        exec(success, error, 'OBD2', 'getMultiplePIDs', [pids]);
    },

    /**
     * Send custom OBD2 command
     * @param {String} command - OBD2 command (e.g., '010C' for RPM)
     * @param {Function} success - Success callback with raw response
     * @param {Function} error - Error callback
     */
    sendCustomCommand: function(command, success, error) {
        exec(success, error, 'OBD2', 'sendCustomCommand', [command]);
    },

    /**
     * Get all supported PIDs
     * @param {Function} success - Success callback with array of supported PIDs
     * @param {Function} error - Error callback
     */
    getSupportedPIDs: function(success, error) {
        exec(success, error, 'OBD2', 'getSupportedPIDs', []);
    },

    /**
     * Get comprehensive vehicle data snapshot
     * @param {Function} success - Success callback with object containing all available data
     * @param {Function} error - Error callback
     */
    getVehicleSnapshot: function(success, error) {
        exec(success, error, 'OBD2', 'getVehicleSnapshot', []);
    },

    /**
     * Start real-time monitoring (calls callback repeatedly)
     * @param {Array} pids - Array of PIDs to monitor
     * @param {Number} interval - Interval in milliseconds (minimum 100ms)
     * @param {Function} callback - Callback with data updates
     * @param {Function} error - Error callback
     */
    startMonitoring: function(pids, interval, callback, error) {
        exec(callback, error, 'OBD2', 'startMonitoring', [pids, interval]);
    },

    /**
     * Stop real-time monitoring
     * @param {Function} success - Success callback
     * @param {Function} error - Error callback
     */
    stopMonitoring: function(success, error) {
        exec(success, error, 'OBD2', 'stopMonitoring', []);
    },

    /**
     * Get Freeze Frame Data
     * @param {Function} success - Success callback with freeze frame data
     * @param {Function} error - Error callback
     */
    getFreezeFrameData: function(success, error) {
        exec(success, error, 'OBD2', 'getFreezeFrameData', []);
    },

    /**
     * Get Fuel System Status
     * @param {Function} success - Success callback with fuel system status
     * @param {Function} error - Error callback
     */
    getFuelSystemStatus: function(success, error) {
        exec(success, error, 'OBD2', 'getFuelSystemStatus', []);
    },

    /**
     * Get OBD Standards
     * @param {Function} success - Success callback with OBD standard info
     * @param {Function} error - Error callback
     */
    getOBDStandards: function(success, error) {
        exec(success, error, 'OBD2', 'getOBDStandards', []);
    },

    // ============ HELPER CONSTANTS ============

    /**
     * Common PID codes for reference
     */
    PID: {
        ENGINE_RPM: '010C',
        VEHICLE_SPEED: '010D',
        COOLANT_TEMP: '0105',
        ENGINE_LOAD: '0104',
        THROTTLE_POS: '0111',
        INTAKE_TEMP: '010F',
        MAF_FLOW: '0110',
        FUEL_LEVEL: '012F',
        FUEL_PRESSURE: '010A',
        INTAKE_PRESSURE: '010B',
        TIMING_ADVANCE: '010E',
        RUNTIME: '011F',
        DISTANCE_MIL: '0121',
        BAROMETRIC_PRESSURE: '0133',
        CONTROL_VOLTAGE: '0142',
        AMBIENT_TEMP: '0146',
        OIL_TEMP: '015C',
        FUEL_RATE: '015E'
    }
};

module.exports = OBD2;
