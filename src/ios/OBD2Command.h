//
//  OBD2Command.h
//  OBD2 Command Parser
//

#import <Foundation/Foundation.h>

@interface OBD2Command : NSObject

+ (double)parseResponse:(NSString *)command withData:(NSString *)response;

@end
