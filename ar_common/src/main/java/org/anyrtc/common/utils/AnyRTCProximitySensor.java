package org.anyrtc.common.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

/**
 * RTCProximitySensor manages functions related to the proximity sensor in the
 * AnyRTC RTMPC_Line demo. On most device, the proximity sensor is implemented as a
 * boolean-sensor. It returns just two values "NEAR" or "FAR". Thresholding is
 * done on the LUX value i.e. the LUX value of the light sensor is compared with
 * a threshold. A LUX-value more than the threshold means the proximity sensor
 * returns "FAR". Anything less than the threshold value and the sensor returns
 * "NEAR".
 * 
 * 说明： Sensor管理器
 * 
 * By AnyRTC.inc  - 2016/9/18
 * 
 * @author Ming
 *
 */
public class AnyRTCProximitySensor implements SensorEventListener {
	private static final String TAG = "RTCProximitySensor";

	// This class should be created, started and stopped on one thread
	// (e.g. the main thread). We use |nonThreadSafe| to ensure that this is
	// the case. Only active when |DEBUG| is set to true.
	private final AnyRTCUtils.NonThreadSafe nonThreadSafe = new AnyRTCUtils.NonThreadSafe();

	private final Runnable onSensorStateListener;
	private final SensorManager sensorManager;
	private Sensor proximitySensor = null;
	private boolean lastStateReportIsNear = false;

	/** Construction */
	static AnyRTCProximitySensor create(Context context, Runnable sensorStateListener) {
		return new AnyRTCProximitySensor(context, sensorStateListener);
	}

	private AnyRTCProximitySensor(Context context, Runnable sensorStateListener) {
		Log.d(TAG, "RTKProximitySensor" + AnyRTCUtils.getThreadInfo());
		onSensorStateListener = sensorStateListener;
		sensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
	}

	/**
	 * Activate the proximity sensor. Also do initializtion if called for the
	 * first time.
	 */
	public boolean start() {
		checkIfCalledOnValidThread();
		Log.d(TAG, "start" + AnyRTCUtils.getThreadInfo());
		if (!initDefaultSensor()) {
			// Proximity sensor is not supported on this device.
			return false;
		}
		sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		return true;
	}

	/** Deactivate the proximity sensor. */
	public void stop() {
		checkIfCalledOnValidThread();
		Log.d(TAG, "stop" + AnyRTCUtils.getThreadInfo());
		if (proximitySensor == null) {
			return;
		}
		sensorManager.unregisterListener(this, proximitySensor);
	}

	/** Getter for last reported state. Set to true if "near" is reported. */
	public boolean sensorReportsNearState() {
		checkIfCalledOnValidThread();
		return lastStateReportIsNear;
	}

	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		checkIfCalledOnValidThread();
		AnyRTCUtils.assertIsTrue(sensor.getType() == Sensor.TYPE_PROXIMITY);
		if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			Log.e(TAG, "The values returned by this sensor cannot be trusted");
		}
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		checkIfCalledOnValidThread();
		AnyRTCUtils.assertIsTrue(event.sensor.getType() == Sensor.TYPE_PROXIMITY);
		// As a best practice; do as little as possible within this method and
		// avoid blocking.
		float distanceInCentimeters = event.values[0];
		if (distanceInCentimeters < proximitySensor.getMaximumRange()) {
			Log.d(TAG, "Proximity sensor => NEAR state");
			lastStateReportIsNear = true;
		} else {
			Log.d(TAG, "Proximity sensor => FAR state");
			lastStateReportIsNear = false;
		}

		// Report about new state to listening client. Client can then call
		// sensorReportsNearState() to query the current state (NEAR or FAR).
		if (onSensorStateListener != null) {
			onSensorStateListener.run();
		}

		Log.d(TAG, "onSensorChanged" + AnyRTCUtils.getThreadInfo() + ": " + "accuracy=" + event.accuracy + ", timestamp="
				+ event.timestamp + ", distance=" + event.values[0]);
	}

	/**
	 * Get default proximity sensor if it exists. Tablet devices (e.g. Nexus 7)
	 * does not support this type of sensor and false will be retured in such
	 * cases.
	 */
	private boolean initDefaultSensor() {
		if (proximitySensor != null) {
			return true;
		}
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null) {
			return false;
		}
		logProximitySensorInfo();
		return true;
	}

	/** Helper method for logging information about the proximity sensor. */
	private void logProximitySensorInfo() {
		if (proximitySensor == null) {
			return;
		}
        StringBuilder info = new StringBuilder("Proximity sensor: ");
        info.append("name=").append(proximitySensor.getName());
        info.append(", vendor: ").append(proximitySensor.getVendor());
        info.append(", power: ").append(proximitySensor.getPower());
        info.append(", resolution: ").append(proximitySensor.getResolution());
        info.append(", max range: ").append(proximitySensor.getMaximumRange());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
          // Added in API level 9.
          info.append(", min delay: ").append(proximitySensor.getMinDelay());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
          // Added in API level 20.
          info.append(", type: ").append(proximitySensor.getStringType());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // Added in API level 21.
        info.append(", max delay: ").append(proximitySensor.getMaxDelay());
        info.append(", reporting mode: ").append(proximitySensor.getReportingMode());
        info.append(", isWakeUpSensor: ").append(proximitySensor.isWakeUpSensor());
        }
        Log.d(TAG, info.toString());
    }

	/**
	 * Helper method for debugging purposes. Ensures that method is called on
	 * same thread as this object was created on.
	 */
	private void checkIfCalledOnValidThread() {
		if (!nonThreadSafe.calledOnValidThread()) {
			throw new IllegalStateException("Method is not called on valid thread");
		}
	}
}
