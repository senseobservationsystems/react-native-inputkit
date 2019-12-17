//
//  HealthBridge+Background.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 12/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation
import HealthKit
import UserNotifications

extension HealthBridge {
  
  @objc func startMonitoring(_ typeString: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    
    NativeLogger.logNativeEvent("\(NSStringFromClass(type(of: self)))::\(#function)")
    
    guard let type = HealthBridge.readPermissions[typeString] else {
      print("### UnknownType given on start monitoring")
      reject(kHealthKitError, "UnknownType given on start monitoring", nil);
      return
    }
    
    let observerQuery = HKObserverQuery(sampleType: type, predicate: nil){ [weak self]
      (query, completionHandler, error) in
      
      // This callback will be called when new data is available
      guard let sampleType = query.objectType as? HKSampleType else {
        // Log this to sentry
        NativeLogger.logNativeEvent("Updates for observer query for \(query.objectType?.identifier ?? "notype") in \(#function)")
        completionHandler()
        return
      }
      
      guard let topic = HealthUtils.getTopic(forHKSampleType: sampleType) else {
        NativeLogger.logNativeEvent("Unknown topic ")
        completionHandler()
        return
      }
      
      // Error Check:
      guard error == nil else {
        // TODO: Log this event to Sentry
        NativeLogger.logNativeEvent("Error: observer query returned with error. \(String(describing: error))")
        completionHandler()
        return
      }
      
      // Handle success case
      self?.emitEvent(withName: SupportedEvents.inputKitUpdates,
                      body: ["topic" : topic], // remove value
                      completion: { completionHandler() })
      // ^ completionHandler is given from background delivery to indicate the end of task.
      // Needs to call otherwise OS will not wake us up from next time.
    }
    
    // Executing the background query
    self.healthStore.execute(observerQuery)
    
    // This has to be called after `healthStore.execute(observerQuery)`
    self.healthStore.enableBackgroundDelivery(for: type, frequency: .immediate){
      (success, error) in
      // This callback will be called when background query is successfully enabled.
      
      // Error Check:
      guard error == nil, success == true else {
        reject(kHealthKitError, "Error during enabling background query", error)
        return
      }
      
      // Handle Success case.
      resolve(nil)
      
      NativeLogger.logNativeEvent("Log called at \(#function) at \(Date())")
      print("### Background Delivery enabled")
    }
  }
  
  @objc func stopMonitoring(_ typeString: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    
    guard let type = HealthBridge.readPermissions[typeString] else {
      print("### UnknownType given on start monitoring")
      reject(kHealthKitError, "UnknownType given on start monitoring", nil);
      return
    }
    
    self.healthStore.stop(HKObserverQuery(sampleType: type, predicate: nil){ _,_,_ in })
    
    resolve(nil)
  }
  
//  func getUpdateHandler() -> UpdateHandler {
//     return { [weak self]
//        (query, completionHandler, error) in
//        
//        // This callback will be called when new data is available
//        guard let sampleType = query.objectType as? HKSampleType else {
//          // Log this to sentry
//          NativeLogger.logNativeEvent("Updates for observer query for \(query.objectType?.identifier ?? "notype") in \(#function)")
//          completionHandler()
//          return
//        }
//      
//        guard let topic = HealthUtils.getTopic(forHKSampleType: sampleType) else {
//          NativeLogger.logNativeEvent("Unknown topic ")
//          completionHandler()
//          return
//        }
//        
//        // Error Check:
//        guard error == nil else {
//          // TODO: Log this event to Sentry
//          NativeLogger.logNativeEvent("Error: observer query returned with error. \(String(describing: error))")
//          completionHandler()
//          return
//        }
//        
//        // Handle success case
//        self?.emitEvent(withName: SupportedEvents.inputKitUpdates,
//                        body: ["topic" : topic], // remove value
//                        completion: { completionHandler() })
//        // ^ completionHandler is given from background delivery to indicate the end of task.
//        // Needs to call otherwise OS will not wake us up from next time.
//      }
//  }
  
}
