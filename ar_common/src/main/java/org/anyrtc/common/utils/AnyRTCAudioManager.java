package org.anyrtc.common.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AnyRTCAudioManager manages all audio related parts of the anyRTC demo. 说明：
 * Audio管理器
 *
 * By AnyRTC.inc - 2016/9/18
 *
 * @author Ming
 *
 */
public class AnyRTCAudioManager {
    private static final String TAG = "AnyRTCAudioManager";
    private static final String SPEAKERPHONE_AUTO = "auto";
    private static final String SPEAKERPHONE_TRUE = "true";
    private static final String SPEAKERPHONE_FALSE = "false";
    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.// TODO(henrika): add support for BLUETOOTH as well.
     */
    public enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE,
    }

    private final Context apprtcContext;
    private final Runnable onStateChangeListener;
    private boolean initialized = false;
    private AudioManager audioManager;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private final AudioDevice defaultAudioDevice;
    // Contains speakerphone setting: auto, true or false
    private final String useSpeakerphone;
    // Proximity sensor object. It measures the proximity of an object in cm
    // relative to the view screen of a device and can therefore be used to
    // assist device switching (close to ear <=> use headset earpiece if
    // available, far from ear <=> use speaker phone).
    private AnyRTCProximitySensor proximitySensor = null;

    // Contains the currently selected audio device.
    private AudioDevice selectedAudioDevice;

    // Contains a list of available audio devices. A Set collection is used to
    // avoid duplicate elements.
    private final Set<AudioDevice> audioDevices = new HashSet<AudioDevice>();

    // Broadcast receiver for wired headset intent broadcasts.
    private BroadcastReceiver wiredHeadsetReceiver;

    // This method is called when the proximity sensor reports a state change,
    // e.g. from "NEAR to FAR" or from "FAR to NEAR".
    private void onProximitySensorChangedState() {
        if (!useSpeakerphone.equals(SPEAKERPHONE_AUTO)) {
            return;
        }
        // The proximity sensor should only be activated when there are exactly two
        // available audio devices.
        if (audioDevices.size() == 2
                && audioDevices.contains(AudioDevice.EARPIECE)
                && audioDevices.contains(AudioDevice.SPEAKER_PHONE)) {
            if(audioManager.isBluetoothA2dpOn()) {
                changeToBluetoothHeadset();
            } else {
                if (proximitySensor.sensorReportsNearState()) {
                    // Sensor reports that a "handset is being held up to a person's ear",
                    // or "something is covering the light sensor".
                    setAudioDevice(AudioDevice.EARPIECE);
                } else {
                    // Sensor reports that a "handset is removed from a person's ear", or
                    // "the light sensor is no longer covered".
                    setAudioDevice(AudioDevice.SPEAKER_PHONE);
                }
            }
        }
    }

    /** Construction */
    public static AnyRTCAudioManager create(Context context, Runnable deviceStateChangeListener) {
        return new AnyRTCAudioManager(context, deviceStateChangeListener);
    }

    private AnyRTCAudioManager(Context context,
                               Runnable deviceStateChangeListener) {
        apprtcContext = context;
        onStateChangeListener = deviceStateChangeListener;
        audioManager = ((AudioManager) context.getSystemService(
                Context.AUDIO_SERVICE));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        useSpeakerphone = sharedPreferences.getString("speakerphone_preference", "auto");
        if (useSpeakerphone.equals(SPEAKERPHONE_FALSE)) {
            defaultAudioDevice = AudioDevice.EARPIECE;
        } else {
            defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
        }
        // Create and initialize the proximity sensor.
        // Tablet devices (e.g. Nexus 7) does not support proximity sensors.
        // Note that, the sensor will not be active until start() has been called.
        proximitySensor = AnyRTCProximitySensor.create(context, new Runnable() {
            // This method will be called each time a state change is detected.
            // Example: user holds his hand over the device (closer than ~5 cm),
            // or removes his hand from the device.
            public void run() {
                onProximitySensorChangedState();
            }
        });
        AnyRTCUtils.logDeviceInfo(TAG);
    }

    public void init() {
        Log.d(TAG, "init");
        if (initialized) {
            return;
        }
        // Store current audio state so we can restore it when close() is called.
        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();
        // Request audio focus before making any device switch.
        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
        // required to be in this mode when playout and/or recording starts for
        // best possible VoIP performance.
        // TODO(henrika): we migh want to start with RINGTONE mode here instead.
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false);
        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a wired headset or by covering/uncovering the
        // proximity sensor.
        updateAudioDeviceState(hasWiredHeadset());
        // Register receiver for broadcast intents related to adding/removing a
        // wired headset (Intent.ACTION_HEADSET_PLUG).
        registerForWiredHeadsetIntentBroadcast();
        registerBluetooth();
        initialized = true;
    }

    public void close() {
        Log.d(TAG, "close");
        if (!initialized) {
            return;
        }
        unregisterForWiredHeadsetIntentBroadcast();
        unRegisterBluetooch();
        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);
        audioManager.stopBluetoothSco();
        audioManager.abandonAudioFocus(null);
        if (proximitySensor != null) {
            proximitySensor.stop();
            proximitySensor = null;
        }
        initialized = false;
    }

    /** Changes selection of the currently active audio device. */
    public void setAudioDevice(AudioDevice device) {
        Log.d(TAG, "setAudioDevice(device=" + device + ")");
        //AnyRTCUtils.assertIsTrue(audioDevices.contains(device));
        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                selectedAudioDevice = AudioDevice.SPEAKER_PHONE;
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                selectedAudioDevice = AudioDevice.EARPIECE;
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                selectedAudioDevice = AudioDevice.WIRED_HEADSET;
                break;
            default:
                Log.e(TAG, "Invalid audio device selection");
                break;
        }
        onAudioManagerChangedState();
    }
    /** Returns current set of available/selectable audio devices. */
    public Set<AudioDevice> getAudioDevices() {
        return Collections.unmodifiableSet(new HashSet<AudioDevice>(audioDevices));
    }
    /** Returns the currently selected audio device. */
    public AudioDevice getSelectedAudioDevice() {
        return selectedAudioDevice;
    }
    /**
     * Registers receiver for the broadcasted intent when a wired headset is
     * plugged in or unplugged. The received intent will have an extra
     * 'state' value where 0 means unplugged, and 1 means plugged.
     */
    private void registerForWiredHeadsetIntentBroadcast() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        /** Receiver which handles changes in wired headset availability. */
        wiredHeadsetReceiver = new BroadcastReceiver() {
            private static final int STATE_UNPLUGGED = 0;
            private static final int STATE_PLUGGED = 1;
            private static final int HAS_NO_MIC = 0;
            private static final int HAS_MIC = 1;
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("state", STATE_UNPLUGGED);
                int microphone = intent.getIntExtra("microphone", HAS_NO_MIC);
                String name = intent.getStringExtra("name");
                Log.d(TAG, "BroadcastReceiver.onReceive" + AnyRTCUtils.getThreadInfo()
                        + ": "
                        + "a=" + intent.getAction()
                        + ", s=" + (state == STATE_UNPLUGGED ? "unplugged" : "plugged")
                        + ", m=" + (microphone == HAS_MIC ? "mic" : "no mic")
                        + ", n=" + name
                        + ", sb=" + isInitialStickyBroadcast());
                boolean hasWiredHeadset = (state == STATE_PLUGGED);
                switch (state) {
                    case STATE_UNPLUGGED:
                        updateAudioDeviceState(hasWiredHeadset);
                        break;
                    case STATE_PLUGGED:
                        if (selectedAudioDevice != AudioDevice.WIRED_HEADSET) {
                            updateAudioDeviceState(hasWiredHeadset);
                        }
                        break;
                    default:
                        Log.e(TAG, "Invalid state");
                        break;
                }
            }
        };
        apprtcContext.registerReceiver(wiredHeadsetReceiver, filter);
    }
    /** Unregister receiver for broadcasted ACTION_HEADSET_PLUG intent. */
    private void unregisterForWiredHeadsetIntentBroadcast() {
        apprtcContext.unregisterReceiver(wiredHeadsetReceiver);
        wiredHeadsetReceiver = null;
    }
    /** Sets the speaker phone mode. */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
    }
    /** Sets the microphone mute state. */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        audioManager.setMicrophoneMute(on);
    }
    /** Gets the current earpiece state. */
    private boolean hasEarpiece() {
        return apprtcContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_TELEPHONY);
    }
    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        return audioManager.isWiredHeadsetOn();
    }

    /** Update list of possible audio devices and make new device selection. */
    private void updateAudioDeviceState(boolean hasWiredHeadset) {
        // Update the list of available audio devices.
        audioDevices.clear();
        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            audioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            audioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece())  {
                audioDevices.add(AudioDevice.EARPIECE);
            }
        }
        Log.d(TAG, "audioDevices: " + audioDevices);
        // Switch to correct audio device given the list of available audio devices.
        if (hasWiredHeadset) {
            setAudioDevice(AudioDevice.WIRED_HEADSET);
        } else {
            setAudioDevice(defaultAudioDevice);
        }
    }
    /** Called each time a new audio device has been added or removed. */
    private void onAudioManagerChangedState() {
        Log.d(TAG, "onAudioManagerChangedState: devices=" + audioDevices
                + ", selected=" + selectedAudioDevice);
        // Enable the proximity sensor if there are two available audio devices
        // in the list. Given the current implementation, we know that the choice
        // will then be between EARPIECE and SPEAKER_PHONE.
        if (audioDevices.size() == 2) {
            AnyRTCUtils.assertIsTrue(audioDevices.contains(AudioDevice.EARPIECE)
                    && audioDevices.contains(AudioDevice.SPEAKER_PHONE));
            // Start the proximity sensor.
            proximitySensor.start();
        } else if (audioDevices.size() == 1) {
            // Stop the proximity sensor since it is no longer needed.
            proximitySensor.stop();
        } else {
            Log.e(TAG, "Invalid device list");
        }
        if (onStateChangeListener != null) {
            // Run callback to notify a listening client. The client can then
            // use public getters to query the new state.
            onStateChangeListener.run();
        }
    }

    /**********************************蓝牙**************************************************/

    private BluetoothConnectionReceiver audioNoisyReceiver;
    /**
     * 注册蓝牙广播接收器
     */
    private void registerBluetooth() {
        audioNoisyReceiver = new BluetoothConnectionReceiver();

        //蓝牙状态广播监听
        IntentFilter audioFilter = new IntentFilter();
        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        apprtcContext.registerReceiver(audioNoisyReceiver, audioFilter);
    }

    /** Unregister receiver for broadcasted bluetooth. */
    private void unRegisterBluetooch() {
        apprtcContext.unregisterReceiver(audioNoisyReceiver);
        audioNoisyReceiver = null;
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker(){
        Log.d(TAG, "Bluetooth changeToSpeaker");
        //注意此处，蓝牙未断开时使用MODE_IN_COMMUNICATION而不是MODE_NORMAL
        if(audioManager.isBluetoothScoOn()) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(true);
        }
    }

    /**
     * 切换到蓝牙音箱
     */
    public void changeToBluetoothHeadset(){
        Log.d(TAG, "Bluetooth changeToBluetoothHeadset");
        if(!audioManager.isBluetoothScoOn()) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
            audioManager.setSpeakerphoneOn(false);
        } else {
            Log.d(TAG, "Bluetooth isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
    }

    public class BluetoothConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
                //蓝牙连接状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    Log.d(TAG, "Bluetooth STATE_CONNECTED");
                    //连接或失联，切换音频输出（到蓝牙）
                    changeToBluetoothHeadset();
                } else if(state == BluetoothAdapter.STATE_DISCONNECTED) {
                    //失联，切换音频输出（到强制仍然扬声器外放）
                    Log.d(TAG, "Bluetooth STATE_DISCONNECTED");
                    changeToSpeaker();
                } else if(state == BluetoothAdapter.STATE_CONNECTING) {
                    //正在连接
                    Log.d(TAG, "Bluetooth STATE_CONNECTING");
                } else if(state == BluetoothAdapter.STATE_DISCONNECTING) {
                    //正在断开
                    Log.d(TAG, "Bluetooth STATE_DISCONNECTING");
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())){
                //本地蓝牙打开或关闭
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    //断开，切换音频输出
                    Log.d(TAG, "Bluetooth STATE_OFF or STATE_TURNING_OFF");
                    changeToSpeaker();
                } else {
                    Log.d(TAG, "Bluetooth STATE_ON");
                }
            }
        }
    }
}
