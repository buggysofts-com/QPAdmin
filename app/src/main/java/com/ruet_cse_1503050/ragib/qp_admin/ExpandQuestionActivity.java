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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExpandQuestionActivity extends AppCompatActivity {

    private ListView expanded_question_list;
    private ExpandedQuestionAdapter adapter;
    private TextView load_indicator;
    private File target_qpack_file;

    private List<UnitQuestion> original_questions;
    private List<UnitQuestion> temporary_questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_expand_question);
        Initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.expanded_question_toolbar_menu,menu);

        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivityForResult(new Intent(ExpandQuestionActivity.this,NewUnitQuestionActivity.class),AppDataStorage.NEW_UNIT_QUESTION_ACTIVITY_CODE);
                return true;
            }
        });
        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(needsSaving()){

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    final View view=getLayoutInflater().inflate(R.layout.load_dlg,null);
                                    final TextView progress_desc=view.findViewById(R.id.progress_desc);
                                    final AlertDialog[] dialog={null};
                                    final AlertDialog.Builder[] builder={new AlertDialog.Builder(ExpandQuestionActivity.this)};

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            builder[0].setCancelable(false);
                                            progress_desc.setText("Saving...");
                                            builder[0].setView(view);
                                            dialog[0]=builder[0].show();
                                        }
                                    });

                                    try {
                                        UtilCollections.SaveQuestion(
                                                target_qpack_file,
                                                temporary_questions,
                                                true
                                        );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setResult(RESULT_OK);
                                            dialog[0].dismiss();
                                            finish();
                                        }
                                    });
                                }
                            }).start();

                        } else {
                            finish();
                        }
                    }
                }).start();

                return true;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if(needsSaving()){
            AlertDialog.Builder alert_builder = new AlertDialog.Builder(ExpandQuestionActivity.this);
            alert_builder.setMessage("Do you want to save changes?");
            alert_builder.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    expanded_question_list.setEnabled(false);
                                    Toast.makeText(ExpandQuestionActivity.this, "Saving question...", Toast.LENGTH_SHORT).show();
                                }
                            });

                            try {
                                UtilCollections.SaveQuestion(target_qpack_file,temporary_questions,true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });

                        }
                    }).start();
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
                case AppDataStorage.EDIT_UNIT_QUESTION_ACTIVITY_CODE:{
                    UnitQuestion returned_question=(UnitQuestion) data.getSerializableExtra("returned_question");
                    int returned_index=data.getIntExtra("returned_index",0);
                    temporary_questions.set(returned_index,returned_question);
                    adapter.notifyDataSetChanged();
                    break;
                }
                case AppDataStorage.NEW_UNIT_QUESTION_ACTIVITY_CODE:{
                    UnitQuestion passed_question=(UnitQuestion) data.getSerializableExtra("returned_question");
                    adapter.add(passed_question);
                }
            }
        }
    }

    private void Initialize() {
        InitializeActivityData();
        InitializeUIComponents();
        InitializeUIComponentsData();
    }

    private void InitializeActivityData() {
        target_qpack_file=new File(getIntent().getStringExtra("target_qpack_file_path"));

    }

    private void InitializeUIComponents() {
        load_indicator=findViewById(R.id.loader_indicator);
        expanded_question_list=findViewById(R.id.expanded_question_list);
    }

    private void InitializeUIComponentsData() {
        new Thread(new Runnable() {

            View view=null;
            TextView progress_desc=null;
            AlertDialog alert=null;

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view=getLayoutInflater().inflate(R.layout.load_dlg,null);
                        progress_desc=view.findViewById(R.id.progress_desc);
                        progress_desc.setText("Opening Question Paper...");
                        alert=
                                new AlertDialog.Builder(ExpandQuestionActivity.this)
                                        .setView(view)
                                        .setCancelable(false)
                                        .show();
                    }
                });

                original_questions=UtilCollections.DecodeQuestionAsList(target_qpack_file);
                temporary_questions=new ArrayList<>(original_questions.size());
                for(int i=0;i<original_questions.size();++i){
                    temporary_questions.add(i,original_questions.get(i));
                }
                adapter=new ExpandedQuestionAdapter(
                        ExpandQuestionActivity.this,
                        R.layout.expanded_question_node,
                        temporary_questions,
                        2
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        expanded_question_list.setAdapter(adapter);
                        load_indicator.setText(adapter.getCount()!=0 ? "":"Empty");
                        alert.dismiss();
                    }
                });

            }
        }).start();

    }

    private boolean needsSaving(){
        boolean ret=false;
        if(temporary_questions.size()!=original_questions.size()) {
            ret=true;
        } else{
            for(int i=0;i<temporary_questions.size();++i){
                if(!temporary_questions.get(i).Equals(original_questions.get(i))){
                    ret=true;
                    break;
                }
            }
        }
        return ret;
    }
}
