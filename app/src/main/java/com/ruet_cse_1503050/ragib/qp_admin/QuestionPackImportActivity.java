package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipFile;

public class QuestionPackImportActivity extends AppCompatActivity {

    private ListView import_qtn_list;
    private CheckBox remove_after_import;
    private Button start_import;

    private DocumentFileNodeAdapter documentNodeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_question_pack_import);

        InitializeUIComponents();
        InitializeUIData();
    }

    private void InitializeUIComponents() {
        import_qtn_list= findViewById(R.id.imported_qtn_list);
        remove_after_import= findViewById(R.id.remove_after_import);
        start_import= findViewById(R.id.start_import);
        start_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if(documentNodeAdapter.getCount()>0){
                    final boolean delete_src=remove_after_import.isChecked();
                    new Thread(new Runnable() {

                        boolean cancelled;
                        AlertDialog alertDialog;
                        AlertDialog.Builder builder;
                        View view;
                        ProgressBar operation_progress;
                        TextView progress_desc;
                        TextView progress_count;
                        TextView cancel_dlg_btn;

                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    cancelled=false;
                                    builder=new AlertDialog.Builder(QuestionPackImportActivity.this);
                                    view=getLayoutInflater().inflate(R.layout.progress_layout,null);
                                    operation_progress=view.findViewById(R.id.operation_progress);
                                    progress_desc=view.findViewById(R.id.progress_desc);
                                    progress_count=view.findViewById(R.id.progress_count);
                                    cancel_dlg_btn=view.findViewById(R.id.cancel_dlg_btn);
                                    operation_progress.setMax(documentNodeAdapter.getCount());
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

                            final int len=documentNodeAdapter.getCount();
                            for(int i=0;i<len;++i){

                                if(!cancelled){

                                    final DocumentFile tmp_doc_file=documentNodeAdapter.getItem(i);

                                    final int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress_desc.setText("Importing: "+tmp_doc_file.getName());
                                            progress_count.setText((finalI + 1) +"/"+ len);
                                        }
                                    });

                                    UtilCollections.FileFromDocFile(
                                            QuestionPackImportActivity.this,
                                            tmp_doc_file,
                                            new File(
                                                    AppDataStorage.LocalQuestionStorageDir.getAbsolutePath()+
                                                            File.separator+tmp_doc_file.getName()
                                            )
                                    );
                                    if(delete_src) tmp_doc_file.delete();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            operation_progress.setProgress(finalI+1);
                                        }
                                    });
                                } else {
                                    break;
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog.dismiss();
                                    if(!cancelled){
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                }
                            });

                        }
                    }).start();
                } else {
                    Toast.makeText(QuestionPackImportActivity.this, "Please insert one or more question pack files to import", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void InitializeUIData() {
        documentNodeAdapter=
                new DocumentFileNodeAdapter(
                        QuestionPackImportActivity.this,
                        R.layout.info_node_layout,
                        new ArrayList<DocumentFile>(0)
                );
        import_qtn_list.setAdapter(documentNodeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.import_toolbar_menu,menu);

        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.setType("*/*");
                startActivityForResult(intent,AppDataStorage.IMPORT_QUESTION_ACTIVITY_CODE);
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
                    Uri uri=data.getData();
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION|
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );
                    DocumentFile file=DocumentFile.fromSingleUri(QuestionPackImportActivity.this,uri);
                    ZipFile validation_file=null;
                    try {
                        validation_file=new ZipFile(new File(UriUtils.getDocumentFileAbsPath(file,QuestionPackImportActivity.this)));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, getString(R.string.not_valid_qpack_txt), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(validation_file.getEntry("qpack_file_validation_key___file_creator_id_1503050")!=null){
                        if(!InList(documentNodeAdapter,file)){
                            documentNodeAdapter.add(file);
                        } else {
                            Toast.makeText(this, getString(R.string.file_alrady_in_list_txt), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.not_valid_qpack_txt), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

    private boolean InList(DocumentFileNodeAdapter adapter,DocumentFile file) {

        String path=UriUtils.getDocumentFileAbsPath(file,QuestionPackImportActivity.this);
        String name=file.getName();
        long size=file.length();

        int len=adapter.getCount();
        for(int i=0;i<len;++i){
            DocumentFile tmp=adapter.getItem(i);
            String tmp_path=UriUtils.getDocumentFileAbsPath(tmp,QuestionPackImportActivity.this);
            String tmp_name=tmp.getName();
            long tmp_size=tmp.length();
            if(path.equals(tmp_path) && name.equals(tmp_name) && size==tmp_size){
                return true;
            }
        }

        return false;
    }
}
