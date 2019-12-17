//
//  RNUtils.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 01/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

@objc(Logger)
class RNLogger: NSObject {

  @objc func log(_ _line: String){
    NativeLogger.log("@@@ [\(Date())] [JS] \(_line)\n")
  }

}
