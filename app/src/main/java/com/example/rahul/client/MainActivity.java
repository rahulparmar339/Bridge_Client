package com.example.rahul.client;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //public ArrayList<String> serverIpAddress;
    Client client = null;
    Handler handler = new Handler();
    int selectedServer = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSeatNoSpinner();

        client = Client.getInstance();
        client.setUdpSocket();

    }
    public void setSeatNoSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.seatNoSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.seatNo_Array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }


    public void onClickFindServer(View view){
        Thread udpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                client.clearServers();
                clearServerList();
                toggleFindServerButton(false);

                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                String clientIpAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                String clientData = "client1 " + clientIpAddress;

                client.getUdpSocket().setBroadcast(true);
                client.getUdpSocket().broadcast(clientData);
                client.getUdpSocket().setSoTimeout(5000);

                Log.e("sucess","broadcasted : "+clientData);

                int serverCount = 1;
                while(true) {
                    String receivedMsg = client.getUdpSocket().receive();

                    //5 second has passed so returned null msg from UDPSocket class
                    if(receivedMsg == null){
                        toggleFindServerButton(true);
                        break;
                    }
                    String serverData[] = receivedMsg.split(" ");
                    String serverName = serverData[0];
                    String serverIpAddress = serverData[1];
                    client.addServer(new Server(serverName, serverIpAddress));
                    displayServerList(serverName, serverIpAddress, serverCount++);

                    Log.e("sucess", "server replied: " + serverName + " " + serverIpAddress);
                }
            }
        });
        udpThread.start();
    }


    public void onClickConnectServer(View view){

        final String serverIP = client.getServer(selectedServer-1).getIP();
        final EditText tableNoEditText= findViewById(R.id.tableNoEditText);
        final int tableNo = Integer.parseInt(tableNoEditText.getText().toString())-1;

        final Spinner seatNoSpinner = findViewById(R.id.seatNoSpinner);
        final int seatNo = seatNoSpinner.getSelectedItemPosition(); // North East South West

        Thread tcpThread = new Thread(new Runnable() {
            @Override
            public void run() {

                client.setTcpSocket(serverIP,9002);
                client.getTcpSocket().send(tableNo+" "+seatNo);

                final String msg = client.getTcpSocket().receive();
                Log.e("Server:","replied "+msg);

                if(msg.compareTo("sucess")==0){
                    client.setTableNo(tableNo);
                    client.setSeatNo(seatNo);
                    goToGameActivity();
                }
            }
        });

        tcpThread.start();

    }

    public void goToGameActivity(){
        Intent intent = new Intent(this,GameActivity.class);
        startActivity(intent);
    }


    public void displayServerList(String serverName, String serverIp, final int serverCount){
        handler.post(new Runnable() {
            @Override
            public void run() {

                final RelativeLayout layout = (RelativeLayout)findViewById(R.id.serverListRelativeLayout);
                String serverName = client.getLastServer().getName();
                String serverIp = client.getLastServer().getIP();

                Log.e("check",serverName+" "+serverIp+" "+serverCount);
                final Button Button = new Button(MainActivity.this);
                Button.setText(serverName+ " "+ serverIp);
                Button.setTextColor(Color.BLACK);
                Button.setBackgroundColor(Color.RED);
                Button.setId(serverCount);
                Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(selectedServer != -1){
                            layout.getChildAt(selectedServer-1).setBackgroundColor(Color.RED);
                        }
                        v.setBackgroundColor(Color.GREEN);
                        selectedServer = v.getId();
                    }
                });

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layout.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
                if(serverCount!=1)
                    params.addRule(RelativeLayout.BELOW, serverCount-1);
                else
                    params.addRule(RelativeLayout.BELOW);

                Button.setLayoutParams(params);
                layout.addView(Button, params);

            }
        });

    }

    public void clearServerList(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.serverListRelativeLayout);
                layout.removeAllViews();
            }
        });
    }

    public void toggleFindServerButton(final boolean state){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Button findServerButton = (Button) findViewById(R.id.findServerButton);
                findServerButton.setEnabled(state);
            }
        });
    }
}
