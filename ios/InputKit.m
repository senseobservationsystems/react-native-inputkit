#import "InputKit.h"


@implementation InputKit

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(sampleMethod:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    resolve(@"hola!");
}

@end
