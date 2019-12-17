export interface HealthBridge {
    isAvailable(): Promise<boolean>;
    isPermissionsAuthorised(types: SampleType[]): Promise<boolean>;
    isProviderInstalled(providerName: HealthProvider): Promise<boolean>;
    requestPermissions(types: SampleType[]): Promise<void>;
    getDistance(startDate: number, endDate: number): Promise<number>;
    getDistanceSamples(startDate: number, endDate: number, limit: number): Promise<QuantitySamples[]>;
    getStepCount(startDate: number, endDate: number): Promise<number>;
    getStepCountDistribution(
        startDate: number,
        endDate: number,
        interval: Interval,
    ): Promise<StepCountDistributionDataPoint>;
    getAccurateDistance(startDate: number, endDate: number): Promise<number>;
    getSleepAnalysisSamples(startDate: number, endDate: number): Promise<SleepAnalysisDataPoint[]>;
    getWeightData(startDate: number, endDate: number): Promise<WeightDataPoint[]>;
    getBloodPressure(startDate: number, endDate: number): Promise<BloodPressureDataPoint[]>;

    startMonitoring(type: SampleType): Promise<void>;
    stopMonitoring(type: SampleType): Promise<void>;

    startTracking(type: SampleType, startDate: number): Promise<void>;
    stopTracking(type: SampleType): Promise<void>;
    getHistoricActivityTrackingData(startDate: number, endDate: number): Promise<ActivitySample>;
    stopTrackingAll(): Promise<void>;
}

export interface StepCountDistributionDataPoint {
    value: QuantitySamples[];
    startDate: IKDate;
    endDate: IKDate;
}

export interface SleepAnalysisDataPoint {
    startDate: IKDate;
    endDate: IKDate;
    type: string;
}

export interface QuantitySamples {
    startDate: IKDate;
    endDate: IKDate;
    value: number;
}

export interface WeightDataPoint {
    time: IKDate;
    weight: number;
    bodyFat: number;
    comment: string;
}

export interface BloodPressureDataPoint {
    time: IKDate;
    systolic: number;
    diastolic: number;
    mean: number;
    pulse: number;
    comment: string;
}

export interface ActivitySample {
    startDate: IKDate;
    endDate: IKDate;
    distance: number;
    stepCount: number;
}

export interface IKPayloadType {
    topic: string;
    samples: QuantitySamples[];
    eventId: string;
}

/**
 * InputKit date defintion interface
 * timestamp -> date in milliseconds
 * formattedString -> formatted date in string format YYYY-MM-DD hh:mm:ss +TZ
 */
export interface IKDate {
    timestamp: number;
    formattedString: string;
}

export declare type Interval = 'week' | 'day' | 'hour' | 'tenMinute';

export declare type RealTimeSampleType = 'stepCount' | 'distanceWalkingRunning';

export declare type SampleType = 'sleep' | 'stepCount' | 'distanceWalkingRunning';

export declare type HealthProvider = 'googleFit' | 'healthKit';

/**
 * FIX ME:
 * InputKit doesn't support Location services at this moment
 * So we only simply disable native bridge interface.
 * Feel free to uncomment code below once it's required.
 */
// export declare interface LocationBridge {
//     requestPermissions(): Promise<void>;
//     startGeoFencing(callback: (region: Location.Region) => {}):void;
// }
