package com.example.arcus.allthesefaces;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

/**
 * Created by arn197 on 3/21/18.
 */

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private Camera mCamera = null;
    private ImageView MyCameraPreview = null;
    private Bitmap bitmap = null;
    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private boolean bProcessing = false;
    ViewGroup myViewGroup = null;
    Context mycontext = null;
    int rectWidth = 80;
    int rectHeight = 80;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight, ImageView CameraPreview,ViewGroup viewGroup, Context context)
    {
        PreviewSizeWidth = PreviewlayoutWidth;
        PreviewSizeHeight = PreviewlayoutHeight;
        MyCameraPreview = CameraPreview;
        myViewGroup = viewGroup;
        bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
        mycontext = context;
        pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1)
    {
//        // At preview mode, the frame data will push to here.
//        if (imageFormat == ImageFormat.NV21)
//        {
//            //We only accept the NV21(YUV420) format.
//            if ( !bProcessing )
//            {
//                FrameData = arg0;
//                mHandler.postDelayed(DoImageProcessing,2000);
//            }
//        }
//        Bitmap bitmap = Bitmap.createBitmap(arg1.getParameters().getPreviewSize().width, arg1.getParameters().getPreviewSize().height, Bitmap.Config.ARGB_8888);
//        Allocation bmData = renderScriptNV21ToRGBA888(
//                getContext(),
//                mCamera.getParameters().getPreviewSize().width, mCamera.getParameters().getPreviewSize().height,
//                arg0);
//        bmData.copyTo(bitmap);
        mHandler.post(DoImageProcessing);
        rectHeight = rectHeight + 20;
        rectWidth = rectWidth + 20;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }

    public void onPause()
    {
        mCamera.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        Camera.Parameters parameters;
        Camera.Size bestSize = null;

        parameters = mCamera.getParameters();

        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        Log.i("H","H" + bestSize.height);
        Log.i("H","W" + bestSize.width);

        parameters.setPreviewSize(bestSize.width, bestSize.height);
        mCamera.setDisplayOrientation(90);

        imageFormat = parameters.getPreviewFormat();

        mCamera.setParameters(parameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        mCamera = Camera.open();
        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    //
    // Native JNI
    //
    public native String stringFromJNI();
    static
    {
        System.loadLibrary("native-lib");
    }

    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.i("MyRealTimeImageProcess", stringFromJNI());
            Detector det = new Detector();
            Classifier detector = det.CreateDetector();
            Bitmap bitmapfinal = scaleDown(bitmap,300,true);
            final List<Classifier.Recognition> results = detector.recognizeImage(bitmapfinal);
            for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                Log.i("Loc",location.toString());
                Log.i("Loc",result.getConfidence().toString());
                if (location != null && result.getConfidence() >= 0) {

                    View rect = new View(mycontext);
                    rect.setBackgroundColor(0x000000FF);
                    rect.setLayoutParams(new ViewGroup.LayoutParams(rectWidth,rectHeight));
                    myViewGroup.removeAllViews();
                    myViewGroup.addView(rect);
                    myViewGroup.postInvalidate();
//                    canvas.drawRect(location, paint);
//
//                    cropToFrameTransform.mapRect(location);
//                    result.setLocation(location);
//                    mappedRecognitions.add(result);
                }
            }
//            bProcessing = true;
//            ImageProcessing(PreviewSizeWidth, PreviewSizeHeight, FrameData, pixels);
//
//            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
//            MyCameraPreview.setImageBitmap(bitmap);
//            bProcessing = false;
        }
    };
}