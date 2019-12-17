//
//  ShortCodeGenerator.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 29/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

// Copied from: https://stackoverflow.com/a/36021870/4464769
struct ShortCodeGenerator {
  
  private static let base62chars = [Character]("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")
  private static let maxBase : UInt32 = 62
  
  static func generateEventID() -> String {
    return "\(String(describing: Date().timeIntervalSince1970)):\(ShortCodeGenerator.getCode(length: 4))"
  }
  
  static func getCode(withBase base: UInt32 = maxBase, length: Int) -> String {
    var code = ""
    for _ in 0..<length {
      let random = Int(arc4random_uniform(min(base, maxBase)))
      code.append(base62chars[random])
    }
    return code
  }
}
