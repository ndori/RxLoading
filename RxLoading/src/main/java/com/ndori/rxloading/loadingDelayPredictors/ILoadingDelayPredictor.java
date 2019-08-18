package com.ndori.rxloading.loadingDelayPredictors;

/**
 * Created on 2019.
 */
public interface ILoadingDelayPredictor {
    /***
     * each time a loading event occured and finished this method will be called
     * @param timeInMilliseconds how long it took it to load
     */
    void onLoadingTime(long timeInMilliseconds);

    /***
     * will be called each time a new loading need to happen
     * @return how long to delay it in Milliseconds
     */
    long getDelayTime();
}
