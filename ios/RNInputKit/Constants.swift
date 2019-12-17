//
//  Constants.swift
//  irisSample
//
//  Created by Umar Nizamani on 25/01/2017.
//  Copyright © 2017 Sense Health BV. All rights reserved.
//

// TODO 265: check if this file is still necessary

import Foundation

struct ErasmusConstants {
  
  enum RNApp {
    static let kTarget = "target"
    static let kBundleName = "CFBundleName"
    static let kStaging = "staging"
  }
  
  enum JSSupportedEvents {
    static let actionTrigger = "ACTION_DID_TRIGGER"
    static let requestSessionId = "REQUEST_VALID_SESSION_ID"
  }
  
  enum Error {
    static let kErrorState = "error_state"
    static let kErrorIris = "error_iris"
  }
}
