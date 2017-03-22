package com.earthgee.plugin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by zhaoruixuan on 2017/3/22.
 */
public class PluginFragment extends Fragment{

    private TextView tv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main,container,false);
        tv= (TextView) view.findViewById(R.id.text);
        tv.setText(getActivity().getIntent().getStringExtra("test"));
        return view;
    }

}
