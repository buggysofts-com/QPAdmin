package com.ruet_cse_1503050.ragib.qp_admin;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

class UserStorage {

    private StorageReference ROOT=FirebaseStorage.getInstance().getReference("admins");

    private StorageReference UserRoot;
    private StorageReference UserInfoDir;
    private StorageReference UserDataDir;

    UserStorage(String email){
        this.UserRoot=ROOT.child(email);
        this.UserInfoDir=UserRoot.child("INFO");
        this.UserDataDir=UserRoot.child("DATA");
    }

    StorageReference getUserRoot() {
        return UserRoot;
    }

    StorageReference getUserInfoDir() {
        return UserInfoDir;
    }

    StorageReference getUserDataDir() {
        return UserDataDir;
    }
}
