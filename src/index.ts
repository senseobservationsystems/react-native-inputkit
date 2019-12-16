import { NativeModules } from 'react-native';

const TestModule = NativeModules.RNInputKit;
const SwiftModule = NativeModules.SwiftModule;

export function jsMethod() {
    return "sample method (no native)";
}

export function objcMethod(){
    return TestModule.test();
}

export function swiftMethod(){
    return SwiftModule.swiftMethod()
}

export default {
    jsMethod,
    objcMethod,
    swiftMethod,
}