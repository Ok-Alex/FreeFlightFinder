package me.akulakovsky.ffsearch.app.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import me.akulakovsky.ffsearch.app.MainActivity;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.StartPoint;
import me.akulakovsky.ffsearch.app.events.LocationEvent;

/**
 * Created by Ok-Alex on 7/24/17.
 */

public class NewStartPointDialogFragment extends DialogFragment {

    public static final String TAG = NewStartPointDialogFragment.class.getSimpleName();

    @BindView(R.id.accuracy) TextView mAccuracy;
    @BindView(R.id.name) EditText mName;

    DecimalFormat mFormatter = new DecimalFormat("#.#");

    private LatLng lastLocation;


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_start_point, null);
        ButterKnife.bind(this, root);

        if (((MainActivity) getActivity()).getLocationService() == null) {

        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("New location")
                .setView(root)
                .setPositiveButton("Save", null)
                .setNegativeButton(getActivity().getString(R.string.dialog_button_cancel), null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        EventBus.getDefault().register(this);
        ((MainActivity) getActivity()).startLocationService();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lastLocation != null) {
                            final StartPoint startPoint = new StartPoint(lastLocation.latitude, lastLocation.longitude, new Date(), mName.getText().toString());

                            Realm realm = Realm.getDefaultInstance();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Number currentIdNum = realm.where(StartPoint.class).max("id");
                                    int nextId;
                                    if(currentIdNum == null) {
                                        nextId = 1;
                                    } else {
                                        nextId = currentIdNum.intValue() + 1;
                                    }
                                    startPoint.id = nextId;
                                    realm.copyToRealm(startPoint);
                                    dismiss();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Please wait for GPS fix...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        ((MainActivity) getActivity()).stopLocationService();
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(LocationEvent event) {
        lastLocation = event.lastLocation;
        mAccuracy.setText(mFormatter.format(event.accuracy) + "m");
    }
}