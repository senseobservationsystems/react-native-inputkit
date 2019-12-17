//
//  HealthBridge+Helper.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 12/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation
import HealthKit

extension HealthBridge {

  internal func querySum(forType type: HKQuantityType, predicate: NSPredicate, unit: HKUnit, completion: @escaping (Int?, Date?, Date?, Error?) -> Void) {
    
    // Setting up Statistics query for sum
    let query = HKStatisticsQuery(quantityType: type, quantitySamplePredicate: predicate, options: .cumulativeSum){
      (query, queryResult, error) in
      // This callback will be called when execution of the statistics query is completed.
      
      // Error check:
      guard error == nil, let result = queryResult else {
        print("[Error] \(#function): \(String(describing: error))")
        completion(nil, nil, nil, error)
        return
      }
      
      // Handling Success case:
      guard let value = result.sumQuantity()?.doubleValue(for: unit) else {
        // When the sumQuantity is nil, then startDate, endDate is nil
        // And accessing the property causes crash
        completion(0, nil, nil, nil)
        return
      }
      
      let sum = Int(round(value))

      completion(sum, result.startDate, result.endDate, nil)
    }
    
    healthStore.execute(query)
  }
  
  internal func queryDistribution(forType type: HKQuantityType, startDate: Date, endDate: Date, interval: DateComponents, unit: HKUnit, completion: @escaping (Array<[String : Any]>?, Date?, Date?, Error?) -> Void) {
    
    let calendar = Calendar.current
    let anchorComponents = calendar.dateComponents([.day, .month, .year, .hour, .minute, .second], from: startDate)
    
    guard let anchorDate = calendar.date(from: anchorComponents) else {
      print("[Error] \(#function): Could not make a valid date from the given component.")
      completion(nil, nil, nil, InputKitError.invalidDate)
      return
    }
    
    // Use a sample predicate to speed up the query
    let samplePredicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate)
    
    // Setting up Statistics query for sum
    let query = HKStatisticsCollectionQuery(quantityType: type,
                                            quantitySamplePredicate: samplePredicate,
                                            options: .cumulativeSum,
                                            anchorDate: anchorDate,
                                            intervalComponents: interval)
    
    query.initialResultsHandler = { [weak self]
      query, queryResults, error in
      
      // This callback will be called when execution of the statistics collection query is completed.
      
      // Error check:
      guard error == nil, let results = queryResults else {
        print("[Error] \(#function): \(String(describing: error))")
        completion(nil, nil, nil, error)
        return
      }
      
      // Handling Success case:
      // Handler expects a Double value as a parameter
      let valueArray = self?.getValueArray(fromStatsCollection: results,
                                     from: startDate,
                                     to: endDate,
                                     unit: unit)
      
      completion(valueArray, startDate, endDate, nil)
    }
    
    healthStore.execute(query)
  }
  
  internal func querySamples(forType type: HKSampleType, startDate: Date, endDate: Date, limit: Int, sortDescriptors : [NSSortDescriptor], completion: @escaping (Array<Any>?, Error?) -> Void) {

      // Query creation
      let predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate)
      let query = HKSampleQuery(sampleType: type, predicate: predicate, limit: limit, sortDescriptors: sortDescriptors) { (query, sampleResults, error) -> Void in
        
        // Error check:
        guard error == nil, let samples = sampleResults else {
          print("[Error] \(#function): \(String(describing: error))")
          completion(nil, error)
          return
        }
        
        completion(samples, nil)
      }
      
      healthStore.execute(query)
  }
  
  private func getValueArray(fromStatsCollection results: HKStatisticsCollection, from startDate: Date, to endDate: Date, unit: HKUnit) -> [[String : Any]] {
    var valueArray = [[String : Any]]()
    results.enumerateStatistics(from: startDate, to: endDate) {
      (result, stop) in
      // Loop through the set of results
      
      let value = Int(round(result.sumQuantity()?.doubleValue(for: unit) ?? 0))
      let dateFormatString = "yyyy-MM-dd HH:mm:ss Z"
      valueArray.append([
        "startDate": [
          "timestamp": result.startDate.unixTimestamp,
          "formattedString": result.startDate.toString(dateFormat: dateFormatString)
        ],
        "endDate": [
          "timestamp": result.endDate.unixTimestamp,
          "formattedString": result.endDate.toString(dateFormat: dateFormatString)
        ],
        "value": value
      ])
    }
    
    return valueArray
  }
}
