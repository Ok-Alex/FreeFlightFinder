package me.akulakovsky.ffsearch.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import me.akulakovsky.ffsearch.app.R;

public class InfoFragment extends Fragment {

    private TextView tvDeviation;
    private View layDeviation2;
    private TextView tvDeviation2;
    private TextView tvToStart;
    private TextView tvTotal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navigation_info, container, false);

        tvDeviation = (TextView) v.findViewById(R.id.deviation);
        layDeviation2 = v.findViewById(R.id.deviation2_layout);
        tvDeviation2 = (TextView) v.findViewById(R.id.deviation2);
        tvToStart = (TextView) v.findViewById(R.id.distance_to_start);
        tvTotal = (TextView) v.findViewById(R.id.total);

        return v;
    }

    public void setDistances(double deviation, double deviation2, double toStart, double total) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        tvDeviation.setText(decimalFormat.format(deviation) + getActivity().getString(R.string.m));
        if (deviation2 != 0) {
            layDeviation2.setVisibility(View.VISIBLE);
            tvDeviation2.setText(decimalFormat.format(deviation2) + getActivity().getString(R.string.m));
        } else {
            layDeviation2.setVisibility(View.GONE);
        }
        tvToStart.setText(decimalFormat.format(toStart) + getActivity().getString(R.string.m));
        tvTotal.setText(decimalFormat.format(total) + getActivity().getString(R.string.m));
    }
}
