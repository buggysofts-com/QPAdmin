package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

public class AnswerScriptActivity extends AppCompatActivity {

    private WebView html_holder;
    private String passed_html;
    private String roll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_answer_script);

        Initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_html_toolbar_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent,AppDataStorage.SINGLE_EXPORT_INTENT_CODE);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case AppDataStorage.SINGLE_EXPORT_INTENT_CODE:{
                if(resultCode==RESULT_OK){
                    DocumentFile export_file=DocumentFile.fromTreeUri(AnswerScriptActivity.this,data.getData()).createFile("text/html",roll);
                    UtilCollections.WriteToDocFile(AnswerScriptActivity.this,export_file,passed_html.getBytes());
                    Toast.makeText(this, "Script Saved Successfully!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void Initialize() {
        InitializeActivityData();
        InitializeUIComponents();
        InitializeUIData();
    }

    private void InitializeActivityData() {
        passed_html=getIntent().getStringExtra("passed_html");
        roll=getIntent().getStringExtra("roll");
    }

    private void InitializeUIComponents() {
        html_holder=findViewById(R.id.html_holder);
    }

    private void InitializeUIData() {
        html_holder.loadData(passed_html,"text/html",null);
    }
}
