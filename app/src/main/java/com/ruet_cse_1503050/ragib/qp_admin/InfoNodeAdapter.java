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

public class InfoNodeAdapter extends ArrayAdapter<InfoNode> {

    private int node_layout_res_id;

    InfoNodeAdapter(@NonNull Context context, int resource, @NonNull List<InfoNode> objects) {
        super(context, resource, objects);
        this.node_layout_res_id=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder=null;
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(node_layout_res_id,null);
            holder=new ViewHolder(
                    (ImageView) convertView.findViewById(R.id.icon),
                    (TextView) convertView.findViewById(R.id.title),
                    (TextView) convertView.findViewById(R.id.description)
            );
            convertView.setTag(holder);
        } else {
            holder=(ViewHolder) convertView.getTag();
        }

        InfoNode node_info=getItem(position);
        holder.getIcon().setImageResource(node_info.icon_res_id);
        holder.getTitle().setText(node_info.node_title);
        holder.getDescription().setText(node_info.node_description);

        return convertView;
    }
}
