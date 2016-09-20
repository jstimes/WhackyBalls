package enterprises.stimes.whackyballs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Jacob on 5/21/2016.
 *
 * Used to get accelerometer data for updating player's ball
 */
public class OrientationSensor implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mOrientation;


    //Negative values indicate right/forwards, positive indicate left/backward
    // (w.r.t. landscape orientation)
    private float leftRightAngle, forwardBackwardAngle;

    public OrientationSensor(Context cxt){
        mSensorManager = (SensorManager) cxt.getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    protected void onResume() {
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth_angle = event.values[0];

        leftRightAngle = event.values[1];
        forwardBackwardAngle = event.values[2];
    }

    public float getLeftRightAngle(){
        return leftRightAngle;
    }

    public float getForwardBackwardAngle(){
        return forwardBackwardAngle;
    }

}
