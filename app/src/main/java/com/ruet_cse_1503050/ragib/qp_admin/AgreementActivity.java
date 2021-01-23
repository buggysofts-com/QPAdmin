package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import java.io.File;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_agreement);

        getSupportActionBar().setTitle("EULA");

        Initialize();
    }

    private void Initialize() {
        InitializeAppData();
    }

    private void InitializeAppData() {
        AppDataStorage.AppDataDir=new File(getFilesDir().getAbsoluteFile()+File.separator+"AppData");
        AppDataStorage.LocalQuestionStorageDir=new File(AppDataStorage.AppDataDir.getAbsolutePath()+File.separator+"LocalQuestions");
        AppDataStorage.LocalTemporaryFilesStorageDir=new File(AppDataStorage.AppDataDir.getAbsolutePath()+File.separator+"LocalTempFiles");
        AppDataStorage.SettingsDataDir=new File(getFilesDir().getAbsoluteFile()+File.separator+"SettingsData");

        if(AppDataStorage.AppDataDir.exists() &&
                        AppDataStorage.LocalQuestionStorageDir.exists() &&
                        AppDataStorage.LocalTemporaryFilesStorageDir.exists() &&
                        AppDataStorage.SettingsDataDir.exists()){
            startActivity(new Intent(AgreementActivity.this,MainActivity.class));
            AgreementActivity.this.finish();
        } else {
            InitializeUIComponents();
        }
    }

    private void InitializeUIComponents() {
        findViewById(R.id.agree_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AgreementActivity.this,MainActivity.class),AppDataStorage.EXIT_CODE);
                AgreementActivity.this.finish();
            }
        });
        ((WebView)findViewById(R.id.agreement_text)).loadData(
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Title</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\n" +
                        "<font color=\"black\">\n" +
                        "    <p align=\"center\">This application is a property of<br><b>Department of Computer Science & Engineering<br>Rajshahi University of Engineering & Technology</b></p>\n" +
                        "</font>\n" +
                        "\n" +
                        "<br><br>\n" +
                        "\n" +
                        "<font color=\"black\">It uses the following device permissions:</font>\n" +
                        "<ul>\n" +
                        "    <li style=\"padding-left:8px\">\n" +
                        "        <font color=\"green\"><u><b>Storage Access Permission</b></u></font>\n" +
                        "        <br>\n" +
                        "        <font color=\"black\">To import/export contents from/to external or internal storage devices.</font>\n" +
                        "    </li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">\n" +
                        "        <font color=\"green\"><u><b>Wifi and Network Access Permission</b></u></font>\n" +
                        "        <br>\n" +
                        "        <font color=\"black\">To download contents via wifi (from partner desktop application - QP Creator) and access/use network services.</font>\n" +
                        "    </li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">\n" +
                        "        <font color=\"green\"><u><b>Internet Usage Permission</b></u></font>\n" +
                        "        <br>\n" +
                        "        <font color=\"black\">To access/modify your data on the database server and download/upload contents</font>\n" +
                        "    </li>\n" +
                        "</ul>\n" +
                        "\n" +
                        "<br><br>\n" +
                        "\n" +
                        "<font color=\"black\">By tapping on <b>AGREE AND CONTINUE</b>, you are agreeing on the following conditions:</font>\n" +
                        "<ul>\n" +
                        "    <li style=\"padding-left:8px\">You are not allowed to distribute this application for financial purposes.</li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">You are not allowed to make alterations to, or modifications of, the whole or any part of the application, or permit the application or any part of it to be combined with, or become incorporated in, any other programs.</li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">You are not allowed to disassemble, decompile, reverse-engineer or create derivative works based on the whole or any part of the application or attempt to do any such thing.</li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">You are not allowed to use its source code or any other components to create similar or any kind of product.</li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">You are not allowed to use this application outside the institute (RUET).</li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">We may add or remove any feature without any prior notice.</li>\n" +
                        "    <br>\n" +
                        "    <li style=\"padding-left:8px\">We have the right to remove your account and/or data without any warnings if you violate any of the conditions specified in this document or your activity with this application seems suspicious.</li>\n" +
                        "</ul>\n" +
                        "\n" +
                        "<br><br>\n" +
                        "\n" +
                        "<font color=\"red\"><b><u>PLEASE NOTE:</u></b></font>\n" +
                        "<br>\n" +
                        "<font color=\"black\">\n" +
                        "    For security reasons, this app stores information in its private storage. Uninstalling this app will remove all the data. So make sure you have exported all the data(questions) before uninstalling the app.\n" +
                        "</font>\n" +
                        "\n" +
                        "<br><br><br>\n" +
                        "\n" +
                        "<font color=\"green\">\n" +
                        "    <p align=\"center\"><b>THANK YOU FOR USING THIS APPLICATION</b></p>\n" +
                        "</font>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>",
                "text/html",
                null
        );
    }
}
