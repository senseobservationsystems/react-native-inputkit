//
//  RNInputKit.m
//  RNInputKit
//
//  Created by Xavier Ramos Oliver on 09/12/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNInputKit.h"

@implementation RNInputKit

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(test:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    resolve(@"test method (native)");
}

@end

