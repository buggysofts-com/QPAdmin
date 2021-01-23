package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class UtilCollections {

    static void WriteToFile(File file_to_write,byte[] data,boolean append){
        BufferedOutputStream buff_out=null;
        try{
            buff_out=new BufferedOutputStream(new FileOutputStream(file_to_write,append));
            buff_out.write(data);
            buff_out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static String ReadFile(File file){
        BufferedInputStream buff_in=null;
        try{
            buff_in=new BufferedInputStream(new FileInputStream(file));
            byte[] data=new byte[(int)file.length()];
            buff_in.read(data);
            buff_in.close();
            return new String(data);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    static void WriteToDocFile(Context context,DocumentFile file_to_write,byte[] data){
        BufferedOutputStream buff_out=null;
        try{
            buff_out=new BufferedOutputStream(context.getContentResolver().openOutputStream(file_to_write.getUri()));
            buff_out.write(data);
            buff_out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void FileFromDocFile(Context context,DocumentFile source, File dest){
        BufferedInputStream buff_in=null;
        BufferedOutputStream buff_out=null;
        try{
            buff_in=new BufferedInputStream(context.getContentResolver().openInputStream(source.getUri()));
            buff_out=new BufferedOutputStream(new FileOutputStream(dest));
            byte[] data=new byte[1024];
            int read_num;
            while ((read_num=buff_in.read(data))!=-1){
                buff_out.write(data,0,read_num);
            }
            buff_out.close();
            buff_in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void DocFileFromFile(Context context,File source,DocumentFile dest){
        BufferedOutputStream buff_out=null;
        BufferedInputStream buff_in=null;
        try {
            buff_in=new BufferedInputStream(new FileInputStream(source));
            buff_out=new BufferedOutputStream(context.getContentResolver().openOutputStream(dest.getUri()));

            byte[] data=new byte[1024];
            int readnum;
            while ((readnum=buff_in.read(data))!=-1){
                buff_out.write(data,0,readnum);
            }

            buff_out.close();
            buff_in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void WipeDirectory(File dir){
        File[] files=dir.listFiles();
        for(int i=0;i<files.length;++i){
            if(files[i].isFile()){
                files[i].delete();
            } else {
                WipeDirectory(files[i]);
            }
        }
    }

    static List<UnitQuestion> DecodeQuestionAsList(File question_pack_file) {
        List<UnitQuestion> questions=null;
        try{

            ZipFile zipFile=new ZipFile(question_pack_file);
            int zip_entry_size=(zipFile.size()-1)/10;
            questions=new ArrayList<>(0);
            for(int i=0;i<zip_entry_size;++i){
                questions.add(new UnitQuestion());
            }

            Enumeration<? extends ZipEntry> entries= zipFile.entries();
            while (entries.hasMoreElements()){
                ZipEntry current_qtn_entry=entries.nextElement();
                if(!current_qtn_entry.isDirectory()){
                    String name=current_qtn_entry.getName();
                    if(!name.equals("qpack_file_validation_key___file_creator_id_1503050")) {
                        String fp=getPathFirstPart(name);
                        String lp=getPathLastPart(name);
                        String comment=current_qtn_entry.getComment();
                        long size=current_qtn_entry.getSize();
                        int index=getQuestionIndex(fp);
                        switch (lp){

                            case "qimg":{

                                File file_to_write=
                                        new File(
                                                AppDataStorage.LocalTemporaryFilesStorageDir.getAbsolutePath()
                                                        +File.separator+comment+"_"+ size +".tmp"
                                        );

                                if(size>0){
                                    if(!file_to_write.exists()){
                                        WriteFromZip(zipFile.getInputStream(current_qtn_entry),file_to_write);
                                    } else {
                                        if(file_to_write.length()!=size){
                                            WriteFromZip(zipFile.getInputStream(current_qtn_entry),file_to_write);
                                        }
                                    }
                                    questions.get(index).setImagePath(file_to_write.getAbsolutePath());
                                } else {
                                    questions.get(index).setImagePath("");
                                }


                                break;
                            }
                            case "qtxt":{
                                questions.get(index).setQuestionText(WriteFromZip(zipFile.getInputStream(current_qtn_entry)));
                                break;
                            }
                            case "ans0":{
                                String[] ans0=WriteFromZip(zipFile.getInputStream(current_qtn_entry)).split("\n");
                                AnsNode[] answers=questions.get(index).getAnswers();
                                answers[0]=new AnsNode(ans0[0],ans0[1].equals("1"));
                                questions.get(index).setAnswers(answers);
                                break;
                            }
                            case "ans1":{
                                String[] ans1=WriteFromZip(zipFile.getInputStream(current_qtn_entry)).split("\n");
                                AnsNode[] answers=questions.get(index).getAnswers();
                                answers[1]=new AnsNode(ans1[0],ans1[1].equals("1"));
                                questions.get(index).setAnswers(answers);

                                break;
                            }
                            case "ans2":{
                                String[] ans2=WriteFromZip(zipFile.getInputStream(current_qtn_entry)).split("\n");
                                AnsNode[] answers=questions.get(index).getAnswers();
                                answers[2]=new AnsNode(ans2[0],ans2[1].equals("1"));
                                questions.get(index).setAnswers(answers);
                                break;
                            }
                            case "ans3":{
                                String[] ans3=WriteFromZip(zipFile.getInputStream(current_qtn_entry)).split("\n");
                                AnsNode[] answers=questions.get(index).getAnswers();
                                answers[3]=new AnsNode(ans3[0],ans3[1].equals("1"));
                                questions.get(index).setAnswers(answers);
                                break;
                            }
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return questions;
    }

    static void SaveQuestion(File output,List<UnitQuestion> questions,boolean delete_prev) throws Exception{

        if(delete_prev){
            output.delete();
        }

        ZipOutputStream zip_out = new ZipOutputStream(new FileOutputStream(output));
        int loop_len=questions.size();
        for(int i=0;i<loop_len;++i){

            String CURRENT_QTN_NAME="QTN"+ (i + 1);
            UnitQuestion temporary_qtn=questions.get(i);

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator)); // main directory entry placed

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QimgDir"+File.separator)); // img dir entry opened

            File img_file=new File(temporary_qtn.getImagePath());
            ZipEntry img_entry=new ZipEntry(CURRENT_QTN_NAME+File.separator+"QimgDir"+File.separator+"qimg");
            img_entry.setComment(Long.toString(Calendar.getInstance().getTimeInMillis()));
            img_entry.setSize(img_file.length());
            zip_out.putNextEntry(img_entry); // img entry placed
            UtilCollections.WriteToZip(zip_out,img_file); // img file was written to te img entry
            zip_out.closeEntry(); // img entry close

            zip_out.closeEntry(); // img dir entry closed

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QDescDir"+File.separator)); // question text directory opened

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QDescDir"+File.separator+"qtxt")); // qtext entry placed
            UtilCollections.WriteToZip(zip_out,temporary_qtn.getQuestionText()); // question description was written to qtxt entry
            zip_out.closeEntry(); // qtxt entry close

            zip_out.closeEntry(); // question desc entry closed


            AnsNode[] answers=temporary_qtn.getAnswers();
            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QAnsDir"+File.separator)); // answers dir entry placed

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QAnsDir"+File.separator+"ans0")); // ans0 placed inside ans dir
            UtilCollections.WriteToZip(zip_out,answers[0].ans+'\n'+(answers[0].state?"1":"0")); // ans0 was written inside answer dir
            zip_out.closeEntry(); // ans0 entry closed

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QAnsDir"+File.separator+"ans1")); // ans1 placed inside ans dir
            UtilCollections.WriteToZip(zip_out,answers[1].ans+'\n'+(answers[1].state?"1":"0")); // ans1 was written inside answer dir
            zip_out.closeEntry(); // ans1 entry closed

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QAnsDir"+File.separator+"ans2")); // ans2 placed inside ans dir
            UtilCollections.WriteToZip(zip_out,answers[2].ans+'\n'+(answers[2].state?"1":"0")); // ans2 was written inside answer dir
            zip_out.closeEntry(); // ans2 entry closed

            zip_out.putNextEntry(new ZipEntry(CURRENT_QTN_NAME+File.separator+"QAnsDir"+File.separator+"ans3")); // ans3 placed inside ans dir
            UtilCollections.WriteToZip(zip_out,answers[3].ans+'\n'+(answers[3].state?"1":"0")); // ans3 was written inside answer dir
            zip_out.closeEntry(); // ans3 entry closed

            zip_out.closeEntry(); // answers dir entry closed

            zip_out.closeEntry(); // close main directory entry
        }

        zip_out.putNextEntry(new ZipEntry("qpack_file_validation_key___file_creator_id_1503050")); // put qpack file validation key file
        zip_out.closeEntry(); // close qpack file validation key file entry

        zip_out.close();  // close the entire stream
    }

    static boolean WriteToZip(ZipOutputStream zip_out, File file_to_write){
        int readnum;
        BufferedInputStream buff_in=null;
        FileInputStream file_in=null;
        try {
            file_in=new FileInputStream(file_to_write);
            buff_in=new BufferedInputStream(file_in);
            byte[] data=new byte[4096];
            while ((readnum=buff_in.read(data))!=-1){
                zip_out.write(data,0,readnum);
            }
            buff_in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static boolean WriteToZip(ZipOutputStream zip_out, String string_to_write){
        try {
            byte[] data=string_to_write.getBytes();
            zip_out.write(data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static boolean WriteFromZip(InputStream zip_in, File file_to_write){
        int readnum;
        BufferedOutputStream buff_out=null;
        FileOutputStream file_out=null;
        try {
            file_out=new FileOutputStream(file_to_write);
            buff_out=new BufferedOutputStream(file_out);
            byte[] data=new byte[4096];
            while ((readnum=zip_in.read(data))!=-1){
                buff_out.write(data,0,readnum);
            }
            buff_out.close();
            zip_in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    static String WriteFromZip(InputStream zip_in){
        byte[] string_data;
        try {
            List<Byte> byte_list=new ArrayList<>(0);
            byte data;
            while ((data=(byte)zip_in.read())!=-1){
                byte_list.add(data);
            }
            string_data=new byte[byte_list.size()];
            for(int i=0;i<string_data.length;++i){
                string_data[i]=byte_list.get(i);
            }
            zip_in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return new String(string_data);
    }

    static String getPathFirstPart(String path){
        int separator_index=path.indexOf(File.separator);
        if(separator_index==-1) return path;
        return path.substring(0,separator_index);
    }

    static String getPathLastPart(String path){
        int separator_index=path.lastIndexOf(File.separator);
        if(separator_index==-1) return path;
        return path.substring(separator_index+1);
    }

    static int getQuestionIndex(String first_part){
        return (Integer.parseInt(first_part.substring(3)) - 1);
    }

    static String getQualifiedUserName(String original){
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<original.length();++i){
            char current_char=original.charAt(i);
            if(current_char=='.' ||
                    current_char=='#' ||
                    current_char=='$' ||
                    current_char=='[' ||
                    current_char=='@' ||
                    current_char==']' ){
                builder.append('_');
            } else builder.append(current_char);
        }
        return builder.toString();
    }

    static String getSizeStr(long size){

        int mode=0; // 0:Byte,1:KB,2:MB,3:GB
        while(size>(1L<<(10*(mode+1)))) {
            ++mode;
        }

        double modified_size=((double) size)/((double)(1L<<(10*mode)));
        String s=Double.toString(modified_size);

        int dp_index=s.indexOf('.');
        if(dp_index!=-1 && (s.length()-dp_index) > 2) {
            s=s.substring(0,dp_index+3);
        }

        switch (mode){
            case 0:{
                s+=(size>1?" Byte":" Bytes");
                break;
            }
            case 1:{
                s+=" KB";
                break;
            }
            case 2:{
                s+=" MB";
                break;
            }
            case 3:{
                s+=" GB";
                break;
            }
            default:{
                s="["+"Too large"+"]";
            }
        }

        return (s);
    }

    static boolean isConnectedToInternet(Context context){
        if(isNetworkAvailable(context)){
            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection)(new URL("http://clients3.google.com/generate_204")).openConnection();
                urlConnection.setRequestProperty("User-Agent","Test");
                urlConnection.setRequestProperty("Connection","close");
                urlConnection.setConnectTimeout(1500);
                urlConnection.connect();
                return (urlConnection.getResponseCode()==204 && urlConnection.getContentLength()==0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null;
    }

}
