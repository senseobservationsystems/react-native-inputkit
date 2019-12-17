//
//  EventHandler.swift
//  InputKitProber
//
//  Created by Tatsuya Kaneko on 29/06/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

import Foundation

// TODO: should this class have process queue?
@objc(EventHandlerBridge)
class EventHandlerBridge: RCTEventEmitter {

  var availableListeners = Set<String>()
  var pendingEvents = [Event]()
  var completionBlocks = [String : () -> Void]()
  
  override func supportedEvents() -> [String]! {
    return [SupportedEvents.inputKitUpdates,
            SupportedEvents.inputKitTracking]
  }

  
  // called by JS layer when a listener is ready
  // This method can be called from multiple threads
  @objc func onListenerReady(_ name: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ) {
    
    NativeLogger.logNativeEvent("\(NSStringFromClass(type(of: self)))::\(#function)")
    
    NativeLogger.logNativeEvent("new listener: \(name) became available.")
    
    availableListeners.insert(name)
    
    for event in self.pendingEvents {
      guard name == event.name else { break }
      
   	  self.emit(withName: event.name, body: event.body, completion: event.completion)
    }
    resolve(nil)
  }
  
  // called by JS layer when processing event is completed.
  // This method can be called from multiple threads
  @objc func onEventDidProcessed(_ eventId: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock ){
    guard let completionHandler = self.completionBlocks[eventId] else {
      // TODO: Notify Error! This should never happen
      return
    }
    self.completionBlocks.removeValue(forKey: eventId)
    resolve(nil)
    // This callback potentially triggers everything to be stopped and deallocated.
    completionHandler()
  }

  // Not exposed to JS
  // called by native components such as Health Kit.
  // This method can be called from multiple threads
  func emit(withName name: String, body: [String: Any], completion: @escaping () -> Void) {
    
    NativeLogger.logNativeEvent("\(NSStringFromClass(type(of: self)))::\(#function)")
    
    // TODO: this check might be not sufficient if there are multiple listeners per type of event.
    guard availableListeners.contains(name) else {
      pendingEvents.append(Event(name: name, body: body, completion: completion))
      return
    }
    
    var bodyCopy = body
    let eventId = ShortCodeGenerator.generateEventID()
    bodyCopy["eventId"] = eventId
    self.completionBlocks[eventId] = completion
    self.sendEvent(withName: name, body: bodyCopy)
  }
}
