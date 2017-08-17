package me.akulakovsky.ffsearch.app.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import me.akulakovsky.ffsearch.app.entities.StartPoint;
import me.akulakovsky.ffsearch.app.utils.DateUtils;

/**
 * Created by Ok-Alex on 7/24/17.
 */

public class StartPointRealmAdapter extends RealmBaseAdapter<StartPoint> implements ListAdapter {

    private LayoutInflater mInflater;

    public StartPointRealmAdapter(Context context, @Nullable OrderedRealmCollection<StartPoint> data) {
        super(data);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        StartPointRealmAdapter.ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_search, viewGroup, false);
            holder = new StartPointRealmAdapter.ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (StartPointRealmAdapter.ViewHolder) view.getTag();
        }

        if (adapterData != null) {
            StartPoint startPoint = adapterData.get(i);
            holder.fill(startPoint);
        }

        return view;
    }

    public static class ViewHolder {

        @BindView(R.id.search_number) TextView tvName;
        @BindView(R.id.date) TextView tvDate;
        @BindView(R.id.time) TextView tvTime;

        public ViewHolder(View row) {
            ButterKnife.bind(this, row);
        }

        public void fill(StartPoint startPoint) {
            tvName.setText(TextUtils.isEmpty(startPoint.name) ? "Location #" + startPoint.id : startPoint.name);
            tvDate.setText(DateUtils.toPrettyDate(startPoint.date));
            tvTime.setText(DateUtils.toPrettyTime(startPoint.date));
        }
    }
}
