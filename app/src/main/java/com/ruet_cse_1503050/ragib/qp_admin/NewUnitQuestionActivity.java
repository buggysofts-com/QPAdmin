package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.util.Calendar;

public class NewUnitQuestionActivity extends AppCompatActivity {

    private ImageView question_image;
    private ImageButton delete_img;
    private ImageButton edit_img;
    private ImageButton take_img;
    private EditText question_description;
    private CheckBox ans_choice0;
    private CheckBox ans_choice1;
    private CheckBox ans_choice2;
    private CheckBox ans_choice3;
    private EditText edit_ans0;
    private EditText edit_ans1;
    private EditText edit_ans2;
    private EditText edit_ans3;

    private UnitQuestion original_empty_question;
    private UnitQuestion question_to_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_new_unit_question);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Initialize();

    }

    @Override
    public void onBackPressed() {
        if(!question_to_save.Equals(original_empty_question)){
            AlertDialog.Builder alert_builder = new AlertDialog.Builder(NewUnitQuestionActivity.this);
            alert_builder.setMessage("Do you want to save changes?");
            alert_builder.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(CheckQValidity()){
                        SaveQuestion();
                        getIntent().putExtra("returned_question",question_to_save);
                        setResult(RESULT_OK,getIntent());
                        finish();
                    }
                }
            });
            alert_builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert_builder.show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apply_changes_toolbar_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(CheckQValidity()){
                    SaveQuestion();
                    getIntent().putExtra("returned_question",question_to_save);
                    setResult(RESULT_OK,getIntent());
                    finish();

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
                case AppDataStorage.IMPORT_IMAGE_CODE:{
                    File input_file=new File(
                            UriUtils.getDocumentFileAbsPath(
                                    DocumentFile.fromSingleUri(
                                            NewUnitQuestionActivity.this,
                                            data.getData()
                                    ),
                                    NewUnitQuestionActivity.this
                            )
                    );
                    question_to_save.setImagePath(input_file.getAbsolutePath());
                    question_image.setImageDrawable(Drawable.createFromPath(question_to_save.getImagePath()));
                    break;
                }
                case AppDataStorage.CAMERA_INTENT_CODE:{
                    question_to_save.setImagePath(AppDataStorage.recent_captured_img_file.getAbsolutePath());
                    question_image.setImageDrawable(Drawable.createFromPath(AppDataStorage.recent_captured_img_file.getAbsolutePath()));
                    break;
                }
            }
        }
    }


    private void Initialize() {
        InitializeActivityData();
        InitializeUIComponents();
    }

    private void InitializeActivityData() {
        original_empty_question=
                new UnitQuestion(
                        "",
                        "",
                        new AnsNode[]{
                                new AnsNode("",false),
                                new AnsNode("",false),
                                new AnsNode("",false),
                                new AnsNode("",false)
                        }
                );
        question_to_save=original_empty_question.CopyQuestionAsNew();
    }

    private void InitializeUIComponents() {

        question_image=findViewById(R.id.question_image);
        delete_img=findViewById(R.id.delete_img);
        edit_img=findViewById(R.id.edit_img);
        take_img=findViewById(R.id.take_img);
        question_description=findViewById(R.id.question_description);
        ans_choice0=findViewById(R.id.ans_choice0);
        ans_choice1=findViewById(R.id.ans_choice1);
        ans_choice2=findViewById(R.id.ans_choice2);
        ans_choice3=findViewById(R.id.ans_choice3);
        edit_ans0=findViewById(R.id.edit_ans0);
        edit_ans1=findViewById(R.id.edit_ans1);
        edit_ans2=findViewById(R.id.edit_ans2);
        edit_ans3=findViewById(R.id.edit_ans3);

        delete_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question_to_save.setImagePath("");
                question_image.setImageDrawable(getDrawable(R.drawable.img_place_holder));
            }
        });
        edit_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent,AppDataStorage.IMPORT_IMAGE_CODE);
            }
        });
        take_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDataStorage.recent_captured_img_file=new File(
                        AppDataStorage.LocalTemporaryFilesStorageDir.getAbsolutePath()+
                                Calendar.getInstance().getTimeInMillis()
                );
                Intent cam_intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(cam_intent.resolveActivity(getPackageManager())!=null){
                    Uri img_uri=FileProvider.getUriForFile(
                            NewUnitQuestionActivity.this,
                            "com.ruet_cse_1503050.ragib.qp_admin.fileprovider",
                            AppDataStorage.recent_captured_img_file
                    );
                    cam_intent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri);
                    startActivityForResult(cam_intent,AppDataStorage.CAMERA_INTENT_CODE);
                } else {
                    Toast.makeText(NewUnitQuestionActivity.this, "Sorry, your phone does not support camera!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean CheckQValidity() {

        String err_str="";
        if(question_description.getText().toString().trim().length()==0) {
            question_description.setText("");
            err_str+="- Please add question text\n";
        }

        boolean add_all_q_choice=false;
        if(edit_ans0.getText().toString().trim().length()==0){
            add_all_q_choice=true;
            edit_ans0.setText("");
        }
        if(edit_ans1.getText().toString().trim().length()==0){
            add_all_q_choice=true;
            edit_ans1.setText("");
        }
        if(edit_ans2.getText().toString().trim().length()==0){
            add_all_q_choice=true;
            edit_ans2.setText("");
        }
        if(edit_ans3.getText().toString().trim().length()==0){
            add_all_q_choice=true;
            edit_ans3.setText("");
        }
        if(add_all_q_choice){
            err_str+="- Please fill all the answer choices\n";
        }

        if(!ans_choice0.isChecked() && !ans_choice1.isChecked() &&
                !ans_choice2.isChecked() && !ans_choice3.isChecked()){
            err_str+="- Please mark at least one answer choice as the correct answer";
        }

        if(!err_str.equals("")){
            AlertDialog.Builder alert=new AlertDialog.Builder(NewUnitQuestionActivity.this);
            alert.setTitle("Fix errors");
            alert.setMessage(err_str);
            alert.show();
            return false;
        } else {
            return true;
        }

    }

    private void SaveQuestion() {
        if(question_to_save.getImagePath()==null) question_to_save.setImagePath("");
        question_to_save.setQuestionText(question_description.getText().toString());
        question_to_save.setAnswers(new AnsNode[]{
                new AnsNode(edit_ans0.getText().toString(),ans_choice0.isChecked()),
                new AnsNode(edit_ans1.getText().toString(),ans_choice1.isChecked()),
                new AnsNode(edit_ans2.getText().toString(),ans_choice2.isChecked()),
                new AnsNode(edit_ans3.getText().toString(),ans_choice3.isChecked())
        });
    }

}
