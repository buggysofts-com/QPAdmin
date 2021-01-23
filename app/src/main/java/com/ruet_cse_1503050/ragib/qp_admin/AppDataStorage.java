package com.ruet_cse_1503050.ragib.qp_admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

public final class AppDataStorage {

    /*intended for app settings and internal app data*/
    /**/static File AppDataDir;
    /**/static File LocalQuestionStorageDir;
    /**/static File LocalTemporaryFilesStorageDir;
    /**/static File SettingsDataDir;

    /*intended for in-app useage*/
    static File export_helper_file;
    static File recent_captured_img_file;

    static FirebaseAuth auth;
    static FirebaseUser current_user;
    static FirebaseDatabase database;
    static FirebaseStorage storage;
    static DatabaseReference base_ref;
    static StorageReference storage_ref;
    static UserMetaData userMetaData=null;
    static UserStorage userStorage=null;

    /*Activity request codes*/
    static final int NEW_UNIT_QUESTION_ACTIVITY_CODE=101;
    static final int IMPORT_QUESTION_ACTIVITY_CODE=102;
    static final int EXPANDED_QUESTION_ACTIVITY_CODE=103;
    static final int EDIT_UNIT_QUESTION_ACTIVITY_CODE=104;
    static final int IMPORT_IMAGE_CODE=105;
    static final int CAMERA_INTENT_CODE=106;
    static final int SINGLE_EXPORT_INTENT_CODE=107;
    static final int MULTIPLE_EXPORT_INTENT_CODE=108;
    static  final int ACCOUNT_TASK_CODE=110;
    static final int EXIT_CODE=200;
}
