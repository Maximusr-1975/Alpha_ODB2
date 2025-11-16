/**
 * Alphanywhere Integration Example for OBD2 Plugin
 *
 * This file demonstrates how to integrate the OBD2 plugin with Alphanywhere
 * mobile applications for comprehensive vehicle diagnostics.
 */

// ============================================
// ALPHANYWHERE COMPONENT INTEGRATION
// ============================================

/**
 * Initialize OBD2 functionality in Alphanywhere component
 * Call this in the component's onShow event
 */
function initializeOBD2Component() {
    // Check if plugin is available
    if (typeof OBD2 === 'undefined') {
        alert('OBD2 Plugin not available. Make sure it is installed.');
        return false;
    }

    // Initialize UI state
    updateConnectionStatus(false);

    return true;
}

/**
 * Scan for Bluetooth OBD2 devices
 * Alphanywhere UX Component Button onClick event
 */
function scanForDevices() {
    {dialog.object}.showWaitMessage('Scanning for devices...');

    OBD2.scanDevices(
        function(devices) {
            {dialog.object}.hideWaitMessage();

            if (devices.length === 0) {
                alert('No OBD2 devices found. Make sure your device is paired.');
                return;
            }

            // Populate device list in Alphanywhere dropdown
            var deviceList = {dialog.object}.getValue('DEVICE_LIST');
            var options = [];

            devices.forEach(function(device) {
                options.push({
                    text: device.name + ' (' + device.address + ')',
                    value: device.address
                });
            });

            // Update dropdown list
            {dialog.object}.populateDropdownBox('DEVICE_LIST', options);

            alert('Found ' + devices.length + ' device(s)');
        },
        function(error) {
            {dialog.object}.hideWaitMessage();
            alert('Error scanning devices: ' + error);
        }
    );
}

/**
 * Connect to selected OBD2 device
 * Alphanywhere UX Component Button onClick event
 */
function connectToOBD2() {
    var deviceAddress = {dialog.object}.getValue('DEVICE_LIST');

    if (!deviceAddress) {
        alert('Please select a device first');
        return;
    }

    {dialog.object}.showWaitMessage('Connecting to OBD2 device...');

    OBD2.connect(deviceAddress,
        function(success) {
            {dialog.object}.hideWaitMessage();

            // Initialize the connection
            OBD2.initialize(
                function(initialized) {
                    updateConnectionStatus(true);
                    alert('Connected and initialized successfully');

                    // Start collecting initial data
                    getVehicleInfo();
                },
                function(error) {
                    alert('Initialization error: ' + error);
                }
            );
        },
        function(error) {
            {dialog.object}.hideWaitMessage();
            updateConnectionStatus(false);
            alert('Connection error: ' + error);
        }
    );
}

/**
 * Disconnect from OBD2 device
 * Alphanywhere UX Component Button onClick event
 */
function disconnectFromOBD2() {
    // Stop monitoring if active
    stopRealTimeMonitoring();

    OBD2.disconnect(
        function(success) {
            updateConnectionStatus(false);
            alert('Disconnected from OBD2 device');
        },
        function(error) {
            alert('Disconnect error: ' + error);
        }
    );
}

/**
 * Update connection status UI
 */
function updateConnectionStatus(connected) {
    if (connected) {
        {dialog.object}.setValue('CONNECTION_STATUS', 'Connected');
        {dialog.object}.setStyle('CONNECTION_STATUS', 'color:green; font-weight:bold;');
    } else {
        {dialog.object}.setValue('CONNECTION_STATUS', 'Disconnected');
        {dialog.object}.setStyle('CONNECTION_STATUS', 'color:red; font-weight:bold;');
    }
}

// ============================================
// REAL-TIME MONITORING
// ============================================

var monitoringActive = false;

/**
 * Start real-time vehicle monitoring
 * Updates dashboard with live data
 */
function startRealTimeMonitoring() {
    if (monitoringActive) {
        alert('Monitoring already active');
        return;
    }

    // Define PIDs to monitor
    var pids = [
        '010C',  // Engine RPM
        '010D',  // Vehicle Speed
        '0105',  // Coolant Temperature
        '0104',  // Engine Load
        '0111',  // Throttle Position
        '012F'   // Fuel Level
    ];

    var interval = 1000; // Update every second

    OBD2.startMonitoring(pids, interval,
        function(data) {
            monitoringActive = true;

            // Update Alphanywhere UI controls with real-time data
            {dialog.object}.setValue('RPM_DISPLAY', Math.round(data['010C'] || 0));
            {dialog.object}.setValue('SPEED_DISPLAY', Math.round(data['010D'] || 0));
            {dialog.object}.setValue('COOLANT_DISPLAY', Math.round(data['0105'] || 0));
            {dialog.object}.setValue('ENGINE_LOAD', Math.round(data['0104'] || 0));
            {dialog.object}.setValue('THROTTLE_POS', Math.round(data['0111'] || 0));
            {dialog.object}.setValue('FUEL_LEVEL', Math.round(data['012F'] || 0));

            // Update gauge controls if using Alphanywhere gauges
            updateGauges(data);

            // Save to Alphanywhere local storage for later sync
            saveMonitoringData(data);
        },
        function(error) {
            console.error('Monitoring error: ' + error);
            monitoringActive = false;
        }
    );
}

/**
 * Stop real-time monitoring
 */
function stopRealTimeMonitoring() {
    if (!monitoringActive) return;

    OBD2.stopMonitoring(
        function(success) {
            monitoringActive = false;
            alert('Monitoring stopped');
        },
        function(error) {
            alert('Error stopping monitoring: ' + error);
        }
    );
}

/**
 * Update gauge controls with real-time data
 */
function updateGauges(data) {
    // Update RPM gauge (0-8000 RPM)
    var rpm = data['010C'] || 0;
    {dialog.object}.runAction('gaugeRPM', {value: rpm});

    // Update speed gauge (0-200 km/h)
    var speed = data['010D'] || 0;
    {dialog.object}.runAction('gaugeSpeed', {value: speed});

    // Update temperature gauge (0-120°C)
    var temp = data['0105'] || 0;
    {dialog.object}.runAction('gaugeTemp', {value: temp});
}

/**
 * Save monitoring data to Alphanywhere local storage
 * Can be synced to server later
 */
function saveMonitoringData(data) {
    var record = {
        timestamp: new Date().toISOString(),
        rpm: data['010C'],
        speed: data['010D'],
        coolantTemp: data['0105'],
        engineLoad: data['0104'],
        throttle: data['0111'],
        fuelLevel: data['012F']
    };

    // Save to Alphanywhere local storage
    var lObj = {dialog.object}.localStorageGet('obd2_logs') || [];
    lObj.push(record);

    // Keep only last 1000 records
    if (lObj.length > 1000) {
        lObj = lObj.slice(-1000);
    }

    {dialog.object}.localStorageSet('obd2_logs', lObj);
}

// ============================================
// DIAGNOSTIC TROUBLE CODES
// ============================================

/**
 * Get and display Diagnostic Trouble Codes
 */
function getDiagnosticTroubleCodes() {
    {dialog.object}.showWaitMessage('Reading DTCs...');

    OBD2.getDTCs(
        function(dtcs) {
            {dialog.object}.hideWaitMessage();

            if (dtcs.length === 0) {
                {dialog.object}.setValue('DTC_LIST', 'No trouble codes found');
                alert('No DTCs - Vehicle systems OK');
            } else {
                var dtcText = 'Found ' + dtcs.length + ' trouble code(s):\n\n';
                dtcText += dtcs.join('\n');

                {dialog.object}.setValue('DTC_LIST', dtcText);

                // Save DTCs to Alphanywhere database
                saveDTCsToDatabase(dtcs);
            }
        },
        function(error) {
            {dialog.object}.hideWaitMessage();
            alert('Error reading DTCs: ' + error);
        }
    );
}

/**
 * Clear all DTCs
 */
function clearDiagnosticTroubleCodes() {
    if (!confirm('Clear all diagnostic trouble codes? This will also reset the MIL (Check Engine Light).')) {
        return;
    }

    {dialog.object}.showWaitMessage('Clearing DTCs...');

    OBD2.clearDTCs(
        function(success) {
            {dialog.object}.hideWaitMessage();
            {dialog.object}.setValue('DTC_LIST', 'DTCs cleared');
            alert('DTCs cleared successfully');
        },
        function(error) {
            {dialog.object}.hideWaitMessage();
            alert('Error clearing DTCs: ' + error);
        }
    );
}

/**
 * Save DTCs to Alphanywhere database via Ajax callback
 */
function saveDTCsToDatabase(dtcs) {
    var data = {
        vin: {dialog.object}.getValue('VIN'),
        dtcs: dtcs,
        timestamp: new Date().toISOString()
    };

    {dialog.object}.ajaxCallback('', '', 'saveDTCs', '', JSON.stringify(data));
}

// ============================================
// VEHICLE INFORMATION
// ============================================

/**
 * Get comprehensive vehicle information
 */
function getVehicleInfo() {
    {dialog.object}.showWaitMessage('Reading vehicle information...');

    // Get VIN
    OBD2.getVIN(
        function(data) {
            {dialog.object}.setValue('VIN', data.vin);

            // Get supported PIDs
            OBD2.getSupportedPIDs(
                function(pids) {
                    {dialog.object}.hideWaitMessage();
                    {dialog.object}.setValue('SUPPORTED_PIDS', pids.length + ' PIDs supported');

                    // Get vehicle snapshot
                    getVehicleSnapshot();
                },
                function(error) {
                    console.error('Error getting PIDs: ' + error);
                }
            );
        },
        function(error) {
            {dialog.object}.hideWaitMessage();
            alert('Error getting VIN: ' + error);
        }
    );
}

/**
 * Get comprehensive vehicle data snapshot
 */
function getVehicleSnapshot() {
    OBD2.getVehicleSnapshot(
        function(snapshot) {
            // Display in Alphanywhere controls
            {dialog.object}.setValue('SNAPSHOT_RPM', Math.round(snapshot.rpm || 0));
            {dialog.object}.setValue('SNAPSHOT_SPEED', Math.round(snapshot.speed || 0));
            {dialog.object}.setValue('SNAPSHOT_TEMP', Math.round(snapshot.coolantTemp || 0));
            {dialog.object}.setValue('SNAPSHOT_LOAD', Math.round(snapshot.engineLoad || 0));
            {dialog.object}.setValue('SNAPSHOT_FUEL', Math.round(snapshot.fuelLevel || 0));

            // Save snapshot to local storage
            {dialog.object}.localStorageSet('last_snapshot', snapshot);
        },
        function(error) {
            alert('Error getting snapshot: ' + error);
        }
    );
}

// ============================================
// DATA SYNC TO ALPHANYWHERE SERVER
// ============================================

/**
 * Sync collected OBD2 data to Alphanywhere server
 * Call this periodically or on user action
 */
function syncDataToServer() {
    var logs = {dialog.object}.localStorageGet('obd2_logs') || [];

    if (logs.length === 0) {
        alert('No data to sync');
        return;
    }

    {dialog.object}.showWaitMessage('Syncing data to server...');

    var syncData = {
        vin: {dialog.object}.getValue('VIN'),
        logs: logs
    };

    {dialog.object}.ajaxCallback('', '', 'syncOBD2Data', '', JSON.stringify(syncData),
        {
            onComplete: function(result) {
                {dialog.object}.hideWaitMessage();

                if (result.success) {
                    // Clear local storage after successful sync
                    {dialog.object}.localStorageSet('obd2_logs', []);
                    alert('Data synced successfully');
                } else {
                    alert('Sync failed: ' + result.error);
                }
            }
        }
    );
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Format timestamp for display
 */
function formatTimestamp(timestamp) {
    var date = new Date(timestamp);
    return date.toLocaleString();
}

/**
 * Export data as CSV for Alphanywhere reporting
 */
function exportDataAsCSV() {
    var logs = {dialog.object}.localStorageGet('obd2_logs') || [];

    if (logs.length === 0) {
        alert('No data to export');
        return;
    }

    var csv = 'Timestamp,RPM,Speed,Coolant Temp,Engine Load,Throttle,Fuel Level\n';

    logs.forEach(function(log) {
        csv += [
            log.timestamp,
            log.rpm,
            log.speed,
            log.coolantTemp,
            log.engineLoad,
            log.throttle,
            log.fuelLevel
        ].join(',') + '\n';
    });

    // Use Alphanywhere file system API to save
    {dialog.object}.fileDownload(csv, 'obd2_data.csv', 'text/csv');
}
