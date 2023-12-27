package com.example.glass;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.glass.GlassGestureDetector.Gesture;
import com.example.glass.GlassGestureDetector.OnGestureListener;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnGestureListener {


    private GlassGestureDetector glassGestureDetector;
    private WebView myWebView;

    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        glassGestureDetector = new GlassGestureDetector(this, this);

        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("https://www.youtube.com/c/matthewhallberg");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ImageView imageView = (ImageView) findViewById(R.id.ImageView);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
//                    try {
//                        bitmap = android.provider.MediaStore.Images.Media
//                                .getBitmap(cr, selectedImage);
//
//                        imageView.setImageBitmap(bitmap);
//                        Toast.makeText(thisectedImage.toStr, seling(),
//                                Toast.LENGTH_LONG).show();
//                    } catch (Exception e) {
//                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
//                                .show();
//                        Log.e("Camera", e.toString());
//                    }
                }
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
        switch (gesture) {
            case SWIPE_DOWN:
                Log.d("App", "Swipe Down!");
                finish();
                return true;
            case TAP:
                Log.d("App", "TAPPED!");
                takePicture();
                return true;
//            case SWIPE_FORWARD:
//                Log.d("App", "swipe forward");
//                myWebView.scrollBy(0, scrollSpeed);
//                return true;
//            case SWIPE_BACKWARD:
//                Log.d("App", "swipe backward");
//                myWebView.scrollBy(0, -scrollSpeed);
//                return true;
//            case TWO_FINGER_SWIPE_FORWARD:
//                Log.d("App", "double forward");
//                simulateClick(myWebView.getWidth()/4,myWebView.getHeight()/2);
//                return true;
//            case TWO_FINGER_SWIPE_BACKWARD:
//                Log.d("App", "double backward");
//                myWebView.goBack();
//                return true;
            default:
                return false;
        }
    }

    //https://stackoverflow.com/questions/20886857/how-to-simulate-a-tap-at-a-specific-coordinate-in-an-android-webview
//    private void simulateClick(float x, float y) {
//        long downTime = SystemClock.uptimeMillis();
//        long eventTime = SystemClock.uptimeMillis();
//        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[1];
//        MotionEvent.PointerProperties pp1 = new MotionEvent.PointerProperties();
//        pp1.id = 0;
//        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
//        properties[0] = pp1;
//        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
//        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
//        pc1.x = x;
//        pc1.y = y;
//        pc1.pressure = 1;
//        pc1.size = 1;
//        pointerCoords[0] = pc1;
//        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime,
//                MotionEvent.ACTION_DOWN, 1, properties,
//                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
//        dispatchTouchEvent(motionEvent);
//
//        motionEvent = MotionEvent.obtain(downTime, eventTime,
//                MotionEvent.ACTION_UP, 1, properties,
//                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
//        dispatchTouchEvent(motionEvent);
//    }
}