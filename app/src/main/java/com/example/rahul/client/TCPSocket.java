package com.example.rahul.client;


import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by rahul on 19-Feb-18.
 */

public class TCPSocket {
    public  Socket socket = null;

    private static OutputStream outputStream = null;
    private static PrintWriter printWriter = null;

    private static InputStream inputStream = null;
    private static InputStreamReader inputStreamReader = null;
    private static BufferedReader bufferedReader = null;


    public TCPSocket(String serverIP, int serverPortNo){
        try {
            socket = new Socket(serverIP,serverPortNo);

            outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);

            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg){
        printWriter.println(msg);
        printWriter.flush();
    }

    public String receive(){
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeSocket(){
        try {
            printWriter.close();
            outputStream.close();

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
