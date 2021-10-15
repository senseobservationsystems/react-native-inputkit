//
//  HealthBridge.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 23/05/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation
import HealthKit

//@objc (HealthBridge)
extension HealthBridge { // No NSObject inheritance, since RCTEventEmitter inherits from NSObject already
  
    @objc func getStepCount(_ startDate: Date, endDate: Date, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
      
      let stepCountType = HKObjectType.quantityType(forIdentifier: .stepCount)!
      let predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: .strictEndDate)
      
      querySum(forType: stepCountType, predicate: predicate, unit: HKUnit.count()) {
        (value, start, end, error) in
        // This callback will be called when query for sum of step count is completed
        
        // Error Check:
        guard error == nil else {
          reject(kHealthKitError, "Failed in getStepCount", error)
          return
        }
        
        guard value != nil, start != nil, end != nil else {
          // HK does not provide start, end if there is no value is registered today.
          // So we need to use the startDate and endDate given in the query.
          resolve(0.0)
          return
        }
        
        // Handle Success:
        resolve(value!)
      }
    }
  
    @objc func getDistance(_ startDate: Date, endDate: Date, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
      let distanceType = HKObjectType.quantityType(forIdentifier: .distanceWalkingRunning)!
      let interval = "minute"; // TODO: should be received as an argument
      
      guard let intervalComponents = intervals[interval] else {
        reject(kHealthKitError, "Failed in getDistance", InputKitError.unknownInterval)
        return
      }
      
      queryDistribution(forType: distanceType, startDate: startDate, endDate: endDate, interval: intervalComponents, unit: HKUnit.meter()) {
        (value, startDate, endDate, error) in
        // This callback will be called when query for sum of step count is completed
        
        // Error Check:
        guard error == nil, value != nil, startDate != nil, endDate != nil else {
          reject(kHealthKitError, "Failed in getDistance", error)
          return
        }
        
        // Handle Success:
        // TODO: resolve with only the sum of all values
        // TODO: 2, take a deep look at the edge intervals to remove not desired part
        
        var sumDistanceValue = 0.0
        if let queryObjects = value {
          for queryObject in queryObjects {
            if let distance = queryObject["value"] {
             sumDistanceValue = sumDistanceValue + (distance as! Double)
            }
          }
        }
        
        resolve(sumDistanceValue)
      }
    }
  
    @objc func getAccurateDistance(_ startDate: Date, endDate: Date, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
      let distanceType = HKObjectType.quantityType(forIdentifier: .distanceWalkingRunning)!
      let interval = "minute"; // TODO: should be received as an argument
      
      guard let intervalComponents = intervals[interval] else {
        reject(kHealthKitError, "Failed in getAccurateDistance", InputKitError.unknownInterval)
        return
      }
      
      queryDistribution(forType: distanceType, startDate: startDate, endDate: endDate, interval: intervalComponents, unit: HKUnit.meter()) {
        (value, startDate, endDate, error) in
        // This callback will be called when query for sum of step count is completed
        
        // Error Check:
        guard error == nil, value != nil, startDate != nil, endDate != nil else {
          reject(kHealthKitError, "Failed in getAccurateDistance", error)
          return
        }
        
        // Handle Success:
        // TODO: resolve with only the sum of all values
        // TODO: 2, take a deep look at the edge intervals to remove not desired part
        
        var sumDistanceValue = 0.0
        if let distanceValues = value {
          for distanceValue in distanceValues {
            sumDistanceValue = sumDistanceValue + Double(distanceValue["value"] as! Int)
          }
        }
        
        resolve(sumDistanceValue)
      }
    }
  
    
    // Get distance samples between start and end date (inclusive + overlapping) with the latest ones first and limit them by the limit count.
    // The distance sample values returned are always in meters
    // Specify a limit of 0 for unlimited samples
    @objc func getDistanceSamples(_ startDate: Date, endDate: Date, limit: Int, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
        
        var actualLimit = limit
        if actualLimit == 0 {
            actualLimit = HKObjectQueryNoLimit
        }
        
        if let distanceType = HKObjectType.quantityType(forIdentifier: .distanceWalkingRunning) {
            querySamples(forType: distanceType, startDate: startDate, endDate: endDate, limit: actualLimit, sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)]) {
                (results, error) in
                // This callback will be called when query is completed
                
                // Error Check:
                guard error == nil, let samples = results else {
                    reject(kHealthKitError, "Failed in getDistanceSamples", error)
                    return
                }
                
                let dateFormatString = "yyyy-MM-dd HH:mm:ss Z"
                var distanceSamples: [[String : Any]] = []
                
                for item in samples {
                    if let sample = item as? HKQuantitySample {
                        let sampleDict = [
                            "startDate": [
                                "timestamp": sample.startDate.unixTimestamp,
                                "formattedString": sample.startDate.toString(dateFormat: dateFormatString)
                            ],
                            "endDate": [
                                "timestamp": sample.endDate.unixTimestamp,
                                "formattedString": sample.endDate.toString(dateFormat: dateFormatString)
                            ],
                            "value": sample.quantity.doubleValue( for: HKUnit.meter() )
                            ] as [String : Any]
                        
                        distanceSamples.append(sampleDict)
                    }
                }
                
                resolve(distanceSamples)
            }
        }
    }
  
    @objc func getStepCountDistribution(_ startDate: Date, endDate: Date, interval: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
      
      let stepCountType = HKObjectType.quantityType(forIdentifier: .stepCount)!
      
      guard let intervalComponents = intervals[interval] else {
        reject(kHealthKitError, "Failed in getStepCountDistribution", InputKitError.unknownInterval)
        return
      }
      
      queryDistribution(forType: stepCountType, startDate: startDate, endDate: endDate, interval: intervalComponents, unit: HKUnit.count()) {
        (value, startDate, endDate, error) in
        // This callback will be called when query for sum of step count is completed
        
        // Error Check:
        guard error == nil, value != nil, startDate != nil, endDate != nil else {
          reject(kHealthKitError, "Failed in getStepCountDistribution", error)
          return
        }
        
        // Handle Success:
        let dateFormatString = "yyyy-MM-dd HH:mm:ss Z"
        resolve([
          "startDate": [
            "timestamp": startDate!.unixTimestamp,
            "formattedString": startDate!.toString(dateFormat: dateFormatString)
          ],
          "endDate": [
            "timestamp": endDate!.unixTimestamp,
            "formattedString": endDate!.toString(dateFormat: dateFormatString)
          ],
          "value": value! // Array of dictionaries
        ])
      }
    }
  
  @objc func getSleepAnalysisSamples(_ startDate: Date, endDate: Date, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      if let sleepType = HKObjectType.categoryType(forIdentifier: .sleepAnalysis) {
        querySamples(forType: sleepType, startDate: startDate, endDate: endDate, limit: HKObjectQueryNoLimit, sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)]) {
          (results, error) in
          // This callback will be called when query for sleep analysis is completed
          
          // Error Check:
          guard error == nil, let samples = results else {
            reject(kHealthKitError, "Failed to get sleep analysis samples", error)
            return
          }
          
          let dateFormatString = "yyyy-MM-dd HH:mm:ss Z"
          var sleepSamples: [[String : Any]] = []
          
          for item in samples {
            if let sample = item as? HKCategorySample {
              let type: String
              if #available(iOS 10.0, *) {
                switch sample.value {
                case HKCategoryValueSleepAnalysis.inBed.rawValue:
                  type = "InBed"
                case HKCategoryValueSleepAnalysis.asleep.rawValue:
                  type = "Asleep"
                case HKCategoryValueSleepAnalysis.awake.rawValue:
                  type = "Awake"
                default:
                  type = "Undefined"
                }
              } else {
                switch sample.value {
                case HKCategoryValueSleepAnalysis.inBed.rawValue:
                  type = "InBed"
                case HKCategoryValueSleepAnalysis.asleep.rawValue:
                  type = "Asleep"
                default:
                  type = "Undefined"
                }
              }
              
              let sampleDict = [
                "startDate": [
                  "timestamp": sample.startDate.unixTimestamp,
                  "formattedString": sample.startDate.toString(dateFormat: dateFormatString)
                ],
                "endDate": [
                  "timestamp": sample.endDate.unixTimestamp,
                  "formattedString": sample.endDate.toString(dateFormat: dateFormatString)
                ],
                "type": type
                ] as [String : Any]
              
              sleepSamples.append(sampleDict)
            }
          }
          
          resolve(sleepSamples)
        }
      }
    }
  
    func emitEvent(){
      NativeLogger.logNativeEvent("Log called at \(#function) at \(Date())")
      self.sendEvent(withName: SupportedEvents.inputKitUpdates,
                      body: ["topic": "PushNotification",
                             "success": true])
    }
}
