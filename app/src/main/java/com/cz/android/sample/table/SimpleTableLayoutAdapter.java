package com.cz.android.sample.table;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.cz.android.sample.R;
import com.cz.android.table.TableZoomLayout;

import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SimpleTableLayoutAdapter extends TableZoomLayout.Adapter {
    private static final int HEADER_TYPE=0;
    private static final int CELL_ITEM=1;
    private final SparseArray<String> imageArray=new SparseArray<>();
    private final LayoutInflater layoutInflater;
    private final List<List<String>> items;

    public SimpleTableLayoutAdapter(Context context, List<List<String>> items) {
        this.layoutInflater = LayoutInflater.from(context);
        this.items=items;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return items.get(0).size();
    }

    @Override
    public int getTableCellWidth(View tableColumnView, int row, int column) {
        return 300;
    }

    @Override
    public int getTableCellHeight(View tableColumnView, int row, int column) {
        return 100;
    }

    @Override
    public int getRowSpan(int row, int column) {
        if(1==row&&0==column){
            return 2;
        } else {
            return super.getRowSpan(row, column);
        }
//        return super.getRowSpan(row, column);
    }

    @Override
    public int getColumnSpan(int row, int column) {
//        if(1==row&&0==column){
//            return 2;
//        } else {
//            return super.getColumnSpan(row, column);
//        }
        return super.getColumnSpan(row, column);
    }

    @Override
    public int getViewType(int row, int column) {
        return 0==row ? HEADER_TYPE : CELL_ITEM;
    }

    public String getItem(int row, int column){
        return items.get(row).get(column);
    }

    @Override
    public View getView(Context context,ViewGroup parent, int viewType) {
        if(viewType==HEADER_TYPE){
            return layoutInflater.inflate(R.layout.zoom_simple_header_text_item,parent,false);
        } else {
            return layoutInflater.inflate(R.layout.zoom_simple_table_item,parent,false);
        }
    }

    @Override
    public void onBindView(View view, final int row, final int column) {
        int viewType = getViewType(row, column);
        final String item = getItem(row, column);
        if(viewType==HEADER_TYPE){
             final TextView textView=view.findViewById(R.id.textView);
             textView.setText(item);
        } else {
            int columnCount = getColumnCount();
            int index = row * columnCount + column;
            final ImageView imageView=view.findViewById(R.id.imageView);
            final TextView textView=view.findViewById(R.id.textView);
            String image = imageArray.get(index);
            if(null==image){
                image=Data.getImage();
                imageArray.put(index,image);
            }
            Glide.with(view.getContext()).load(image).transition(withCrossFade()).into(imageView);
//            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(value);
//            final Random random=new Random();
//            for(int i=0;i<1;i++){
//                float red = random.nextInt(255);
//                float green = random.nextInt(255) / 2f;
//                float blue = random.nextInt(255) / 2f;
//                int color= 0xff000000 | ((int) (red   * 255.0f + 0.5f) << 16) |
//                        ((int) (green * 255.0f + 0.5f) <<  8) | (int) (blue  * 255.0f + 0.5f);
//                ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
//                stringBuilder.setSpan(colorSpan, i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
            textView.setText("Row:"+row+" Column:"+column);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.invalidate();
                    Toast.makeText(v.getContext(), "Row:"+row+" Column:"+column, Toast.LENGTH_SHORT).show();
                }
            });
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(v.getContext(), "Long press "+item, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
