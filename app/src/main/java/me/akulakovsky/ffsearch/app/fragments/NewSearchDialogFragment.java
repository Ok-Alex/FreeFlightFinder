package me.akulakovsky.ffsearch.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.util.Date;

import me.akulakovsky.ffsearch.app.NavigationActivity;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.MySearch;
import me.akulakovsky.ffsearch.app.utils.Settings;

public class NewSearchDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener, SensorEventListener {

    private View v;
    private EditText etBearing;
    private EditText etBearing2;
    private EditText etModelName;
    private EditText etFrequency;
    private EditText etTime;
    private EditText etWind;
    private CheckBox chkSave;

    private Sensor mOrientationSensor;
    private SensorManager sensorManager;

    private int currentBearing = 1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        v = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_new_search, null);
        etBearing = (EditText) v.findViewById(R.id.bearing);
        etBearing2 = (EditText) v.findViewById(R.id.bearing2);
        etModelName = (EditText) v.findViewById(R.id.model);
        etFrequency = (EditText) v.findViewById(R.id.frequency);
        etTime = (EditText) v.findViewById(R.id.time);
        etWind = (EditText) v.findViewById(R.id.wind);
        chkSave = (CheckBox) v.findViewById(R.id.save);
        chkSave.setOnCheckedChangeListener(this);
        chkSave.setChecked(Settings.get().getValue(Settings.KEY_SAVE_ENABLED, false));
        ((CheckBox) v.findViewById(R.id.compass)).setOnCheckedChangeListener(this);
        ((CheckBox) v.findViewById(R.id.compass2)).setOnCheckedChangeListener(this);

        etModelName.setText(Settings.get().getValue(Settings.KEY_LAST_MODEL_NAME, ""));
        etFrequency.setText(Settings.get().getValue(Settings.KEY_LAST_FREQUENCY, ""));

        sensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        mOrientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.dialog_title_new_search))
                .setView(v)
                .setPositiveButton(getActivity().getString(R.string.dialog_button_start), this)
                .setNegativeButton(getActivity().getString(R.string.dialog_button_cancel), null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            Button btnNeutral = d.getButton(Dialog.BUTTON_NEUTRAL);
            Button btnNegative = d.getButton(Dialog.BUTTON_NEGATIVE);
            if (positiveButton != null) {
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(etBearing.getText())) {
                            etBearing.setError(getActivity().getString(R.string.dialog_bearing_error));
                        } else {
                            if (chkSave.isChecked()) {
                                Settings.get().putValue(Settings.KEY_LAST_MODEL_NAME, etModelName.getText().toString());
                                Settings.get().putValue(Settings.KEY_LAST_FREQUENCY, etFrequency.getText().toString());
                            } else {
                                Settings.get().putValue(Settings.KEY_LAST_MODEL_NAME, "");
                                Settings.get().putValue(Settings.KEY_LAST_FREQUENCY, "");
                            }

                            double bearing = Double.parseDouble(etBearing.getText().toString());
                            while (bearing > 360) {
                                bearing = bearing - 360;
                            }
                            bearing = bearing - Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);

                            double bearing2 = 0;
                            if (!TextUtils.isEmpty(etBearing2.getText())) {
                                bearing2 = Double.parseDouble(etBearing2.getText().toString());
                                while (bearing2 > 360) {
                                    bearing2 = bearing2 - 360;
                                }
                                bearing2 = bearing2 - Settings.get().getValue(Settings.KEY_BEARING_MISTAKE, 0.0f);
                            }

                            double distance = -1;
                            if (!TextUtils.isEmpty(etWind.getText()) && !TextUtils.isEmpty(etTime.getText())) {
                                distance = Double.parseDouble(etTime.getText().toString()) * Double.parseDouble(etWind.getText().toString());
                            }

                            Intent intent = new Intent(getActivity(), NavigationActivity.class);
//                            MySearch mySearch = MySearch.newSearch(getActivity(),
//                                    null,
//                                    bearing,
//                                    bearing2,
//                                    etModelName.getText().toString(),
//                                    etFrequency.getText().toString(),
//                                    new Date());
//                            intent.putExtra("search", mySearch);
//                            intent.putExtra("distance", distance);
//                            startActivity(intent);

                            sensorManager.unregisterListener(NewSearchDialogFragment.this);
                            dismiss();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.compass:
                currentBearing = 1;
                if (b) {
                    //sensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_UI);
                    //sensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
                } else {
                    sensorManager.unregisterListener(this);
                }
                break;

            case R.id.compass2:
                currentBearing = 2;
                if (b) {
                    //sensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_UI);
                    //sensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
                } else {
                    sensorManager.unregisterListener(this);
                }
                break;

            case R.id.save:
                Settings.get().putValue(Settings.KEY_SAVE_ENABLED, b);
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            mMagnetic = sensorEvent.values.clone();
//        }
//
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            mGravity = sensorEvent.values.clone();
//        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            if (currentBearing == 1) {
                etBearing.setText(new DecimalFormat("#.#").format(sensorEvent.values[0]).replace(",", "."));
            } else {
                etBearing2.setText(new DecimalFormat("#.#").format(sensorEvent.values[0]).replace(",", "."));
            }
        }
//
//        if (mMagnetic != null && mGravity != null) {
//            float[] r = new float[3];
//            SensorManager.getRotationMatrix(r, I, accels, mags);
//            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X,
//                    SensorManager.AXIS_Z, remappedR);
//            SensorManager.getOrientation(remappedR, orientation);
//            newAzimuth = (float) Math.round(Math.toDegrees(orientation[0]));
//            newAzimuth = (newAzimuth + 360)%360;
//        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
