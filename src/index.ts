import { NativeModules } from 'react-native';

const InputKit = NativeModules.InputKit;

export function sampleMethod() {
    return InputKit.sampleMethod();
}

export default {
    sampleMethod,
}