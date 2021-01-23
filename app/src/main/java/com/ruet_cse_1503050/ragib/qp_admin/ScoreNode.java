package com.ruet_cse_1503050.ragib.qp_admin;

import java.io.Serializable;

class ScoreNode implements Serializable {
    String key;
    String value;
    ScoreNode(String key,String value){
        this.key=key;
        this.value=value;
    }
}