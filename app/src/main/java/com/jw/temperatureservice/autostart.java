package com.jw.temperatureservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class autostart extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,TemperatureService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arg0.startForegroundService(intent);
        } else {
            arg0.startService(intent);
        }
        Log.i(TemperatureService.TAG, "Autostart Service");
    }
}
