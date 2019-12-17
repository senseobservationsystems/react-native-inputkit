//
//  HealthBridge+Anchors.swift
//  Erasmus
//
//  Created by Umar Nizamani on 06/10/2017.
//  Copyright © 2017 Sense Health BV. All rights reserved.
//

import Foundation
import HealthKit
import CoreMotion

extension HealthBridge {

  // Start tracking realtime activity updates after the specified startDate
  // This function emits a tracking event everytime there is a new sample after the specified start date
  @objc func startTracking(_ type: String, startDate: Date, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    
    if (type != SampleType.stepCount && type != SampleType.distanceWalkingRunning) {
      reject(kInputKitError, "Real time activity tracking only supports stepCount and distanceWalkingRunning sample type", InputKitError.unsupportedType)
      return
    }

    NativeLogger.logNativeEvent("\(NSStringFromClass(Swift.type(of: self)))::\(#function)")
    self.currentTrackedTypes.append(type)
    
    self.pedoMeter.startUpdates(from: startDate, withHandler:  {
      (data: CMPedometerData?, error) -> Void in
      guard error == nil else {
        print("[Error] \(#function): \(String(describing: error))")
        reject(kInputKitError, "Got error when reading pedometer data", error)
        return
      }
      
      if let sampleData = data {
        let dateFormatString = "yyyy-MM-dd HH:mm:ss Z"
        let value = type == SampleType.stepCount ? sampleData.numberOfSteps : sampleData.distance
        
        // Create a sample with start and end time
        let samples: [String : Any] = [
          "startDate": [
            "timestamp": sampleData.startDate.unixTimestamp,
            "formattedString": sampleData.startDate.toString(dateFormat: dateFormatString)
          ],
          "endDate": [
            "timestamp": sampleData.endDate.unixTimestamp,
            "formattedString": sampleData.endDate.toString(dateFormat: dateFormatString)
          ],
          "value": value as Any,
        ]
        
        // Handle success case
        self.emitEvent(withName: SupportedEvents.inputKitTracking,
                       body: [
                        "topic" : type,
                        "samples" : samples
                        ],
                       completion: {})
      } else {
        print("Received null data from CoreMotion for realtime tracking")
      }
    
    });
    
    resolve(true)
  }
  
  // Stop emitting events for the specified query
  @objc func stopTracking(_ type: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    //remove respective type from current tracked types array
    self.currentTrackedTypes = self.currentTrackedTypes.filter { $0 != type }
    
    //only stop CMPedometer update when there's no type needed to be tracked
    if (self.currentTrackedTypes.count == 0) {
      self.pedoMeter.stopUpdates()
    }
    resolve(true)
  }
  
  // Returns historic activity tracking data.
  // NOTE: This function only returns data from the past 7 days. If the startDate is older than 7 days then the function will reject the promise
  @objc func getHistoricActivityTrackingData(_ startDate: Date, endDate: Date, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    
    NativeLogger.logNativeEvent("\(NSStringFromClass(type(of: self)))::\(#function)")
    
    if startDate >= endDate {
      reject(kHealthKitError, "startDate cannot be >= endDate", nil)
      return
    }
    
    // If the date is older than the last 7 days then return an error as pedometer class cannot return data older than 7 days
    if startDate < Calendar.current.date(byAdding: .day, value: -7, to: Date())! {
      reject(kHealthKitError, "StartDate cannot be older than 7 days", nil)
      return
    }
    
    self.pedoMeter.queryPedometerData(from: startDate, to: endDate, withHandler: { (data: CMPedometerData?, error) -> Void in
      
      let dateFormatString = "yyyy-MM-dd HH:mm:ss Z"
      
      if let sampleData = data {
        // Create a sample with start and end time
        let sample: [String : Any] = [
          "startDate": [
            "timestamp": sampleData.startDate.unixTimestamp,
            "formattedString": sampleData.startDate.toString(dateFormat: dateFormatString)
          ],
          "endDate": [
            "timestamp": sampleData.endDate.unixTimestamp,
            "formattedString": sampleData.endDate.toString(dateFormat: dateFormatString)
          ],
          "distance": sampleData.distance ?? 0,
          "stepCount": sampleData.numberOfSteps,
        ]
        
        resolve(sample)
      } else {
        // Create an empty sample to not disrupt the flow in simulator
        let emptySample: [String : Any] = [
          "startDate": [
            "timestamp": startDate.unixTimestamp,
            "formattedString": startDate.toString(dateFormat: dateFormatString)
          ],
          "endDate": [
            "timestamp": endDate.unixTimestamp,
            "formattedString": endDate.toString(dateFormat: dateFormatString)
          ],
          "distance": 0,
          "stepCount": 0
        ]
        
        // Resolve the empty sample
        resolve(emptySample)
      }
    });
  }
  
  // Stop emitting events for the specified query
  @objc func stopTrackingAll(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    self.pedoMeter.stopUpdates() 
    resolve(true)
  }
}
