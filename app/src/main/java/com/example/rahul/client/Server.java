package com.example.rahul.client;

/**
 * Created by rahul on 19-Feb-18.
 */

public class Server {
    private String name = null;
    private String IP = null;

    public Server(String name, String IP){
        this.name = name;
        this.IP = IP;
    }

    public String getName(){
        return this.name;
    }

    public String getIP(){
        return this.IP;
    }
}
