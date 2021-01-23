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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

public class OfflineExplorerListAdapter extends ArrayAdapter<File> {

    private SparseBooleanArray marked;

    private int node_layout_res_id;

    OfflineExplorerListAdapter(@NonNull Context context, int resource, @NonNull List<File> objects) {

        super(context, resource, objects);
        this.node_layout_res_id=resource;
        marked=new SparseBooleanArray(0);
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

        File question_file=getItem(position);
        int q_count=-1;
        try {
            q_count=(new ZipFile(question_file).size()-1)/10;
        } catch (IOException e) {
            e.printStackTrace();
            q_count=-1;
        }

        holder.getIcon().setImageResource(marked.get(position) ? R.drawable.selected_icon_gr : R.drawable.question_icon);
        holder.getTitle().setText(question_file.getName());
        holder.getDescription().setText( q_count!=-1 ? (q_count+" "+((q_count>1)?"Questions":"Question")):("Corrupted Question File") );

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
