import { NativeModules } from 'react-native';

import logger from '../../helper/logger';

// tslint:disable-next-line:variable-name
const EventHandlerTaskService = async (event: any) => {
    logger.debug('EventHandlerTaskService > called in background. > Received Event > ' + JSON.stringify(event));
    // broadcast an event from Health Kit if required
    const broadcastedEventJson = event.data_event;
    if (broadcastedEventJson !== undefined) {
        const broadcastedEvent = JSON.parse(broadcastedEventJson);
        broadcastedEventHandler.broadcastEvent(broadcastedEvent);
    }
};

const broadcastedEventHandler = {
    broadcastEvent: (event: any) => {
        const objSamples = JSON.parse(event.samples);
        NativeModules.EventHandlerBridge.emit(event.eventId, event.name, event.topic, JSON.stringify(objSamples));
    },
};

export default EventHandlerTaskService;
