//
//  Header.h
//  RNBackgroundGeolocationSample
//
//  Created by Tatsuya Kaneko on 07/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NativeLogger: NSObject
+(bool) enabled;
+(void) logNativeEvent: (NSString*) line;
+(void) log: (NSString*) line;
@end
