//
//  OBD2Plugin.m
//  OBD2 Cordova Plugin for iOS
//

#import "OBD2Plugin.h"
#import "OBD2Manager.h"

@interface OBD2Plugin ()
@property (nonatomic, strong) OBD2Manager *obd2Manager;
@property (nonatomic, strong) NSString *monitoringCallbackId;
@end

@implementation OBD2Plugin

- (void)pluginInitialize {
    [super pluginInitialize];
    self.obd2Manager = [[OBD2Manager alloc] init];
}

#pragma mark - Connection Management

- (void)scanDevices:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSArray *devices = [self.obd2Manager scanDevices];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                       messageAsArray:devices];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)connect:(CDVInvokedUrlCommand*)command {
    NSString *deviceId = [command.arguments objectAtIndex:0];

    [self.commandDelegate runInBackground:^{
        BOOL success = [self.obd2Manager connectToDevice:deviceId];

        CDVPluginResult *result;
        if (success) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                        messageAsString:@"Connected successfully"];
        } else {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                        messageAsString:@"Connection failed"];
        }

        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)disconnect:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        [self.obd2Manager disconnect];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                    messageAsString:@"Disconnected"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)isConnected:(CDVInvokedUrlCommand*)command {
    BOOL connected = [self.obd2Manager isConnected];

    NSDictionary *resultDict = @{@"connected": @(connected)};
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                            messageAsDictionary:resultDict];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)initialize:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        BOOL success = [self.obd2Manager initializeConnection];

        CDVPluginResult *result;
        if (success) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                        messageAsString:@"Initialized successfully"];
        } else {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                        messageAsString:@"Initialization failed"];
        }

        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

#pragma mark - Engine Parameters

- (void)getEngineRPM:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"010C" key:@"rpm" command:command];
}

- (void)getSpeed:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"010D" key:@"speed" command:command];
}

- (void)getCoolantTemperature:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0105" key:@"temperature" command:command];
}

- (void)getEngineLoad:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0104" key:@"load" command:command];
}

- (void)getThrottlePosition:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0111" key:@"position" command:command];
}

- (void)getIntakeAirTemperature:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"010F" key:@"temperature" command:command];
}

- (void)getMAFAirFlowRate:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0110" key:@"rate" command:command];
}

- (void)getFuelLevel:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"012F" key:@"level" command:command];
}

- (void)getFuelPressure:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"010A" key:@"pressure" command:command];
}

- (void)getIntakeManifoldPressure:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"010B" key:@"pressure" command:command];
}

- (void)getTimingAdvance:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"010E" key:@"advance" command:command];
}

- (void)getEngineRuntime:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"011F" key:@"runtime" command:command];
}

- (void)getDistanceSinceMIL:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0121" key:@"distance" command:command];
}

- (void)getBarometricPressure:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0133" key:@"pressure" command:command];
}

- (void)getControlModuleVoltage:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0142" key:@"voltage" command:command];
}

- (void)getAmbientAirTemperature:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0146" key:@"temperature" command:command];
}

- (void)getEngineOilTemperature:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"015C" key:@"temperature" command:command];
}

- (void)getFuelInjectionTiming:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"015D" key:@"timing" command:command];
}

- (void)getFuelRate:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"015E" key:@"rate" command:command];
}

#pragma mark - Oxygen Sensors

- (void)getO2Sensor1Voltage:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0114" key:@"voltage" command:command];
}

- (void)getO2Sensor2Voltage:(CDVInvokedUrlCommand*)command {
    [self getOBDValue:@"0115" key:@"voltage" command:command];
}

#pragma mark - Diagnostic Trouble Codes

- (void)getDTCs:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSArray *dtcs = [self.obd2Manager getDTCs];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                       messageAsArray:dtcs];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getPendingDTCs:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSArray *dtcs = [self.obd2Manager getPendingDTCs];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                       messageAsArray:dtcs];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)clearDTCs:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        BOOL success = [self.obd2Manager clearDTCs];

        CDVPluginResult *result;
        if (success) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                        messageAsString:@"DTCs cleared"];
        } else {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                        messageAsString:@"Failed to clear DTCs"];
        }

        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getDTCCount:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSInteger count = [self.obd2Manager getDTCCount];

        NSDictionary *resultDict = @{@"count": @(count)};
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

#pragma mark - Vehicle Information

- (void)getVIN:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSString *vin = [self.obd2Manager getVIN];

        NSDictionary *resultDict = @{@"vin": vin ?: @"Unknown"};
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getCalibrationID:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSString *calibrationId = [self.obd2Manager getCalibrationID];

        NSDictionary *resultDict = @{@"calibrationId": calibrationId ?: @"Unknown"};
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getECUName:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSString *ecuName = [self.obd2Manager getECUName];

        NSDictionary *resultDict = @{@"ecuName": ecuName ?: @"Unknown"};
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

#pragma mark - Advanced Features

- (void)getMultiplePIDs:(CDVInvokedUrlCommand*)command {
    NSArray *pids = [command.arguments objectAtIndex:0];

    [self.commandDelegate runInBackground:^{
        NSDictionary *results = [self.obd2Manager getMultiplePIDs:pids];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:results];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)sendCustomCommand:(CDVInvokedUrlCommand*)command {
    NSString *cmd = [command.arguments objectAtIndex:0];

    [self.commandDelegate runInBackground:^{
        NSString *response = [self.obd2Manager sendCommand:cmd];

        NSDictionary *resultDict = @{
            @"command": cmd,
            @"response": response ?: @""
        };

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getSupportedPIDs:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSArray *supportedPIDs = [self.obd2Manager getSupportedPIDs];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                       messageAsArray:supportedPIDs];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getVehicleSnapshot:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSDictionary *snapshot = [self.obd2Manager getVehicleSnapshot];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:snapshot];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)startMonitoring:(CDVInvokedUrlCommand*)command {
    NSArray *pids = [command.arguments objectAtIndex:0];
    NSNumber *interval = [command.arguments objectAtIndex:1];

    self.monitoringCallbackId = command.callbackId;

    __weak typeof(self) weakSelf = self;
    [self.obd2Manager startMonitoring:pids
                             interval:[interval doubleValue] / 1000.0
                             callback:^(NSDictionary *data) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:data];
        [result setKeepCallbackAsBool:YES];
        [weakSelf.commandDelegate sendPluginResult:result
                                        callbackId:weakSelf.monitoringCallbackId];
    }];
}

- (void)stopMonitoring:(CDVInvokedUrlCommand*)command {
    [self.obd2Manager stopMonitoring];
    self.monitoringCallbackId = nil;

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsString:@"Monitoring stopped"];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)getFreezeFrameData:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSDictionary *freezeFrame = [self.obd2Manager getFreezeFrameData];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:freezeFrame];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getFuelSystemStatus:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSDictionary *status = [self.obd2Manager getFuelSystemStatus];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:status];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)getOBDStandards:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSDictionary *standards = [self.obd2Manager getOBDStandards];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:standards];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

#pragma mark - Helper Methods

- (void)getOBDValue:(NSString *)obdCommand key:(NSString *)key command:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSNumber *value = [self.obd2Manager getOBDValue:obdCommand];

        if (value) {
            NSDictionary *resultDict = @{key: value};
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                    messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        } else {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                        messageAsString:@"Failed to get OBD data"];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }];
}

- (void)onAppTerminate {
    [self.obd2Manager disconnect];
}

@end
