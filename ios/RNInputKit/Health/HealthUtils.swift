//
//  HealthUtils.swift
//  Erasmus
//
//  Created by Tatsuya Kaneko on 15/08/2017.
//  Copyright © 2017 Sense Health BV. All rights reserved.
//

import Foundation
import HealthKit

class HealthUtils {
  
  static func getTopic(forHKSampleType sampleType: HKSampleType) -> String? {
    
    let keys = HealthBridge.readPermissions.allKeys(forValue: sampleType)
    guard keys.count == 1 else { return nil }
    
    return keys[0]
  }
}
