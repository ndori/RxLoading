package com.ndori.demo;

import android.graphics.Rect;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;

import java.util.Random;

/**
 * Created on 2017.
 */

public class Utils {

    public static final int MAX_MILLISECONDS = 2000;
    public static final int MIN_MILLISECONDS = 500;
    private static Random random = new Random();

    public static int getRandomDelayMilliseconds(){
        final int rand = getRand(MIN_MILLISECONDS, MAX_MILLISECONDS);
        Log.d("Utils", "random delay = " + rand);
        return rand;
    }
    public static int getRand(int max){
        return random.nextInt(max);
    }
    public static int getRand(int min, int max){
        return random.nextInt(max-min) + min;
    }

    public static void enlargeTouchAreaSides(View view){
        Rect touchableArea = new Rect();
        view.getHitRect(touchableArea);
        touchableArea.left+=20;
        touchableArea.right+=20;
        ((View) view.getParent()).setTouchDelegate(new TouchDelegate(touchableArea, view));
    }
}
