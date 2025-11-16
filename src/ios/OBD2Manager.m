//
//  OBD2Manager.m
//  OBD2 Manager for iOS
//

#import "OBD2Manager.h"
#import "OBD2Command.h"

@interface OBD2Manager () <NSStreamDelegate>
@property (nonatomic, strong) EASession *session;
@property (nonatomic, strong) EAAccessory *accessory;
@property (nonatomic, strong) NSInputStream *inputStream;
@property (nonatomic, strong) NSOutputStream *outputStream;
@property (nonatomic, assign) BOOL connected;
@property (nonatomic, strong) NSTimer *monitoringTimer;
@property (nonatomic, copy) MonitoringCallback monitoringCallback;
@property (nonatomic, strong) NSArray *monitoringPIDs;
@property (nonatomic, strong) NSMutableData *receivedData;
@end

@implementation OBD2Manager

- (instancetype)init {
    self = [super init];
    if (self) {
        _connected = NO;
        _receivedData = [NSMutableData data];

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(accessoryConnected:)
                                                     name:EAAccessoryDidConnectNotification
                                                   object:nil];

        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(accessoryDisconnected:)
                                                     name:EAAccessoryDidDisconnectNotification
                                                   object:nil];

        [[EAAccessoryManager sharedAccessoryManager] registerForLocalNotifications];
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self disconnect];
}

#pragma mark - Connection Management

- (NSArray *)scanDevices {
    NSMutableArray *devices = [NSMutableArray array];
    NSArray *accessories = [[EAAccessoryManager sharedAccessoryManager] connectedAccessories];

    for (EAAccessory *accessory in accessories) {
        NSDictionary *deviceInfo = @{
            @"name": accessory.name ?: @"Unknown",
            @"address": @(accessory.connectionID).stringValue,
            @"manufacturer": accessory.manufacturer ?: @"Unknown",
            @"model": accessory.modelNumber ?: @"Unknown",
            @"serial": accessory.serialNumber ?: @"Unknown"
        };
        [devices addObject:deviceInfo];
    }

    return devices;
}

- (BOOL)connectToDevice:(NSString *)deviceId {
    NSArray *accessories = [[EAAccessoryManager sharedAccessoryManager] connectedAccessories];

    for (EAAccessory *accessory in accessories) {
        if ([@(accessory.connectionID).stringValue isEqualToString:deviceId]) {
            return [self connectToAccessory:accessory];
        }
    }

    return NO;
}

- (BOOL)connectToAccessory:(EAAccessory *)accessory {
    if (!accessory) return NO;

    // Try to find WiFi AT Command protocol first (for WiFi OBD2 adapters)
    NSString *protocolString = nil;
    for (NSString *protocol in accessory.protocolStrings) {
        if ([protocol rangeOfString:@"com."].location != NSNotFound) {
            protocolString = protocol;
            break;
        }
    }

    if (!protocolString && accessory.protocolStrings.count > 0) {
        protocolString = accessory.protocolStrings[0];
    }

    if (!protocolString) {
        NSLog(@"No suitable protocol found");
        return NO;
    }

    self.accessory = accessory;
    self.session = [[EASession alloc] initWithAccessory:accessory forProtocol:protocolString];

    if (!self.session) {
        NSLog(@"Failed to create session");
        return NO;
    }

    self.inputStream = self.session.inputStream;
    self.outputStream = self.session.outputStream;

    [self.inputStream setDelegate:self];
    [self.outputStream setDelegate:self];

    [self.inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [self.outputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];

    [self.inputStream open];
    [self.outputStream open];

    self.connected = YES;

    NSLog(@"Connected to OBD2 device: %@", accessory.name);
    return YES;
}

- (void)disconnect {
    self.connected = NO;
    [self stopMonitoring];

    if (self.inputStream) {
        [self.inputStream close];
        [self.inputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        self.inputStream = nil;
    }

    if (self.outputStream) {
        [self.outputStream close];
        [self.outputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        self.outputStream = nil;
    }

    self.session = nil;
    self.accessory = nil;
}

- (BOOL)isConnected {
    return self.connected && self.session != nil;
}

- (BOOL)initializeConnection {
    if (![self isConnected]) return NO;

    // Reset all
    [self sendRawCommand:@"ATZ"];
    [NSThread sleepForTimeInterval:1.0];

    // Echo off
    [self sendRawCommand:@"ATE0"];
    [NSThread sleepForTimeInterval:0.1];

    // Linefeed off
    [self sendRawCommand:@"ATL0"];
    [NSThread sleepForTimeInterval:0.1];

    // Spaces off
    [self sendRawCommand:@"ATS0"];
    [NSThread sleepForTimeInterval:0.1];

    // Headers off
    [self sendRawCommand:@"ATH0"];
    [NSThread sleepForTimeInterval:0.1];

    // Set protocol to auto
    [self sendRawCommand:@"ATSP0"];
    [NSThread sleepForTimeInterval:0.1];

    // Verify connection
    NSString *response = [self sendCommand:@"0100"];
    return response && ![response containsString:@"ERROR"];
}

#pragma mark - Command Execution

- (NSString *)sendCommand:(NSString *)command {
    if (![self isConnected]) {
        NSLog(@"Not connected to OBD2 device");
        return nil;
    }

    [self.receivedData setLength:0];

    NSString *cmd = [NSString stringWithFormat:@"%@\r", command];
    NSData *data = [cmd dataUsingEncoding:NSASCIIStringEncoding];

    NSInteger bytesWritten = [self.outputStream write:data.bytes maxLength:data.length];
    if (bytesWritten <= 0) {
        NSLog(@"Failed to write command");
        return nil;
    }

    // Wait for response
    NSDate *timeout = [NSDate dateWithTimeIntervalSinceNow:5.0];
    while ([timeout timeIntervalSinceNow] > 0) {
        [NSRunLoop.currentRunLoop runMode:NSDefaultRunLoopMode beforeDate:[NSDate dateWithTimeIntervalSinceNow:0.1]];

        NSString *response = [[NSString alloc] initWithData:self.receivedData encoding:NSASCIIStringEncoding];
        if ([response containsString:@">"]) {
            // Remove prompt and clean up
            response = [response stringByReplacingOccurrencesOfString:@">" withString:@""];
            response = [response stringByReplacingOccurrencesOfString:@" " withString:@""];
            response = [response stringByReplacingOccurrencesOfString:@"\r" withString:@""];
            response = [response stringByReplacingOccurrencesOfString:@"\n" withString:@""];

            NSLog(@"Command: %@ Response: %@", command, response);
            return response;
        }
    }

    NSLog(@"Command timeout");
    return nil;
}

- (void)sendRawCommand:(NSString *)command {
    NSString *cmd = [NSString stringWithFormat:@"%@\r", command];
    NSData *data = [cmd dataUsingEncoding:NSASCIIStringEncoding];
    [self.outputStream write:data.bytes maxLength:data.length];
    [NSThread sleepForTimeInterval:0.1];
    [self.receivedData setLength:0];
}

- (NSNumber *)getOBDValue:(NSString *)command {
    NSString *response = [self sendCommand:command];
    if (!response || [response containsString:@"ERROR"] || [response containsString:@"NODATA"]) {
        return nil;
    }

    double value = [OBD2Command parseResponse:command withData:response];
    return @(value);
}

- (NSDictionary *)getMultiplePIDs:(NSArray *)pids {
    NSMutableDictionary *results = [NSMutableDictionary dictionary];

    for (NSString *pid in pids) {
        NSNumber *value = [self getOBDValue:pid];
        results[pid] = value ?: [NSNull null];
    }

    return results;
}

#pragma mark - NSStreamDelegate

- (void)stream:(NSStream *)stream handleEvent:(NSStreamEvent)eventCode {
    switch (eventCode) {
        case NSStreamEventHasBytesAvailable: {
            if (stream == self.inputStream) {
                uint8_t buffer[1024];
                NSInteger bytesRead = [self.inputStream read:buffer maxLength:sizeof(buffer)];
                if (bytesRead > 0) {
                    [self.receivedData appendBytes:buffer length:bytesRead];
                }
            }
            break;
        }
        case NSStreamEventErrorOccurred:
            NSLog(@"Stream error: %@", stream.streamError);
            break;
        case NSStreamEventEndEncountered:
            NSLog(@"Stream ended");
            break;
        default:
            break;
    }
}

#pragma mark - Diagnostic Trouble Codes

- (NSArray *)getDTCs {
    NSString *response = [self sendCommand:@"03"];
    if (!response || [response containsString:@"NODATA"]) {
        return @[];
    }
    return [self parseDTCs:response];
}

- (NSArray *)getPendingDTCs {
    NSString *response = [self sendCommand:@"07"];
    if (!response || [response containsString:@"NODATA"]) {
        return @[];
    }
    return [self parseDTCs:response];
}

- (BOOL)clearDTCs {
    NSString *response = [self sendCommand:@"04"];
    return response && ![response containsString:@"ERROR"];
}

- (NSInteger)getDTCCount {
    NSString *response = [self sendCommand:@"0101"];
    if (response && response.length >= 6) {
        NSString *countHex = [response substringWithRange:NSMakeRange(4, 2)];
        unsigned int count;
        [[NSScanner scannerWithString:countHex] scanHexInt:&count];
        return count & 0x7F;
    }
    return 0;
}

- (NSArray *)parseDTCs:(NSString *)response {
    NSMutableArray *dtcs = [NSMutableArray array];

    response = [response stringByReplacingOccurrencesOfString:@"43" withString:@""];
    response = [response stringByReplacingOccurrencesOfString:@"47" withString:@""];

    for (NSUInteger i = 0; i < response.length; i += 4) {
        if (i + 4 <= response.length) {
            NSString *code = [response substringWithRange:NSMakeRange(i, 4)];
            if (![code isEqualToString:@"0000"]) {
                [dtcs addObject:[self formatDTC:code]];
            }
        }
    }

    return dtcs;
}

- (NSString *)formatDTC:(NSString *)hexCode {
    char prefixes[] = {'P', 'C', 'B', 'U'};

    unsigned int firstByte;
    NSString *firstByteHex = [hexCode substringToIndex:2];
    [[NSScanner scannerWithString:firstByteHex] scanHexInt:&firstByte];

    int prefixIndex = (firstByte >> 6) & 0x03;
    char prefix = prefixes[prefixIndex];
    int digit1 = (firstByte >> 4) & 0x03;
    int digit2 = firstByte & 0x0F;
    NSString *lastTwo = [hexCode substringFromIndex:2];

    return [NSString stringWithFormat:@"%c%d%d%@", prefix, digit1, digit2, lastTwo];
}

#pragma mark - Vehicle Information

- (NSString *)getVIN {
    NSString *response = [self sendCommand:@"0902"];
    if (response && ![response containsString:@"NODATA"]) {
        return [self parseASCII:response withHeader:@"49020"];
    }
    return @"Unknown";
}

- (NSString *)getCalibrationID {
    NSString *response = [self sendCommand:@"0904"];
    if (response && ![response containsString:@"NODATA"]) {
        return [self parseASCII:response withHeader:@"4904"];
    }
    return @"Unknown";
}

- (NSString *)getECUName {
    NSString *response = [self sendCommand:@"090A"];
    if (response && ![response containsString:@"NODATA"]) {
        return [self parseASCII:response withHeader:@"490A"];
    }
    return @"Unknown";
}

- (NSString *)parseASCII:(NSString *)response withHeader:(NSString *)header {
    response = [response stringByReplacingOccurrencesOfString:header withString:@""];
    NSMutableString *result = [NSMutableString string];

    for (NSUInteger i = 0; i < response.length; i += 2) {
        if (i + 2 <= response.length) {
            NSString *hex = [response substringWithRange:NSMakeRange(i, 2)];
            unsigned int decimal;
            [[NSScanner scannerWithString:hex] scanHexInt:&decimal];
            if (decimal >= 32 && decimal <= 126) {
                [result appendFormat:@"%c", (char)decimal];
            }
        }
    }

    return [result stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
}

#pragma mark - Advanced Features

- (NSArray *)getSupportedPIDs {
    NSMutableArray *supported = [NSMutableArray array];

    NSString *response1 = [self sendCommand:@"0100"];
    if (response1) [self parseSupportedPIDs:response1 baseID:0x01 supported:supported];

    NSString *response2 = [self sendCommand:@"0120"];
    if (response2) [self parseSupportedPIDs:response2 baseID:0x21 supported:supported];

    NSString *response3 = [self sendCommand:@"0140"];
    if (response3) [self parseSupportedPIDs:response3 baseID:0x41 supported:supported];

    return supported;
}

- (void)parseSupportedPIDs:(NSString *)response baseID:(int)baseID supported:(NSMutableArray *)supported {
    response = [response stringByReplacingOccurrencesOfString:@"41" withString:@""];
    if (response.length >= 8) {
        NSString *bitmap = [response substringToIndex:8];
        unsigned long long value;
        [[NSScanner scannerWithString:bitmap] scanHexLongLong:&value];

        for (int i = 0; i < 32; i++) {
            if (value & (1ULL << (31 - i))) {
                NSString *pid = [NSString stringWithFormat:@"01%02X", baseID + i];
                [supported addObject:pid];
            }
        }
    }
}

- (NSDictionary *)getVehicleSnapshot {
    NSMutableDictionary *snapshot = [NSMutableDictionary dictionary];

    snapshot[@"rpm"] = [self getOBDValue:@"010C"] ?: [NSNull null];
    snapshot[@"speed"] = [self getOBDValue:@"010D"] ?: [NSNull null];
    snapshot[@"coolantTemp"] = [self getOBDValue:@"0105"] ?: [NSNull null];
    snapshot[@"engineLoad"] = [self getOBDValue:@"0104"] ?: [NSNull null];
    snapshot[@"throttle"] = [self getOBDValue:@"0111"] ?: [NSNull null];
    snapshot[@"fuelLevel"] = [self getOBDValue:@"012F"] ?: [NSNull null];
    snapshot[@"timestamp"] = @([[NSDate date] timeIntervalSince1970] * 1000);

    return snapshot;
}

- (void)startMonitoring:(NSArray *)pids interval:(NSTimeInterval)interval callback:(MonitoringCallback)callback {
    self.monitoringPIDs = pids;
    self.monitoringCallback = callback;

    self.monitoringTimer = [NSTimer scheduledTimerWithTimeInterval:interval
                                                            target:self
                                                          selector:@selector(monitoringTick)
                                                          userInfo:nil
                                                           repeats:YES];
}

- (void)stopMonitoring {
    [self.monitoringTimer invalidate];
    self.monitoringTimer = nil;
    self.monitoringCallback = nil;
    self.monitoringPIDs = nil;
}

- (void)monitoringTick {
    if (!self.monitoringCallback) return;

    NSDictionary *data = [self getMultiplePIDs:self.monitoringPIDs];
    NSMutableDictionary *result = [data mutableCopy];
    result[@"timestamp"] = @([[NSDate date] timeIntervalSince1970] * 1000);

    self.monitoringCallback(result);
}

- (NSDictionary *)getFreezeFrameData {
    NSString *response = [self sendCommand:@"0202"];
    NSMutableDictionary *freezeFrame = [NSMutableDictionary dictionary];

    if (response && ![response containsString:@"NODATA"]) {
        freezeFrame[@"raw"] = response;
    }

    return freezeFrame;
}

- (NSDictionary *)getFuelSystemStatus {
    NSString *response = [self sendCommand:@"0103"];
    NSMutableDictionary *status = [NSMutableDictionary dictionary];

    if (response && response.length >= 6) {
        NSString *statusHex = [response substringWithRange:NSMakeRange(4, 2)];
        unsigned int statusCode;
        [[NSScanner scannerWithString:statusHex] scanHexInt:&statusCode];

        status[@"code"] = @(statusCode);
        status[@"description"] = [self getFuelSystemDescription:statusCode];
    }

    return status;
}

- (NSString *)getFuelSystemDescription:(int)code {
    switch (code) {
        case 1: return @"Open loop due to insufficient engine temperature";
        case 2: return @"Closed loop, using oxygen sensor feedback";
        case 4: return @"Open loop due to engine load OR fuel cut due to deceleration";
        case 8: return @"Open loop due to system failure";
        case 16: return @"Closed loop, using at least one oxygen sensor but fault";
        default: return @"Unknown";
    }
}

- (NSDictionary *)getOBDStandards {
    NSString *response = [self sendCommand:@"011C"];
    NSMutableDictionary *standards = [NSMutableDictionary dictionary];

    if (response && response.length >= 6) {
        NSString *standardHex = [response substringWithRange:NSMakeRange(4, 2)];
        unsigned int standardCode;
        [[NSScanner scannerWithString:standardHex] scanHexInt:&standardCode];

        standards[@"code"] = @(standardCode);
        standards[@"description"] = [self getOBDStandardDescription:standardCode];
    }

    return standards;
}

- (NSString *)getOBDStandardDescription:(int)code {
    NSDictionary *descriptions = @{
        @1: @"OBD-II as defined by CARB",
        @2: @"OBD as defined by EPA",
        @3: @"OBD and OBD-II",
        @4: @"OBD-I",
        @5: @"Not OBD compliant",
        @6: @"EOBD (Europe)",
        @7: @"EOBD and OBD-II",
        @8: @"EOBD and OBD",
        @9: @"EOBD, OBD and OBD II",
        @10: @"JOBD (Japan)",
        @11: @"JOBD and OBD II",
        @12: @"JOBD and EOBD",
        @13: @"JOBD, EOBD, and OBD II"
    };

    return descriptions[@(code)] ?: @"Unknown";
}

#pragma mark - Accessory Notifications

- (void)accessoryConnected:(NSNotification *)notification {
    EAAccessory *accessory = notification.userInfo[EAAccessoryKey];
    NSLog(@"Accessory connected: %@", accessory.name);
}

- (void)accessoryDisconnected:(NSNotification *)notification {
    EAAccessory *accessory = notification.userInfo[EAAccessoryKey];
    NSLog(@"Accessory disconnected: %@", accessory.name);

    if (accessory == self.accessory) {
        [self disconnect];
    }
}

@end
