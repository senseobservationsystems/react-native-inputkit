//
//  Constants.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 23/05/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

let kInputKitError = "InputKitError"
let kHealthKitError = "HealthKitError"

enum InputKitError: CustomNSError {
    case invalidDate
    case unknownInterval
    case emptyFieldInHKResult
    case nilSelfInClosure
    case unsupportedType
//    case resultIsNotSumQuantity

    var errorUserInfo: [String : Any] {
        switch self {
        case .invalidDate:
            return [NSLocalizedDescriptionKey: "The given date was invalid"]
        case .unknownInterval:
          return [NSLocalizedDescriptionKey: "The given interval is unknown."]
        case .emptyFieldInHKResult:
            return [NSLocalizedDescriptionKey: "HKResult returned from Query contained empty fields"]
        case .nilSelfInClosure:
            return [NSLocalizedDescriptionKey: "Lost reference to self due to weak reference."]
        case .unsupportedType:
            return [NSLocalizedDescriptionKey: "The specified type is unsupported for this query"]
//        case .resultIsNotSumQuantity:
//            return [NSLocalizedDescriptionKey: "The result is not sumQuantity, while the query expects sumQuantity"]
        }
    }
}

struct SupportedEvents {
  static let inputKitUpdates = "inputKitUpdates"
  static let inputKitTracking = "inputKitTracking"
}

struct SampleType {
  static let sleep = "sleep"
  static let stepCount = "stepCount"
  static let distanceWalkingRunning = "distanceWalkingRunning"
}

struct ProviderName {
  static let healthKit = "healthKit"
}

let intervals: [String : DateComponents] = [
  "week": DateComponents(weekOfYear: 1),
  "day": DateComponents(day: 1),
  "hour": DateComponents(hour: 1),
  "tenMinute": DateComponents(minute: 10),
  "minute": DateComponents(minute: 1)
]