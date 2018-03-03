package com.example.rahul.client;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    Client client = null;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        client = Client.getInstance();
        startGame();
    }

    public void startGame(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> cards = receiveCards();
                displayCards(cards);
            }
        });
        thread.start();
        displayBiddingBox();
    }

    public ArrayList<Integer> receiveCards(){
        ArrayList<Integer> cards = new ArrayList<>();
        String cardsString = client.getTcpSocket().receive();
        String cardsStringArray[] = cardsString.substring(1, cardsString.length()-1).split(", ");

        for(int i=0; i<cardsStringArray.length; i++){
            cards.add(Integer.parseInt(cardsStringArray[i]));
        }
        return cards;
    }

    public void displayBiddingBox(){

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.relativeLayout);

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int buttonSize = (int) (displayWidth*(0.1));
        int leftMargin = (int) (displayWidth*(0.25));
        int topMargin = (int) ((displayHeight - (buttonSize * 7)) /2);
        int bottomMargin = (int) (displayHeight- topMargin - buttonSize);
        int rightMargin = (int) (displayWidth - leftMargin);

        //Log.e("check",""+displayWidth+" "+leftMargin+" "+displayHeight+" "+rightMargin+" "+buttonSize);

        for(int i=0; i<7; i++){
            leftMargin = (int) (displayWidth*(0.25));
            for(int j=0; j<5; j++){
                Button button = new Button(this);
                button.setText("B"+ (i*5+j));
                button.setId(i*5+j);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(buttonSize,buttonSize);
                params.setMargins(leftMargin,topMargin,rightMargin,bottomMargin);
                button.setPadding(0,0,0,0);
                button.setLayoutParams(params);

                button.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
                                                    client.getTcpSocket().send("clicked on bidding box" + view.getId());
                                              }
                                          }
                );
                layout.addView(button);


                leftMargin+=buttonSize;
            }
            topMargin += buttonSize;
        }
    }


    public void displayCards(final ArrayList<Integer> cards){
        handler.post(new Runnable() {
            @Override
            public void run() {
                // display player cards here
            }
        });
    }
}
