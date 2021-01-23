package com.ruet_cse_1503050.ragib.qp_admin;

class ExamInfoNode {
    String pointer;
    String exam_id;
    String title;
    String password;
    String duration;
    String accessible;
    String qpack_ref_path;
    ExamInfoNode(String pointer,String exam_id,String title,String password,String duration,String accessible,String qpack_ref_path){
        this.pointer=pointer;
        this.exam_id=exam_id;
        this.title=title;
        this.password=password;
        this.duration=duration;
        this.accessible=accessible;
        this.qpack_ref_path=qpack_ref_path;
    }
}
