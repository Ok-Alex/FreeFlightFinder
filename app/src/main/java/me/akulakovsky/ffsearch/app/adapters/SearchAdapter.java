package me.akulakovsky.ffsearch.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import me.akulakovsky.ffsearch.app.R;
import me.akulakovsky.ffsearch.app.entities.MySearch;
import me.akulakovsky.ffsearch.app.utils.DateUtils;

public class SearchAdapter extends CursorAdapter {

    private LayoutInflater inflater;
    private Context context;
    private DecimalFormat decimalFormat;

    public SearchAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.decimalFormat = new DecimalFormat("#.##");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_search, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MySearch mySearch = new MySearch(cursor);
        ((TextView) view.findViewById(R.id.search_number)).setText(this.context.getString(R.string.adapter_search_number) + mySearch.getId());
        //((TextView) view.findViewById(R.id.model_name)).setText(this.context.getString(R.string.adapter_model_name) + " " + mySearch.getModelName());
        //((TextView) view.findViewById(R.id.frequency)).setText(this.context.getString(R.string.adapter_frequency) + " " + mySearch.getFrequency());
        //((TextView) view.findViewById(R.id.bearing)).setText(this.context.getString(R.string.adapter_bearing) + " " + decimalFormat.format(mySearch.getBearing()) + "°");
        ((TextView) view.findViewById(R.id.date)).setText(DateUtils.toPrettyDate(mySearch.getDate()));
        ((TextView) view.findViewById(R.id.time)).setText(DateUtils.toPrettyTime(mySearch.getDate()));
    }
}
