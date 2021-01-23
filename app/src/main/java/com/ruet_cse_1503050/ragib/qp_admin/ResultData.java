package com.ruet_cse_1503050.ragib.qp_admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class ResultData implements Serializable {
    String ID;
    List<ScoreNode> scores=new ArrayList<>(0);
    ResultData(String ID){
        this.ID=ID;
    }
}
