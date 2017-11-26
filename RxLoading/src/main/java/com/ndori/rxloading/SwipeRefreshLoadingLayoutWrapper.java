package com.ndori.rxloading;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

/**
 * Created on 2017.
 */

public class SwipeRefreshLoadingLayoutWrapper extends BaseLoadingLayout {

    private final SwipeRefreshLayout swipeRefreshLayout;
    public SwipeRefreshLoadingLayoutWrapper(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }



    public static SwipeRefreshLoadingLayoutWrapper create(SwipeRefreshLayout swipeRefreshLayout) {
        return new SwipeRefreshLoadingLayoutWrapper(swipeRefreshLayout);
    }


    @Override
    public void setState(LoadingState state) {
        switch (state){
            case LOADING:
                swipeRefreshLayout.setRefreshing(true);
                break;
            default:
                swipeRefreshLayout.setRefreshing(false);
                break;
        }
    }

    @Override
    public LoadingState getState() {
        return swipeRefreshLayout.isRefreshing() ? LoadingState.LOADING : LoadingState.DONE;
    }
}
