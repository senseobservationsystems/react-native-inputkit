/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React, { Fragment, useState, useEffect, useCallback } from 'react';
import { SafeAreaView, StyleSheet, ScrollView, View, Text, StatusBar, TouchableHighlight } from 'react-native';

import * as HealthManager from './manager';

const App = () => {
    /**
     * isAvailable
     *
     * To check wether Healthkit (iOS) or GoogleFit (Android) are available on the device
     */
    const [isHealthAvailable, setIsHealthAvailable] = useState();
    useEffect(() => {
        const checkIfIsAvailable = async () => {
            await HealthManager.isAvailable()
                .then(r => setIsHealthAvailable(r))
                .catch(e => console.log(`==== Rejected isAvailable: ${JSON.stringify(e)}`));
        };
        checkIfIsAvailable();
    }, []);

    /**
     * requestPermissions
     *
     * To request user's permissions to use Healthkit (iOS) data.
     */
    const [arePermissionsGranted, setArePermissionsGranted] = useState();
    const requestPermissions = useCallback(async () => {
        await HealthManager.requestPermissions(['stepCount'])
            .then(r => setArePermissionsGranted(true))
            .catch(e => {
                console.log(`==== Rejected requestRermissions: ${JSON.stringify(e)}`);
                setArePermissionsGranted(false);
            });
    }, []);

    /**
     * getStepCount
     *
     * Returns all the steps between the two argument dates
     */

    const [stepCount, setStepCount] = useState();
    const getStepCount = useCallback(async () => {
        await HealthManager.getStepCount(new Date('2019-12-01'), new Date('2019-12-18'))
            .then(s => setStepCount(s))
            .catch(e => console.log(`==== Rejected getStepCount: ${JSON.stringify(e)}`));
    }, []);

    return (
        <Fragment>
            <StatusBar barStyle='dark-content' />
            <SafeAreaView>
                <ScrollView contentInsetAdjustmentBehavior='automatic' style={styles.scrollView}>
                    <View
                        style={{
                            height: 20,
                            width: 20,
                            backgroundColor: isHealthAvailable ? 'green' : 'red',
                        }}
                    />
                    <View style={styles.body}>
                        <TouchableHighlight
                            style={{
                                height: 100,
                                alignItems: 'center',
                                justifyContent: 'center',
                                backgroundColor: '#BBBBBB',
                            }}
                            onPress={requestPermissions}
                        >
                            <Text>{arePermissionsGranted ? 'Permissions granted' : 'Request Permissions'}</Text>
                        </TouchableHighlight>
                        <View style={{ height: 20, width: 20 }} />
                        <TouchableHighlight
                            style={{
                                height: 100,
                                alignItems: 'center',
                                justifyContent: 'center',
                                backgroundColor: '#BBBBBB',
                            }}
                            onPress={getStepCount}
                        >
                            <Text>{`Get step Count: ${stepCount}`}</Text>
                        </TouchableHighlight>
                    </View>
                </ScrollView>
            </SafeAreaView>
        </Fragment>
    );
};

const styles = StyleSheet.create({
    scrollView: {
        backgroundColor: 'white',
    },
    body: {
        backgroundColor: 'white',
    },
});

export default App;
