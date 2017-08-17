package me.akulakovsky.ffsearch.app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.adapters.StartPointRealmAdapter;
import me.akulakovsky.ffsearch.app.entities.StartPoint;

/**
 * Created by Ok-Alex on 7/24/17.
 */

public class StartPointsFragment extends ListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private StartPointRealmAdapter adapter;

    private ActionMode mActionMode;

    private List<StartPoint> deleteItems = new ArrayList<>();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getActivity().getString(R.string.listview_empty_text_start_points));

        Realm realm = Realm.getDefaultInstance();
        RealmResults<StartPoint> results = realm.where(StartPoint.class)
                .findAllSorted("date");

        adapter = new StartPointRealmAdapter(getActivity(), results);

        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
        setListShown(true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mActionMode == null) {
            final StartPoint startPoint = adapter.getItem(i);
            new AlertDialog.Builder(getActivity())
                    .setTitle("Location info")
                    .setMessage(TextUtils.isEmpty(startPoint.name) ? "Location #" + startPoint.id : startPoint.name)
                    .setPositiveButton("OK", null)
                    .create().show();
        } else {
            StartPoint startPoint = adapter.getItem(i);
            if (deleteItems.contains(startPoint)) {
                deleteItems.remove(startPoint);
                getListView().setItemChecked(i, false);
            } else {
                deleteItems.add(startPoint);
                getListView().setItemChecked(i, true);
            }
            mActionMode.setTitle(deleteItems.size() + " selected");
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        //MySearch mySearch = new MySearch((Cursor) adapter.getItem(i));
        //mySearch.delete(getActivity());
        StartPoint startPoint = adapter.getItem(i);
        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
            deleteItems.add(startPoint);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            getListView().setItemChecked(i, true);
        } else {
            if (deleteItems.contains(startPoint)) {
                deleteItems.remove(startPoint);
                getListView().setItemChecked(i, false);
            } else {
                deleteItems.add(startPoint);
                getListView().setItemChecked(i, true);
            }
        }
        mActionMode.setTitle(deleteItems.size() + " selected");

        return true;
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_main_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.delete:
                    Realm realm = Realm.getDefaultInstance();
                    for (final StartPoint startPoint: deleteItems) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                startPoint.deleteFromRealm();
                            }
                        });
                    }
                    actionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            getListView().clearChoices();

            //workaround for some items not being unchecked.
            //see http://stackoverflow.com/a/10542628/1366471
            for (int i = 0; i < getListView().getChildCount(); i++) {
                getListView().getChildAt(i).setSelected(false);
                getListView().getChildAt(i).getBackground().setState(new int[] { 0 });
            }

            getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
            getListView().setAdapter(adapter);
            mActionMode = null;
            deleteItems.clear();
        }
    }
}