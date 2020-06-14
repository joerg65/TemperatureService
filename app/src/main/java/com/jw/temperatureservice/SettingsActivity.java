package com.jw.temperatureservice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    public GraphView graphview;
    private SetCalculation onSetCalculation;

    private float mMultiplicator;
    private int mSubtractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        SettingsFragment fragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
        }
        graphview = this.getWindow().findViewById(R.id.graphview);

        onSetCalculation = new SetCalculation () {

            @Override
            public void setSubtrahend(float aSubtrahend) {
                if (graphview != null) {
                    graphview.setSubtrahend(aSubtrahend);
                }
            }

            @Override
            public void setFactor(float aFactor) {
                if (graphview != null) {
                    graphview.setFactor(aFactor);
                }
            }
        };
        fragment.setSetCalculation(onSetCalculation);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Map<String,?> keys = prefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            if  (entry.getKey().contains("subtrahend")) {
                mSubtractor = Integer.valueOf((String)entry.getValue());
            }

            if  (entry.getKey().contains("factor")) {
                mMultiplicator = Float.valueOf((String)entry.getValue());
            }
        }

        onSetCalculation.setSubtrahend(mSubtractor);
        onSetCalculation.setFactor(mMultiplicator);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(this, TemperatureService.class);
                this.stopService(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }

                this.finish();
                break;
            case R.id.button_licenses:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                try {
                    InputStream is  = getApplicationContext().getAssets().open("licenses");
                    builder.setMessage(readFromfile("licenses", getApplicationContext()))
                            .setCancelable(true)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    public static String readFromfile(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName, Context.MODE_PRIVATE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line + "\r\n");
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }



    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SetCalculation onSetCalculation ;
        public void setSetCalculation (SetCalculation  onSetCalculation ) {
            this.onSetCalculation  = onSetCalculation ;
        }

        private void onSetSubtractor (Float aData) {
            if(onSetCalculation !=null)
                onSetCalculation.setSubtrahend(aData);
        }

        private void onSetMultiplicator (Float aData) {
            if(onSetCalculation !=null)
                onSetCalculation.setFactor(aData);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference limitPreference = findPreference("warn_limit");
            if (limitPreference != null) {
                limitPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }
                        });
            }

            EditTextPreference subtractionPreference = findPreference("subtrahend");
            if (subtractionPreference != null) {
                subtractionPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }
                        });
                subtractionPreference.setOnPreferenceChangeListener(
                        new Preference.OnPreferenceChangeListener() {
                            @Override
                            public boolean onPreferenceChange(Preference preference, Object newValue) {
                                float mSubtractor = Float.valueOf((String)newValue);
                                onSetSubtractor(mSubtractor);
                                return true;
                            }
                        }
                );
            }

            EditTextPreference multiplicationPreference = findPreference("factor");
            if (multiplicationPreference != null) {
                multiplicationPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            }
                        });
                multiplicationPreference.setOnPreferenceChangeListener(
                        new Preference.OnPreferenceChangeListener() {
                            @Override
                            public boolean onPreferenceChange(Preference preference, Object newValue) {
                                float mMultiplicator = Float.valueOf((String)newValue);
                                onSetMultiplicator(mMultiplicator);
                                return true;
                            }
                        }
                );
            }

            EditTextPreference frequencyPreference = findPreference("set_frequency");
            if (frequencyPreference != null) {
                frequencyPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }
                        });
            }
        }

    }
}