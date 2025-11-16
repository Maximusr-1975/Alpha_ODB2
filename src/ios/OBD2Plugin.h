//
//  OBD2Plugin.h
//  OBD2 Cordova Plugin for iOS
//
//  Comprehensive OBD2 diagnostics plugin with Bluetooth support
//

#import <Cordova/CDVPlugin.h>

@interface OBD2Plugin : CDVPlugin

// Connection Management
- (void)scanDevices:(CDVInvokedUrlCommand*)command;
- (void)connect:(CDVInvokedUrlCommand*)command;
- (void)disconnect:(CDVInvokedUrlCommand*)command;
- (void)isConnected:(CDVInvokedUrlCommand*)command;
- (void)initialize:(CDVInvokedUrlCommand*)command;

// Engine Parameters
- (void)getEngineRPM:(CDVInvokedUrlCommand*)command;
- (void)getSpeed:(CDVInvokedUrlCommand*)command;
- (void)getCoolantTemperature:(CDVInvokedUrlCommand*)command;
- (void)getEngineLoad:(CDVInvokedUrlCommand*)command;
- (void)getThrottlePosition:(CDVInvokedUrlCommand*)command;
- (void)getIntakeAirTemperature:(CDVInvokedUrlCommand*)command;
- (void)getMAFAirFlowRate:(CDVInvokedUrlCommand*)command;
- (void)getFuelLevel:(CDVInvokedUrlCommand*)command;
- (void)getFuelPressure:(CDVInvokedUrlCommand*)command;
- (void)getIntakeManifoldPressure:(CDVInvokedUrlCommand*)command;
- (void)getTimingAdvance:(CDVInvokedUrlCommand*)command;
- (void)getEngineRuntime:(CDVInvokedUrlCommand*)command;
- (void)getDistanceSinceMIL:(CDVInvokedUrlCommand*)command;
- (void)getBarometricPressure:(CDVInvokedUrlCommand*)command;
- (void)getControlModuleVoltage:(CDVInvokedUrlCommand*)command;
- (void)getAmbientAirTemperature:(CDVInvokedUrlCommand*)command;
- (void)getEngineOilTemperature:(CDVInvokedUrlCommand*)command;
- (void)getFuelInjectionTiming:(CDVInvokedUrlCommand*)command;
- (void)getFuelRate:(CDVInvokedUrlCommand*)command;

// Oxygen Sensors
- (void)getO2Sensor1Voltage:(CDVInvokedUrlCommand*)command;
- (void)getO2Sensor2Voltage:(CDVInvokedUrlCommand*)command;

// Diagnostic Trouble Codes
- (void)getDTCs:(CDVInvokedUrlCommand*)command;
- (void)getPendingDTCs:(CDVInvokedUrlCommand*)command;
- (void)clearDTCs:(CDVInvokedUrlCommand*)command;
- (void)getDTCCount:(CDVInvokedUrlCommand*)command;

// Vehicle Information
- (void)getVIN:(CDVInvokedUrlCommand*)command;
- (void)getCalibrationID:(CDVInvokedUrlCommand*)command;
- (void)getECUName:(CDVInvokedUrlCommand*)command;

// Advanced Features
- (void)getMultiplePIDs:(CDVInvokedUrlCommand*)command;
- (void)sendCustomCommand:(CDVInvokedUrlCommand*)command;
- (void)getSupportedPIDs:(CDVInvokedUrlCommand*)command;
- (void)getVehicleSnapshot:(CDVInvokedUrlCommand*)command;
- (void)startMonitoring:(CDVInvokedUrlCommand*)command;
- (void)stopMonitoring:(CDVInvokedUrlCommand*)command;
- (void)getFreezeFrameData:(CDVInvokedUrlCommand*)command;
- (void)getFuelSystemStatus:(CDVInvokedUrlCommand*)command;
- (void)getOBDStandards:(CDVInvokedUrlCommand*)command;

@end
