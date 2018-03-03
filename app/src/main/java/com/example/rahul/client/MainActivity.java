package com.example.rahul.client;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSeatNoSpinner();

        client = Client.getInstance();
        client.setUdpSocket();
        displayServerList();
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
       client.clearServers();

       Thread udpThread = new Thread(new Runnable() {
           @Override
           public void run() {

           client.getUdpSocket().setBroadcast(true);

           WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
           String clientIpAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
           String clientData = "client1 " + clientIpAddress;
           client.getUdpSocket().broadcast(clientData);

           Log.e("sucess","broadcasted : "+clientData);

           while(true) {
               String serverData[] = client.getUdpSocket().receive().split(" ");
               String serverName = serverData[0];
               String serverIpAddress = serverData[1];
               client.addServer(new Server(serverName, serverIpAddress));

               Log.e("sucess", "server replied: " + serverName + " " + serverIpAddress);
           }

           }
       });
        udpThread.start();
    }


    public void onClickConnectServer(View view){

        final String serverIP = client.getServer(0).getIP();

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


    public void displayServerList(){

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.serverListRelativeLayout);

        Random rnd = new Random();
        int prevTextViewId = 0;

        for(int i = 0; i < 10; i++)
        {
            final TextView textView = new TextView(this);
            textView.setText("Text "+i);
            textView.setTextColor(rnd.nextInt() | 0xff000000);
            //textView.setTextSize(20);


            int curTextViewId = prevTextViewId + 1;
            textView.setId(curTextViewId);
            final RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(250,100);

            params.addRule(RelativeLayout.BELOW, prevTextViewId);
            textView.setLayoutParams(params);

            prevTextViewId = curTextViewId;
            layout.addView(textView, params);


            //Log.e("Height :"," "+textView.getHeight());
            //Log.e("Width  :"," "+textView.getWidth());

        }
    }

}
