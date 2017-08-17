package me.akulakovsky.ffsearch.app.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;

import me.akulakovsky.ffsearch.app.entities.BearingRealm;

/**
 * Created by Ok-Alex on 7/7/17.
 */

public class BearingRealmAdapter extends ArrayAdapter<BearingRealm> {

    private LayoutInflater mInflater;
    private DecimalFormat format = new DecimalFormat("#.##");

    public BearingRealmAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        BearingRealm bearingRealm = getItem(position);

        String note = bearingRealm.description;
        if (TextUtils.isEmpty(note)) {
            note = "Model " + (position + 1);
        }

        ((TextView) convertView).setText(note + " / " + format.format(bearingRealm.bearing));
        ((TextView) convertView).setTextColor(bearingRealm.color);

        return convertView;
    }
}
