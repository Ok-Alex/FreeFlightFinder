package me.akulakovsky.ffsearch.app.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.SearchRealm;
import me.akulakovsky.ffsearch.app.utils.DateUtils;

/**
 * Created by Ok-Alex on 7/7/17.
 */

public class SearchRealmAdapter extends RealmBaseAdapter<SearchRealm> implements ListAdapter {

    private Context context;
    private LayoutInflater mInflater;

    public SearchRealmAdapter(Context context, @Nullable OrderedRealmCollection<SearchRealm> data) {
        super(data);
        this.context = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_search, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (adapterData != null) {
            SearchRealm searchRealm = adapterData.get(i);
            holder.fill(context, searchRealm);
        }

        return view;
    }

    public static class ViewHolder {

        @BindView(R.id.search_number) TextView tvSearchNumber;
        @BindView(R.id.date) TextView tvDate;
        @BindView(R.id.time) TextView tvTime;

        public ViewHolder(View row) {
            ButterKnife.bind(this, row);
        }

        public void fill(Context context, SearchRealm searchRealm) {
            tvSearchNumber.setText(context.getString(R.string.adapter_search_number) + searchRealm.id);
            tvDate.setText(DateUtils.toPrettyDate(searchRealm.date));
            tvTime.setText(DateUtils.toPrettyTime(searchRealm.date));
        }
    }
}
