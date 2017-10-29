/*
 * Copyright 2017 ndori
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ndori.rxloading;

import android.view.View;

/**
 * Created by ndori on 8/3/2017.
 *
 * must be idempotent
 */

public interface ILoadingLayout {

    /**
     * Set the state of one operation with id. <br/>
     * If you want to use more than one operation on a single LoadingLayout use this method. <br/>
     * The order state importance is as follows: <br/>
     * {@link LoadingState#LOADING_FAIL} <br/>
     * {@link LoadingState#LOADING} <br/>
     * {@link LoadingState#DONE} <br/>
     * {@link LoadingState#NO_DATA} <br/>
     * e.g. if there is at least one fail it will show the fail view, otherwise for loading etc.... <br/>
     *
     * @param operationId - a unique identifier for a certain operation, you must use it every time you want to change it's state
     * @param state the new state
     */
    void setState(String operationId, LoadingState state);

    /**
     * Will set the state of the loading interface, please consider usings {@link #setState(String, LoadingState)} for mutli-operations
     * Using this method will override any previous state used in other methods, so you can consider it as "Force" set.
     * @param state the new state
     */
    void setState(LoadingLayout.LoadingState state);

    /**
     * @return the actual state the object is in.
     */
    LoadingLayout.LoadingState getState();

    /**
     * @return if true user will be shown a retry button that should be used to retry the operation that has failed.
     */
    boolean isRetryEnabled();

    /**
     * @param operationId - a unique identifier for a certain operation.
     * @return will return the state of the operation, please note that the actual state of the layout might be different.
     */
    LoadingState getState(String operationId);


    /**
     * it can accommodate multiple listener so one click will trigger multiple retries,
     * add should only add an onClickListener instance once, so multiple calls with the same instance will only get one call
     * @param onClickListener  this listener will be called from the retry button if one exists.
     */
    void addOnFailedActionButtonClickListener(View.OnClickListener onClickListener);

    /**
     * a common use can be in case you register multiple operations and upon retrying you would like to remove the lisener to avoid another retry.
     * @param onClickListener a listener set by {@link #addOnFailedActionButtonClickListener(View.OnClickListener)}
     */
    void removeOnFailedActionButtonClickListener(View.OnClickListener onClickListener);

    /**
     * you can use it in order to add some specific action if the state is empty e.g. "add" action
     * @param onNoDataActionListener will be called upon the action button in the no data screen is used
     */
    void setOnNoDataActionListener(View.OnClickListener onNoDataActionListener);

    /*
     * @param isRetryEnabled enabled by default, if disabled the error screen will not show a retry button
     */
    void setIsRetryEnabled(boolean isRetryEnabled);

    /**
     * @param failText text for the error screen e.g. "Network Error Occurred"
     */
    void setFailedText(String failText);

    /**
     * all the possible states for the layout to be in. <br/>
     * {@link #LOADING}, {@link #LOADING_FAIL}, {@link #DONE}, {@link #NO_DATA}
     */
    enum LoadingState {
        /**
         * represent a pending operation
         */
        LOADING,
        /**
         * the operation has failed, you might be able to retry in this state.
         */
        LOADING_FAIL,
        /**
         * the loading has completed successfully and no data exists, this is an optional state and can be used to represent an empty list etc..
         */
        NO_DATA,
        /**
         * the operation has completed successfully and data was delivered.
         */
        DONE
    }

    /**
     * inherit from this to define an explicit configuration for {@link ILoadingLayout}
     */
    class ILoadingStateConfiguration {
        final LoadingState state;
        private final String operationId;

        /**
         *
         * @param operationId the id to be used with {@link ILoadingLayout#setState(String, LoadingState)}
         * @param state the state to be used with {@link ILoadingLayout#setState(String, LoadingState)}
         */
        public ILoadingStateConfiguration(String operationId, LoadingState state) {
            this.operationId = operationId;
            this.state = state;
        }

        /**
         * this will set the state and you should override it to add more functionality
         * @param ILoadingLayout the layout the you will change
         */
        public void set(ILoadingLayout ILoadingLayout){
            ILoadingLayout.setState(operationId, state);
        }


        public LoadingState getState() {
            return state;
        }
    }

    /***
     * a wrapper configuration for {@link LoadingState#LOADING_FAIL}
     * it allows you to change the text, retry and listener per configuration
     */
    class FailILoadingStateConfiguration extends ILoadingStateConfiguration {
        private final String failText;
        private final Boolean isRetryEnabled;
        private final View.OnClickListener retryListener;

        //null means don't touch
        public FailILoadingStateConfiguration(String operationId, String failText, Boolean isRetryEnabled, View.OnClickListener retryListener) {
            super(operationId, LoadingState.LOADING_FAIL);
            this.failText = failText;
            this.isRetryEnabled = isRetryEnabled;
            this.retryListener = retryListener;
        }

        @Override
        public void set(ILoadingLayout ILoadingLayout) {
            super.set(ILoadingLayout);
            if ( failText != null)
                ILoadingLayout.setFailedText(failText);
            if ( isRetryEnabled != null)
                ILoadingLayout.setIsRetryEnabled(isRetryEnabled);
            if ( retryListener != null) {
                ILoadingLayout.removeOnFailedActionButtonClickListener(retryListener); //if was already set, safety
                ILoadingLayout.addOnFailedActionButtonClickListener(retryListener);
            }
        }
    }

    /***
     * a wrapper configuration for {@link LoadingState#DONE}
     */
    class DoneILoadingStateConfiguration extends ILoadingStateConfiguration {

        public DoneILoadingStateConfiguration(String operationId) {
            super(operationId, LoadingState.DONE);
        }
    }

    /***
     * a wrapper configuration for {@link LoadingState#LOADING}
     */
    class LoadingILoadingStateConfiguration extends ILoadingStateConfiguration {

        public LoadingILoadingStateConfiguration(String operationId) {
            super(operationId, LoadingState.LOADING);
        }
    }

    /***
     * a wrapper configuration for {@link LoadingState#NO_DATA}
     * //TODO: add the ability to change some parameters here as well
     */
    class NoDataILoadingStateConfiguration extends ILoadingStateConfiguration {

        public NoDataILoadingStateConfiguration(String operationId) {
            super(operationId, LoadingState.NO_DATA);
        }
    }
}
