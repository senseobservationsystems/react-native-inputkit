package nl.sense.rninputkit.inputkit.googlefit.sensor;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import nl.sense.rninputkit.inputkit.HealthProvider.SensorListener;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by panjiyudasetya on 6/15/17.
 */
@SuppressWarnings("weakReference")
public abstract class SensorApi {
    private SensorOptions mOptions;
    private Context mContext;

    public SensorApi(@NonNull Context context) {
        mContext = context;
    }

    /**
     * Set sensor api options
     * @param options {@link SensorOptions}
     */
    protected void setOptions(@NonNull SensorOptions options) {
        mOptions = options;
    }

    /**
     * Subscribing relevant Sensor
     * @param listener sensor listener
     * @throws IllegalStateException whenever {@link SensorApi#mOptions} unspecified.
     *          Make sure to call {@link SensorApi#setOptions(SensorOptions)} before subscribing.
     */
    public void subscribe(@NonNull final SensorListener listener) {
        if (mOptions == null) throw new IllegalStateException("Sensor options unspecified!");

        Fitness.getSensorsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .findDataSources(mOptions.getDataSourcesRequest())
                .addOnSuccessListener(new OnSuccessListener<List<DataSource>>() {
                    @Override
                    public void onSuccess(List<DataSource> dataSources) {
                        DataSource dataSource = findDataSource(dataSources);
                        if (dataSource == null) {
                            String message = "No Data sources available for " + mOptions.getDataType().getName();
                            IKResultInfo errorInfo = new IKResultInfo(
                                    IKStatus.Code.INVALID_REQUEST,
                                    message
                            );
                            listener.onSubscribe(errorInfo);
                            return;
                        }

                        registerSensorListener(dataSource, listener);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        IKResultInfo errorInfo = new IKResultInfo(
                                IKStatus.Code.INVALID_REQUEST,
                                e.getMessage()
                        );
                        listener.onSubscribe(errorInfo);
                    }
                });
    }

    /**
     * Stop subscribing data from relevant Sensor synchronously
     * @throws IllegalStateException whenever {@link SensorApi#mOptions} unspecified.
     *          Make sure to call {@link SensorApi#setOptions(SensorOptions)} before unsubscribing.
     */
    public Task<Boolean> unsubscribe() {
        if (mOptions == null) throw new IllegalStateException("Sensor options unspecified!");

        return Fitness.getSensorsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .remove(mOptions.getSensorListener());
    }

    /**
     * Stop subscribing data from relevant Sensor
     * @param listener sensor listener
     * @throws IllegalStateException whenever {@link SensorApi#mOptions} unspecified.
     *          Make sure to call {@link SensorApi#setOptions(SensorOptions)} before unsubscribing.
     */
    public void unsubscribe(@NonNull final SensorListener listener) {
        if (mOptions == null) throw new IllegalStateException("Sensor options unspecified!");

        Fitness.getSensorsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .remove(mOptions.getSensorListener())
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isSuccess) {
                        if (isSuccess) {
                            IKResultInfo info = new IKResultInfo(
                                    IKStatus.Code.VALID_REQUEST,
                                    "Successfully remove sensor listener.");
                            listener.onUnsubscribe(info);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        IKResultInfo info = new IKResultInfo(
                                IKStatus.Code.INVALID_REQUEST,
                                e.getMessage());
                        listener.onUnsubscribe(info);
                    }
                });
    }

    /**
     * Helper function to create Sensor Request on specific {@link DataSource}
     * @param dataSource Sensor {@link DataSource}
     * @return  {@link SensorRequest}
     */
    private SensorRequest buildSensorRequest(@NonNull DataSource dataSource) {
        return new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(mOptions.getDataType())
                .setSamplingRate(mOptions.getSamplingRate(), mOptions.getSamplingTimeUnit())
                .build();
    }

    /**
     * Helper function to register sensor listener into Sensor API
     * @param dataSource Sensor {@link DataSource}
     * @param listener sensor listener
     */
    private void registerSensorListener(@NonNull DataSource dataSource,
                                        @NonNull final SensorListener listener) {
        SensorRequest request = buildSensorRequest(dataSource);
        Fitness.getSensorsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .add(request, mOptions.getSensorListener())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        IKResultInfo info = new IKResultInfo(
                                IKStatus.Code.VALID_REQUEST,
                                "Successfully added sensor listener");
                        listener.onSubscribe(info);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        IKResultInfo info = new IKResultInfo(
                                IKStatus.Code.INVALID_REQUEST,
                                e.getMessage());
                        listener.onUnsubscribe(info);
                    }
                });
    }

    /**
     * Helper function to find a correct {@link DataSource} for relevant sensor.
     * @param dataSourcesResult {@link DataSource} collection
     * @return {@link DataSource}
     */
    private DataSource findDataSource(@Nullable List<DataSource> dataSourcesResult) {
        if (dataSourcesResult == null) return null;
        for (DataSource dataSource : dataSourcesResult) {
            if (mOptions.getDataType().equals(dataSource.getDataType()))
                return dataSource;
        }
        return null;
    }
}
