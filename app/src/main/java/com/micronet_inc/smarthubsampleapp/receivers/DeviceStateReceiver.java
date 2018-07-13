package com.micronet_inc.smarthubsampleapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import android.util.Pair;
import com.micronet_inc.smarthubsampleapp.activities.MainActivity;

public class DeviceStateReceiver extends BroadcastReceiver {

    public final String TAG = getClass().getSimpleName();
    private static final int MICRONET_869_MCU_VID = 0x15A2;
    private static final int MICRONET_869_MCU_PID = 0x305;

    public static final String dockAction = "com.micronet.smarthubsampleapp.dockevent";
    public static final String portsAttachedAction = "com.micronet.smarthubsampleapp.portsattached";
    public static final String portsDetachedAction = "com.micronet.smarthubsampleapp.portsdetached";

    public DeviceStateReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Reduce lag in activity by going async
        goAsync();

        // Get the action from intent
        String action = intent.getAction();

        // Handle action
        if (Intent.ACTION_DOCK_EVENT.equals(action)) {
            int dockState = intent.getIntExtra(android.content.Intent.EXTRA_DOCK_STATE, -1);
            MainActivity.setDockState(dockState);

            Pair<String, Integer> extra = new Pair<>(android.content.Intent.EXTRA_DOCK_STATE, dockState);
            sendLocalBroadcast(context, dockAction, extra);
            Log.d(TAG, "Dock event received: " + dockState);
        } else {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            // Check if attached or detached device is the Virtual Coms
            if(device.getProductId() == MICRONET_869_MCU_PID && device.getVendorId() == MICRONET_869_MCU_VID){

                // Send broadcast depending on action
                if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
                    sendLocalBroadcast(context, portsAttachedAction, null);
                    MainActivity.setTtyPortsState(true);
                }else{
                    sendLocalBroadcast(context, portsDetachedAction, null);
                    MainActivity.setTtyPortsState(false);
                }

                Log.d(TAG, "Sent local broadcast with action: " + action);
            }
        }
    }

    public static IntentFilter getLocalIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(dockAction);
        intentFilter.addAction(portsAttachedAction);
        intentFilter.addAction(portsDetachedAction);
        return intentFilter;
    }

    private void sendLocalBroadcast(Context context, String action, Pair<String, Integer> extra){
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        Intent localIntent = new Intent(action);

        // Add extra if not null
        if(extra != null){
            localIntent.putExtra(extra.first, extra.second);
        }

        localBroadcastManager.sendBroadcast(localIntent);
    }
}
