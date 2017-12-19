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

package com.ndori.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ndori.rxloading.ILoadingLayout;
import com.ndori.rxloading.LoadingLayout;
import com.ndori.rxloading.RxLoading;
import com.ndori.rxloading.stateProviders.DefaultStateProvider;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.ndori.demo.RxUtils.choose;

public class NetworkRequestActivity extends AppCompatActivity {

    private final Action1<Throwable> onError = new Action1<Throwable>() {
@Override
public void call(Throwable throwable) {
    Log.e("DEBUG", "on Network Fail", throwable);
}
};
    private TextView dataArrviedText;
    private Action1<Integer> onDataArrived = new Action1<Integer>() {
        @Override
        public void call(Integer integer) {
            dataArrviedText.setText("Data Arrived Successfully!! Request Result =" + integer);
            Log.e("DEBUG", "on Network Ended");
        }
    };
    private RxLoading<Integer> rxLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_request);
        dataArrviedText = (TextView) findViewById(R.id.textViewVisible);
        LoadingLayout loadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        rxLoading = RxLoading.<Integer>create().setStateProvider(new IntegerStateProvider()).bind(loadingLayout);
    }

    public void onGoodNetworkCall(View v){
        doGoodNetworkCall();
    }

    public void onBadNetworkCall(View v){
        doBadNetworkCall();
    }


    private void doGoodNetworkCall() {
        goodNetworkRequest().compose(rxLoading)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(onDataArrived, onError);
    }

    private void doBadNetworkCall() {
        choose(badNetworkRequestInItem(), goodNetworkRequest())
                .compose(rxLoading)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(onDataArrived, onError);
    }

    Integer requestResult = 0;
    @NonNull
    private Observable<Integer> goodNetworkRequest() {
        return Observable.just(++requestResult).delay(Utils.getRandomDelayMilliseconds(), TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io());
    }

    Integer badRequestResult = 0;
    @NonNull
    private Observable<Integer> badNetworkRequestInItem() {
        return Observable.just(--badRequestResult).delay(Utils.getRandomDelayMilliseconds(), TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io());
    }


    public static class IntegerStateProvider extends DefaultStateProvider<Integer> {
        @Override
        public ILoadingLayout.LoadingState nextState(Integer num) {
            if (num < 0)
                return ILoadingLayout.LoadingState.LOADING_FAIL;
            if (num == 0)
                return ILoadingLayout.LoadingState.NO_DATA;
            return super.nextState(num);
        }

        @Override
        public String getFailedMessage(Integer num) {
            return "Request Result = " + num + ", try again";
        }

        @Override
        public Boolean isRetryEnabled(Integer num) {
            return true;
        }
    }




}
