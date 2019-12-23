import { Health, SampleType } from 'react-native-inputkit';

export async function isAvailable() {
    return Health.reqSharedInstance().then(h => h.isAvailable());
}

export async function requestPermissions(sampleTypes: SampleType[]) {
    return Health.reqSharedInstance().then(h => h.requestPermissions(sampleTypes));
}

export async function getStepCount(start: Date, end: Date) {
    return Health.reqSharedInstance().then(h => h.getStepCount(start, end));
}
