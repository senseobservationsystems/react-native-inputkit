//
//  SwiftModule.swift
//  RNInputKit
//
//  Created by Xavier Ramos Oliver on 16/12/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//
import Foundation

@objc(SwiftModule)
class SwiftModule: NSObject {
    @objc func swiftMethod(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        resolve("swift method (native)");
  }
}
