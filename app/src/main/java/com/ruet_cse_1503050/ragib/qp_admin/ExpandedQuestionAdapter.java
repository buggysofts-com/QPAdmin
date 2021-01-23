package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

public class ExpandedQuestionAdapter extends ArrayAdapter<UnitQuestion> {

    private int layout_id;
    private int source_id; // 1 - NewQuestionActivity , 2 - ExpandQuestionActivity

    ExpandedQuestionAdapter(@NonNull Context context, int resource, @NonNull List<UnitQuestion> objects,int source_id) {
        super(context, resource, objects);
        this.layout_id=resource;
        this.source_id=source_id;
    }

    final class QuestionViewer{
        ImageView question_image;
        TextView question_description;
        CheckBox ans_choice0;
        CheckBox ans_choice1;
        CheckBox ans_choice2;
        CheckBox ans_choice3;
        ImageButton delete_question;
        ImageButton edit_question;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        QuestionViewer holder;
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(layout_id,null);
            holder=new QuestionViewer();
            holder.question_image=convertView.findViewById(R.id.question_image);
            holder.question_description=convertView.findViewById(R.id.question_description);
            holder.ans_choice0=convertView.findViewById(R.id.ans_choice0); holder.ans_choice0.setEnabled(false);
            holder.ans_choice1=convertView.findViewById(R.id.ans_choice1); holder.ans_choice1.setEnabled(false);
            holder.ans_choice2=convertView.findViewById(R.id.ans_choice2); holder.ans_choice2.setEnabled(false);
            holder.ans_choice3=convertView.findViewById(R.id.ans_choice3); holder.ans_choice3.setEnabled(false);
            holder.delete_question=convertView.findViewById(R.id.delete_question);
            holder.edit_question=convertView.findViewById(R.id.edit_question);
            convertView.setTag(holder);
        } else {
            holder=(QuestionViewer) convertView.getTag();
        }

        final UnitQuestion question=getItem(position);

        holder.delete_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(question);
            }
        });
        holder.edit_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (source_id){
                    case 1:{
                        Intent intent=new Intent(getContext(),EditQuestionActivity.class);
                        intent.putExtra("passed_question",question);
                        intent.putExtra("passed_index",position);
                        ((NewQuestionActivity) getContext()).startActivityForResult(intent,AppDataStorage.EDIT_UNIT_QUESTION_ACTIVITY_CODE);
                        break;
                    }
                    case 2:{
                        Intent intent=new Intent(getContext(),EditQuestionActivity.class);
                        intent.putExtra("passed_question",question);
                        intent.putExtra("passed_index",position);
                        ((ExpandQuestionActivity) getContext()).startActivityForResult(intent,AppDataStorage.EDIT_UNIT_QUESTION_ACTIVITY_CODE);
                        break;
                    }
                }
            }
        });

        holder.question_description.setText(question.getQuestionText());
        holder.ans_choice0.setText(question.getAnswers()[0].ans);
        holder.ans_choice1.setText(question.getAnswers()[1].ans);
        holder.ans_choice2.setText(question.getAnswers()[2].ans);
        holder.ans_choice3.setText(question.getAnswers()[3].ans);
        holder.ans_choice0.setChecked(question.getAnswers()[0].state);
        holder.ans_choice1.setChecked(question.getAnswers()[1].state);
        holder.ans_choice2.setChecked(question.getAnswers()[2].state);
        holder.ans_choice3.setChecked(question.getAnswers()[3].state);

        new QuestionImageLoader(getContext(),holder.question_image,question).execute();

        return convertView;

    }
}
