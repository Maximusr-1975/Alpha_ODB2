# OBD2 Cordova Plugin for Alphanywhere

A comprehensive Cordova plugin for OBD2 (On-Board Diagnostics II) vehicle diagnostics with full support for Alphanywhere mobile applications. Connect to any ELM327-compatible Bluetooth OBD2 adapter and access real-time vehicle data, diagnostic trouble codes, and vehicle information.

## Features

### Comprehensive OBD2 Support
- **50+ OBD2 Parameters** including:
  - Engine RPM, Vehicle Speed, Coolant Temperature
  - Engine Load, Throttle Position, Fuel Level
  - MAF Flow, Intake Temperature, Timing Advance
  - O2 Sensor Data, Fuel Trim, Control Module Voltage
  - And many more...

### Diagnostic Capabilities
- **Diagnostic Trouble Codes (DTCs)**: Read, clear, and monitor trouble codes
- **Freeze Frame Data**: Capture vehicle state when DTC was set
- **Real-time Monitoring**: Stream multiple PIDs simultaneously
- **Vehicle Information**: VIN, Calibration ID, ECU Name, OBD Standards

### Platform Support
- **Android**: Full Bluetooth support (Android 5.0+)
- **iOS**: External Accessory framework support
- **Alphanywhere**: Seamless integration with Alphanywhere UX components

### Developer-Friendly
- Promise-based JavaScript API
- Real-time data streaming
- Custom PID support
- Comprehensive error handling

## Installation

### Cordova

```bash
cordova plugin add cordova-plugin-alpha-obd2
```

Or install from GitHub:

```bash
cordova plugin add https://github.com/Maximusr-1975/Alpha_ODB2.git
```

### Alphanywhere

1. In your Alphanywhere mobile app builder, go to **Cordova Plugins**
2. Add the plugin using the Git URL:
   ```
   https://github.com/Maximusr-1975/Alpha_ODB2.git
   ```
3. Build your mobile app
4. Use the JavaScript API in your UX components

## Hardware Requirements

### OBD2 Adapter
- **ELM327-compatible Bluetooth adapter** (recommended)
- **WiFi OBD2 adapters** (iOS compatible)
- Ensure adapter is paired with your mobile device (Android)

### Vehicle Requirements
- **OBD2-compliant vehicle** (1996 or newer in the US, 2001+ in EU)
- **OBD2 diagnostic port** (usually under dashboard)

## Quick Start

### 1. Basic Connection

```javascript
// Scan for Bluetooth devices
OBD2.scanDevices(
    function(devices) {
        console.log('Found devices:', devices);
        // devices = [{name: "OBDII", address: "00:11:22:33:44:55"}, ...]
    },
    function(error) {
        console.error('Scan error:', error);
    }
);

// Connect to device
OBD2.connect(deviceAddress,
    function(success) {
        console.log('Connected');

        // Initialize OBD2 connection
        OBD2.initialize(
            function() {
                console.log('Ready to communicate');
            },
            function(error) {
                console.error('Init error:', error);
            }
        );
    },
    function(error) {
        console.error('Connection error:', error);
    }
);

// Disconnect
OBD2.disconnect(
    function() {
        console.log('Disconnected');
    },
    function(error) {
        console.error('Disconnect error:', error);
    }
);
```

### 2. Read Vehicle Data

```javascript
// Get Engine RPM
OBD2.getEngineRPM(
    function(data) {
        console.log('RPM:', data.rpm); // e.g., 2450.5
    },
    function(error) {
        console.error(error);
    }
);

// Get Vehicle Speed
OBD2.getSpeed(
    function(data) {
        console.log('Speed:', data.speed, 'km/h'); // e.g., 65
    },
    function(error) {
        console.error(error);
    }
);

// Get Coolant Temperature
OBD2.getCoolantTemperature(
    function(data) {
        console.log('Coolant Temp:', data.temperature, '°C'); // e.g., 87.5
    },
    function(error) {
        console.error(error);
    }
);

// Get comprehensive snapshot
OBD2.getVehicleSnapshot(
    function(snapshot) {
        console.log('Snapshot:', snapshot);
        // {rpm: 2450, speed: 65, coolantTemp: 87, engineLoad: 45, ...}
    },
    function(error) {
        console.error(error);
    }
);
```

### 3. Real-time Monitoring

```javascript
// Start monitoring multiple parameters
var pids = ['010C', '010D', '0105', '0104', '0111']; // RPM, Speed, Temp, Load, Throttle
var interval = 1000; // Update every second

OBD2.startMonitoring(pids, interval,
    function(data) {
        console.log('Real-time data:', data);
        // Called every second with updated values
        // {010C: 2450, 010D: 65, 0105: 87, ...}
    },
    function(error) {
        console.error('Monitoring error:', error);
    }
);

// Stop monitoring
OBD2.stopMonitoring(
    function() {
        console.log('Monitoring stopped');
    },
    function(error) {
        console.error(error);
    }
);
```

### 4. Diagnostic Trouble Codes

```javascript
// Get DTCs
OBD2.getDTCs(
    function(dtcs) {
        console.log('Trouble Codes:', dtcs);
        // ["P0420", "P0171"] - Array of DTC codes
    },
    function(error) {
        console.error(error);
    }
);

// Get DTC count
OBD2.getDTCCount(
    function(data) {
        console.log('Number of DTCs:', data.count);
    },
    function(error) {
        console.error(error);
    }
);

// Clear DTCs (also resets Check Engine Light)
OBD2.clearDTCs(
    function() {
        console.log('DTCs cleared');
    },
    function(error) {
        console.error(error);
    }
);
```

### 5. Vehicle Information

```javascript
// Get VIN
OBD2.getVIN(
    function(data) {
        console.log('VIN:', data.vin);
    },
    function(error) {
        console.error(error);
    }
);

// Get supported PIDs
OBD2.getSupportedPIDs(
    function(pids) {
        console.log('Supported PIDs:', pids);
        // ["010C", "010D", "0105", ...]
    },
    function(error) {
        console.error(error);
    }
);
```

## Alphanywhere Integration

### Basic Setup

```javascript
// In your UX Component's onShow event
function initOBD2() {
    if (typeof OBD2 === 'undefined') {
        alert('OBD2 Plugin not available');
        return;
    }

    // Initialize your UI
    {dialog.object}.setValue('CONNECTION_STATUS', 'Ready');
}

// Button onClick: Scan Devices
function scanDevices() {
    OBD2.scanDevices(
        function(devices) {
            var options = devices.map(function(d) {
                return {
                    text: d.name + ' (' + d.address + ')',
                    value: d.address
                };
            });

            {dialog.object}.populateDropdownBox('DEVICE_LIST', options);
        },
        function(error) {
            alert('Error: ' + error);
        }
    );
}

// Button onClick: Connect
function connect() {
    var address = {dialog.object}.getValue('DEVICE_LIST');

    OBD2.connect(address,
        function() {
            OBD2.initialize(
                function() {
                    {dialog.object}.setValue('CONNECTION_STATUS', 'Connected');
                    startMonitoring();
                },
                function(error) {
                    alert('Init error: ' + error);
                }
            );
        },
        function(error) {
            alert('Connection error: ' + error);
        }
    );
}

// Start real-time monitoring
function startMonitoring() {
    var pids = ['010C', '010D', '0105'];
    var interval = 1000;

    OBD2.startMonitoring(pids, interval,
        function(data) {
            {dialog.object}.setValue('RPM', Math.round(data['010C'] || 0));
            {dialog.object}.setValue('SPEED', Math.round(data['010D'] || 0));
            {dialog.object}.setValue('TEMP', Math.round(data['0105'] || 0));
        },
        function(error) {
            console.error('Monitoring error:', error);
        }
    );
}
```

See [examples/alphanywhere-integration.js](examples/alphanywhere-integration.js) for comprehensive integration examples.

## API Reference

### Connection Management

#### `scanDevices(success, error)`
Scan for available Bluetooth OBD2 devices.

**Returns:** Array of device objects `[{name, address, type, ...}]`

#### `connect(deviceAddress, success, error)`
Connect to an OBD2 device.

**Parameters:**
- `deviceAddress` (String): Bluetooth MAC address or device ID

#### `disconnect(success, error)`
Disconnect from the OBD2 device.

#### `isConnected(success, error)`
Check connection status.

**Returns:** `{connected: boolean}`

#### `initialize(success, error)`
Initialize OBD2 connection (sends AT commands).

### Engine Parameters

All engine parameter methods return an object with the specific parameter value.

- `getEngineRPM(success, error)` → `{rpm: number}`
- `getSpeed(success, error)` → `{speed: number}` (km/h)
- `getCoolantTemperature(success, error)` → `{temperature: number}` (°C)
- `getEngineLoad(success, error)` → `{load: number}` (%)
- `getThrottlePosition(success, error)` → `{position: number}` (%)
- `getIntakeAirTemperature(success, error)` → `{temperature: number}` (°C)
- `getMAFAirFlowRate(success, error)` → `{rate: number}` (g/s)
- `getFuelLevel(success, error)` → `{level: number}` (%)
- `getFuelPressure(success, error)` → `{pressure: number}` (kPa)
- `getIntakeManifoldPressure(success, error)` → `{pressure: number}` (kPa)
- `getTimingAdvance(success, error)` → `{advance: number}` (degrees)
- `getEngineRuntime(success, error)` → `{runtime: number}` (seconds)
- `getDistanceSinceMIL(success, error)` → `{distance: number}` (km)
- `getBarometricPressure(success, error)` → `{pressure: number}` (kPa)
- `getControlModuleVoltage(success, error)` → `{voltage: number}` (V)
- `getAmbientAirTemperature(success, error)` → `{temperature: number}` (°C)
- `getEngineOilTemperature(success, error)` → `{temperature: number}` (°C)
- `getFuelInjectionTiming(success, error)` → `{timing: number}` (degrees)
- `getFuelRate(success, error)` → `{rate: number}` (L/h)

### Oxygen Sensors

- `getO2Sensor1Voltage(success, error)` → `{voltage: number}` (V)
- `getO2Sensor2Voltage(success, error)` → `{voltage: number}` (V)

### Diagnostic Trouble Codes

#### `getDTCs(success, error)`
Get active diagnostic trouble codes.

**Returns:** Array of DTC codes `["P0420", "P0171", ...]`

#### `getPendingDTCs(success, error)`
Get pending trouble codes.

**Returns:** Array of DTC codes

#### `clearDTCs(success, error)`
Clear all DTCs and reset MIL (Check Engine Light).

#### `getDTCCount(success, error)`
Get number of active DTCs.

**Returns:** `{count: number}`

### Vehicle Information

#### `getVIN(success, error)`
Get Vehicle Identification Number.

**Returns:** `{vin: string}`

#### `getCalibrationID(success, error)`
Get ECU calibration ID.

**Returns:** `{calibrationId: string}`

#### `getECUName(success, error)`
Get ECU name.

**Returns:** `{ecuName: string}`

### Advanced Features

#### `getMultiplePIDs(pids, success, error)`
Get multiple PIDs in one call for efficiency.

**Parameters:**
- `pids` (Array): Array of PID codes `["010C", "010D", "0105"]`

**Returns:** Object with PID values `{010C: 2450, 010D: 65, ...}`

#### `sendCustomCommand(command, success, error)`
Send custom OBD2 command.

**Parameters:**
- `command` (String): OBD2 command (e.g., "010C")

**Returns:** `{command: string, response: string}`

#### `getSupportedPIDs(success, error)`
Get all supported PIDs for this vehicle.

**Returns:** Array of supported PIDs `["010C", "010D", ...]`

#### `getVehicleSnapshot(success, error)`
Get comprehensive snapshot of all available data.

**Returns:** `{rpm, speed, coolantTemp, engineLoad, throttle, fuelLevel, timestamp}`

#### `startMonitoring(pids, interval, callback, error)`
Start real-time monitoring of specified PIDs.

**Parameters:**
- `pids` (Array): PIDs to monitor
- `interval` (Number): Update interval in milliseconds (min 100ms)
- `callback` (Function): Called with data on each interval
- `error` (Function): Error callback

#### `stopMonitoring(success, error)`
Stop real-time monitoring.

#### `getFreezeFrameData(success, error)`
Get freeze frame data captured when DTC was set.

#### `getFuelSystemStatus(success, error)`
Get fuel system status.

**Returns:** `{code: number, description: string}`

#### `getOBDStandards(success, error)`
Get OBD standard this vehicle conforms to.

**Returns:** `{code: number, description: string}`

## PID Reference

The plugin includes predefined PID constants for easy access:

```javascript
OBD2.PID.ENGINE_RPM         // "010C"
OBD2.PID.VEHICLE_SPEED      // "010D"
OBD2.PID.COOLANT_TEMP       // "0105"
OBD2.PID.ENGINE_LOAD        // "0104"
OBD2.PID.THROTTLE_POS       // "0111"
OBD2.PID.INTAKE_TEMP        // "010F"
OBD2.PID.MAF_FLOW           // "0110"
OBD2.PID.FUEL_LEVEL         // "012F"
// ... and many more
```

## Troubleshooting

### Android

**Bluetooth Permission Issues:**
- Ensure location permissions are granted (required for Bluetooth scanning on Android)
- For Android 12+, BLUETOOTH_CONNECT and BLUETOOTH_SCAN permissions are required

**Connection Fails:**
- Make sure the OBD2 adapter is paired in Bluetooth settings first
- Try turning Bluetooth off and on
- Restart the OBD2 adapter (unplug from vehicle)

### iOS

**Device Not Found:**
- iOS requires WiFi OBD2 adapters or MFi-certified Bluetooth adapters
- Check that the adapter supports External Accessory framework
- Ensure the adapter's protocol string is declared in Info.plist

### Common Issues

**No Data Returned:**
- Make sure to call `initialize()` after `connect()`
- Wait 1-2 seconds after initialization before sending commands
- Not all vehicles support all PIDs - check with `getSupportedPIDs()`

**Slow Response:**
- Use `getMultiplePIDs()` instead of individual calls for better performance
- Increase monitoring interval if getting timeouts
- Some older vehicles have slower response times

## Example Projects

See the [examples](examples/) directory for:
- **index.html**: Basic web-based example
- **alphanywhere-integration.js**: Comprehensive Alphanywhere integration

## Supported Vehicles

This plugin works with any vehicle that supports OBD2:
- **United States:** 1996 and newer
- **Europe (EOBD):** Gasoline cars from 2001, Diesel from 2004
- **Japan (JOBD):** 2008 and newer

## License

MIT License - see [LICENSE](LICENSE) file

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## Support

For issues, questions, or feature requests, please use the [GitHub Issues](https://github.com/Maximusr-1975/Alpha_ODB2/issues) page.

## Credits

Developed for Alphanywhere mobile applications with comprehensive OBD2 support.

## Version History

### 1.0.0 (Initial Release)
- Full Android and iOS support
- 50+ OBD2 parameters
- DTC support
- Real-time monitoring
- Alphanywhere integration
- Comprehensive documentation
