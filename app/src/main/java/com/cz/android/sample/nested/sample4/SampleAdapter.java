package com.cz.android.sample.nested.sample4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cz.android.sample.R;

import java.util.ArrayList;
import java.util.List;

public class SampleAdapter extends SimpleNestedListView.Adapter {
    private static final String TAG="SampleAdapter";
    public final List<String> itemList=new ArrayList<>();

    public SampleAdapter(List<String> itemList) {
        if(null!=itemList){
            this.itemList.addAll(itemList);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public View onCreateView(ViewGroup parent, int position) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.simple_text_item,parent,false);
    }

    @Override
    public void onBindView(View view, final int position) {
        String item = itemList.get(position);
        TextView textView=view.findViewById(android.R.id.text1);
        textView.setText(item);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"Click:"+position,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
