//
//  UtilBridge.m
//  RNBackgroundGeolocationSample
//
//  Created by Tatsuya Kaneko on 06/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

//#import "RNBackgroundGeolocationSample-Swift.h"

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(Logger, NSObject)

RCT_EXTERN_METHOD(log:(NSString * _Nonnull)_line);

+ (BOOL) requiresMainQueueSetup {
  return false;
}

@end
