//
//  DateExtesion.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 24/05/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

extension Date {

  var unixTimestamp: Double {
      return self.timeIntervalSince1970 * 1000
  }
  
  func toString(dateFormat format : String) -> String
  {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = format
    return dateFormatter.string(from: self)
  }
}

extension Data {
  
  var hexString: String {
    return map {String(format: "%02.2hhx", arguments: [$0]) }.joined()
  }
}

extension Dictionary where Value: Equatable {
  func allKeys(forValue val: Value) -> [Key] {
    return self.filter { $1 == val }.map { $0.0 }
  }
  
  mutating func merge(with dictionary: Dictionary) {
    dictionary.forEach { updateValue($1, forKey: $0) }
  }
  
  func merged(with dictionary: Dictionary) -> Dictionary {
    var dict = self
    dict.merge(with: dictionary)
    return dict
  }
}
