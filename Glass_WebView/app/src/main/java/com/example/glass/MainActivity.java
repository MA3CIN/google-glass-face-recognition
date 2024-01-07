package com.example.glass;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.glass.GlassGestureDetector.Gesture;
import com.example.glass.GlassGestureDetector.OnGestureListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnGestureListener {


    private GlassGestureDetector glassGestureDetector;
    private WebView myWebView;

    private int siteCounter =0 ;

    // adb reverse tcp:5005 tcp:5005    <--- Needed! + run web as http, without ssl!
    private final String baseUrl = "http://localhost:5005/";



    private static final int REQUEST_CODE = 999;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        glassGestureDetector = new GlassGestureDetector(this, this);

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl(baseUrl);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void changeSite() {
        Map<Integer, String> myMap = new HashMap<Integer, String>();
        myMap.put(0, "");
        myMap.put(1, "Create");
        myMap.put(2, "Recognize");
        myMap.put(3, "List");
        myMap.put(4, "Statistics");

        if (siteCounter == 4){
            siteCounter = 0;
            myWebView.loadUrl(baseUrl);
        } else {
            siteCounter ++;
            Log.d("TAG", baseUrl + "People/" + myMap.get(siteCounter));
            myWebView.loadUrl(baseUrl + "People/" + myMap.get(siteCounter));
        }


    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return glassGestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        int scrollSpeed = 100;
        switch (gesture) {
            case SWIPE_DOWN:
                Log.d("App", "Swipe Down!");
                finish();
                return true;
            case TAP:
                Log.d("App", "TAPPED!");
                changeSite();
                return true;
            case SWIPE_FORWARD:
                Log.d("App", "swipe forward");
                myWebView.scrollBy(0, scrollSpeed);
                return true;
            case SWIPE_BACKWARD:
                Log.d("App", "swipe backward");
                myWebView.scrollBy(0, -scrollSpeed);
                return true;
            case TWO_FINGER_SWIPE_FORWARD:
                Log.d("App", "double forward - functionality disabled");
            case TWO_FINGER_SWIPE_BACKWARD:
                Log.d("App", "double backward");
                myWebView.goBack();
                return true;
            default:
                return false;
        }
    }

}