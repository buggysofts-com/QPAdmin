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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Calendar;

public class EditQuestionActivity extends AppCompatActivity {

    private UnitQuestion passed_question;
    private UnitQuestion question_to_edit;
    private int passed_index;

    private ImageView question_image;
    private ImageButton delete_img;
    private ImageButton edit_img;
    private ImageButton take_img;
    private TextView question_description;
    private ImageButton edit_question_desc;
    private CheckBox ans_choice0;
    private ImageButton edit_ans0;
    private CheckBox ans_choice1;
    private ImageButton edit_ans1;
    private CheckBox ans_choice2;
    private ImageButton edit_ans2;
    private CheckBox ans_choice3;
    private ImageButton edit_ans3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_edit_question);
        Initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apply_changes_toolbar_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(!question_to_edit.Equals(passed_question)){
                    getIntent().putExtra("returned_question",question_to_edit);
                    getIntent().putExtra("returned_index",passed_index);
                    setResult(RESULT_OK,getIntent());
                    System.out.println("Path ret: "+question_to_edit.getImagePath()+" : "+new File(question_to_edit.getImagePath()).exists());
                    finish();
                } else {
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
                                            EditQuestionActivity.this,
                                            data.getData()
                                    ),
                                    EditQuestionActivity.this
                            )
                    );
                    System.out.println(input_file.getAbsolutePath());
                    question_to_edit.setImagePath(input_file.getAbsolutePath());
                    question_image.setImageDrawable(Drawable.createFromPath(question_to_edit.getImagePath()));
                    break;
                }
                case AppDataStorage.CAMERA_INTENT_CODE:{
                    question_to_edit.setImagePath(AppDataStorage.recent_captured_img_file.getAbsolutePath());
                    question_image.setImageDrawable(Drawable.createFromPath(AppDataStorage.recent_captured_img_file.getAbsolutePath()));
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!question_to_edit.Equals(passed_question)){
            AlertDialog.Builder alert_builder = new AlertDialog.Builder(EditQuestionActivity.this);
            alert_builder.setMessage("Do you want to save changes?");
            alert_builder.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getIntent().putExtra("returned_question",question_to_edit);
                    getIntent().putExtra("returned_index",passed_index);
                    setResult(RESULT_OK,getIntent());
                    finish();
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

    private void Initialize() {
        InitializeActivityData();
        InitializeUIComponents();
        InitializeUIComponentsData();
    }

    private void InitializeActivityData() {
        passed_question = (UnitQuestion) getIntent().getSerializableExtra("passed_question");
        passed_index = getIntent().getIntExtra("passed_index",0);
        question_to_edit=passed_question.CopyQuestionAsNew();
    }

    private void InitializeUIComponents() {
        question_image=findViewById(R.id.question_image);
        delete_img=findViewById(R.id.delete_img);
        edit_img=findViewById(R.id.edit_img);
        take_img=findViewById(R.id.take_img);
        question_description=findViewById(R.id.question_description);
        edit_question_desc=findViewById(R.id.edit_question_desc);
        ans_choice0=findViewById(R.id.ans_choice0);
        edit_ans0=findViewById(R.id.edit_ans0);
        ans_choice1=findViewById(R.id.ans_choice1);
        edit_ans1=findViewById(R.id.edit_ans1);
        ans_choice2=findViewById(R.id.ans_choice2);
        edit_ans2=findViewById(R.id.edit_ans2);
        ans_choice3=findViewById(R.id.ans_choice3);
        edit_ans3=findViewById(R.id.edit_ans3);

        delete_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(question_to_edit.getImagePath()!=null){
                    AlertDialog.Builder alert_builder=new AlertDialog.Builder(EditQuestionActivity.this);
                    alert_builder.setMessage("The image associated with this question will be removed. " +
                            "The question will be displayed without any image. Are you sure to remove this image?");
                    alert_builder.setTitle("Remove image");
                    alert_builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            question_image.setImageDrawable(getDrawable(R.drawable.img_place_holder));
                            question_to_edit.setImagePath("");
                        }
                    });
                    alert_builder.setNegativeButton("Cancel",null);
                    alert_builder.show();
                } else {
                    Toast.makeText(EditQuestionActivity.this, "Image not available", Toast.LENGTH_SHORT).show();
                }
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
                            EditQuestionActivity.this,
                            "com.ruet_cse_1503050.ragib.qp_admin.fileprovider",
                            AppDataStorage.recent_captured_img_file
                    );
                    cam_intent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri);
                    startActivityForResult(cam_intent,AppDataStorage.CAMERA_INTENT_CODE);
                } else {
                    Toast.makeText(EditQuestionActivity.this, "Sorry, your phone does not support camera!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        edit_question_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View edit_box=getLayoutInflater().inflate(R.layout.text_edit_dialog_layout,null);
                final EditText txt_edit_box=edit_box.findViewById(R.id.txt_edit_box);
                txt_edit_box.setHint("Type Question Text...");
                txt_edit_box.setText(question_to_edit.getQuestionText());
                txt_edit_box.setSelection(txt_edit_box.length());

                AlertDialog.Builder alert_builder=new AlertDialog.Builder(EditQuestionActivity.this);
                alert_builder.setTitle("Edit");
                alert_builder.setIcon(R.drawable.edit_icon);
                alert_builder.setView(edit_box);
                alert_builder.setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        question_to_edit.setQuestionText(txt_edit_box.getText().toString().trim());
                        question_description.setText(question_to_edit.getQuestionText());
                    }
                });
                alert_builder.setNegativeButton("Cancel",null);

                final AlertDialog dialog=alert_builder.create();
                txt_edit_box.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(TextUtils.isEmpty(s.toString().trim())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
        ans_choice0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                question_to_edit.setAnswer(
                        0,
                        new AnsNode(
                                question_to_edit.getAnswer(0).ans,
                                isChecked
                        )
                );
            }
        });
        ans_choice1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                question_to_edit.setAnswer(
                        1,
                        new AnsNode(
                                question_to_edit.getAnswer(1).ans,
                                isChecked
                        )
                );
            }
        });
        ans_choice2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                question_to_edit.setAnswer(
                        2,
                        new AnsNode(
                                question_to_edit.getAnswer(2).ans,
                                isChecked
                        )
                );
            }
        });
        ans_choice3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                question_to_edit.setAnswer(
                        3,
                        new AnsNode(
                                question_to_edit.getAnswer(3).ans,
                                isChecked
                        )
                );
            }
        });

        edit_ans0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View edit_box=getLayoutInflater().inflate(R.layout.text_edit_dialog_layout,null);
                final EditText txt_edit_box=edit_box.findViewById(R.id.txt_edit_box);
                txt_edit_box.setHint("Type answer...");
                txt_edit_box.setText(question_to_edit.getAnswers()[0].ans);
                txt_edit_box.setSelection(txt_edit_box.length());

                AlertDialog.Builder alert_builder=new AlertDialog.Builder(EditQuestionActivity.this);
                alert_builder.setTitle("Edit");
                alert_builder.setIcon(R.drawable.edit_icon);
                alert_builder.setView(edit_box);
                alert_builder.setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        question_to_edit.setAnswer(
                                0,
                                new AnsNode(
                                        txt_edit_box.getText().toString().trim(),
                                        question_to_edit.getAnswer(0).state
                                )
                        );
                        ans_choice0.setText(question_to_edit.getAnswers()[0].ans);
                    }
                });
                alert_builder.setNegativeButton("Cancel",null);

                final AlertDialog dialog=alert_builder.create();
                txt_edit_box.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(TextUtils.isEmpty(s.toString().trim())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
        edit_ans1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View edit_box=getLayoutInflater().inflate(R.layout.text_edit_dialog_layout,null);
                final EditText txt_edit_box=edit_box.findViewById(R.id.txt_edit_box);
                txt_edit_box.setHint("Type answer...");
                txt_edit_box.setText(question_to_edit.getAnswers()[1].ans);
                txt_edit_box.setSelection(txt_edit_box.length());

                AlertDialog.Builder alert_builder=new AlertDialog.Builder(EditQuestionActivity.this);
                alert_builder.setTitle("Edit");
                alert_builder.setIcon(R.drawable.edit_icon);
                alert_builder.setView(edit_box);
                alert_builder.setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        question_to_edit.setAnswer(
                                1,
                                new AnsNode(
                                        txt_edit_box.getText().toString().trim(),
                                        question_to_edit.getAnswer(1).state
                                )
                        );
                        ans_choice1.setText(question_to_edit.getAnswers()[1].ans);
                    }
                });
                alert_builder.setNegativeButton("Cancel",null);

                final AlertDialog dialog=alert_builder.create();
                txt_edit_box.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(TextUtils.isEmpty(s.toString().trim())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
        edit_ans2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View edit_box=getLayoutInflater().inflate(R.layout.text_edit_dialog_layout,null);
                final EditText txt_edit_box=edit_box.findViewById(R.id.txt_edit_box);
                txt_edit_box.setHint("Type answer...");
                txt_edit_box.setText(question_to_edit.getAnswers()[2].ans);
                txt_edit_box.setSelection(txt_edit_box.length());

                AlertDialog.Builder alert_builder=new AlertDialog.Builder(EditQuestionActivity.this);
                alert_builder.setTitle("Edit");
                alert_builder.setIcon(R.drawable.edit_icon);
                alert_builder.setView(edit_box);
                alert_builder.setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        question_to_edit.setAnswer(
                                2,
                                new AnsNode(
                                        txt_edit_box.getText().toString().trim(),
                                        question_to_edit.getAnswer(2).state
                                )
                        );
                        ans_choice2.setText(question_to_edit.getAnswers()[2].ans);
                    }
                });
                alert_builder.setNegativeButton("Cancel",null);

                final AlertDialog dialog=alert_builder.create();
                txt_edit_box.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(TextUtils.isEmpty(s.toString().trim())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
        edit_ans3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View edit_box=getLayoutInflater().inflate(R.layout.text_edit_dialog_layout,null);
                final EditText txt_edit_box=edit_box.findViewById(R.id.txt_edit_box);
                txt_edit_box.setHint("Type answer...");
                txt_edit_box.setText(question_to_edit.getAnswers()[3].ans);
                txt_edit_box.setSelection(txt_edit_box.length());

                AlertDialog.Builder alert_builder=new AlertDialog.Builder(EditQuestionActivity.this);
                alert_builder.setTitle("Edit");
                alert_builder.setIcon(R.drawable.edit_icon);
                alert_builder.setView(edit_box);
                alert_builder.setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        question_to_edit.setAnswer(
                                3,
                                new AnsNode(
                                        txt_edit_box.getText().toString().trim(),
                                        question_to_edit.getAnswer(3).state
                                )
                        );
                        ans_choice3.setText(question_to_edit.getAnswers()[3].ans);
                    }
                });
                alert_builder.setNegativeButton("Cancel",null);

                final AlertDialog dialog=alert_builder.create();
                txt_edit_box.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(TextUtils.isEmpty(s.toString().trim())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
    }

    private void InitializeUIComponentsData() {

        Drawable img_drawable;
        if(question_to_edit.getImagePath()!=null && new File(question_to_edit.getImagePath()).length()!=0){
            img_drawable=Drawable.createFromPath(question_to_edit.getImagePath());
        } else {
            img_drawable=getDrawable(R.drawable.img_place_holder);
        }
        question_image.setImageDrawable(img_drawable);
        question_description.setText(question_to_edit.getQuestionText());
        ans_choice0.setText(question_to_edit.getAnswers()[0].ans);
        ans_choice1.setText(question_to_edit.getAnswers()[1].ans);
        ans_choice2.setText(question_to_edit.getAnswers()[2].ans);
        ans_choice3.setText(question_to_edit.getAnswers()[3].ans);
        ans_choice0.setChecked(question_to_edit.getAnswers()[0].state);
        ans_choice1.setChecked(question_to_edit.getAnswers()[1].state);
        ans_choice2.setChecked(question_to_edit.getAnswers()[2].state);
        ans_choice3.setChecked(question_to_edit.getAnswers()[3].state);

    }
}
