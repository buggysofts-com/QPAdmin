package com.ruet_cse_1503050.ragib.qp_admin;

import android.widget.ImageView;
import android.widget.TextView;

final class ViewHolder {

    private ImageView icon;
    private TextView title;
    private TextView description;

    ViewHolder(ImageView icon,TextView title,TextView description){
        this.icon=icon;
        this.title=title;
        this.description=description;
    }

    ImageView getIcon(){
        return this.icon;
    }

    TextView getTitle(){
        return this.title;
    }

    TextView getDescription(){
        return this.description;
    }
}
