//
//  NativeUtils.m
//  RNBackgroundGeolocationSample
//
//  Created by Tatsuya Kaneko on 07/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NativeLogger.h"


@implementation NativeLogger

static bool enabled = true;

+(void) logNativeEvent: (NSString*) line {
  if (enabled) {
    [self log: [NSString stringWithFormat: @"### [%@] [Native] %@ \n", [NSDate date], line]];
  }
}

+(void) log: (NSString*) line {
  dispatch_async(dispatch_get_main_queue(), ^{
    NSLog(@"[SenseLogger] %@\n", line);
    
    NSString *documentsDirectory = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents"];
    NSString *fileName = [documentsDirectory stringByAppendingPathComponent:@"Log.txt"];

    NSLog(@"Logging to file at %@", fileName);
    
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:fileName];
    if (fileHandle){
      [fileHandle seekToEndOfFile];
      [fileHandle writeData:[line dataUsingEncoding:NSUTF8StringEncoding]];
      [fileHandle closeFile];
    }
    else{
      [line writeToFile:fileName
                atomically:NO
                  encoding:NSStringEncodingConversionAllowLossy
                     error:nil];
    }
  });
}

@end
