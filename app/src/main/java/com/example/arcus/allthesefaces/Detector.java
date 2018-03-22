package com.example.arcus.allthesefaces;

import android.util.Log;

import java.io.IOException;

/**
 * Created by arn197 on 3/21/18.
 */

public class Detector{
    private static final int input_size = 300;
    private Classifier detector;

    public Classifier CreateDetector(){
        try{
            detector = TensorFlowODAPI.create(input_size);
        } catch(final IOException e){
            Log.i("Got an error",e.toString());
        }
        return detector;
    }
}