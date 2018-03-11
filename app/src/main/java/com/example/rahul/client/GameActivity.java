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
import android.widget.ImageView;
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
    int currBid = 0;
    int doubleStatus =0;
    int redoubleStatus = 0;
    int lastProposer = -1;
    boolean bidTurn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        client = Client.getInstance();
        startGame();
    }

    public void startGame(){
        displayBiddingBox();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> cards = receiveCards();
                displayCards(cards);

                while(true){
                    String bidData = client.getTcpSocket().receive();
                    if(bidData.compareTo("bid finished")==0){
                        break;
                    }
                    else {
                        bidTurn = true;
                        String bidDataArray[] = bidData.split(" ");
                        currBid = Integer.parseInt(bidDataArray[0]);
                        lastProposer = Integer.parseInt(bidDataArray[1]);
                        doubleStatus = Integer.parseInt(bidDataArray[2]);
                        redoubleStatus = Integer.parseInt(bidDataArray[3]);
                    }
                }
            }
        });
        thread.start();

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
                                                    if(bidTurn==true){
                                                        if(currBid<view.getId()){
                                                            client.getTcpSocket().send(""+view.getId());
                                                        }
                                                        bidTurn = false;
                                                    }

                                              }
                                          }
                );
                layout.addView(button);
                leftMargin+=buttonSize;
            }
            topMargin += buttonSize;
        }
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

    public void displayCards(final ArrayList<Integer> cards){
        handler.post(new Runnable() {
            @Override
            public void run() {
                ImageView iv_card1, iv_card2, iv_card3, iv_card4, iv_card5, iv_card6, iv_card7, iv_card8, iv_card9, iv_card10, iv_card11, iv_card12, iv_card13;
                // connecting .xml to java
                iv_card1 = (ImageView) findViewById(R.id.iv_card1);
                iv_card2 = (ImageView) findViewById(R.id.iv_card2);
                iv_card3 = (ImageView) findViewById(R.id.iv_card3);
                iv_card4 = (ImageView) findViewById(R.id.iv_card4);
                iv_card5 = (ImageView) findViewById(R.id.iv_card5);
                iv_card6 = (ImageView) findViewById(R.id.iv_card6);
                iv_card7 = (ImageView) findViewById(R.id.iv_card7);
                iv_card8 = (ImageView) findViewById(R.id.iv_card8);
                iv_card9 = (ImageView) findViewById(R.id.iv_card9);
                iv_card10 = (ImageView) findViewById(R.id.iv_card10);
                iv_card11 = (ImageView) findViewById(R.id.iv_card11);
                iv_card12 = (ImageView) findViewById(R.id.iv_card12);
                iv_card13 = (ImageView) findViewById(R.id.iv_card13);

         // setting 13 cards on screen using assignImages
                assignImages(cards.get(0),iv_card1);
                assignImages(cards.get(1),iv_card2);
                assignImages(cards.get(2),iv_card3);
                assignImages(cards.get(3),iv_card4);
                assignImages(cards.get(4),iv_card5);
                assignImages(cards.get(5),iv_card6);
                assignImages(cards.get(6),iv_card7);
                assignImages(cards.get(7),iv_card8);
                assignImages(cards.get(8),iv_card9);
                assignImages(cards.get(9),iv_card10);
                assignImages(cards.get(10),iv_card11);
                assignImages(cards.get(11),iv_card12);
                assignImages(cards.get(12),iv_card13);
            }
        });
    }
    // to set given position on screen to card of the deck
    // names gives as one to ten j,q,k  and first letter of suit i.e. c for club
    // 100 series for club 200 for diamond 300 for hearts 400 for spades
    // ace has value of 14 becuase its highest ranked card of deck and not 1
    public void assignImages(int card, ImageView image){
        switch (card){
            case 102:
                image.setImageResource(R.drawable.twoc);
                break;
            case 103:
                image.setImageResource(R.drawable.threec);
                break;
            case 104:
                image.setImageResource(R.drawable.fourc);
                break;
            case 105:
                image.setImageResource(R.drawable.fivec);
                break;
            case 106:
                image.setImageResource(R.drawable.sixc);
                break;
            case 107:
                image.setImageResource(R.drawable.sevenc);
                break;
            case 108:
                image.setImageResource(R.drawable.eightc);
                break;
            case 109:
                image.setImageResource(R.drawable.ninec);
                break;
            case 110:
                image.setImageResource(R.drawable.tenc);
                break;
            case 111:
                image.setImageResource(R.drawable.jc);
                break;
            case 112:
                image.setImageResource(R.drawable.qc);
                break;
            case 113:
                image.setImageResource(R.drawable.kc);
                break;
            case 114:
                image.setImageResource(R.drawable.onec);
                break;
            case 202:
                image.setImageResource(R.drawable.twod);
                break;
            case 203:
                image.setImageResource(R.drawable.threed);
                break;
            case 204:
                image.setImageResource(R.drawable.fourd);
                break;
            case 205:
                image.setImageResource(R.drawable.fived);
                break;
            case 206:
                image.setImageResource(R.drawable.sixd);
                break;
            case 207:
                image.setImageResource(R.drawable.sevend);
                break;
            case 208:
                image.setImageResource(R.drawable.eightd);
                break;
            case 209:
                image.setImageResource(R.drawable.nined);
                break;
            case 210:
                image.setImageResource(R.drawable.tend);
                break;
            case 211:
                image.setImageResource(R.drawable.jd);
                break;
            case 212:
                image.setImageResource(R.drawable.qd);
                break;
            case 213:
                image.setImageResource(R.drawable.kd);
                break;
            case 214:
                image.setImageResource(R.drawable.oned);
                break;
            case 302:
                image.setImageResource(R.drawable.twoh);
                break;
            case 303:
                image.setImageResource(R.drawable.threeh);
                break;
            case 304:
                image.setImageResource(R.drawable.fourh);
                break;
            case 305:
                image.setImageResource(R.drawable.fiveh);
                break;
            case 306:
                image.setImageResource(R.drawable.sixh);
                break;
            case 307:
                image.setImageResource(R.drawable.sevenh);
                break;
            case 308:
                image.setImageResource(R.drawable.eighth);
                break;
            case 309:
                image.setImageResource(R.drawable.nineh);
                break;
            case 310:
                image.setImageResource(R.drawable.tenh);
                break;
            case 311:
                image.setImageResource(R.drawable.jh);
                break;
            case 312:
                image.setImageResource(R.drawable.qh);
                break;
            case 313:
                image.setImageResource(R.drawable.kh);
                break;
            case 314:
                image.setImageResource(R.drawable.oneh);
                break;
            case 402:
                image.setImageResource(R.drawable.twos);
                break;
            case 403:
                image.setImageResource(R.drawable.threes);
                break;
            case 404:
                image.setImageResource(R.drawable.fours);
                break;
            case 405:
                image.setImageResource(R.drawable.fives);
                break;
            case 406:
                image.setImageResource(R.drawable.sixs);
                break;
            case 407:
                image.setImageResource(R.drawable.sevens);
                break;
            case 408:
                image.setImageResource(R.drawable.eights);
                break;
            case 409:
                image.setImageResource(R.drawable.nines);
                break;
            case 410:
                image.setImageResource(R.drawable.tens);
                break;
            case 411:
                image.setImageResource(R.drawable.js);
                break;
            case 412:
                image.setImageResource(R.drawable.qs);
                break;
            case 413:
                image.setImageResource(R.drawable.ks);
                break;
            case 414:
                image.setImageResource(R.drawable.ones);
                break;
        }
    }
}
