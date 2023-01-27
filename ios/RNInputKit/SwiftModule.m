//
//  SwiftModule.m
//  RNInputKit
//
//  Created by Xavier Ramos Oliver on 16/12/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(SwiftModule, NSObject)

RCT_EXTERN_METHOD(swiftMethod:(RCTPromiseResolveBlock _Nonnull)resolve reject:(RCTPromiseRejectBlock _Nonnull)reject);

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

@end
