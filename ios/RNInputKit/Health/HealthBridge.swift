//
//  HealthBridge.swift
//  Erasmus
//
//  Created by Tatsuya Kaneko on 19/06/2017.
//  Copyright © 2017 Sense Health BV. All rights reserved.
//

import Foundation
import HealthKit
import CoreMotion

@objc (HealthBridge)
class HealthBridge: RCTEventEmitter { // No NSObject inheritance, since RCTEventEmitter inherits from NSObject already
  
  let healthStore = HKHealthStore()
  
  // For realtime tracking
  let pedoMeter = CMPedometer()
  var currentTrackedTypes = [String]()

  @objc override func supportedEvents() -> [String]! {
    return [SupportedEvents.inputKitUpdates]
  }
  
  static let quantiyTypes: [String: HKSampleType] = [
    "stepCount": HKObjectType.quantityType(forIdentifier: .stepCount)!,
    "distanceWalkingRunning": HKObjectType.quantityType(forIdentifier: .distanceWalkingRunning)!
  ]
  
  // Default units for quantity types
  static let quantityUnits: [HKQuantityTypeIdentifier: HKUnit] = [
    HKQuantityTypeIdentifier.stepCount: HKUnit.count(),
    HKQuantityTypeIdentifier.distanceWalkingRunning: HKUnit.meter()
  ]
  
  static let categoryTypes: [String: HKSampleType] = [
    "sleep": HKObjectType.categoryType(forIdentifier: .sleepAnalysis)!,
  ]
  
  // It would be a time saver if we could expose Health.type to RN side, but unfortunately we can not.
  // We could convert string to HKObject directly here, but Bridge should not know too much detail about HealthKit. Rather Health class in InputKit should know more details. Therefore, we use mapping from string to custom type Health.type.
  static let readPermissions: [String: HKSampleType] = quantiyTypes.merged(with: categoryTypes)
  
  
  @objc func isAvailable(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolve(HKHealthStore.isHealthDataAvailable())
  }

  @objc func isProviderInstalled(_ providerName: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard !providerName.isEmpty else {
      reject(kInputKitError, "Provider name must be provided.", nil)
      return
    }
    
    //TODO: add another health provider app installation check
    if providerName == ProviderName.healthKit {
      //Consider that HealthKit has been built in iOS devices, we can simply resolve this request
      resolve(true)
      return
    }
    
    reject(kInputKitError, providerName + " is not supported in InputKit!", nil)
  }
  
  @objc func requestPermissions(_ typesToRead: [String], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
    let typeSet = Set(typesToRead.compactMap { HealthBridge.readPermissions[$0] })
    
    healthStore.requestAuthorization(toShare: nil, read: typeSet) {
      (success, error) in
      
      guard error == nil, success == true else {
        reject(kHealthKitError, "Error while requesting permissions", error)
        return
      }
      
      resolve(nil)
    }
  }

  func emitEvent(withName name: String, body: [String : Any], completion: @escaping () -> Void) {
    guard let eventHandler = self.bridge.module(forName: "EventHandlerBridge") as? EventHandlerBridge else {
      //TODO: this should never happen
      NativeLogger.logNativeEvent("EventHandlerBridge doesn't exist in bridge.module")
      return
    }
    
    NativeLogger.logNativeEvent("\(NSStringFromClass(type(of: self)))::\(#function)")
    eventHandler.emit(withName: name, body: body, completion: completion)
  }
}
