package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewQuestionActivity extends AppCompatActivity {

    private ListView question_list;
    private EditText file_name;
    private ImageButton save_btn;

    private List<UnitQuestion> questions;

    private ExpandedQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_new_question);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.import_toolbar_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivityForResult(new Intent(NewQuestionActivity.this,NewUnitQuestionActivity.class),AppDataStorage.NEW_UNIT_QUESTION_ACTIVITY_CODE);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if(needsSaving()){
            AlertDialog.Builder alert_builder = new AlertDialog.Builder(NewQuestionActivity.this);
            alert_builder.setMessage("Do you want to save the question?");
            alert_builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(file_name.getText().toString().isEmpty()){
                        Toast.makeText(NewQuestionActivity.this, "Please specify a file name", Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                final View view=getLayoutInflater().inflate(R.layout.load_dlg,null);
                                final TextView progress_desc=view.findViewById(R.id.progress_desc);
                                final AlertDialog[] dialog={null};
                                final AlertDialog.Builder[] builder={new AlertDialog.Builder(NewQuestionActivity.this)};

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        builder[0].setCancelable(false);
                                        progress_desc.setText("Saving...");
                                        builder[0].setView(view);
                                        dialog[0]=builder[0].show();
                                    }
                                });


                                File file_to_save=new File(
                                        AppDataStorage.LocalQuestionStorageDir.getAbsolutePath()+
                                                File.separator+file_name.getText().toString()+".qpack"
                                );

                                try {
                                    UtilCollections.SaveQuestion(
                                            file_to_save,
                                            questions,
                                            false
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog[0].dismiss();
                                        finish();
                                    }
                                });

                            }
                        }).start();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case AppDataStorage.NEW_UNIT_QUESTION_ACTIVITY_CODE:{
                    UnitQuestion returned_question = ((UnitQuestion) data.getSerializableExtra("returned_question"));
                    adapter.add(returned_question);
                    adapter.notifyDataSetChanged();
                    break;
                }
                case AppDataStorage.EDIT_UNIT_QUESTION_ACTIVITY_CODE:{
                    UnitQuestion returned_question=(UnitQuestion) data.getSerializableExtra("returned_question");
                    int returned_index=data.getIntExtra("returned_index",0);
                    questions.set(returned_index,returned_question);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void Initialize() {
        InitiializeActivityData();
        InitializeUIComponents();
        InitializeUIComponentsData();
    }

    private void InitiializeActivityData() {
    }

    private void InitializeUIComponents() {
        question_list=findViewById(R.id.question_list);
        file_name=findViewById(R.id.file_name);
        save_btn=findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(file_name.getText().toString().isEmpty()){
                    Toast.makeText(NewQuestionActivity.this, "Please specify a file name", Toast.LENGTH_SHORT).show();
                } else {
                    if(needsSaving()){

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                final View view=getLayoutInflater().inflate(R.layout.load_dlg,null);
                                final TextView progress_desc=view.findViewById(R.id.progress_desc);
                                final AlertDialog[] dialog={null};
                                final AlertDialog.Builder[] builder={new AlertDialog.Builder(NewQuestionActivity.this)};

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        builder[0].setCancelable(false);
                                        progress_desc.setText("Saving...");
                                        builder[0].setView(view);
                                        dialog[0]=builder[0].show();
                                    }
                                });


                                File file_to_save=new File(
                                        AppDataStorage.LocalQuestionStorageDir.getAbsolutePath()+
                                                File.separator+file_name.getText().toString()+".qpack"
                                );
                                try {
                                    UtilCollections.SaveQuestion(
                                            file_to_save,
                                            questions,
                                            false
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog[0].dismiss();
                                        finish();
                                    }
                                });

                            }
                        }).start();
                    } else {
                        Toast.makeText(NewQuestionActivity.this, "Empty Questions can not be saved!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void InitializeUIComponentsData() {
        adapter=new ExpandedQuestionAdapter(
                NewQuestionActivity.this,R.layout.expanded_question_node,
                questions=new ArrayList<UnitQuestion>(0),
                1
        );
        question_list.setAdapter(adapter);
    }

    private boolean needsSaving(){
        return (questions.size()>0);
    }
}
