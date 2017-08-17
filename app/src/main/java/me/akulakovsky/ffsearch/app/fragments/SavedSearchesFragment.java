package me.akulakovsky.ffsearch.app.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.akulakovsky.ffsearch.app.NavigationActivityV2;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.adapters.SearchRealmAdapter;
import me.akulakovsky.ffsearch.app.entities.SearchRealm;

public class SavedSearchesFragment extends ListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private SearchRealmAdapter adapter;

    private ActionMode mActionMode;

    private List<SearchRealm> deleteItems = new ArrayList<>();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getActivity().getString(R.string.listview_empty_text_searches));

        Realm realm = Realm.getDefaultInstance();
        RealmResults<SearchRealm> results = realm.where(SearchRealm.class)
                .findAllSorted("id");

        adapter = new SearchRealmAdapter(getActivity(), results);

        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
        setListShown(true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mActionMode == null) {
            final SearchRealm mySearch = adapter.getItem(i);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.dialog_title_select_action))
                    .setItems(R.array.item_actions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    NavigationActivityV2.start(getContext(), mySearch.id, false);
                                    break;

                                case 1:
                                    NavigationActivityV2.start(getContext(), mySearch.id, true);
                                    break;

                                case 2:
                                    dialogInterface.dismiss();
                                    break;
                            }
                        }
                    })
                    .create().show();
        } else {
            SearchRealm searchRealm = adapter.getItem(i);
            if (deleteItems.contains(searchRealm)) {
                deleteItems.remove(searchRealm);
                getListView().setItemChecked(i, false);
            } else {
                deleteItems.add(searchRealm);
                getListView().setItemChecked(i, true);
            }
            mActionMode.setTitle(deleteItems.size() + " selected");
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        //MySearch mySearch = new MySearch((Cursor) adapter.getItem(i));
        //mySearch.delete(getActivity());
        SearchRealm searchRealm = adapter.getItem(i);
        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
            deleteItems.add(searchRealm);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            getListView().setItemChecked(i, true);
        } else {
            if (deleteItems.contains(searchRealm)) {
                deleteItems.remove(searchRealm);
                getListView().setItemChecked(i, false);
            } else {
                deleteItems.add(searchRealm);
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
                    for (final SearchRealm mySearch: deleteItems) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                mySearch.deleteFromRealm();
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
