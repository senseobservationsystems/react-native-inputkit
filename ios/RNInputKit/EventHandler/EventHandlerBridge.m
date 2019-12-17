//
//  EventHandler.m
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 29/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTConvert.h>
//#import "InputKitProber-Swift.h"

@interface RCT_EXTERN_MODULE(EventHandlerBridge, NSObject)

RCT_EXTERN_METHOD(onListenerReady:(NSString * _Nonnull)name resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(onEventDidProcessed:(NSString * _Nonnull)eventId resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

+ (BOOL) requiresMainQueueSetup {
  return false;
}

@end
