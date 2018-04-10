package com.example.rahul.client;

import android.util.Log;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by rahul on 19-Feb-18.
 */

public class Client {
    private static Client client = null;
    private static String name = null;
    private static ArrayList<Server> servers;
    private static int tableNo;
    private static int seatNo;
    private static UDPSocket udpSocket = null;
    private static TCPSocket tcpSocket = null;

    public static Client getInstance(){
        if(client == null){
            client = new Client();
        }
        return client;
    }
    public Client(){
        servers = new ArrayList<Server>();
    }

    public void setUdpSocket(){
        udpSocket = new UDPSocket();
    }
    public UDPSocket getUdpSocket(){
        return udpSocket;
    }

    public void setTcpSocket(String serverIP, int serverPortNo){
        tcpSocket = new TCPSocket(serverIP,serverPortNo);
    }
    public TCPSocket getTcpSocket(){
        return tcpSocket;
    }

    public void addServer(Server server){
        servers.add(server);
    }
    public Server getServer(int index){
            return servers.get(index);
    }
    public Server getLastServer() { return servers.get(servers.size()-1); }
    public ArrayList<Server> getServers(){
        return servers;
    }
    public int getServersSize(){
        return servers.size();
    }
    public void clearServers(){
        servers.clear();
    }

    public void setTableNo(int no){
        tableNo = no;
    }
    public int getTableNo(){
        return tableNo;
    }
    public void setSeatNo(int no){
        seatNo = no;
    }
    public  int getSeatNo(){
        return seatNo;
    }

}
