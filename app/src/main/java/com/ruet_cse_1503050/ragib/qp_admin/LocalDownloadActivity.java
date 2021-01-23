package com.ruet_cse_1503050.ragib.qp_admin;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class LocalDownloadActivity extends AppCompatActivity {

    private TextView connect_help_txt;
    private TextView ip_txt;
    private ProgressBar rcv_progress;
    private TextView rcving_file_name;

    private ServerSocket serverSocket;
    private Socket socket;
    private DatagramSocket info_socket;
    private DatagramPacket info_packet;
    private BufferedInputStream buff_in;
    private BufferedOutputStream buff_out;

    private boolean exit_all=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionBar);
        setContentView(R.layout.activity_local_download);

        Initialize();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CloseAll();
        exit_all=true;
    }

    private void Initialize(){
        InitializeUIComponents();
        InitializeUIComponentsData();
        InitializeConnections();
    }

    private void InitializeUIComponents() {
        connect_help_txt=findViewById(R.id.connect_help_txt);
        ip_txt=findViewById(R.id.ip_txt);
        rcv_progress=findViewById(R.id.rcv_progress);
        rcving_file_name=findViewById(R.id.rcving_file_name);
    }

    private void InitializeUIComponentsData() {
        WifiManager manager=(WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        connect_help_txt.setText("Waiting to be connected as");
        ip_txt.setText(Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress()));
        rcv_progress.setIndeterminate(true);
    }

    private void InitializeConnections() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    serverSocket=new ServerSocket(1817);
                    serverSocket.setSoTimeout(5000);
                    while (!exit_all){
                        try{
                            socket=serverSocket.accept();
                            break;
                        }catch (SocketTimeoutException e){
                            continue;
                        }
                    }

                    info_socket=new DatagramSocket(4921);
                    info_socket.setSoTimeout(5000);
                    info_packet=new DatagramPacket(new byte[256],256);
                    final String[] file_name = {null};
                    final long[] file_length = {0};
                    boolean response_available=true;
                    try{
                        info_socket.receive(info_packet);
                        String tmp=new String(info_packet.getData(),info_packet.getOffset(),info_packet.getLength());
                        file_name[0]=tmp.substring(0,tmp.indexOf('/'));
                        file_length[0]=Long.parseLong(tmp.substring(tmp.indexOf('/')+1));
                    }catch (SocketTimeoutException e){
                        e.printStackTrace();
                        response_available=false;
                    }

                    if(response_available){

                        final boolean[] modify_progress={false};
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                rcving_file_name.setText("Receiving: "+file_name[0]);
                                if(file_length[0]<Integer.MAX_VALUE){
                                    rcv_progress.setIndeterminate(false);
                                    rcv_progress.setMax(((int) file_length[0]));
                                    modify_progress[0]=true;
                                }
                            }
                        });

                        try{
                            buff_in=new BufferedInputStream(socket.getInputStream());
                            buff_out=new BufferedOutputStream(
                                    new FileOutputStream(
                                            AppDataStorage.LocalQuestionStorageDir.getAbsolutePath()+
                                                    File.separator+file_name[0]
                                    )
                            );
                            final long[] total_rcvd={0};
                            final int[] read_num={0};
                            final byte[] data=new byte[131072];
                            while ((read_num[0]=buff_in.read(data))!=-1){
                                buff_out.write(data,0,read_num[0]);
                                total_rcvd[0]+=read_num[0];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(modify_progress[0]){
                                            rcv_progress.setProgress((int)(total_rcvd[0]));
                                        }
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LocalDownloadActivity.this, "Something went wrong, Please retry", Toast.LENGTH_LONG).show();
                                    CloseAll();
                                    finish();
                                }
                            });
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LocalDownloadActivity.this, file_name[0]+" received successfully", Toast.LENGTH_LONG).show();
                                CloseAll();
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LocalDownloadActivity.this, "Other end is not responding. Closing connection...", Toast.LENGTH_LONG).show();
                                CloseAll();
                                finish();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void CloseAll(){
        try {
            if(serverSocket!=null){
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(socket!=null){
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(info_socket!=null){
                info_socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(buff_out!=null){
                buff_out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(buff_in!=null){
                buff_in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
