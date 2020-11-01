package com.example.aidlserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public final int CONSTANT_SAMPLING_TIME = 8000;

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int CONSTANT_SENSOR_DATA = 54;
    private static ServiceHandler mHandler;
    //Thread independently handling the background operations.
    private static HandlerThread mHandlerThread = new HandlerThread("BackgroundThread");
    private static List<IDBCallback> mCallbackList = new ArrayList<>();
    private static IDBCallback idbCallback;

    private SensorManager senSensorManager;
    private Sensor senOrientation;
    private float[] Q = new float[4];
    long magnTim;
    private String quaternion;
    private static String orientationData;

    private TextView tvSensorData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSensorData = findViewById(R.id.tv_sensor_data);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senOrientation = senSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        registerListeners();
    }


    public void registerListeners() {
        senSensorManager.registerListener(this, senOrientation, CONSTANT_SAMPLING_TIME);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        getValues(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void getValues(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            SensorManager.getQuaternionFromVector(Q, sensorEvent.values);
            quaternion = String.format("%.20f, %.20f, %.20f, %.20f, %d ", Q[0], Q[1], Q[2], Q[3], magnTim);

            tvSensorData.setText(quaternion);
            orientationData = quaternion;
            if (idbCallback != null)
                sendMsgToHandler(idbCallback, CONSTANT_SENSOR_DATA, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerListeners();
    }


    /**
     * Handler class to evaluate results and reply back
     * to the caller
     */
    private static class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                int callbackIndex = msg.arg1;
                Bundle bundle = msg.getData();
                switch (msg.what) {
                    case CONSTANT_SENSOR_DATA:
                        mCallbackList.get(callbackIndex).handleOrientationDetail(orientationData);
                        break;
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    /**
     * Create and Send message to handler
     *
     * @param callback
     * @param flag
     * @param bundle
     */
    static void sendMsgToHandler(IDBCallback callback, int flag, Bundle bundle) {
        //mCallbackList.register(callback);
        // get array size
        int position = 0;
        if (callback != null) {
            mCallbackList.add(callback);
        }

        position = (mCallbackList.size() > 0 ? mCallbackList.size() - 1 : 0);
        Message message = mHandler.obtainMessage();
        message.arg1 = position;
        if (bundle != null)
            message.setData(bundle);

        message.what = flag;
        mHandler.sendMessage(message);
    }

    public static void getScreenOrientation(IDBCallback callback) {
        idbCallback = callback;
        mHandlerThread.start();
        mHandler = new ServiceHandler(mHandlerThread.getLooper());
        sendMsgToHandler(callback, CONSTANT_SENSOR_DATA, null);
    }
}
