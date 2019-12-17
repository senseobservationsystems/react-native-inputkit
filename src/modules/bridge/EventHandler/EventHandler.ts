import { NativeModules } from 'react-native';

export interface EventHandlerBridge {
    onListenerReady(name: string): Promise<void>;
    onEventDidProcessed(eventId: string): Promise<void>;
}

export interface EventSubscriber {
    startMonitoring(topic: string, callback: (value: object, completionHandler: () => void) => void): any;
    stopMonitoring(topic: string): any;
    eventDidProcessed(eventId: string): any;
}

const EventHandlerBridge = NativeModules.EventHandlerBridge;

class EventHandler {
    private eventHandlerBridge: EventHandlerBridge;

    constructor() {
        this.eventHandlerBridge = EventHandlerBridge;
    }

    listenerDidMount(name: string) {
        return this.eventHandlerBridge.onListenerReady(name);
    }

    eventDidProcessed(eventId: string) {
        return this.eventHandlerBridge.onEventDidProcessed(eventId);
    }
}

let eventHandler: EventHandler;

export default {
    reqSharedInstance: (): Promise<EventHandler> => {
        if (eventHandler === null || eventHandler === undefined) {
            eventHandler = new EventHandler();
            return Promise.resolve(eventHandler);
        }
        return Promise.resolve(eventHandler);
    },
};
