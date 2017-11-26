package com.ndori.rxloading;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

/**
 * Created on 2017.
 */

public abstract class BaseLoadingLayout implements ILoadingLayout {

    protected MultiStateLoadingLayout multiStateLoadingLayout = new MultiStateLoadingLayout(this);


    @Override
    public final void setState(String operationId, LoadingState state) {
        multiStateLoadingLayout.setState(operationId, state);
    }

    @Override
    public final LoadingState getState(String operationId) {
        return multiStateLoadingLayout.getState(operationId);
    }

    @Override
    public boolean isRetryEnabled() {
        return false;
    }


    @Override
    public void addOnFailedActionButtonClickListener(View.OnClickListener onClickListener) {

    }

    @Override
    public void removeOnFailedActionButtonClickListener(View.OnClickListener onClickListener) {

    }

    @Override
    public void setOnNoDataActionListener(View.OnClickListener onNoDataActionListener) {

    }

    @Override
    public void setIsRetryEnabled(boolean isRetryEnabled) {

    }

    @Override
    public void setFailedText(String failText) {

    }
}
