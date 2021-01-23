package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class OnlineExplorerListAdapter extends ArrayAdapter<OnlineFileMetaDataNode> {

    private SparseBooleanArray marked;

    private int node_layout_res_id;

    OnlineExplorerListAdapter(@NonNull Context context, int resource, @NonNull List<OnlineFileMetaDataNode> objects) {
        super(context, resource, objects);
        this.node_layout_res_id=resource;
        marked=new SparseBooleanArray(0);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(node_layout_res_id,null);
            holder=new ViewHolder(
                    (ImageView) convertView.findViewById(R.id.icon),
                    (TextView) convertView.findViewById(R.id.title),
                    (TextView) convertView.findViewById(R.id.description)
            );
            convertView.setTag(holder);
        } else {
            holder=(ViewHolder) convertView.getTag();
        }

        OnlineFileMetaDataNode question_info=getItem(position);
        long size=Integer.parseInt(question_info.size);

        holder.getIcon().setImageResource(marked.get(position) ? R.drawable.selected_icon_gr : R.drawable.question_icon);
        holder.getTitle().setText(question_info.name);
        holder.getDescription().setText(UtilCollections.getSizeStr(size));

        return convertView;
    }

    boolean getSelectionStatusAt(int index){
        return marked.get(index);
    }

    void SetSelectionAt(int index){
        if(index>=0 && index<getCount()){
            this.marked.put(index,true);
        }
        notifyDataSetChanged();
    }

    void RemoveSelecitonAt(int index){
        if(index>=0 && index<getCount()){
            marked.delete(index);
        }
        notifyDataSetChanged();
    }

    void ClearAllSelection(){
        marked.clear();
        notifyDataSetChanged();
    }

    void SelectAll(){
        int lim=getCount();
        for (int i=0;i<lim;++i){
            marked.put(i,true);
        }
        notifyDataSetChanged();
    }

    int getSelectionSize(){
        return marked.size();
    }
}
