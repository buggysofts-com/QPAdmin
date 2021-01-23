package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExamResultActivity extends AppCompatActivity {

    private ListView result_list;
    private ResultListAdapter init_adapter;
    private ResultData result;
    private String exam_title;
    private String course_code;
    private String course_title;

    private class SearchMode implements ActionMode.Callback {

        private InputMethodManager imm;
        private List<ScoreNode> search_list;

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            search_list=new ArrayList<>(0);

            View view=getLayoutInflater().inflate(R.layout.search_layout,null);
            EditText search_txt=view.findViewById(R.id.search_txt);
            search_txt.setHint("Search by Roll...");
            search_txt.setHintTextColor(Color.parseColor("#AAAAAA"));
            search_txt.setBackgroundColor(getResources().getColor(R.color.color_white));
            search_txt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    search_list.clear();
                    int len=init_adapter.getCount();
                    for(int i=0;i<len;++i){
                        if(init_adapter.getItem(i).key.contains(s.toString())){
                            search_list.add(init_adapter.getItem(i));
                        }
                    }
                    result_list.setAdapter(
                            new ResultListAdapter(
                                    ExamResultActivity.this,
                                    R.layout.result_mark_node,
                                    search_list,true,
                                    s.toString()
                            )
                    );
                }
            });
            actionMode.setCustomView(view);
            search_txt.requestFocus();
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(search_txt, InputMethodManager.SHOW_IMPLICIT);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            View view = getCurrentFocus();
            if (view != null && imm!=null && imm.isActive()) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            result_list.setAdapter(init_adapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_exam_result);

        Initiaize();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.marks_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startSupportActionMode(new SearchMode());
                return true;
            }
        });

        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent=new Intent(ExamResultActivity.this,ExportResultActivity.class);
                intent.putExtra("result_list",result);
                intent.putExtra("exam_title",exam_title);
                intent.putExtra("course_code",course_code);
                intent.putExtra("course_title",course_title);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        getMenuInflater().inflate(R.menu.detailed_ans_script_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Toast.makeText(ExamResultActivity.this, "Collecting script data. Please wait...", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(UtilCollections.isConnectedToInternet(ExamResultActivity.this)){
                            AppDataStorage.base_ref.child("results")
                                    .child(result.ID).child(init_adapter.getItem(info.position).key)
                                    .child("script").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Intent intent=new Intent(ExamResultActivity.this,AnswerScriptActivity.class);
                                    intent.putExtra("passed_html",dataSnapshot.getValue(String.class));
                                    intent.putExtra("roll",init_adapter.getItem(info.position).key);
                                    startActivity(intent);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ExamResultActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
                return true;
            }
        });
    }

    private void Initiaize() {
        InitiaizeActivityData();
        InitiaizeUIComponents();
        InitiaizeUIComponentsData();
    }

    private void InitiaizeActivityData() {
        result=((ResultData) getIntent().getSerializableExtra("result_list"));
        exam_title=getIntent().getStringExtra("exam_title");
        course_code=getIntent().getStringExtra("course_code");
        course_title=getIntent().getStringExtra("course_title");
    }

    private void InitiaizeUIComponents() {
        result_list=findViewById(R.id.result_list);
        registerForContextMenu(result_list);
    }

    private void InitiaizeUIComponentsData() {
        Sort(result.scores);
        init_adapter=new ResultListAdapter(
                ExamResultActivity.this,
                R.layout.result_mark_node,result.scores,false,null
        );
        result_list.setAdapter(init_adapter);
    }

    private void Sort(List<ScoreNode> list){
        Sort(list,0,list.size()-1);
    }

    private void Sort(List<ScoreNode> list,int start,int end){
        if(start<end){
            int partition_index=Partition(list,start,end);
            Sort(list,start,partition_index-1);
            Sort(list,partition_index+1,end);
        }
    }

    private int Partition(List<ScoreNode> list, int start, int end) {
        int p_index=start;
        Swap(list.get(end),list.get((start+end)/2));
        String pivot_val=list.get(end).key;
        for(int i=start;i<end;++i){
            if(list.get(i).key.compareTo(pivot_val)<0){
                Swap(list.get(i),list.get(p_index));
                ++p_index;
            }
        }
        Swap(list.get(end),list.get(p_index));
        return p_index;
    }

    private void Swap(ScoreNode p1,ScoreNode p2){
        ScoreNode tmp=new ScoreNode(p1.key,p1.value);
        p1=p2;
        p2=tmp;
    }
}
