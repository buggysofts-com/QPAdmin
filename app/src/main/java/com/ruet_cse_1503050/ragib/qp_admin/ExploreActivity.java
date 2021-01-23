package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private TabLayout explore_location;
    private ListView explorer_question_list;
    private TextView load_indicator;

    private OfflineExplorerListAdapter offline_adapter;
    private OnlineExplorerListAdapter online_adapter;

    private Menu main_menu;
    private ActionMode offline_multichoice_actionmode=null;
    private ActionMode online_multichoice_actionmode=null;

    private class MultiSelectActionMode implements ActionMode.Callback {

        private String title;
        private String sub_title;

        MultiSelectActionMode(String title,String sub_title){
            this.title=title;
            this.sub_title=sub_title;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(this.title);
            actionMode.setSubtitle(this.sub_title);
            explore_location.setVisibility(View.GONE);
            switch (explore_location.getSelectedTabPosition()){
                case 0:{
                    getMenuInflater().inflate(R.menu.explorer_offline_actionmode_menu,menu);
                    break;
                }
                case 1:{
                    getMenuInflater().inflate(R.menu.explorer_online_actionmode_menu,menu);
                    break;
                }
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            switch (menuItem.getItemId()){
                case R.id.eoffam0:{

                    if(offline_adapter.getSelectionSize()>0){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                                    ArrayList<File> files_to_upload=new ArrayList<>(0);
                                    for(int i=0;i<offline_adapter.getCount();++i){
                                        if(offline_adapter.getSelectionStatusAt(i)){
                                            files_to_upload.add(offline_adapter.getItem(i));
                                        }
                                    }
                                    final File[] raw_file_array=new File[files_to_upload.size()];
                                    for(int i=0;i<files_to_upload.size();++i){
                                        raw_file_array[i]=files_to_upload.get(i);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            offline_multichoice_actionmode.finish();
                                            UploadFiles(raw_file_array);
                                        }
                                    });

                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(ExploreActivity.this, "Please select at least one question", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.eoffam1:{
                    if(offline_adapter.getSelectionSize()>0){
                        ArrayList<Uri> shareable_uris=new ArrayList<>(0);
                        for(int i=0;i<offline_adapter.getCount();++i){
                            if(offline_adapter.getSelectionStatusAt(i)){
                                shareable_uris.add(
                                        FileProvider.getUriForFile(
                                                ExploreActivity.this,
                                                "com.ruet_cse_1503050.ragib.qp_admin.fileprovider",
                                                offline_adapter.getItem(i)
                                        )
                                );
                            }
                        }
                        Intent intent=new Intent(Intent.ACTION_SEND_MULTIPLE);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,shareable_uris);
                        intent.setType("*/*");
                        startActivity(Intent.createChooser(intent,"Share using..."));
                        offline_multichoice_actionmode.finish();
                    } else {
                        Toast.makeText(ExploreActivity.this, "Please select at least one question", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.eoffam2:{

                    if(offline_adapter.getSelectionSize()>0){
                        int len=offline_adapter.getCount();
                        for(int i=0;i<len;++i){
                            if(offline_adapter.getSelectionStatusAt(i)){
                                offline_adapter.getItem(i).delete();
                            }
                        }
                        ReloadOfflineList();
                        offline_multichoice_actionmode.finish();
                    } else {
                        Toast.makeText(ExploreActivity.this, "Please select at least one question", Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
                case R.id.eoffam3:{
                    if(offline_adapter.getSelectionSize()>0){
                        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),AppDataStorage.MULTIPLE_EXPORT_INTENT_CODE);
                    } else {
                        Toast.makeText(ExploreActivity.this, "Please select at least one question", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.eoffam4:{
                    offline_adapter.SelectAll();
                    offline_multichoice_actionmode.setTitle(offline_adapter.getCount()+(offline_adapter.getCount()>1?" items selected":" item selected"));
                    break;
                }
                case R.id.eoffam5:{
                    offline_adapter.ClearAllSelection();
                    offline_multichoice_actionmode.setTitle("Select questions");
                    break;
                }

                case R.id.eonam0:{

                    if(online_adapter.getSelectionSize()>0){

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                                    ArrayList<OnlineFileMetaDataNode> files_to_download=new ArrayList<>(0);
                                    for(int i=0;i<online_adapter.getCount();++i){
                                        if(online_adapter.getSelectionStatusAt(i)){
                                            files_to_download.add(online_adapter.getItem(i));
                                        }
                                    }
                                    final OnlineFileMetaDataNode[] metadatas=new OnlineFileMetaDataNode[files_to_download.size()];
                                    for(int i=0;i<files_to_download.size();++i){
                                        metadatas[i]=files_to_download.get(i);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            online_multichoice_actionmode.finish();
                                            DownloadFiles(metadatas,AppDataStorage.current_user);
                                        }
                                    });

                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(ExploreActivity.this, "Please select at least one question", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.eonam1:{
                    if(online_adapter.getSelectionSize()>0){

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                                    ArrayList<OnlineFileMetaDataNode> files_to_delete=new ArrayList<>(0);
                                    for(int i=0;i<online_adapter.getCount();++i){
                                        if(online_adapter.getSelectionStatusAt(i)){
                                            files_to_delete.add(online_adapter.getItem(i));
                                        }
                                    }
                                    final OnlineFileMetaDataNode[] metaDataNodes=new OnlineFileMetaDataNode[files_to_delete.size()];
                                    for(int i=0;i<files_to_delete.size();++i){
                                        metaDataNodes[i]=files_to_delete.get(i);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            online_multichoice_actionmode.finish();
                                            DeleteOnlineFiles(metaDataNodes,AppDataStorage.current_user);
                                        }
                                    });

                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(ExploreActivity.this, "Please select at least one question", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.eonam2:{
                    online_adapter.SelectAll();
                    online_multichoice_actionmode.setTitle(online_adapter.getCount()+(online_adapter.getCount()>1?" items selected":" item selected"));
                    break;
                }
                case R.id.eonam3:{
                    online_adapter.ClearAllSelection();
                    online_multichoice_actionmode.setTitle("Select questions");
                    break;
                }

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            if(offline_adapter!=null) offline_adapter.ClearAllSelection();
            if(online_adapter!=null)online_adapter.ClearAllSelection();
            offline_multichoice_actionmode=null;
            online_multichoice_actionmode=null;
            explore_location.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_explore);
        Initialize();
        InitializeUIComponents();
        InitializeUIData();
    }

    private void Initialize() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.main_menu=menu;
        getMenuInflater().inflate(R.menu.explore_toolbar_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivityForResult(new Intent(ExploreActivity.this,QuestionPackImportActivity.class),AppDataStorage.IMPORT_QUESTION_ACTIVITY_CODE);
                return true;
            }
        });
        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title="";
                String sub_title="";
                switch (explore_location.getSelectedTabPosition()){
                    case 0:{
                        title="Select questions...";
                        break;
                    }
                    case 1:{
                        title="Select questions...";
                        break;
                    }
                }
                switch (explore_location.getSelectedTabPosition()){
                    case 0:{
                        offline_multichoice_actionmode=startSupportActionMode(new MultiSelectActionMode(title,sub_title));
                        break;
                    }
                    case 1:{
                        online_multichoice_actionmode=startSupportActionMode(new MultiSelectActionMode(title,sub_title));
                        break;
                    }
                }
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case AppDataStorage.IMPORT_QUESTION_ACTIVITY_CODE:{
                    ReloadOfflineList();
                    break;
                }
                case AppDataStorage.EXPANDED_QUESTION_ACTIVITY_CODE:{
                    offline_adapter.notifyDataSetChanged();
                    break;
                }
                case AppDataStorage.SINGLE_EXPORT_INTENT_CODE:{
                    DocumentFile file_to_write=
                            DocumentFile.fromTreeUri(
                                    ExploreActivity.this,data.getData()
                            ).createFile("application/octet-stream",AppDataStorage.export_helper_file.getName());
                    UtilCollections.DocFileFromFile(
                            ExploreActivity.this,
                            AppDataStorage.export_helper_file,
                            file_to_write
                    );
                    AppDataStorage.export_helper_file=null; // reset to avoid collision

                    break;
                }
                case AppDataStorage.MULTIPLE_EXPORT_INTENT_CODE:{

                    final DocumentFile output_dir=DocumentFile.fromTreeUri(ExploreActivity.this,data.getData());

                    new Thread(new Runnable() {

                        boolean cancelled;
                        AlertDialog alertDialog;
                        AlertDialog.Builder builder;
                        View view;
                        ProgressBar operation_progress;
                        TextView progress_desc;
                        TextView progress_count;
                        TextView cancel_dlg_btn;

                        final int len=offline_adapter.getCount();
                        final int max=offline_adapter.getSelectionSize();
                        final int[] done_count={0};

                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    cancelled=false;
                                    builder=new AlertDialog.Builder(ExploreActivity.this);
                                    view=getLayoutInflater().inflate(R.layout.progress_layout,null);
                                    operation_progress=view.findViewById(R.id.operation_progress);
                                    progress_desc=view.findViewById(R.id.progress_desc);
                                    progress_count=view.findViewById(R.id.progress_count);
                                    cancel_dlg_btn=view.findViewById(R.id.cancel_dlg_btn);
                                    operation_progress.setMax(max);
                                    builder.setCancelable(false);
                                    builder.setView(view);

                                    cancel_dlg_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            cancelled=true;
                                            operation_progress.setIndeterminate(true);
                                            progress_desc.setText(getString(R.string.cancel_pending_txt));
                                            progress_count.setText(null);
                                        }
                                    });

                                    alertDialog=builder.show();
                                }
                            });

                            for(int i=0;i<len;++i){

                                if(offline_adapter.getSelectionStatusAt(i)){

                                    if(!cancelled){

                                        final File input_file=offline_adapter.getItem(i);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progress_desc.setText("Exporting: "+input_file.getName());
                                                progress_count.setText(++done_count[0] +"/"+ max);
                                            }
                                        });

                                        UtilCollections.DocFileFromFile(
                                                ExploreActivity.this,
                                                input_file,
                                                output_dir.createFile("application/octet-stream",input_file.getName())
                                        );
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                operation_progress.setProgress(done_count[0]);
                                            }
                                        });
                                    } else {
                                        break;
                                    }
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog.dismiss();
                                    if(!cancelled){
                                        if(offline_multichoice_actionmode!=null){
                                            offline_multichoice_actionmode.finish();
                                        }
                                    }
                                }
                            });

                        }
                    }).start();
                    break;
                }
            }
        }

    }

    private void InitializeUIComponents() {

        load_indicator=findViewById(R.id.load_indicator);
        explore_location=findViewById(R.id.explore_location);
        explorer_question_list=findViewById(R.id.explorer_question_list);

        registerForContextMenu(explorer_question_list);

        explore_location.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                switch (explore_location.getSelectedTabPosition()){
                    case 0:{
                        main_menu.getItem(0).setVisible(true);
                        ReloadOfflineList();
                        break;
                    }
                    case 1:{

                        main_menu.getItem(0).setVisible(false);
                        explorer_question_list.setAdapter(null);
                        load_indicator.setText("Loading...");

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                                    AppDataStorage.userMetaData.getUserStorageMetaData().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            List<OnlineFileMetaDataNode> question_info=new ArrayList<>(0);
                                            Iterator<DataSnapshot> datas=dataSnapshot.getChildren().iterator();
                                            while (datas.hasNext()){
                                                DataSnapshot tmp=datas.next();
                                                String key=tmp.getKey();
                                                HashMap<String,Object> val=(HashMap<String, Object>) tmp.getValue();
                                                question_info.add(
                                                        new OnlineFileMetaDataNode(
                                                                key,
                                                                (String) val.get("name"),
                                                                (String) val.get("size"),
                                                                (String) val.get("ref_path")
                                                        )
                                                );
                                            }

                                            online_adapter=new OnlineExplorerListAdapter(
                                                    ExploreActivity.this,
                                                    R.layout.info_node_layout,
                                                    question_info
                                            );
                                            if(explore_location.getSelectedTabPosition()==1){
                                                explorer_question_list.setAdapter(online_adapter);
                                                load_indicator.setText(online_adapter.getCount()!=0 ? "":"Empty");
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });

                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            explorer_question_list.setAdapter(
                                                    new OnlineExplorerListAdapter(
                                                            ExploreActivity.this,
                                                            R.layout.info_node_layout,
                                                            new ArrayList<OnlineFileMetaDataNode>(0)
                                                    )
                                            );
                                            load_indicator.setText("Not connected to internet");
                                            Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        explorer_question_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (explore_location.getSelectedTabPosition()){
                    case 0:{
                        if(offline_multichoice_actionmode==null){
                            Intent intent=new Intent(ExploreActivity.this,ExpandQuestionActivity.class);
                            intent.putExtra("target_qpack_file_path",offline_adapter.getItem(position).getAbsolutePath());
                            startActivityForResult(intent,AppDataStorage.EXPANDED_QUESTION_ACTIVITY_CODE);
                        } else {
                            if(!offline_adapter.getSelectionStatusAt(position)){
                                offline_adapter.SetSelectionAt(position);
                            } else {
                                offline_adapter.RemoveSelecitonAt(position);
                            }
                            int marked_count=offline_adapter.getSelectionSize();
                            offline_multichoice_actionmode.setTitle(
                                    marked_count>0 ? (marked_count +(marked_count>1?" items selected":" item selected")):("Select questions...")
                            );
                        }
                        break;
                    }
                    case 1:{
                        if(online_multichoice_actionmode!=null){
                            if(!online_adapter.getSelectionStatusAt(position)){
                                online_adapter.SetSelectionAt(position);
                            } else {
                                online_adapter.RemoveSelecitonAt(position);
                            }
                            int marked_count=online_adapter.getSelectionSize();
                            online_multichoice_actionmode.setTitle(
                                    marked_count>0 ? (marked_count +(marked_count>1?" items selected":" item selected")):("Select questions...")
                            );
                        }
                        break;
                    }
                }
            }
        });

        explorer_question_list.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo) {

                final AdapterView.AdapterContextMenuInfo info=((AdapterView.AdapterContextMenuInfo) menuInfo);
                switch (explore_location.getSelectedTabPosition()){
                    case 0:{
                        if(offline_multichoice_actionmode==null){
                            getMenuInflater().inflate(R.menu.explorer_offline_context_menu,menu);
                            menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    File file_to_share=offline_adapter.getItem(info.position);
                                    Uri shareable_uri=FileProvider.getUriForFile(
                                            ExploreActivity.this,
                                            "com.ruet_cse_1503050.ragib.qp_admin.fileprovider",
                                            file_to_share
                                    );
                                    Intent intent=new Intent(Intent.ACTION_SEND);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    intent.putExtra(Intent.EXTRA_STREAM,shareable_uri);
                                    intent.setType("*/*");
                                    startActivity(Intent.createChooser(intent,"Share using..."));
                                    return true;
                                }
                            });
                            menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    File file_to_dlt=offline_adapter.getItem(info.position);
                                    offline_adapter.RemoveSelecitonAt(info.position);
                                    offline_adapter.remove(file_to_dlt);
                                    file_to_dlt.delete();
                                    load_indicator.setText(offline_adapter.getCount()!=0 ? "":"Empty");
                                    return true;
                                }
                            });
                            menu.getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    AppDataStorage.export_helper_file=offline_adapter.getItem(info.position);
                                    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),AppDataStorage.SINGLE_EXPORT_INTENT_CODE);
                                    return true;
                                }
                            });
                            menu.getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(UtilCollections.isConnectedToInternet(getApplicationContext())){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        UploadFiles(new File[]{offline_adapter.getItem(info.position)});
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();

                                    return true;
                                }
                            });
                        }
                        break;
                    }
                    case 1:{

                        if(online_multichoice_actionmode==null){
                            getMenuInflater().inflate(R.menu.explorer_online_context_menu,menu);

                            final OnlineFileMetaDataNode selected_node=online_adapter.getItem(info.position);
                            final StorageReference qpack_file_ref=FirebaseStorage.getInstance().getReference(selected_node.ref_path);

                            menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if(UtilCollections.isConnectedToInternet(getApplicationContext())){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        DownloadFiles(new OnlineFileMetaDataNode[]{selected_node},AppDataStorage.current_user);
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();

                                    return true;
                                }
                            });
                            menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    OnlineFileMetaDataNode[] metaDataNodes=new OnlineFileMetaDataNode[]{online_adapter.getItem(info.position)};
                                    DeleteOnlineFiles(metaDataNodes,AppDataStorage.current_user);
                                    return true;
                                }
                            });
                            menu.getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    final AlertDialog[] alertDialog=new AlertDialog[1];
                                    final AlertDialog.Builder builder=new AlertDialog.Builder(ExploreActivity.this);
                                    final View view=getLayoutInflater().inflate(R.layout.exam_scheduler_dialog_layout,null,false);
                                    final EditText exam_title=view.findViewById(R.id.exam_title);
                                    final EditText exam_password=view.findViewById(R.id.exam_password);
                                    final EditText exam_duration=view.findViewById(R.id.exam_duration);
                                    final EditText marks_per_qtn=view.findViewById(R.id.marks_per_qtn);
                                    final EditText neg_mark_percentage=view.findViewById(R.id.neg_mark_percentage);
                                    final EditText course_code=view.findViewById(R.id.course_code);
                                    final EditText course_title=view.findViewById(R.id.course_title);
                                    final Switch public_accessible=view.findViewById(R.id.public_accessible);
                                    final TextView add_exam=view.findViewById(R.id.add_exam);
                                    final TextView cloase_dlg=view.findViewById(R.id.close_dlg);

                                    exam_title.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });
                                    exam_password.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });
                                    exam_duration.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });
                                    marks_per_qtn.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });
                                    neg_mark_percentage.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });
                                    course_code.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });
                                    course_title.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if(exam_title.getText().toString().trim().isEmpty() ||
                                                    exam_password.getText().toString().trim().isEmpty() ||
                                                    exam_duration.getText().toString().trim().isEmpty() ||
                                                    marks_per_qtn.getText().toString().trim().isEmpty() ||
                                                    neg_mark_percentage.getText().toString().trim().isEmpty() ||
                                                    course_code.getText().toString().trim().isEmpty()||
                                                    course_title.getText().toString().trim().isEmpty()) {
                                                add_exam.setEnabled(false);
                                                add_exam.setTextColor(Color.parseColor("#AAAAAA"));

                                            } else {
                                                add_exam.setEnabled(true);
                                                add_exam.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            }
                                        }
                                    });


                                    cloase_dlg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog[0].dismiss();
                                        }
                                    });
                                    add_exam.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                                                        final String ID=Long.toString(Calendar.getInstance().getTimeInMillis());
                                                        final DatabaseReference new_question_ref=AppDataStorage.base_ref.child("questions").child(ID);
                                                        final DatabaseReference result_ref=AppDataStorage.base_ref.child("results").child(ID);

                                                        final HashMap<String,String> scheduled_exam_node=new HashMap<>(0);
                                                        scheduled_exam_node.put("title",exam_title.getText().toString().trim());
                                                        scheduled_exam_node.put("password",exam_password.getText().toString().trim());
                                                        scheduled_exam_node.put("duration",exam_duration.getText().toString().trim());
                                                        scheduled_exam_node.put("accessible",public_accessible.isChecked()?"true":"false");
                                                        scheduled_exam_node.put("qpack_ref_path", selected_node.ref_path);
                                                        scheduled_exam_node.put("marks_per_qtn",marks_per_qtn.getText().toString().trim());
                                                        scheduled_exam_node.put("neg_mark_percentage", neg_mark_percentage.getText().toString().trim());
                                                        scheduled_exam_node.put("course_code", course_code.getText().toString().trim());
                                                        scheduled_exam_node.put("course_title", course_title.getText().toString().trim());

                                                        new_question_ref.setValue(scheduled_exam_node).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task1) {
                                                                if(task1.isSuccessful()) {
                                                                    result_ref.setValue("null").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task2) {
                                                                            if(task2.isSuccessful()){
                                                                                AppDataStorage.userMetaData.getScheduledExamsMetaData().push().setValue(ID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task3) {
                                                                                        if(!task3.isSuccessful()){
                                                                                            result_ref.removeValue();
                                                                                            new_question_ref.removeValue();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            } else {
                                                                                new_question_ref.removeValue();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });

                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                alertDialog[0].dismiss();
                                                                Toast.makeText(ExploreActivity.this, "Successfully added to scheduled exam list", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                    } else {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(ExploreActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }

                                                }
                                            }).start();
                                        }
                                    });

                                    builder.setView(view);
                                    builder.setCancelable(false);
                                    builder.setTitle("Add Exam");
                                    alertDialog[0]=builder.show();
                                    return true;
                                }
                            });
                        }
                        break;
                    }
                }
            }
        });
    }

    private void UploadFiles(final File[] files) {

        new Thread(new Runnable() {

            final AlertDialog[] alertDialog=new AlertDialog[1];
            final AlertDialog.Builder builder=new AlertDialog.Builder(ExploreActivity.this);
            final View view = LayoutInflater.from(ExploreActivity.this).inflate(R.layout.progress_layout,null,false);
            final ProgressBar operation_progress=view.findViewById(R.id.operation_progress);
            final TextView progress_desc=view.findViewById(R.id.progress_desc);
            final TextView progress_count=view.findViewById(R.id.progress_count);
            final TextView cancel_dlg_btn=view.findViewById(R.id.cancel_dlg_btn);

            final StringBuilder failed_file_names=new StringBuilder();

            private int upload_index=-1;
            private boolean cancelled=false;

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        operation_progress.setMax(files.length);
                        operation_progress.setIndeterminate(files.length==1);
                        builder.setView(view);
                        builder.setCancelable(false);
                        progress_desc.setText("Preparing...");
                        alertDialog[0]=builder.show();
                    }
                });

                Upload();

            }

            private void Upload(){

                if((++upload_index<files.length) && !cancelled){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress_desc.setText("Uploading: "+files[upload_index].getName());
                            operation_progress.setProgress(upload_index);
                            progress_count.setText(upload_index+"/"+files.length);
                        }
                    });

                    final UploadTask uptask = AppDataStorage.userStorage.getUserDataDir().child(files[upload_index].getName()).putFile(
                            FileProvider.getUriForFile(
                                    ExploreActivity.this,
                                    "com.ruet_cse_1503050.ragib.qp_admin.fileprovider",
                                    files[upload_index]
                            )
                    );

                    cancel_dlg_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!uptask.isComplete()){
                                uptask.cancel();
                            }
                            cancelled=true;
                            operation_progress.setIndeterminate(true);
                            progress_desc.setText("Cancelling...");
                            progress_count.setText("");
                        }
                    });

                    uptask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            operation_progress.setProgress(upload_index+1);
                            progress_count.setText((upload_index+1)+"/"+files.length);

                            if(task.isSuccessful()){

                                String file_name=files[upload_index].getName();
                                String file_size_str=Long.toString(files[upload_index].length());
                                String cloud_path=task.getResult().getMetadata().getPath();

                                HashMap<String,String> map=new HashMap<>(0);
                                map.put("name",file_name);
                                map.put("size",file_size_str);
                                map.put("ref_path",cloud_path);

                                AppDataStorage.userMetaData.getUserStorageMetaData().push().setValue(map);

                            } else {
                                failed_file_names.append("-  "+files[upload_index].getName()+'\n');
                            }

                            Upload();

                        }
                    });

                } else {
                    for(int i=upload_index;i<files.length;++i){
                        failed_file_names.append("-  "+files[i].getName()+'\n');
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog[0].dismiss();
                            if(failed_file_names.toString().isEmpty()){
                                Toast.makeText(ExploreActivity.this, "All files uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                AlertDialog.Builder fail_dlg_builder=new AlertDialog.Builder(ExploreActivity.this)
                                        .setTitle("Failed uploads")
                                        .setMessage(failed_file_names)
                                        .setPositiveButton("Close",null);
                                fail_dlg_builder.show();
                            }
                        }
                    });
                }
            }

        }).start();
    }

    private void DownloadFiles(final OnlineFileMetaDataNode[] metaDataNodes, final FirebaseUser user) {

        new Thread(new Runnable() {

            final AlertDialog[] alertDialog=new AlertDialog[1];
            final AlertDialog.Builder builder=new AlertDialog.Builder(ExploreActivity.this);
            final View view = LayoutInflater.from(ExploreActivity.this).inflate(R.layout.progress_layout,null,false);
            final ProgressBar operation_progress=view.findViewById(R.id.operation_progress);
            final TextView progress_desc=view.findViewById(R.id.progress_desc);
            final TextView progress_count=view.findViewById(R.id.progress_count);
            final TextView cancel_dlg_btn=view.findViewById(R.id.cancel_dlg_btn);

            final StringBuilder failed_file_names=new StringBuilder();
            private int download_index=-1;
            private boolean cancelled=false;

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        operation_progress.setMax(metaDataNodes.length);
                        operation_progress.setIndeterminate(metaDataNodes.length==1);
                        builder.setView(view);
                        builder.setCancelable(false);
                        progress_desc.setText("Preparing...");
                        alertDialog[0]=builder.show();
                    }
                });
                Download();

            }

            private void Download(){

                if((++download_index<metaDataNodes.length) && !cancelled){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress_desc.setText("Downloading: "+metaDataNodes[download_index].name);
                            operation_progress.setProgress(download_index);
                            progress_count.setText(download_index+"/"+metaDataNodes.length);
                        }
                    });

                    final  File saving_file=new File(
                            AppDataStorage.LocalQuestionStorageDir.getAbsolutePath()+
                                    File.separator+metaDataNodes[download_index].name
                    );
                    final FileDownloadTask downtask=AppDataStorage.storage.getReference(metaDataNodes[download_index].ref_path).getFile(saving_file);

                    cancel_dlg_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!downtask.isComplete()){
                                downtask.cancel();
                            }
                            cancelled=true;
                            operation_progress.setIndeterminate(true);
                            progress_desc.setText("Cancelling...");
                            progress_count.setText("");
                        }
                    });

                    downtask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {

                            operation_progress.setProgress(download_index+1);
                            progress_count.setText((download_index+1)+"/"+metaDataNodes.length);

                            if(!task.isSuccessful()){
                                failed_file_names.append("-  "+metaDataNodes[download_index].name+'\n');
                            }

                            Download();
                        }
                    });

                } else {
                    for(int i=download_index;i<metaDataNodes.length;++i){
                        failed_file_names.append("-  "+metaDataNodes[i].name+'\n');
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog[0].dismiss();
                            if(failed_file_names.toString().isEmpty()){
                                Toast.makeText(ExploreActivity.this, "All files downloaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                AlertDialog.Builder fail_dlg_builder=new AlertDialog.Builder(ExploreActivity.this)
                                        .setTitle("Failed downloads")
                                        .setMessage(failed_file_names)
                                        .setPositiveButton("Close",null);
                                fail_dlg_builder.show();
                            }
                        }
                    });
                }
            }

        }).start();
    }

    private void DeleteOnlineFiles(final OnlineFileMetaDataNode[] metadatas, final FirebaseUser user){

        new Thread(new Runnable() {

            private final AlertDialog[] alertDialog=new AlertDialog[1];
            private final AlertDialog.Builder builder=new AlertDialog.Builder(ExploreActivity.this);
            private final View view = LayoutInflater.from(ExploreActivity.this).inflate(R.layout.progress_layout,null,false);
            private final ProgressBar operation_progress=view.findViewById(R.id.operation_progress);
            private final TextView progress_desc=view.findViewById(R.id.progress_desc);
            private final TextView progress_count=view.findViewById(R.id.progress_count);
            private final TextView cancel_dlg_btn=view.findViewById(R.id.cancel_dlg_btn);

            private final StringBuilder failed_file_names=new StringBuilder();
            private int delete_index=-1;
            private boolean cancelled=false;
            private List<OnlineFileMetaDataNode> metadatas_to_delete=new ArrayList<>(0);
            private List<OnlineFileMetaDataNode> metadatas_to_skip=new ArrayList<>(0);

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        operation_progress.setMax(metadatas.length);
                        operation_progress.setIndeterminate(metadatas.length==1);
                        builder.setView(view);
                        builder.setCancelable(false);
                        progress_desc.setText("Preparing...");
                        alertDialog[0]=builder.show();
                    }
                });

                AppDataStorage.base_ref.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        List<String> scheduled_qrefs=new ArrayList<>(0);
                        while (iterator.hasNext()){
                            scheduled_qrefs.add(iterator.next().child("qpack_ref_path").getValue(String.class));
                        }
                        for(int i=0;i<metadatas.length;++i){
                            boolean present=false;
                            String cursor=metadatas[i].ref_path;
                            for(int j=0;j<scheduled_qrefs.size();++j){
                                if(cursor.equals(scheduled_qrefs.get(j))){
                                    present=true;
                                }
                            }
                            if(!present) metadatas_to_delete.add(metadatas[i]);
                            else metadatas_to_skip.add(metadatas[i]);
                        }
                        Delete();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

            }

            private void Delete(){

                if((++delete_index<metadatas_to_delete.size()) && !cancelled){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress_desc.setText("Deleting: "+metadatas_to_delete.get(delete_index).name);
                            operation_progress.setProgress(delete_index);
                            progress_count.setText(delete_index+"/"+metadatas_to_delete.size());
                        }
                    });

                    final Task<Void> dlttask=AppDataStorage.storage.getReference(metadatas_to_delete.get(delete_index).ref_path).delete();
                    cancel_dlg_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancelled=true;
                            operation_progress.setIndeterminate(true);
                            progress_desc.setText("Cancelling...");
                            progress_count.setText("");
                        }
                    });
                    dlttask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            operation_progress.setProgress(delete_index+1);
                            progress_count.setText((delete_index+1)+"/"+metadatas_to_delete.size());

                            if(task.isSuccessful()){
                                AppDataStorage.userMetaData.getUserStorageMetaData()
                                        .child(metadatas_to_delete.get(delete_index).key)
                                        .removeValue();
                                online_adapter.remove(metadatas_to_delete.get(delete_index));
                            } else {
                                failed_file_names.append("-  "+metadatas_to_delete.get(delete_index).name+'\n');
                            }

                            Delete();
                        }
                    });

                } else {

                    for(int i=delete_index;i<metadatas_to_delete.size();++i){
                        failed_file_names.append("-  "+metadatas_to_delete.get(i).name+'\n');
                    }

                    final StringBuilder skipped_file_names=new StringBuilder();
                    for(int i=0;i<metadatas_to_skip.size();++i){
                        skipped_file_names.append("-  ").append(metadatas_to_skip.get(i).name).append('\n');
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog[0].dismiss();
                            if(failed_file_names.length()==0 && metadatas_to_skip.size()==0){
                                Toast.makeText(ExploreActivity.this, (metadatas.length>1?"Selected Files ":"")+"Deleted Successfully", Toast.LENGTH_SHORT).show();
                            } else {

                                String msgp1 =
                                        (failed_file_names.length()>0?"Failed:\n":"")+
                                                failed_file_names.toString()+
                                                (failed_file_names.length()>0?"\n\n":"");

                                String msgp2=
                                        (metadatas_to_skip.size()>0?"Skipped (Scheduled for Exams):\n":"")+
                                                skipped_file_names.toString()+
                                                (metadatas_to_skip.size()>0?"\nIf you want to delete skipped files, first delete corresponding exams and retry.":"");

                                AlertDialog.Builder fail_dlg_builder=new AlertDialog.Builder(ExploreActivity.this)
                                        .setTitle("Failed to delete")
                                        .setMessage(msgp1+msgp2)
                                        .setPositiveButton("Close",null);
                                fail_dlg_builder.show();
                            }
                        }
                    });
                }
            }

        }).start();
    }

    private void InitializeUIData() {
        InitializeLists();
    }

    private void InitializeLists() {
        InitializeOfflineList();
        InitializeOnlineList();
    }

    private void InitializeOfflineList() {
        ReloadOfflineList();
    }

    private void InitializeOnlineList() {
        online_adapter=new OnlineExplorerListAdapter(
                ExploreActivity.this,
                R.layout.info_node_layout,
                new ArrayList<OnlineFileMetaDataNode>(0)
        );
    }

    private void ReloadOfflineList(){
        load_indicator.setText("Loading...");
        File[] files=AppDataStorage.LocalQuestionStorageDir.listFiles();
        List<File> available_questions=new ArrayList<>(0);
        int len=files.length;
        for (int i=0;i<len;++i){
            available_questions.add(files[i]);
        }
        offline_adapter=new OfflineExplorerListAdapter(
                ExploreActivity.this,
                R.layout.info_node_layout,
                available_questions
        );
        explorer_question_list.setAdapter(offline_adapter);
        load_indicator.setText(offline_adapter.getCount()!=0 ? "":"Empty");
    }
}
