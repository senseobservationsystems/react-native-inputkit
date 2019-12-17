//
//  Event.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 29/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

struct Event {
  let name: String
  let body: [String : Any]
  let completion: () -> Void
}
