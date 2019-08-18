package com.ndori.rxloading.loadingDelayPredictors;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2019.
 */
public class ThresholdLoadingDelayPredictor implements ILoadingDelayPredictor {

    private final long delayThreshold;
    private static final int MAX_HISTORY = 9;
    private static final double WEIGHT_PIPS = 1d / (MAX_HISTORY+1);
    private List<Long> history = new ArrayList<>(MAX_HISTORY);

    /***
     * this predictor takes the delay as a threshold, and counts good and bad timing, if it overall good it will make a delay so loading will not be shown.
     * the older the event occur it's weight is lesser (last event have bigger weight) and in order to prevent a flicker + delay, bad Sevent timing which are close to the
     * threshold have a bigger negative weight
     * @param delayThreshold - how much to delay 250 milliseconds is a usually a good value
     * @param isFirstDelayed - should we start with a delay or not?
     */
    public ThresholdLoadingDelayPredictor(long delayThreshold, boolean isFirstDelayed) {
        this.delayThreshold = delayThreshold;
        if (isFirstDelayed)
            history.add(0L);
    }

    @Override
    public void onLoadingTime(long timeInMilliseconds) {
        history.add(0, timeInMilliseconds);
        if ( history.size() > MAX_HISTORY)
            history.remove(history.size() -1);
    }

    @Override
    public long getDelayTime() {
        double weight = 1;
        double res = 0d;
        for (Long time: history) {
            if ( time <= delayThreshold)
                res += weight; //we want a delay
            else{
                res -= weight; //no need for delay
                if ( time <= delayThreshold*2)
                    res -= weight *0.3; //very bad, flicker+delay - make more negative by 30% of the current weight
            }
            weight -= WEIGHT_PIPS;
        }
        return res > 0 ? delayThreshold : 0;
    }
}
