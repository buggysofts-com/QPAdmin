package com.ruet_cse_1503050.ragib.qp_admin;

class OnlineFileMetaDataNode {
    String key;
    String name;
    String size;
    String ref_path;
    OnlineFileMetaDataNode(String key,String name,String size,String ref_path){
        this.key=key;
        this.name=name;
        this.size=size;
        this.ref_path=ref_path;
    }
}
