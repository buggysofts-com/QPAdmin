package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class DocumentFileNodeAdapter extends ArrayAdapter<DocumentFile> {

    private int node_layout_res_id;

    DocumentFileNodeAdapter(@NonNull Context context, int resource, @NonNull List<DocumentFile> objects) {
        super(context, resource, objects);
        this.node_layout_res_id=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
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

        DocumentFile file=getItem(position);
        holder.getIcon().setImageResource(R.drawable.question_icon);
        holder.getTitle().setText(file.getName());
        holder.getDescription().setText(UriUtils.getDocumentFileAbsPath(file,getContext()));

        return convertView;

    }
}
