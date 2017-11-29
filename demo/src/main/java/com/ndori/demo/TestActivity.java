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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ndori.rxloading.ILoadingLayout;
import com.ndori.rxloading.LoadingLayout;
import com.ndori.rxloading.RxLoading;
import com.ndori.rxloading.stateProviders.DefaultStateProvider;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGoodNetworkCall(View v){
        Log.d("DEBUG", "on Network Call");
        doGoodNetworkCall(getLoadingLayoutForButton(v));
    }

    public void onBadNetworkCall(View v){
        doBadNetworkCall(getLoadingLayoutForButton(v));
    }

    public void onSwitchButton(View v) {
        LoadingLayout layout = getLoadingLayoutForButton(v);
        LoadingLayout.LoadingState state = layout.getState(String.valueOf(v.getId()));
        switch (state){

            case LOADING:
                layout.setState(String.valueOf(v.getId()), ILoadingLayout.LoadingState.DONE);
                break;
            case LOADING_FAIL:
                layout.setState(String.valueOf(v.getId()), ILoadingLayout.LoadingState.LOADING);
                break;
            case NO_DATA:
                layout.setState(String.valueOf(v.getId()), ILoadingLayout.LoadingState.LOADING_FAIL);
                break;
            case DONE:
                layout.setState(String.valueOf(v.getId()), ILoadingLayout.LoadingState.NO_DATA);
                break;
        }

        if ( v instanceof TextView){
            ((TextView) v).setText(layout.getState(String.valueOf(v.getId())).toString());
        }

    }

    private LoadingLayout getLoadingLayoutForButton(View v) {
        return (LoadingLayout) ((ViewGroup) v.getParent()).findViewById(R.id.loadingLayout);
    }

    private void doGoodNetworkCall(final LoadingLayout layout) {
        Observable.defer(() -> {
            Log.e("DEBUG", "Network Call started");
            return goodNetworkRequest().compose(new RxLoading<>(layout))
                    .map(integer -> {
                        Log.e("DEBUG", "map2");
                        return integer;
                    }).subscribeOn(Schedulers.io());
        }).subscribeOn(Schedulers.io())
                .subscribe(integer -> Log.e("DEBUG", "on Network Ended"),
                        throwable -> Log.e("ERROR", "error", throwable));
    }


    public class IntegerStateProvider extends DefaultStateProvider<Integer> {
        @Override
        public ILoadingLayout.LoadingState nextState(Integer num) {
            if ( num < 0 )
                return ILoadingLayout.LoadingState.LOADING_FAIL;
            if ( num == 0)
                return ILoadingLayout.LoadingState.NO_DATA;
            return super.nextState(num);
        }

        @Override
        public String getFailedMessage(Integer num) {
            return "number is " + num + " less then zero, try again";
        }

        @Override
        public Boolean isRetryEnabled(Integer num) {
            return true;
        }
    }
    @NonNull
    private Observable<Integer> goodNetworkRequest() {
        return Observable.just(1).delay(2, TimeUnit.SECONDS, Schedulers.newThread()).map(integer -> {
            Log.e("DEBUG", "map");
            return integer;
        });
    }

    private void doBadNetworkCall(final LoadingLayout layout) {
        final RxLoading<Integer> rxLoading = RxLoading.create();
        rxLoading.setStateProvider(new IntegerStateProvider());
        choose(badNetworkRequestInItem(), goodNetworkRequest())
                .compose(rxLoading)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.e("DEBUG", "on Network Ended");
                Toast.makeText(TestActivity.this, "Got number = " + integer, Toast.LENGTH_SHORT).show();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(TestActivity.this, "ERROR!!!! - end of subscription", Toast.LENGTH_SHORT).show();
                Log.e("DEBUG", "on Network Fail", throwable);
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(() -> rxLoading.bind(layout), 4000);
    }


    private <T> Observable<T> choose(final Observable<T>... observables) {
        return Observable.just(null).flatMap(new Func1<Object, Observable<T>>() {
            int i = 0;
            @Override
            public Observable<T> call(Object o) {
                return observables[i++];
            }
        });
    }



    @NonNull
    private Observable<Integer> badNetworkRequest() {
        return Observable.just(0).delay(2, TimeUnit.SECONDS).map(integer -> {
            integer = null;
            integer.equals(0);
            return 5/0;
        });
    }

    @NonNull
    private Observable<Integer> badNetworkRequestInItem() {
        return Observable.just(-5).delay(2, TimeUnit.SECONDS);
    }


}
