package com.jw.temperatureservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import static java.lang.Math.max;

public class TemperatureService extends Service {

    public static final String TAG = "com.jw.temperatureservice";

    private static final int ONGOING_NOTIFICATION_ID = 101;
    private static final String CHANNEL_DEFAULT_IMPORTANCE = "com.jw.temperatureservice";
    private Notification.Builder notificationBuilder;
    private NotificationManager manager;

    private Timer t;

    private boolean rootcheck = false;
    private Shell sh = null;

    private int temp0, temp1, temp0_old, temp1_old;

    private float mSubtractor = 25, mMultiplicator = 1.5f;
    private int mLimit = 75;
    private int mFrequency = 1000;
    private long mPeriod = 1000000;
    private long mFactorProzentToPeriod = 10000;

    private String mPath_Export = "/sys/class/pwm/pwmchip0/export";
    private String mPath_Enable = "/sys/class/pwm/pwmchip0/pwm1/enable";
    private String mPath_Period = "/sys/class/pwm/pwmchip0/pwm1/period";
    private String mPath_DutyCycle = "/sys/class/pwm/pwmchip0/pwm1/duty_cycle";
    private String mPath_Zone0 = "/sys/class/thermal/thermal_zone0/temp";
    private String mPath_Zone1 = "/sys/class/thermal/thermal_zone1/temp";;

    public TemperatureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        t = new Timer();

        if (RootTools.isRootAvailable()) {
            Log.i(TAG, "Root is available");
            rootcheck = true;
        }

        if (RootTools.isAccessGiven()) {
            // your app has been granted root access
            Log.i(TAG, "RootTools access is given");

            try {
                sh = RootTools.getShell(true);
                //sh.add(command);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (RootDeniedException e) {
                e.printStackTrace();
            }
        }  else {
            Log.e(TAG, "RootTools access is not given");
        }

        String model = getDeviceName().toLowerCase();
        if ((!model.contains("odroid-c4"))&(!model.contains("odroidn"))&(!model.contains("odroid-n"))) {
            Log.e(TAG, "Wrong device! Device: " + model);
            stopSelf();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String,?> keys = prefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            if  (entry.getKey().contains("subtrahend")) {
                mSubtractor = Float.valueOf((String)entry.getValue());
            }

            if  (entry.getKey().contains("factor")) {
                mMultiplicator = Float.valueOf((String)entry.getValue());
            }

            if  (entry.getKey().contains("warn_limit")) {
                mLimit = Integer.valueOf((String)entry.getValue());
            }

            if  (entry.getKey().contains("export")) {
                mPath_Export = (String)entry.getValue();
            }
            if  (entry.getKey().contains("enable_pwm")) {
                mPath_Enable = (String)entry.getValue();
            }

            if  (entry.getKey().contains("set_period")) {
                mPath_Period = (String)entry.getValue();
            }

            if  (entry.getKey().contains("set_duty_cycle")) {
                mPath_DutyCycle = (String)entry.getValue();
            }

            if  (entry.getKey().contains("set_frequency")) {
                mFrequency = Integer.valueOf((String)entry.getValue());
                Double ms = (1d / mFrequency) * 1000;
                mPeriod = (long) (ms * 1000 * 1000);
                mFactorProzentToPeriod = mPeriod / 100;
            }
        }

        //enable and set pwm
        String cmd = "echo 1 > " + mPath_Export;
        try {

            sh.add(new Command(0, cmd){
                @Override
                public void commandOutput(int id, String line)
                {
                    super.commandOutput(id, line);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        cmd = "echo " + mPeriod + " > " + mPath_Period;
        try {

            sh.add(new Command(0, cmd){
                @Override
                public void commandOutput(int id, String line)
                {
                    super.commandOutput(id, line);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        cmd = "echo 1 > " + mPath_Enable;
        try {

            sh.add(new Command(0, cmd){
                @Override
                public void commandOutput(int id, String line)
                {
                    super.commandOutput(id, line);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        timer.cancel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, SettingsActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationChannel chan = new NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, "My Service", NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            RemoteViews collapsedView = new RemoteViews(getPackageName(),
                    R.layout.notification_collapsed);

            notificationBuilder =
                    new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                            .setContentTitle(getText(R.string.app_name))
                            .setContentText(getText(R.string.notification_content))
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentIntent(pendingIntent)
                            .setCustomContentView(collapsedView)
                            //.setCustomBigContentView(expandedView)
                            .setSmallIcon(R.drawable.ic_fan);

            notificationBuilder.setOnlyAlertOnce(true);

            startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build());
        }

        if (t != null) {
            t.scheduleAtFixedRate(timer, 500 , 2500);
            Log.i(TAG, "onStartCommand().t.scheduleAtFixedRate()");
        }

        return Service.START_NOT_STICKY;
    }

    TimerTask timer = new TimerTask() {

        @Override
        public void run() {

            Boolean update_pwm = false;
            RemoteViews collapsedView = new RemoteViews(getPackageName(),
                    R.layout.notification_collapsed);

            if (temp0 != temp0_old) {
                Log.i(TAG, "Temperature zone 0 changed: " + temp0);

                update_pwm = true;

                collapsedView.setTextViewText(R.id.text_view_temp0, String.valueOf(temp0) + "°C");

                if (temp0 > 75) {
                    collapsedView.setTextColor(R.id.text_view_temp0, Color.RED);
                    notificationBuilder.setOnlyAlertOnce(false);

                } else {
                    collapsedView.setTextColor(R.id.text_view_temp0, Color.BLACK);
                    notificationBuilder.setOnlyAlertOnce(true);
                }
            }

            if (temp1 != temp1_old) {
                Log.i(TAG, "Temperature zone 1 changed: " + temp1);

                update_pwm = true;

                collapsedView.setTextViewText(R.id.text_view_temp1, String.valueOf(temp1) + "°C");

                if (temp0 > 75) {
                    collapsedView.setTextColor(R.id.text_view_temp1, Color.RED);
                    notificationBuilder.setOnlyAlertOnce(false);

                } else {
                    collapsedView.setTextColor(R.id.text_view_temp1, Color.BLACK);
                    notificationBuilder.setOnlyAlertOnce(true);
                }
            }

            if (max(temp0, temp1) > mLimit ) {
                collapsedView.setImageViewResource(R.id.iv_icon, R.drawable.ic_fan_red);
            } else {
                collapsedView.setImageViewResource(R.id.iv_icon, R.drawable.ic_fan_green);
            }

            temp0_old = temp0;
            temp1_old = temp1;

            if (update_pwm) {

                int duty_cycle = (int) ((int)(max(temp0, temp1) - mSubtractor) * mMultiplicator);
                if (duty_cycle > 100) duty_cycle = 100;
                if (duty_cycle < 25) duty_cycle = 25;

                duty_cycle *= mFactorProzentToPeriod;
                final String cmd = "echo " + duty_cycle + " > " + mPath_DutyCycle;

                try {

                    sh.add(new Command(0, cmd){
                        @Override
                        public void commandOutput(int id, String line)
                        {
                            super.commandOutput(id, line);
                        }

                        @Override
                        public void commandCompleted(int id, int exitcode) {
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                notificationBuilder.setCustomContentView(collapsedView);
                collapsedView.setTextViewText(R.id.text_view_dutycycle, String.valueOf(duty_cycle / mFactorProzentToPeriod) + "%");

                manager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build());
            }

            try {
                sh.add(new Command(0, "cat " + mPath_Zone0){
                    @Override
                    public void commandOutput(int id, String line)
                    {
                        super.commandOutput(id, line);
                        try {
                            temp0 = Integer.valueOf(line) / 1000;
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void commandCompleted(int id, int exitcode) {
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sh.add(new Command(0, "cat " + mPath_Zone1){
                    @Override
                    public void commandOutput(int id, String line)
                    {
                        super.commandOutput(id, line);
                        try {
                            temp1 = Integer.valueOf(line) / 1000;
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void commandCompleted(int id, int exitcode) {
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }
}
