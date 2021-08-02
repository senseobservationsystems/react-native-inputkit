//
//  UtilsIK.swift
//  RNBackgroundGeolocationSample
//
//  Created by Tatsuya Kaneko on 07/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

@objc(UtilsIK)
class UtilsIK: NSObject {

  @objc func abort() {
    NativeLogger.logNativeEvent("=====================Crash!!===================================")
    fatalError()
  }
}

