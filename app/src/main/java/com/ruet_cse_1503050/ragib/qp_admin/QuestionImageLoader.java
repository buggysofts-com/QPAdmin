package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

public class QuestionImageLoader extends AsyncTask<Void,Void,Void> {

    private WeakReference<ImageView> imgViewRef;
    private WeakReference<Context> context_ref;
    private UnitQuestion question;
    private Bitmap img_bitmap;

    QuestionImageLoader(Context context,ImageView imageView, UnitQuestion question){
        imgViewRef=new WeakReference<>(imageView);
        context_ref=new WeakReference<>(context);
        this.question=question;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(!question.getImagePath().equals("") || question.getImagePath()==null){
            /*Bitmap source_bitmap=BitmapFactory.decodeFile(question.getImagePath());
            int source_h=source_bitmap.getHeight();
            int source_w=source_bitmap.getWidth();
            int thumb_w=1024;
            int thumb_h=((thumb_w*source_h)/source_w)+((thumb_w*source_h)%source_w);
            img_bitmap=ThumbnailUtils.extractThumbnail(source_bitmap,thumb_w,thumb_h);*/
            img_bitmap=BitmapFactory.decodeFile(question.getImagePath());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        imgViewRef.get().setImageDrawable(context_ref.get().getDrawable(R.drawable.img_place_holder));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(img_bitmap!=null){
            imgViewRef.get().setImageBitmap(img_bitmap);
        }
    }
}
