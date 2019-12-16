import { NativeModules } from 'react-native';

const TestModule = NativeModules.RNInputKit;

export function sampleMethod() {
    return "sample method (no native)";
}

export function testMethod(){
    return TestModule.test();
}

export default {
    sampleMethod,
    testMethod,
}