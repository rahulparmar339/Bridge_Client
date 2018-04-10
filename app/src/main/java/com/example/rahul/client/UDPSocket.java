package com.example.rahul.client;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by rahul on 19-Feb-18.
 */

public class UDPSocket {

    private static final String BROADCAST_IP_ADDRESS = "255.255.255.255";
    private static final int CLIENT_PORT_NO = 9999;
    private static final int SERVER_PORT_NO = 4445;

    private  DatagramSocket socket = null;

    public UDPSocket(){
        try {
            socket = new DatagramSocket(CLIENT_PORT_NO);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String msg){
        byte[] sendBuffer = msg.getBytes();
        DatagramPacket sendPacket =null;

        try {
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(BROADCAST_IP_ADDRESS), SERVER_PORT_NO);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBroadcast(boolean val){
        try {
            socket.setBroadcast(val);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public String receive(){
        byte[] recvBuffer = new byte[1000];
        DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
        try {
            socket.receive(recvPacket);
            return new String(recvPacket.getData()).trim();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close(){
        socket.close();
    }

    public void setSoTimeout(int time){
        try {
            socket.setSoTimeout(time);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
