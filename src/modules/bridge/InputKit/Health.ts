import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

import logger from '../../../helper/logger';
import EventHandler from '../EventHandler/EventHandler';
import {
    ActivitySample,
    BloodPressureDataPoint,
    HealthBridge,
    HealthProvider,
    IKPayloadType,
    Interval,
    QuantitySamples,
    SampleType,
    SleepAnalysisDataPoint,
    StepCountDistributionDataPoint,
    WeightDataPoint,
} from './types';

const monitorUpdates = 'inputKitUpdates';
const trackingUpdates = 'inputKitTracking';

type HealthCallback = (payload: IKPayloadType) => void;

class Health {
    private healthBridge: HealthBridge;
    private healthBridgeEmitter: NativeEventEmitter;

    // Object with topic as key and a callback as value
    private callbacks: { [topic: string]: HealthCallback } = {};
    private trackingCallbacks: { [topic: string]: HealthCallback } = {};

    constructor() {
        this.healthBridge = NativeModules.HealthBridge;
        this.healthBridgeEmitter = new NativeEventEmitter(NativeModules.EventHandlerBridge);

        // Add monitoring listener
        this.healthBridgeEmitter.addListener(monitorUpdates, (payload: IKPayloadType) => {
            const callback = this.callbacks[payload.topic];
            logger.native(`Health received inputKitUpdates ${JSON.stringify(payload)}`);

            if (callback !== undefined) {
                this.processCallback(callback, payload);
            }
        });
        // Add tracking listener
        this.healthBridgeEmitter.addListener(trackingUpdates, (payload: IKPayloadType) => {
            const callback: HealthCallback = this.trackingCallbacks[payload.topic];
            logger.native(`Health received inputKitTracking ${JSON.stringify(payload)}`);

            if (callback !== undefined) {
                this.processCallback(callback, payload);
            }
        });

        EventHandler.reqSharedInstance().then(eventHandler => {
            logger.native('Listener did mount');
            eventHandler.listenerDidMount(monitorUpdates);
            eventHandler.listenerDidMount(trackingUpdates);
        });
    }

    /**
     * Check whether the HealthKit/GoogleFit is available on the device.
     *
     * @return Promise containing boolean for whether the HealthKit/GoogleFit is available on the device.
     */
    isAvailable(): Promise<boolean> {
        return this.healthBridge.isAvailable();
    }

    /**
     * Check authorised permission for the given SampleType(s) within health provider.
     * @param types: Array of SampleType.
     *
     * @return Promise containing boolean for whether the requested permission has been authorised.
     */
    isPermissionsAuthorised(types: SampleType[]): Promise<boolean> {
        // Because of HealthKit doesn't have functionality to check does permission has been authorised
        // or not, we always return true.
        if (Platform.OS === 'ios') {
            return Promise.resolve(true);
        }
        return this.healthBridge.isPermissionsAuthorised(types);
    }

    /**
     * Check Health Provider installation.
     * @param providerName: Name of health provider.
     *
     * @return promise contains an information either health provider installed or not.
     */
    isProviderInstalled(providerName: HealthProvider): Promise<boolean> {
        return this.healthBridge.isProviderInstalled(providerName);
    }

    /**
     *  Request read permissions for the given SampleType(s).
     *  User not granting permission will not result in reject block to be called.
     *
     *  @param types: Array of SampleType.
     *  @return Promise
     */
    requestPermissions(types: SampleType[]): Promise<void> {
        return this.healthBridge.requestPermissions(types);
    }

    /**
     *  Returns Promise contains a total distance of walking and running of a specific range.
     *
     *  @param startDate: start date of distance request.
     *  @param endDate: end date of distance request.
     *  @return Promise containing: number for total walking and running distance in meters.
     */
    getDistance(startDate: Date, endDate: Date): Promise<number> {
        if (Platform.OS === 'ios') {
            return this.healthBridge.getAccurateDistance(startDate.getTime(), endDate.getTime());
        }
        return this.healthBridge.getDistance(startDate.getTime(), endDate.getTime());
    }

    /**
     *  Returns Promise containing distance samples for the specific range. Sorted recent data first.
     *
     *  @param startDate: start date of the range.
     *  @param endDate: end date of the range.
     *  @param limit: limit of items to retreive
     *  @return Promise containing an array of distance samples formated as:
     *     startDate: object contains timestamp and formatted string for startDate.
     *     endDate: object contains timestamp and formatted string for endDate.
     *     value: the actual distance travelled during this range in meters
     *
     *    eg)
     *      [
     *          {
     *              "startDate": {
     *                  "timestamp": 1501602660000.0,
     *                  "formattedString": "2017-08-01 22:51:00 +0700"
     *              },
     *              "value": 134.5,
     *              "endDate": {
     *                  "timestamp": 1501627860000.0,
     *                  "formattedString": "2017-08-02 05:51:00 +0700"
     *              }
     *          }
     *      ]
     */
    getDistanceSamples(startDate: Date, endDate: Date, limit: number): Promise<QuantitySamples[]> {
        return this.healthBridge.getDistanceSamples(startDate.getTime(), endDate.getTime(), limit);
    }

    /**
     *  Returns Promise contains a total step count value of a specific range.
     *
     *  @param startDate: start date of get step count request.
     *  @param endDate: end date of get step count request.
     *  @return Promise containing: number for total steps.
     */
    getStepCount(startDate: Date, endDate: Date): Promise<number> {
        return this.healthBridge.getStepCount(startDate.getTime(), endDate.getTime());
    }

    /**
     *  Returns Promise contains distribution of step count value through out a specific range.
     *
     *  @param startDate: start date of the range.
     *  @param endDate: end date of the range.
     *  @param interval: Interval
     *  @return Promise containing an array of objects formated as:
     *     value: Array of objects. Each element represents one interval.
     *     startDate: object contains timestamp and formatted string for startDate.
     *     endDate: object contains timestamp and formatted string for endDate.
     *
     *    eg)
     *       {
     *           "startDate": {
     *               "timestamp":1234560000,
     *               "formattedString":"2017-07-06 00:00:00 +0200"
     *           },
     *           "endDate": {
     *               "timestamp":1234560000,
     *               "formattedString":"2017-07-06 23:59:99 +0200"
     *           },
     *           "value": [
     *               {
     *                   "startDate":{
     *                       "timestamp":1234560000,
     *                       "formattedString":"2017-07-06 00:00:00 +0200"
     *                   },
     *                   "endDate":{
     *                       "timestamp":1234560000,
     *                       "formattedString":"2017-07-06 00:10:00 +0200"
     *                   },
     *                   "value": 20
     *               },
     *               ...
     *           ]
     *       }
     */
    getStepCountDistribution(
        startDate: Date,
        endDate: Date,
        interval: Interval,
    ): Promise<StepCountDistributionDataPoint> {
        return this.healthBridge.getStepCountDistribution(startDate.getTime(), endDate.getTime(), interval);
    }

    /**
     *  Returns Promise contains sleep analysis data of a specific range. Sorted recent data first.
     *
     *  @param startDate: start date of the range.
     *  @param endDate: end date of the range.
     *  @return Promise containing an array of sleep analysis samples formated as:
     *     startDate: object contains timestamp and formatted string for startDate.
     *     endDate: object contains timestamp and formatted string for endDate.
     *     type: sample type, there are three sample type available: "inBed", "asleep", and "awake"
     *
     *    eg)
     *      [
     *          {
     *              "startDate": {
     *                  "timestamp": 1501602660000.0,
     *                  "formattedString": "2017-08-01 22:51:00 +0700"
     *              },
     *              "type": "InBed",
     *              "endDate": {
     *                  "timestamp": 1501627860000.0,
     *                  "formattedString": "2017-08-02 05:51:00 +0700"
     *              }
     *          },
     *          {
     *              "startDate": {
     *                  "timestamp": 1501563060000.0,
     *                  "formattedString": "2017-08-01 23:51:00 +0700"
     *              },
     *              "type": "Asleep",
     *              "endDate": {
     *                  "timestamp": 1501581060000.0,
     *                  "formattedString": "2017-08-02 04:51:00 +0700"
     *              }
     *          }
     *      ]
     */
    getSleepAnalysisSamples(startDate: Date, endDate: Date): Promise<SleepAnalysisDataPoint[]> {
        return this.healthBridge.getSleepAnalysisSamples(startDate.getTime(), endDate.getTime());
    }

    /**
     *  Returns Promise contains weight data value through out a specific range.
     */
    getWeightData(startDate: Date, endDate: Date): Promise<WeightDataPoint[]> {
        return this.healthBridge.getWeightData(startDate.getTime(), endDate.getTime());
    }

    /**
     *  Returns Promise contains blood pressure data value through out a specific range.
     */
    getBloodPressure(startDate: Date, endDate: Date): Promise<BloodPressureDataPoint[]> {
        return this.healthBridge.getBloodPressure(startDate.getTime(), endDate.getTime());
    }

    /**
     * Subscribes the change of data for a particular type of data from Health Data Provider(HealthKit, GoogleFit, etc).
     * Everytime a new data is delivered, the callback that you provide as the second argument will be called.
     *
     * This callback can be called even when app is terminated. If you want to receive the background event,
     * this method should be called in one of the method called in the initialization process of the app lifecycle.
     * e.g.) `componentDidMount` of the root component.
     *
     * This method itself does not provide any values. You can run queries that you would like to run in the callback.
     *
     * At the end of the callback, you must call the completion handler given in the arguments of the callback.
     * This callback is to notify OS the end of process to minimize the time that OS allocates for us to execute tasks.
     * Not calling this method at the end of callback will result in no further updates from Health Data Provider.(iOS)
     * Maximum time for the callback is approximately 30 seconds. (iOS)
     *
     * @param topic string for which type of data that you want to start monitoring.
     * @param callback callback that is called when we receive a new updates from Health Data Provider on Native side.
     */
    startMonitoring(topic: SampleType, callback: HealthCallback) {
        logger.native(`Registering Listener for inputKitUpdates with topic: ${topic}`);
        this.callbacks[topic] = callback;
        this.healthBridge.startMonitoring(topic);
    }

    /**
     * Unsubscribe the change of data for a particular type of data
     * from Health Data Provider(HealthKit, GoogleFit, etc).
     * When you call this method while the topic is not subscribed, it will not do anything.
     * @param topic string for which type of data that you want to stop monitoring.
     */
    stopMonitoring(topic: SampleType) {
        logger.native(`Deregister inputKitUpdates with topic: ${topic}`);
        delete this.callbacks[topic];
        this.healthBridge.stopMonitoring(topic);
    }

    /**
     * Subscribes to all new data created after the specified date for a particular type of data from
     * Health Data Provider(HealthKit, GoogleFit, etc).
     * Everytime new data is produced in the Health Provider, the callback that you provide as the second
     * argument will be called with all new data created since.
     *
     * This API guarantees returning data when the app is in foreground but cannot promise the callback
     * being called when the app is in background or killed
     *
     * @param topic string for which type of data that you want to start tracking.
     * @param startDate the time after which all new samples created should be delivered
     * @param callback callback that is called when we receive a new updates from Health Data Provider on Native side.
     */
    // tslint:disable-next-line:max-line-length
    startTracking(topic: SampleType, startDate: Date, callback: HealthCallback): Promise<void> {
        // Track only if we are not already tracking the sample
        if (this.trackingCallbacks[topic] !== undefined) {
            logger.debug(`Already tracking topic: ${topic}`);
            Promise.reject('Already tracking topic');
        }

        this.trackingCallbacks[topic] = callback;
        return this.healthBridge.startTracking(topic, startDate.getTime());
    }

    /**
     * Unsubscribe the change of data for a particular type of data
     * from Health Data Provider(HealthKit, GoogleFit, etc).
     * When you call this method while the topic is not subscribed, it will not do anything.
     * @param topic string for which type of data that you want to stop tracking new data.
     */
    stopTracking(topic: SampleType) {
        delete this.trackingCallbacks[topic];
        return this.healthBridge.stopTracking(topic);
    }

    /**
     * Returns historic activity tracking data
     * NOTE: This function can only return activity data from the last 7 days.
     *       If requested for data older than that, the funciton will reject the promis.
     * On iOS to ensure this works in real-time, the CoreMotion provider is used
     *
     * Note: This API guarantees returning data when the app is in foreground but cannot
     * promise the callback being called when the app is in background or killed
     *
     * @param startDate the starting data of the activity samples
     * @param endDate the ending date of the activity samples
     */
    getHistoricActivityTrackingData(startDate: Date, endDate: Date): Promise<ActivitySample> {
        if (Platform.OS !== 'ios') {
            return Promise.reject('Not implemented for Android');
        }
        return this.healthBridge.getHistoricActivityTrackingData(startDate.getTime(), endDate.getTime());
    }

    /**
     * Unsubscribe the change of data for all types of data from Health Data Provider(HealthKit, GoogleFit, etc).
     * When you call this method while no topics are subscribed, it will not do anything.
     */
    stopTrackingAll() {
        this.trackingCallbacks = {};
        return this.healthBridge.stopTrackingAll();
    }

    eventDidProcessed(eventId: string) {
        EventHandler.reqSharedInstance().then(eventHandler => {
            eventHandler.eventDidProcessed(eventId);
        });
    }

    private processCallback(callback: HealthCallback, payload: IKPayloadType): void {
        logger.debug('With callback defined!');
        callback.call(null, payload);
        logger.native(`completionHandler is called for: ${payload.eventId}`);
        this.eventDidProcessed(payload.eventId);
    }
}

let health: Health;

export default {
    reqSharedInstance: (): Promise<Health> => {
        if (health === null || health === undefined) {
            health = new Health();
            return Promise.resolve(health);
        }
        return Promise.resolve(health);
    },
};
