package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import java.util.regex.Pattern;

public class ResultListAdapter extends ArrayAdapter<ScoreNode> {

    private int layout_res_id;
    private boolean search_adapter;
    private String search_str;

    final class Holder{
        TextView roll;
        TextView mark;
    }

    ResultListAdapter(@NonNull Context context, int resource, @NonNull List<ScoreNode> objects, boolean search_adapter, String search_str) {
        super(context, resource, objects);
        this.layout_res_id=resource;
        this.search_adapter=search_adapter;
        this.search_str=search_str;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Holder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(layout_res_id,parent,false);
            holder=new Holder();
            holder.roll=convertView.findViewById(R.id.student_roll);
            holder.mark=convertView.findViewById(R.id.student_mark);
            convertView.setTag(holder);
        } else {
            holder= ((Holder) convertView.getTag());
        }

        ScoreNode value=getItem(position);
        if(search_adapter){
            holder.roll.setText(
                    Html.fromHtml(
                            value.key.replaceAll(
                                    Pattern.quote(search_str), "<font color='#FF4545'>"+search_str+"</font>"
                            )
                    )
            );
        }else {
            holder.roll.setText(value.key);
        }
        holder.mark.setText("Score :  "+value.value);

        return convertView;
    }
}
