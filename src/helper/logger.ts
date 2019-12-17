import { NativeModules } from 'react-native';

function nativeLogger(message: string) {
    if (__DEV__) {
        NativeModules.Logger.log(message);
    } else {
        logger.debug(message);
    }
}

let logger: {
    error: (error: any, stack?: any) => void;
    warn: (...anything: any[]) => void;
    debug: (...anything: any[]) => void;
    native: (...anything: any[]) => void;
};

// if (__DEV__) {
    // tslint:disable-next-line:no-var-requires
    // TODO: we shouldn't be using reactotron in react-native-inputkit.
    // const Reactotron = require('reactotron-react-native').default;
    // logger = {
    //     error: Reactotron.error,
    //     warn: Reactotron.warn,
    //     debug: Reactotron.log,
    //     native: nativeLogger,
    // };
// } else {
    // logger = {
    //     // tslint:disable-next-line:no-console
    //     error: console.error,
    //     // tslint:disable-next-line:no-console
    //     warn: console.warn,
    //     // tslint:disable-next-line:no-console
    //     debug: console.log,
    //     // tslint:disable-next-line:no-console
    //     native: nativeLogger,
    // };
// }
logger = {
    // tslint:disable-next-line:no-console
    error: console.error,
    // tslint:disable-next-line:no-console
    warn: console.warn,
    // tslint:disable-next-line:no-console
    debug: console.log,
    // tslint:disable-next-line:no-console
    native: nativeLogger,
};

export default logger;
