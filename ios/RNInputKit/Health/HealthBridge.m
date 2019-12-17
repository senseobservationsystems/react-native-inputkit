//
//  HealthBridge.m
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 23/05/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTConvert.h>
//#import "InputKitProber-Swift.h"

@interface RCT_EXTERN_MODULE(HealthBridge, NSObject)

RCT_EXTERN_METHOD(isAvailable:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(isProviderInstalled:(NSString * _Nonnull)providerName resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(requestPermissions:(NSArray<NSString *> * _Nonnull)typesToRead resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getStepCount:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getDistance:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getDistanceSamples:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate limit:(NSInteger * _Nonnull)limit resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getStepCountDistribution:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate interval:(NSString * _Nonnull)interval resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getAccurateDistance:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getSleepAnalysisSamples:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(startMonitoring:(NSString * _Nonnull)typeString resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(stopMonitoring:(NSString * _Nonnull)typeString resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

// Tracking Features using CoreMotion

RCT_EXTERN_METHOD(startTracking:(NSString * _Nonnull)type startDate: (NSDate * _Nonnull)startDate resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(stopTracking:(NSString * _Nonnull)type resolve: (RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(getHistoricActivityTrackingData:(NSDate * _Nonnull)startDate endDate:(NSDate * _Nonnull)endDate resolve:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

RCT_EXTERN_METHOD(stopTrackingAll:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);


+ (BOOL) requiresMainQueueSetup {
  return false;
}

@end
