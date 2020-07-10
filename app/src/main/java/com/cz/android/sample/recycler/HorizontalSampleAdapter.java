package com.cz.android.sample.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cz.android.sample.R;

import java.util.ArrayList;
import java.util.List;

public class HorizontalSampleAdapter extends RecyclerView.Adapter {
    private static final String TAG="SampleAdapter";
    public final List<String> itemList=new ArrayList<>();

    public HorizontalSampleAdapter(List<String> itemList) {
        if(null!=itemList){
            this.itemList.addAll(itemList);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.horizontal_simple_text_item, parent, false);
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        String item = itemList.get(position);
        TextView textView=holder.itemView.findViewById(android.R.id.text1);
        textView.setText(item);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"Click:"+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


}
