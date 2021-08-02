//
//  UtilsIKBridge.m
//  RNBackgroundGeolocationSample
//
//  Created by Tatsuya Kaneko on 07/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//


#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(UtilsIK, NSObject)

RCT_EXTERN_METHOD(abort);

+ (BOOL) requiresMainQueueSetup {
  return false;
}

@end
