package com.example.rahul.client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameActivity extends AppCompatActivity {

    Client client = null;
    Handler handler = new Handler();
    int currBid = 0;
    int doubleStatus =0;
    int redoubleStatus = 0;
    int lastProposer = -1;
    boolean bidTurn = false;
    boolean myTurn = false;
    boolean dummyTurn = false;
    int currSuit = -1;
    static HashMap<Integer,String> map = new HashMap<>(); // card number to card id 102 -> twoc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        client = Client.getInstance();
        startGame();
    }

    public void startGame(){
        hidePlayedCards();
        displayBiddingBox();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> cards = receiveCards();
                displayCards(cards,0);

                while(true){
                    String bidData = client.getTcpSocket().receive();
                    Log.e("check","bidData: "+bidData);
                    
                    if(bidData.compareTo("bid finished")==0){
                        removeBiddingBox();
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

                while(true){
                    String gameData[] = client.getTcpSocket().receive().split(" ");
                    if(gameData[0].compareTo("currSuit") == 0){
                         Log.e("check",""+gameData[1]);
                         if(gameData[1].compareTo("dummyTurn") == 0)dummyTurn = true;
                         currSuit = Integer.parseInt(gameData[2]);
                         myTurn = true;
                    }
                    else if(gameData[0].compareTo("displayPlayedCard") == 0){
                         int player = Integer.parseInt(gameData[1]);
                         int cardId = Integer.parseInt(gameData[2]);
                         int playerPosition = calculatePosition(player);
                         removeCard(cardId, playerPosition);
                         displayPlayedCard(playerPosition, Integer.parseInt(gameData[2]));
                    }
                    else if(gameData[0].compareTo("dummyPlayer") == 0) {
                         int dummyPlayer = Integer.parseInt(gameData[1]);
                         ArrayList<Integer> dummyPlayerCards = receiveCards();

                         int dummyPlayerPosition = calculatePosition(dummyPlayer);
                         displayCards(dummyPlayerCards, dummyPlayerPosition);
                    }
                    else if(gameData[0].compareTo("winner") == 0){
                            Log.e("winner is",""+gameData[1]);
                            break;
                    }
                    else if(gameData[0].compareTo("handComplete") == 0){
                            hidePlayedCards();
                    }
                }

            }
        });
        thread.start();

    }
    public void hidePlayedCards(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                int resID = getResources().getIdentifier("playedCardTop", "id", getPackageName());
                ImageView imageView= (ImageView) findViewById(resID);
                imageView.setVisibility(View.INVISIBLE);

                resID = getResources().getIdentifier("playedCardBottom", "id", getPackageName());
                imageView= (ImageView) findViewById(resID);
                imageView.setVisibility(View.INVISIBLE);

                resID = getResources().getIdentifier("playedCardLeft", "id", getPackageName());
                imageView= (ImageView) findViewById(resID);
                imageView.setVisibility(View.INVISIBLE);

                resID = getResources().getIdentifier("playedCardRight", "id", getPackageName());
                imageView= (ImageView) findViewById(resID);
                imageView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public int calculatePosition(int player){
        int playerPosition = 0;
        int seatNo = client.getSeatNo();
        while(true){
            if(seatNo == player)break;
            seatNo = (seatNo + 1)%4;
            playerPosition++;
        }
        return playerPosition;
    }

    public void displayPlayedCard(final int playerPosition, final int playedCard){
        handler.post(new Runnable() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                int resID = 0;
                switch(playerPosition){
                    case 0:
                        resID = getResources().getIdentifier("playedCardBottom", "id", getPackageName());
                        break;
                    case 1:
                        resID = getResources().getIdentifier("playedCardLeft", "id", getPackageName());
                        break;
                    case 2:
                        resID = getResources().getIdentifier("playedCardTop", "id", getPackageName());
                        break;
                    case 3:
                        resID = getResources().getIdentifier("playedCardRight", "id", getPackageName());
                        break;
                }
                ImageView imageView= (ImageView) findViewById(resID);
                imageView.setVisibility(View.VISIBLE);
                assignImages(playedCard, imageView, playerPosition%2);
            }
        });
    }

    public void removeBiddingBox(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.relativeLayout);
                for(int i=0; i<38 ;i++){
                    layout.removeView(findViewById(i));
                }
            }
        });
    }

    @SuppressLint("ResourceType")
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

        leftMargin = (int) (displayWidth*(0.25));

        Button xx = new Button(this);
        xx.setText("XX");
        xx.setId(35);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (buttonSize * 1.5), buttonSize);
        params.setMargins(leftMargin,topMargin,rightMargin,bottomMargin);
        xx.setPadding(0,0,0,0);
        xx.setLayoutParams(params);

        xx.setOnClickListener(new View.OnClickListener() {
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
        layout.addView(xx);
        leftMargin += buttonSize*1.5;

        Button pass = new Button(this);
        pass.setText("PASS");
        pass.setId(36);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int) (buttonSize * 2), buttonSize);
        params1.setMargins(leftMargin,topMargin,rightMargin,bottomMargin);
        pass.setPadding(0,0,0,0);
        pass.setLayoutParams(params1);

        pass.setOnClickListener(new View.OnClickListener() {
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
        layout.addView(pass);
        leftMargin += buttonSize*2;

        Button x = new Button(this);
        x.setText("X");
        x.setId(37);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int) (buttonSize * 1.5), buttonSize);
        params2.setMargins(leftMargin,topMargin,rightMargin,bottomMargin);
        x.setPadding(0,0,0,0);
        x.setLayoutParams(params2);
        x.setOnClickListener(new View.OnClickListener() {
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
        layout.addView(x);
        leftMargin += buttonSize*1.5;
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

    public void displayCards(final ArrayList<Integer> cards, final int position){
        handler.post(new Runnable() {
            @Override
            public void run() {

                ImageView iv_cards[] = new ImageView[13];
                int start=0;
                int end = 13;
                switch (position){
                    case 0:
                        start = 0;
                        end = 13;
                        break;
                    case 1:
                        start = 39;
                        end = 52;
                        break;
                    case 2:
                        start = 13;
                        end = 26;
                        break;
                    case 3:
                        start = 26;
                        end = 39;
                        break;
                }
                for(int i =0; start<end; start++, i++){
                    int resID = getResources().getIdentifier("iv_card"+(start+1), "id", getPackageName());
                    iv_cards[i] = (ImageView) findViewById(resID);
                    iv_cards[i].setId(cards.get(i));
                    assignImages(cards.get(i), iv_cards[i], position % 2);
                    if(position == 0 || position == 2)
                        assignListener(iv_cards[i]);
                }
            }
        });
    }

    public void assignImages(int card, ImageView image, int orientation){
        if(orientation==0) {
            switch (card) {
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
        else{
            switch (card){
                case 102:
                    image.setImageResource(R.drawable.twocr);
                    break;
                case 103:
                    image.setImageResource(R.drawable.threecr);
                    break;
                case 104:
                    image.setImageResource(R.drawable.fourcr);
                    break;
                case 105:
                    image.setImageResource(R.drawable.fivecr);
                    break;
                case 106:
                    image.setImageResource(R.drawable.sixcr);
                    break;
                case 107:
                    image.setImageResource(R.drawable.sevencr);
                    break;
                case 108:
                    image.setImageResource(R.drawable.eightcr);
                    break;
                case 109:
                    image.setImageResource(R.drawable.ninecr);
                    break;
                case 110:
                    image.setImageResource(R.drawable.tencr);
                    break;
                case 111:
                    image.setImageResource(R.drawable.jcr);
                    break;
                case 112:
                    image.setImageResource(R.drawable.qcr);
                    break;
                case 113:
                    image.setImageResource(R.drawable.kcr);
                    break;
                case 114:
                    image.setImageResource(R.drawable.onecr);
                    break;
                case 202:
                    image.setImageResource(R.drawable.twodr);
                    break;
                case 203:
                    image.setImageResource(R.drawable.threedr);
                    break;
                case 204:
                    image.setImageResource(R.drawable.fourdr);
                    break;
                case 205:
                    image.setImageResource(R.drawable.fivedr);
                    break;
                case 206:
                    image.setImageResource(R.drawable.sixdr);
                    break;
                case 207:
                    image.setImageResource(R.drawable.sevendr);
                    break;
                case 208:
                    image.setImageResource(R.drawable.eightdr);
                    break;
                case 209:
                    image.setImageResource(R.drawable.ninedr);
                    break;
                case 210:
                    image.setImageResource(R.drawable.tendr);
                    break;
                case 211:
                    image.setImageResource(R.drawable.jdr);
                    break;
                case 212:
                    image.setImageResource(R.drawable.qdr);
                    break;
                case 213:
                    image.setImageResource(R.drawable.kdr);
                    break;
                case 214:
                    image.setImageResource(R.drawable.onedr);
                    break;
                case 302:
                    image.setImageResource(R.drawable.twohr);
                    break;
                case 303:
                    image.setImageResource(R.drawable.threehr);
                    break;
                case 304:
                    image.setImageResource(R.drawable.fourhr);
                    break;
                case 305:
                    image.setImageResource(R.drawable.fivehr);
                    break;
                case 306:
                    image.setImageResource(R.drawable.sixhr);
                    break;
                case 307:
                    image.setImageResource(R.drawable.sevenhr);
                    break;
                case 308:
                    image.setImageResource(R.drawable.eighthr);
                    break;
                case 309:
                    image.setImageResource(R.drawable.ninehr);
                    break;
                case 310:
                    image.setImageResource(R.drawable.tenhr);
                    break;
                case 311:
                    image.setImageResource(R.drawable.jhr);
                    break;
                case 312:
                    image.setImageResource(R.drawable.qhr);
                    break;
                case 313:
                    image.setImageResource(R.drawable.khr);
                    break;
                case 314:
                    image.setImageResource(R.drawable.onehr);
                    break;
                case 402:
                    image.setImageResource(R.drawable.twosr);
                    break;
                case 403:
                    image.setImageResource(R.drawable.threesr);
                    break;
                case 404:
                    image.setImageResource(R.drawable.foursr);
                    break;
                case 405:
                    image.setImageResource(R.drawable.fivesr);
                    break;
                case 406:
                    image.setImageResource(R.drawable.sixsr);
                    break;
                case 407:
                    image.setImageResource(R.drawable.sevensr);
                    break;
                case 408:
                    image.setImageResource(R.drawable.eightsr);
                    break;
                case 409:
                    image.setImageResource(R.drawable.ninesr);
                    break;
                case 410:
                    image.setImageResource(R.drawable.tensr);
                    break;
                case 411:
                    image.setImageResource(R.drawable.jsr);
                    break;
                case 412:
                    image.setImageResource(R.drawable.qsr);
                    break;
                case 413:
                    image.setImageResource(R.drawable.ksr);
                    break;
                case 414:
                    image.setImageResource(R.drawable.onesr);
                    break;
            }

        }
    }

    public void assignListener(final ImageView imageView){
        imageView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                if(myTurn){
                    LinearLayout parent = (LinearLayout) v.getParent();
                    String parentId = parent.getResources().getResourceName(parent.getId()).split("/")[1];
                    Log.e("check",""+parentId);

                    if((dummyTurn && parentId.compareTo("linearLayoutTop")==0) ||
                            (!dummyTurn && parentId.compareTo("linearLayoutBottom")==0)){

                        if(currSuit == (int) (v.getId()/100)) {
                            client.getTcpSocket().send("" + v.getId());
                            myTurn = false;
                            if(dummyTurn) dummyTurn = false;
                        }
                        else{
                            boolean currSuitCard = false;
                            for(int i=0; i<parent.getChildCount(); i++){
                                if(currSuit == parent.getChildAt(i).getId()/100){
                                    currSuitCard = true;
                                    break;
                                }
                            }
                            if(!currSuitCard){
                                client.getTcpSocket().send(""+v.getId());
                                myTurn = false;
                                if(dummyTurn) dummyTurn = false;
                            }
                        }

                    }
                }
            }
        });
    }

    public void removeChildren(View v , LinearLayout parent){
        parent.removeView(v);
        parent.setWeightSum(parent.getWeightSum()-1);
    }

    public void removeCard(final int cardId, final int playerPosition){
        handler.post(new Runnable() {
            @Override
            public void run() {
                View view = findViewById(cardId);
                if(view == null){
                    String id = null;
                    switch (playerPosition){
                        case 0:
                            id = "linearLayoutBottom";
                            break;
                        case 1:
                            id = "linearLayoutLeft";
                            break;
                        case 2:
                            id = "linearLayoutTop";
                            break;
                        case 3:
                            id = "linearLayoutRight";
                            break;
                    }
                    int resID = getResources().getIdentifier(id, "id", getPackageName());
                    LinearLayout parent = (LinearLayout) findViewById(resID);
                    parent.removeView(parent.getChildAt(0));
                    parent.setWeightSum(parent.getWeightSum()-1);
                }
                else{
                    removeChildren(view,(LinearLayout) view.getParent());
                }
            }
        });
    }

    public String getImageId(int card){
        map.put(102, "twoc");
        map.put(103, "threec");
        map.put(104, "fourc");
        map.put(105, "fivec");
        map.put(106, "sixc");
        map.put(107, "sevenc");
        map.put(108, "eightc");
        map.put(109, "ninec");
        map.put(110, "tenc");
        map.put(111, "jc");
        map.put(112, "qc");
        map.put(113, "kc");
        map.put(114, "ac");
        map.put(202, "twod");
        map.put(203, "threed");
        map.put(204, "fourd");
        map.put(205, "fived");
        map.put(206, "sixd");
        map.put(207, "sevend");
        map.put(208, "eightd");
        map.put(209, "nined");
        map.put(210, "tend");
        map.put(211, "jd");
        map.put(212, "qd");
        map.put(213, "kd");
        map.put(214, "ad");
        map.put(302, "twoh");
        map.put(303, "threeh");
        map.put(304, "fourh");
        map.put(305, "fiveh");
        map.put(306, "sixh");
        map.put(307, "sevenh");
        map.put(308, "eighth");
        map.put(309, "nineh");
        map.put(310, "tenh");
        map.put(311, "jh");
        map.put(312, "qh");
        map.put(313, "kh");
        map.put(314, "ah");
        map.put(402, "twos");
        map.put(403, "threes");
        map.put(404, "fours");
        map.put(405, "fives");
        map.put(406, "sixs");
        map.put(407, "sevens");
        map.put(408, "eights");
        map.put(409, "nines");
        map.put(410, "tens");
        map.put(411, "js");
        map.put(412, "qs");
        map.put(413, "ks");
        map.put(414, "as");
        return map.get(card);
    }
}
