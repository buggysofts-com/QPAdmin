package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class ExportResultActivity extends AppCompatActivity {

    private DocumentFile last_selectedx_doc_dir=null;
    private EditText exam_title;
    private EditText course_details;
    private EditText department_name;
    private EditText institution_name;
    private Spinner pair_count;
    private TextView export_path;
    private Button browse;

    private ResultData data;
    private String exam_name;
    private String course_code;
    private String course_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_export_result);
        Initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case AppDataStorage.SINGLE_EXPORT_INTENT_CODE:{
                    String abs_path=UriUtils.getDocumentFileAbsPath(
                            (last_selectedx_doc_dir=DocumentFile.fromTreeUri(ExportResultActivity.this,data.getData())),
                            ExportResultActivity.this
                    );
                    if(new File(abs_path).exists()){
                        export_path.setText(abs_path);
                    } else {
                        Toast.makeText(this, "Shortcuts are not allowed. Please choose a valid directory", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apply_changes_toolbar_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String xm_ttl=exam_title.getText().toString().trim();
                String crs_ttl=course_details.getText().toString().trim();
                String dept_ttl=department_name.getText().toString().trim();
                String insttn_ttl=institution_name.getText().toString().trim();
                String xport_path=export_path.getText().toString().trim();
                if(xm_ttl.isEmpty()||
                        crs_ttl.isEmpty()||
                        dept_ttl.isEmpty()||
                        insttn_ttl.isEmpty()||
                        xport_path.isEmpty()){
                    Toast.makeText(ExportResultActivity.this, "Please fill all the required info to export the result.", Toast.LENGTH_SHORT).show();
                } else {
                    int N=data.scores.size();
                    int C=Integer.parseInt(pair_count.getSelectedItem().toString());
                    int R= ((int) Math.ceil(((double) N) / ((double) C)));
                    double table_col_width=100.0/ (2.0*C);

                    StringBuilder table_header=new StringBuilder();
                    table_header.append("<tr>\n");
                    for(int i=0;i<C;++i){
                        table_header.append("<th align=\"center\" style=\"width: ").append(table_col_width).append("%;").append(" font-size: larger\">Roll</th><th align=\"center\" style=\"width: ").append(table_col_width).append("%;").append(" font-size: larger\">Score</th>\n");
                    }
                    table_header.append("</tr>\n");

                    StringBuilder table_body=new StringBuilder();
                    for(int i=1;i<=R;++i){
                        table_body.append("<tr>\n");
                        for(int j=0;j<C;++j){
                            int current_position=(i+(j*R));
                            if(current_position<=N){
                                table_body
                                        .append("<td align=\"center\" style=\"width: ")
                                        .append(table_col_width).append("%;")
                                        .append(" font-size: small\">")
                                        .append(data.scores.get(current_position - 1).key)
                                        .append("</td><td align=\"center\" style=\"width: ")
                                        .append(table_col_width).append("%;")
                                        .append(" font-size: small\">")
                                        .append(data.scores.get(current_position - 1).value)
                                        .append("</td>\n");
                            } else {
                                table_body
                                        .append("<td align=\"center\" style=\"width: ")
                                        .append(table_col_width).append("%;")
                                        .append(" font-size: small\">").append("-")
                                        .append("</td><td align=\"center\" style=\"width: ")
                                        .append(table_col_width).append("%;")
                                        .append(" font-size: small\">")
                                        .append("-").append("</td>\n");
                            }
                        }
                        table_body.append("</tr>\n");
                    }

                    String main_html=
                                    "<!DOCTYPE html>\n" +
                                    "<html lang=\"en\">\n" +
                                    "<head>\n" +
                                    "<meta charset=\"UTF-8\">\n" +
                                    "<title>Exam Result</title>\n" +
                                    "</head>\n" +
                                    "<body>\n" +
                                    "<h4 align=\"center\">RESULT OF EXAM "+xm_ttl+"("+data.ID+")"+"</h4>\n"+
                                    "<h5 align=\"center\">Course :  "+crs_ttl+"<br>Department :  "+dept_ttl+"<br>"+insttn_ttl+"</h5>\n" +
                                    "<br>"+
                                    "<table border=\"1\" style=\"width: 100%; margin: 0; padding: 0;\">\n" +
                                    table_header + table_body +
                                    "</table>\n" +
                                    "</body>\n" +
                                    "</html>";

                    DocumentFile file_to_save=last_selectedx_doc_dir.createFile("text/html",xm_ttl+'_'+data.ID+".html");
                    UtilCollections.WriteToDocFile(ExportResultActivity.this,file_to_save,main_html.getBytes());
                    Toast.makeText(ExportResultActivity.this,(xm_ttl+'_'+data.ID+".html"+" has been saved at the specified directory"), Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;
            }
        });
        return true;
    }

    private void Initialize() {
        Initializedata();
        InitializeUIComponents();
    }

    private void Initializedata() {
        data= ((ResultData) getIntent().getSerializableExtra("result_list"));
        exam_name=getIntent().getStringExtra("exam_title");
        course_code=getIntent().getStringExtra("course_code");
        course_title=getIntent().getStringExtra("course_title");
    }

    private void InitializeUIComponents() {
        exam_title=findViewById(R.id.exam_title);
        course_details=findViewById(R.id.course_details);
        department_name=findViewById(R.id.department_name);
        institution_name=findViewById(R.id.institution_name);
        pair_count=findViewById(R.id.pair_count);
        export_path=findViewById(R.id.export_path);
        browse=findViewById(R.id.browse);


        exam_title.setText(exam_name);
        course_details.setText(course_code+" :  "+course_title);
        String[] count=new String[]{"1","2","3","4","5"};
        pair_count.setAdapter(new ArrayAdapter<>(ExportResultActivity.this, android.R.layout.simple_spinner_dropdown_item, count));
        pair_count.setSelection(1);

        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent,AppDataStorage.SINGLE_EXPORT_INTENT_CODE);
            }
        });
    }
}
