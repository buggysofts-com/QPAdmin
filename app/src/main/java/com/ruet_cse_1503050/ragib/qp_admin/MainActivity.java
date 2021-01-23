package com.ruet_cse_1503050.ragib.qp_admin;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView menu_list;
    private InfoNodeAdapter mainMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        RequestPermissionAndProceed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(AppDataStorage.LocalTemporaryFilesStorageDir!=null &&
                AppDataStorage.LocalTemporaryFilesStorageDir.exists()){
            Toast.makeText(this, "Clearing saved caches...", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UtilCollections.WipeDirectory(AppDataStorage.LocalTemporaryFilesStorageDir);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Caches cleared!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case AppDataStorage.ACCOUNT_TASK_CODE:{
                    if(data.getIntExtra("return_mode",0)==0){
                        finish();
                    } else {
                        InitializeFirebaseCoreComponents();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(MainActivity.this,AccountInfoActivity.class);
                startActivityForResult(intent,AppDataStorage.ACCOUNT_TASK_CODE);
                return true;
            }
        });
        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("About QP_Admin");
                builder.setMessage("Developer: Nowrose Muhammad Ragib\nEmail: dev.ragib@gmail.com\nPhone: +8801723085831\n\nFor any help or suggestion, please email at dev.ragib@gmail.com");
                builder.setPositiveButton("Close",null);
                builder.show();
                return true;
            }
        });
        return true;
    }

    private void RequestPermissionAndProceed() {
        // Request permission if needed
        String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(RequiresPermission(permissions)){
            ActivityCompat.requestPermissions(MainActivity.this,permissions,0);
        }
        else {
            Initialize();
        }
    }
    private boolean RequiresPermission(String[] permissions) {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) return false;
        else {
            for(String permission:permissions){
                if(ActivityCompat.checkSelfPermission(MainActivity.this,permission)!=PackageManager.PERMISSION_GRANTED){
                    return true;
                }
            }
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Initialize();
        }
        else {
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("You need to allow the permission request to be able to use this application");
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton("Request permission", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RequestPermissionAndProceed();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    private void Initialize(){
        InitializeUI();
        InitializeData();
    }

    private void InitializeUI(){
        InitializeUIComponents();
        InitializeUIData();
    }
    private void InitializeUIComponents() {
        menu_list= findViewById(R.id.menu_list);
        menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:{
                        startActivity(new Intent(MainActivity.this,NewQuestionActivity.class));
                        break;
                    }
                    case 1:{
                        startActivity(new Intent(MainActivity.this,ExploreActivity.class));
                        break;
                    }
                    case 2:{
                        startActivity(new Intent(MainActivity.this,ExamExplorerActivity.class));
                        break;
                    }
                    case 3:{
                        ConnectivityManager connectivityManager=(ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                        NetworkInfo net_info=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if(net_info.isConnected()){
                            startActivity(new Intent(MainActivity.this,LocalDownloadActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, "WiFi Connection required", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            }
        });
    }
    private void InitializeUIData() {
        List<InfoNode> menu_info_nodes=new ArrayList<>(0);
        menu_info_nodes.add(new InfoNode(R.drawable.add_icon_gr,"Create new Question","Create a new question which will be saved as qpack file that you can customize anytime"));
        menu_info_nodes.add(new InfoNode(R.drawable.question_list,"Explore available questions","Explore offline and online available questions,edit and modify them, schedule exams, and lot more"));
        menu_info_nodes.add(new InfoNode(R.drawable.question_icon,"Explore Exams","Explore posted exams, results, edit them and many more"));
        menu_info_nodes.add(new InfoNode(R.drawable.download_qtn_icon,"Download Questions","Download questions from partner desktop application-QP Creator(both devices should be in the same local network)"));
        mainMenuAdapter=new InfoNodeAdapter(MainActivity.this,R.layout.info_node_layout,menu_info_nodes);
        menu_list.setAdapter(mainMenuAdapter);
    }


    private void InitializeData() {
        InitializeDataDirectories();
        InitializeFirebaseCoreComponents();
    }
    private void InitializeDataDirectories() {

        // AppData initialization block
        if(!AppDataStorage.AppDataDir.exists()){
            AppDataStorage.AppDataDir.mkdir();
        }
        if(!AppDataStorage.LocalQuestionStorageDir.exists()){
            AppDataStorage.LocalQuestionStorageDir.mkdir();
        }
        if(!AppDataStorage.LocalTemporaryFilesStorageDir.exists()){
            AppDataStorage.LocalTemporaryFilesStorageDir.mkdir();
        }

        // Settings initialization block
        if(!AppDataStorage.SettingsDataDir.exists()){
            AppDataStorage.SettingsDataDir.mkdir();
        }
    }
    private void InitializeFirebaseCoreComponents() {
        AppDataStorage.auth = FirebaseAuth.getInstance();
        final FirebaseAuth.AuthStateListener[] listener=new FirebaseAuth.AuthStateListener[1];
        listener[0]=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                AppDataStorage.current_user=firebaseAuth.getCurrentUser();
                if(AppDataStorage.current_user==null || !AppDataStorage.current_user.isEmailVerified()){
                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                    intent.putExtra("back_mode",0);
                    startActivityForResult(intent,AppDataStorage.ACCOUNT_TASK_CODE);
                } else {
                    AppDataStorage.database=FirebaseDatabase.getInstance();
                    AppDataStorage.storage=FirebaseStorage.getInstance();
                    AppDataStorage.base_ref=AppDataStorage.database.getReference();
                    AppDataStorage.storage_ref=AppDataStorage.storage.getReference();
                    AppDataStorage.userMetaData=new UserMetaData(AppDataStorage.current_user.getEmail());
                    AppDataStorage.userStorage=new UserStorage(AppDataStorage.current_user.getEmail());
                }
                AppDataStorage.auth.removeAuthStateListener(listener[0]);
            }
        };
        AppDataStorage.auth.addAuthStateListener(listener[0]);
    }
}
