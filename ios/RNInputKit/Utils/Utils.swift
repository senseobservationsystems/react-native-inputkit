//
//  Utils.swift
//  RNBackgroundGeolocationSample
//
//  Created by Tatsuya Kaneko on 07/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

@objc(Utils)
class Utils: NSObject {

  @objc func abort() {
    NativeLogger.logNativeEvent("=====================Crash!!===================================")
    fatalError()
  }
}

