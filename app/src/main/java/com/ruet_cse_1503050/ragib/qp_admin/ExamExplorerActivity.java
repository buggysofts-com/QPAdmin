package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ExamExplorerActivity extends AppCompatActivity {

    private ListView exam_list;
    private TextView load_indicator;
    private ExamExplorerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_exam_explorer);

        Initialize();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final AdapterView.AdapterContextMenuInfo selected_node_info= ((AdapterView.AdapterContextMenuInfo) menuInfo);
        getMenuInflater().inflate(R.menu.exam_context_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Toast.makeText(ExamExplorerActivity.this, "Collecting results. Please wait...", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                            final ExamInfoNode examInfoNode = adapter.getItem(selected_node_info.position);
                            final ResultData results=new ResultData(examInfoNode.exam_id);
                            final DatabaseReference question_ref = AppDataStorage.base_ref
                                    .child("questions")
                                    .child(examInfoNode.exam_id);
                            final DatabaseReference result_ref = AppDataStorage.base_ref
                                    .child("results")
                                    .child(examInfoNode.exam_id);

                            result_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Iterator<DataSnapshot> iterator=dataSnapshot.getChildren().iterator();
                                    while (iterator.hasNext()){
                                        DataSnapshot current_pair=iterator.next();
                                        results.scores.add(new ScoreNode(current_pair.getKey(),current_pair.child("score").getValue(String.class)));
                                    }
                                    if(results.scores.size()>0){
                                        question_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String exam_title = dataSnapshot.child("title").getValue(String.class);
                                                String course_code = dataSnapshot.child("course_code").getValue(String.class);
                                                String course_title = dataSnapshot.child("course_title").getValue(String.class);
                                                Intent result_intent=new Intent(ExamExplorerActivity.this,ExamResultActivity.class);
                                                result_intent.putExtra("result_list",results);
                                                result_intent.putExtra("exam_title",exam_title);
                                                result_intent.putExtra("course_code",course_code);
                                                result_intent.putExtra("course_title",course_title);
                                                startActivity(result_intent);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                        });
                                    } else {
                                        Toast.makeText(ExamExplorerActivity.this, "Result is empty...", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ExamExplorerActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(UtilCollections.isConnectedToInternet(getApplicationContext())){
                            final ExamInfoNode node=adapter.getItem(selected_node_info.position);
                            AppDataStorage.base_ref
                                    .child("questions").child(node.exam_id).child("accessible")
                                    .setValue(node.accessible.equals("true")? "false":"true")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        node.accessible=node.accessible.equals("true")? "false":"true";
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ExamExplorerActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
                return true;
            }
        });
        menu.getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(UtilCollections.isConnectedToInternet(getApplicationContext())){
                            final AlertDialog.Builder builder=new AlertDialog.Builder(ExamExplorerActivity.this);
                            builder.setMessage("This will delete the exam entry along with the associated results. Are you really sure to remove this ?");
                            builder.setNegativeButton("Cancel",null);
                            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final ExamInfoNode node=adapter.getItem(selected_node_info.position);
                                    AppDataStorage.base_ref.child("questions").child(node.exam_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task1) {
                                            if(task1.isSuccessful()){
                                                AppDataStorage.base_ref.child("results").child(node.exam_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task2) {
                                                        if(task2.isSuccessful()){
                                                            AppDataStorage.userMetaData.getScheduledExamsMetaData().child(node.pointer).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task3) {
                                                                    if(task3.isSuccessful()){
                                                                        adapter.remove(node);
                                                                        adapter.notifyDataSetChanged();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    builder.show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ExamExplorerActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
                return true;
            }
        });
    }

    private void Initialize() {
        InitializeUIComponents();
        InitializeAppData();
    }

    private void InitializeUIComponents() {
        exam_list=findViewById(R.id.exam_list);
        load_indicator=findViewById(R.id.load_indicator);
        registerForContextMenu(exam_list);
    }

    private void InitializeAppData() {

        adapter=new ExamExplorerAdapter(ExamExplorerActivity.this,R.layout.exam_node,new ArrayList<ExamInfoNode>(0));
        exam_list.setAdapter(adapter);
        load_indicator.setText("Loading...");

        new Thread(new Runnable() {
            @Override
            public void run() {

                if(UtilCollections.isConnectedToInternet(getApplicationContext())){
                    AppDataStorage.userMetaData.getScheduledExamsMetaData().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot1) {

                            final Iterator<DataSnapshot> datas=dataSnapshot1.getChildren().iterator();

                            if(!datas.hasNext()){
                                load_indicator.setText("Empty");
                            }

                            while (datas.hasNext()){

                                final DataSnapshot current_question_ref=datas.next();
                                final String key=current_question_ref.getKey();
                                final String val=current_question_ref.getValue(String.class);

                                AppDataStorage.base_ref.child("questions").child(val).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot2) {
                                        adapter.add(
                                                new ExamInfoNode(
                                                        key,val,
                                                        dataSnapshot2.child("title").getValue(String.class),
                                                        dataSnapshot2.child("password").getValue(String.class),
                                                        dataSnapshot2.child("duration").getValue(String.class),
                                                        dataSnapshot2.child("accessible").getValue(String.class),
                                                        dataSnapshot2.child("qpack_ref_path").getValue(String.class)
                                                )
                                        );
                                        adapter.notifyDataSetChanged();
                                        if(!datas.hasNext()){
                                            load_indicator.setText(adapter.getCount()>0?"":"Empty");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExamExplorerActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                            load_indicator.setText("Not connected to internet");
                        }
                    });
                }
            }
        }).start();
    }
}
