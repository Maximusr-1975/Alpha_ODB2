//
//  OBD2Command.m
//  OBD2 Command Parser
//

#import "OBD2Command.h"

@implementation OBD2Command

+ (double)parseResponse:(NSString *)command withData:(NSString *)response {
    // Remove command echo and response header
    response = [response stringByReplacingOccurrencesOfString:command withString:@""];
    response = [response stringByReplacingOccurrencesOfString:@"41" withString:@""];
    response = [response stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];

    // Engine RPM
    if ([command isEqualToString:@"010C"]) {
        return [self parseRPM:response];
    }
    // Vehicle Speed
    else if ([command isEqualToString:@"010D"]) {
        return [self parseByteUnsigned:response offset:0];
    }
    // Coolant Temperature
    else if ([command isEqualToString:@"0105"] ||
             [command isEqualToString:@"010F"] ||  // Intake temp
             [command isEqualToString:@"0146"] ||  // Ambient temp
             [command isEqualToString:@"015C"]) {  // Oil temp
        return [self parseTemperature:response];
    }
    // Engine Load, Throttle, Fuel Level, etc (percentage)
    else if ([command isEqualToString:@"0104"] ||
             [command isEqualToString:@"0111"] ||
             [command isEqualToString:@"012F"] ||
             [command isEqualToString:@"0145"] ||
             [command isEqualToString:@"0147"] ||
             [command isEqualToString:@"0148"] ||
             [command isEqualToString:@"0149"] ||
             [command isEqualToString:@"014A"] ||
             [command isEqualToString:@"014B"] ||
             [command isEqualToString:@"014C"] ||
             [command isEqualToString:@"0152"] ||
             [command isEqualToString:@"015A"] ||
             [command isEqualToString:@"015B"]) {
        return [self parsePercentage:response];
    }
    // MAF Flow
    else if ([command isEqualToString:@"0110"]) {
        return [self parseMAF:response];
    }
    // Fuel Pressure
    else if ([command isEqualToString:@"010A"]) {
        return [self parseFuelPressure:response];
    }
    // Intake Manifold Pressure, Barometric Pressure
    else if ([command isEqualToString:@"010B"] ||
             [command isEqualToString:@"0133"]) {
        return [self parseByteUnsigned:response offset:0];
    }
    // Timing Advance
    else if ([command isEqualToString:@"010E"]) {
        return [self parseTimingAdvance:response];
    }
    // Runtime
    else if ([command isEqualToString:@"011F"] ||
             [command isEqualToString:@"014D"] ||
             [command isEqualToString:@"014E"]) {
        return [self parseRuntime:response];
    }
    // Distance
    else if ([command isEqualToString:@"0121"]) {
        return [self parseTwoByteUnsigned:response];
    }
    // Control Module Voltage
    else if ([command isEqualToString:@"0142"]) {
        return [self parseVoltage:response];
    }
    // Fuel Injection Timing
    else if ([command isEqualToString:@"015D"]) {
        return [self parseFuelTiming:response];
    }
    // Fuel Rate
    else if ([command isEqualToString:@"015E"]) {
        return [self parseFuelRate:response];
    }
    // O2 Sensors
    else if ([command isEqualToString:@"0114"] ||
             [command isEqualToString:@"0115"] ||
             [command isEqualToString:@"0116"] ||
             [command isEqualToString:@"0117"]) {
        return [self parseO2Voltage:response];
    }
    // Fuel Trim
    else if ([command isEqualToString:@"0106"] ||
             [command isEqualToString:@"0107"] ||
             [command isEqualToString:@"0108"] ||
             [command isEqualToString:@"0109"] ||
             [command isEqualToString:@"0155"] ||
             [command isEqualToString:@"0156"] ||
             [command isEqualToString:@"0157"] ||
             [command isEqualToString:@"0158"]) {
        return [self parseFuelTrim:response];
    }
    // Evap Pressure
    else if ([command isEqualToString:@"0132"] ||
             [command isEqualToString:@"0153"]) {
        return [self parseEvapPressure:response];
    }
    // Absolute Load
    else if ([command isEqualToString:@"0143"]) {
        return [self parseAbsoluteLoad:response];
    }
    // Catalyst Temperature
    else if ([command isEqualToString:@"013C"] ||
             [command isEqualToString:@"013D"] ||
             [command isEqualToString:@"013E"] ||
             [command isEqualToString:@"013F"]) {
        return [self parseCatalystTemp:response];
    }
    // Fuel Rail Pressure
    else if ([command isEqualToString:@"0159"]) {
        return [self parseFuelRailPressure:response];
    }

    return 0.0;
}

#pragma mark - Parsing Methods

+ (int)parseByteUnsigned:(NSString *)response offset:(int)offset {
    if (response.length < (offset + 1) * 2) return 0;

    NSString *byteHex = [response substringWithRange:NSMakeRange(offset * 2, 2)];
    unsigned int value;
    [[NSScanner scannerWithString:byteHex] scanHexInt:&value];
    return value;
}

+ (int)parseTwoByteUnsigned:(NSString *)response {
    if (response.length < 4) return 0;

    int a = [self parseByteUnsigned:response offset:0];
    int b = [self parseByteUnsigned:response offset:1];
    return (a * 256) + b;
}

+ (double)parseTemperature:(NSString *)response {
    int a = [self parseByteUnsigned:response offset:0];
    return a - 40.0;
}

+ (double)parsePercentage:(NSString *)response {
    int a = [self parseByteUnsigned:response offset:0];
    return (a * 100.0) / 255.0;
}

+ (double)parseRPM:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return value / 4.0;
}

+ (double)parseMAF:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return value / 100.0;
}

+ (double)parseFuelPressure:(NSString *)response {
    int a = [self parseByteUnsigned:response offset:0];
    return a * 3.0;
}

+ (double)parseTimingAdvance:(NSString *)response {
    int a = [self parseByteUnsigned:response offset:0];
    return (a - 128.0) / 2.0;
}

+ (double)parseRuntime:(NSString *)response {
    return [self parseTwoByteUnsigned:response];
}

+ (double)parseVoltage:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return value / 1000.0;
}

+ (double)parseFuelTiming:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return (value - 26880.0) / 128.0;
}

+ (double)parseFuelRate:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return value / 20.0;
}

+ (double)parseO2Voltage:(NSString *)response {
    int a = [self parseByteUnsigned:response offset:0];
    return a / 200.0;
}

+ (double)parseFuelTrim:(NSString *)response {
    int a = [self parseByteUnsigned:response offset:0];
    return ((a - 128.0) * 100.0) / 128.0;
}

+ (double)parseEvapPressure:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return value - 32767.0;
}

+ (double)parseAbsoluteLoad:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return (value * 100.0) / 255.0;
}

+ (double)parseCatalystTemp:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return (value / 10.0) - 40.0;
}

+ (double)parseFuelRailPressure:(NSString *)response {
    int value = [self parseTwoByteUnsigned:response];
    return value * 10.0;
}

@end
