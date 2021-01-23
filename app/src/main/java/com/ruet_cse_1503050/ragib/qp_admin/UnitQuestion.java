package com.ruet_cse_1503050.ragib.qp_admin;


import java.io.Serializable;

class UnitQuestion implements Serializable {

    private String ImagePath;
    private String QuestionText;
    private AnsNode[] Answers;

    UnitQuestion(){
        this.ImagePath="";
        this.QuestionText="";
        this.Answers=new AnsNode[4];
    }
    UnitQuestion(String ImagePath, String QuestionText,AnsNode[] Answers){
        this.ImagePath=ImagePath;
        this.QuestionText=QuestionText;
        this.Answers=Answers;
    }

    String getImagePath() {
        return ImagePath;
    }

    String getQuestionText() {
        return QuestionText;
    }

    AnsNode[] getAnswers() {
        return Answers;
    }

    AnsNode getAnswer(int index) { return Answers[index]; }

    void setImagePath(String imagePath) {
        this.ImagePath = imagePath;
    }

    void setQuestionText(String questionText) {
        this.QuestionText = questionText;
    }

    void setAnswers(AnsNode[] answers) {
        this.Answers = answers;
    }

    void setAnswer(int index,AnsNode ans){
        this.Answers[index]=ans;
    }


    boolean Equals(UnitQuestion Q){

        if(!this.getImagePath().equals(Q.getImagePath())) return false;
        if(!this.getQuestionText().equals(Q.getQuestionText())) return false;

        AnsNode[] myans=this.Answers;
        AnsNode[] qans=Q.getAnswers();
        for(int i=0;i<4;++i){
            if( !myans[i].ans.equals(qans[i].ans) || myans[i].state!=qans[i].state){
                return false;
            }
        }

        return true;
    }

    UnitQuestion CopyQuestionAsNew(){
        return new UnitQuestion(
                this.ImagePath,
                this.QuestionText,
                new AnsNode[]{this.Answers[0],this.Answers[1],this.Answers[2],this.Answers[3]}
        );
    }
}

