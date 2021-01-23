package com.ruet_cse_1503050.ragib.qp_admin;

import java.io.Serializable;

public class FileInfoNode implements Serializable {
    String file_name;
    long file_length;
    FileInfoNode(String file_name,long file_length){
        this.file_name=file_name;
        this.file_length=file_length;
    }
    FileInfoNode(){}
}
