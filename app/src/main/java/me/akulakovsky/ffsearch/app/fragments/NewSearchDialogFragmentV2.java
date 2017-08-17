package me.akulakovsky.ffsearch.app.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.akulakovsky.ffsearch.app.NavigationActivityV2;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.BearingRealm;
import me.akulakovsky.ffsearch.app.entities.SearchRealm;
import me.akulakovsky.ffsearch.app.entities.StartPoint;
import me.akulakovsky.ffsearch.app.utils.Settings;

/**
 * Created by Ok-Alex on 7/6/17.
 */

public class NewSearchDialogFragmentV2 extends DialogFragment {

    public static final String TAG = NewSearchDialogFragmentV2.class.getSimpleName();

    @BindView(R.id.container) LinearLayout mContainer;
    @BindView(R.id.startLocationSpinner) Spinner mStartLocationSpinner;

    private int[] lineColors;
    private RealmResults<StartPoint> startPointRealmResults;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_search_v2, null);
        ButterKnife.bind(this, root);

        lineColors = getResources().getIntArray(R.array.line_colors);

        Realm realm = Realm.getDefaultInstance();
        startPointRealmResults = realm.where(StartPoint.class)
                .findAllSorted("date");

        List<String> startPointNames = new ArrayList<>();
        startPointNames.add("Current");
        for (int i = 0; i < startPointRealmResults.size(); i++) {
            String name = startPointRealmResults.get(i).name;
            startPointNames.add(TextUtils.isEmpty(name) ? "Location #" + startPointRealmResults.get(i).id : name);
        }

        ArrayAdapter<String> startPointsSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, startPointNames);
        startPointsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStartLocationSpinner.setAdapter(startPointsSpinnerAdapter);

        initFirstBearing();

        return new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.dialog_title_new_search))
                .setView(root)
                .setPositiveButton(getActivity().getString(R.string.dialog_button_start), null)
                .setNegativeButton(getActivity().getString(R.string.dialog_button_cancel), null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        List<BearingRealm> bearings = parseBearings();
                        if (bearings.size() == mContainer.getChildCount()) {
                            //if parsed all, then we good to go

//                            MySearch mySearch = MySearch.newSearch(getContext(), null, new Date());
//                            for (Bearing bearing: bearings) {
//                                bearing.setSearchId(mySearch.getId());
//                                bearing.save(getContext());
//                            }
                            final SearchRealm searchRealm = new SearchRealm();
                            searchRealm.bearings = new RealmList<>();
                            searchRealm.bearings.addAll(bearings);
                            searchRealm.date = new Date();

                            if (mStartLocationSpinner.getSelectedItemPosition() != 0) {
                                StartPoint startPoint = startPointRealmResults.get(mStartLocationSpinner.getSelectedItemPosition() - 1);
                                if (startPoint != null) {
                                    searchRealm.startPointLat = startPoint.lat;
                                    searchRealm.startPointLng = startPoint.lng;
                                }
                            }

                            Realm realm = Realm.getDefaultInstance();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Number currentIdNum = realm.where(SearchRealm.class).max("id");
                                    int nextId;
                                    if(currentIdNum == null) {
                                        nextId = 1;
                                    } else {
                                        nextId = currentIdNum.intValue() + 1;
                                    }
                                    searchRealm.id = nextId;

                                    realm.copyToRealm(searchRealm);
                                    Log.d(TAG, "WE GOOD TO GO!!!");

                                    NavigationActivityV2.start(getContext(), nextId, false);
                                    dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private List<BearingRealm> parseBearings() {
        List<BearingRealm> bearings = new ArrayList<>();
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            View lineView = mContainer.getChildAt(i);
            EditText bearingView = lineView.findViewById(R.id.bearing);
            EditText notesView = lineView.findViewById(R.id.notes);
            View line = lineView.findViewById(R.id.line);
            ColorDrawable lineColor = (ColorDrawable) line.getBackground();

            if (!TextUtils.isEmpty(bearingView.getText())) {
                double bearing = Double.parseDouble(bearingView.getText().toString());
                while (bearing > 360) {
                    bearing = bearing - 360;
                }
                BearingRealm bearingRealm = new BearingRealm();
                bearingRealm.bearing = bearing;
                bearingRealm.description = notesView.getText().toString();
                bearingRealm.color = lineColor.getColor();
                bearings.add(bearingRealm);
            } else {
                bearingView.setError(getString(R.string.dialog_bearing_error));
            }
        }
        return bearings;
    }

    private void initFirstBearing() {
        View firstBearing = mContainer.getChildAt(0);
        firstBearing.findViewById(R.id.remove).setVisibility(View.INVISIBLE);
        EditText notesView = firstBearing.findViewById(R.id.notes);
        loadNote(0, notesView);
        addNotesWatcher(0, notesView);
    }

    @OnClick(R.id.add_more)
    public void onAddMoreClick(View v) {
        final View bearingView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_dialog_bearing, mContainer, false);
        mContainer.addView(bearingView);

        View line = bearingView.findViewById(R.id.line);
        TextView lineName = bearingView.findViewById(R.id.line_name);
        EditText notesView = bearingView.findViewById(R.id.notes);

        int lineIndex = mContainer.getChildCount() - 1;

        loadNote(lineIndex, notesView);
        addNotesWatcher(lineIndex, notesView);

        while (lineIndex >= lineColors.length) {
            lineIndex = Math.abs((lineColors.length) - lineIndex);
        }

        line.setBackgroundColor(lineColors[lineIndex]);
        lineName.setTextColor(lineColors[lineIndex]);

        bearingView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContainer.removeView(bearingView);
                initFirstBearing();
            }
        });
    }

    private void loadNote(int index, EditText editText) {
        String notesString = Settings.get().getValue(Settings.KEY_NOTES, "");
        String[] notes = notesString.split(",");
        if (index < notes.length) {
            editText.setText(!notes[index].equals("null") ? notes[index]: "");
        }
    }

    private void saveNotes(int index, String note) {
        String notesString = Settings.get().getValue(Settings.KEY_NOTES, "");
        String[] notes = notesString.split(",");
        String[] newNotes = notes;
        if (index + 1 > notes.length) {
            newNotes = new String[index + 1];
            System.arraycopy(notes, 0, newNotes, 0, notes.length);
        }

        newNotes[index] = note;

        StringBuilder sb = new StringBuilder();
        for (String note1 : newNotes) {
            sb.append(note1).append(",");
        }
        Settings.get().putValue(Settings.KEY_NOTES, sb.toString());
    }

    private void addNotesWatcher(final int index, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveNotes(index, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}