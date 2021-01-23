package com.ruet_cse_1503050.ragib.qp_admin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

final class UriUtils {

    private static final String PRIMARY_VOLUME_NAME = "primary";

    static String getDocumentFileAbsPath(DocumentFile file,Context context){
        if(file.isFile()){
            return getFullPathFromUri(file.getUri(),context,false);
        } else {
            return getFullPathFromUri(file.getUri(),context,true);
        }
    }

    static String getRootPath(DocumentFile file,Context context){
        String abs_path=null;
        if(file.isFile()){
            abs_path = ExtractVolumePath(file.getUri(),context,false);
        } else {
            abs_path= ExtractVolumePath(file.getUri(),context,true);
        }
        return abs_path;
    }
    static String getFullPathFromFileURI(String uri_str){
        File tmp_file= null;
        try {
            tmp_file = new File(new URI(uri_str));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return tmp_file.getAbsolutePath();
    }

    @Nullable
    private static String getFullPathFromUri(@Nullable final Uri treeUri, Context context,boolean tree) {
        if (treeUri == null) return null;

        String volumePath = ExtractVolumePath(treeUri,context,tree);
        String documentPath = ExtractDocumentPath(treeUri,tree);

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator))
                return volumePath + documentPath;
            else
                return volumePath + File.separator + documentPath;
        }
        else return volumePath;
    }

    private static String ExtractVolumePath(@Nullable final Uri treeUri, Context context,boolean tree){
        String volumePath=getVolumePath(getVolumeIdFromUri(treeUri,tree),context);
        if (volumePath == null) return File.separator;
        if (volumePath.endsWith(File.separator))
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        return volumePath;
    }

    @SuppressLint("ObsoleteSdkInt")
    private static String getVolumePath(final String volumeId, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        try {
            StorageManager mStorageManager =
                    (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);

                // other volumes?
                if (uuid != null && uuid.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);
            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromUri(final Uri treeUri,boolean tree) {
        final String docId =
                tree? DocumentsContract.getTreeDocumentId(treeUri):
                        DocumentsContract.getDocumentId(treeUri);
        final String[] split = docId.split(":");
        if (split.length > 0) return split[0];
        else return null;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String ExtractDocumentPath(final Uri treeUri,boolean tree) {
        final String docId = tree? DocumentsContract.getTreeDocumentId(treeUri):
                DocumentsContract.getDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) return split[1];
        else return File.separator;
    }
}