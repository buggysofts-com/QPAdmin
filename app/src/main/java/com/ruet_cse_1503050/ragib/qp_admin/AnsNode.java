package com.ruet_cse_1503050.ragib.qp_admin;

import java.io.Serializable;

class AnsNode implements Serializable {
    String ans;
    boolean  state;
    AnsNode(){}
    AnsNode(String ans,boolean state){
        this.ans=ans;
        this.state=state;
    }
}
