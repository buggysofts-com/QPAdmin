package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ExamExplorerAdapter extends ArrayAdapter<ExamInfoNode> {

    private int layout_res_id;

    final class Holder{
        TextView exam_title;
        ImageView exam_accessible;
        TextView exam_id;
        TextView exam_pasaword;
        TextView exam_duration;
    }

    ExamExplorerAdapter(@NonNull Context context, int resource, @NonNull List<ExamInfoNode> objects) {
        super(context, resource, objects);
        this.layout_res_id=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Holder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(layout_res_id,null);
            holder=new Holder();
            holder.exam_title=convertView.findViewById(R.id.exam_title);
            holder.exam_id=convertView.findViewById(R.id.exam_id);
            holder.exam_pasaword=convertView.findViewById(R.id.exam_password);
            holder.exam_duration=convertView.findViewById(R.id.exam_duration);
            holder.exam_accessible=convertView.findViewById(R.id.exam_accessible);
            convertView.setTag(holder);
        } else {
            holder=(Holder) convertView.getTag();
        }

        ExamInfoNode node=getItem(position);
        holder.exam_title.setText(node.title);
        holder.exam_id.setText("ID: "+node.exam_id);
        holder.exam_pasaword.setText("Password: "+node.password);
        holder.exam_duration.setText("Duration: "+node.duration+" "+"minutes");
        holder.exam_accessible.setImageDrawable(
                getContext().getResources().getDrawable(
                        node.accessible.equals("true")?R.drawable.visible_icon:R.drawable.invisible_icon
                )
        );

        return convertView;

    }
}
