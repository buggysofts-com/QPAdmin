package com.ruet_cse_1503050.ragib.qp_admin;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserMetaData {

    private DatabaseReference ROOT=FirebaseDatabase.getInstance().getReference("admins");

    private DatabaseReference UserRoot;
    private DatabaseReference UserInfoData;
    private DatabaseReference UserStorageMetaData;
    private DatabaseReference ScheduledExamsMetaData;

    UserMetaData(String email){
        this.UserRoot=ROOT.child(UtilCollections.getQualifiedUserName(email));
        this.UserInfoData=UserRoot.child("USER_INFO_DATA");
        this.UserStorageMetaData=UserRoot.child("USER_STORAGE_METADATA");
        this.ScheduledExamsMetaData=UserRoot.child("SCHEDULED_EXAMS_METADATA");
    }

    DatabaseReference getUserInfoData() {
        return UserInfoData;
    }

    DatabaseReference getUserStorageMetaData() {
        return UserStorageMetaData;
    }

    DatabaseReference getScheduledExamsMetaData() {
        return ScheduledExamsMetaData;
    }
}
