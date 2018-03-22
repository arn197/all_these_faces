package com.example.arcus.allthesefaces;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.hardware.Camera.Size;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

public class MainActivity extends Activity {
    private CameraPreview camPreview;
    private ImageView MyCameraPreview = null;
    private RelativeLayout mainLayout;
    DisplayMetrics metrics = new DisplayMetrics();

    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = this;
        //Set this APK Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        PreviewSizeHeight = metrics.heightPixels;
        PreviewSizeWidth = metrics.widthPixels;
        //Set this APK no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //
        // Create my camera preview
        //
        MyCameraPreview = new ImageView(this);

        SurfaceView camView = new SurfaceView(this);
        SurfaceHolder camHolder = camView.getHolder();
        ViewGroup viewGroup = findViewById(R.id.group);
        camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight, MyCameraPreview,viewGroup,getContext());

        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        mainLayout = findViewById(R.id.main);
        mainLayout.addView(camView, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
        mainLayout.addView(MyCameraPreview, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
    }
    public static Context getContext(){
        return mContext;
    }
    protected void onPause()
    {
        if ( camPreview != null)
            camPreview.onPause();
        super.onPause();
    }
}