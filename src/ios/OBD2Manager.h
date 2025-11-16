//
//  OBD2Manager.h
//  OBD2 Manager for iOS
//

#import <Foundation/Foundation.h>
#import <ExternalAccessory/ExternalAccessory.h>

typedef void (^MonitoringCallback)(NSDictionary *data);

@interface OBD2Manager : NSObject

// Connection
- (NSArray *)scanDevices;
- (BOOL)connectToDevice:(NSString *)deviceId;
- (void)disconnect;
- (BOOL)isConnected;
- (BOOL)initializeConnection;

// Command Execution
- (NSString *)sendCommand:(NSString *)command;
- (NSNumber *)getOBDValue:(NSString *)command;
- (NSDictionary *)getMultiplePIDs:(NSArray *)pids;

// DTC Functions
- (NSArray *)getDTCs;
- (NSArray *)getPendingDTCs;
- (BOOL)clearDTCs;
- (NSInteger)getDTCCount;

// Vehicle Information
- (NSString *)getVIN;
- (NSString *)getCalibrationID;
- (NSString *)getECUName;

// Advanced Features
- (NSArray *)getSupportedPIDs;
- (NSDictionary *)getVehicleSnapshot;
- (void)startMonitoring:(NSArray *)pids interval:(NSTimeInterval)interval callback:(MonitoringCallback)callback;
- (void)stopMonitoring;
- (NSDictionary *)getFreezeFrameData;
- (NSDictionary *)getFuelSystemStatus;
- (NSDictionary *)getOBDStandards;

@end
