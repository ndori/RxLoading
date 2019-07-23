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

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import com.ndori.rxloading.ILoadingLayout.ILoadingStateConfiguration;
import com.ndori.rxloading.ILoadingLayout.LoadingState;
import com.ndori.rxloading.stateProviders.IStateProvider;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by ndori. <br/> <br/>
 * <p>
 * This class is a Transformer meant to ease the use of a {@link ILoadingLayout loading object} with RxJava observables.
 * <br/> <br/>
 * A common use is a network call which returns an observable, we than want to show a loading state until the data is retrieved. <br/>
 * when it is retrieved we want to show this data, but if some error is occurred we might want to show a different message, possibly with a "retry" option. <br/>
 * <br/>
 * let's say we have this method: {@link Observable} getDataFromNetwork(); <br/>
 * we then use it in some fashion, e.g. getDataFromNetwork().map(...).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(...); <br/>
 * in order to use rxloading all we have to do is add it somewhere on the chain, e.g. <br/>
 * getDataFromNetwork().compose(new rxloading(loadingObj))....subscribe(...) <br/>
 * <br/>
 * if the loadingObj is not available at the time of the Subscription creation you can do this: <br/>
 * rxloading rxLoading = new rxloading(); <br/>
 * .<br/>
 * .<br/>
 * getDataFromNetwork().compose(rxLoading)....subscribe(...)<br/>
 * .<br/>
 * .<br/>
 * .<br/>
 * rxLoading.bind(loadingObj); //obj is ready<br/>
 */
public class RxLoading<T> implements Observable.Transformer<T, T> {

    @Nullable
    private LoadingState unsubscribeState = LoadingState.NO_DATA; //default state, it means that unsubscribe while loading considered a no data state
    private boolean isRemoveStateOnUnsubscribe = false;

    public static <T> RxLoading<T> create(){
        return new RxLoading<>();
    }

    public static <T> RxLoading<T> create(ILoadingLayout ILoadingLayout){
        return new RxLoading<>(ILoadingLayout);
    }

    private static final String TAG = RxLoading.class.getSimpleName();
    private WeakReference<ILoadingLayout> loadingInterface = new WeakReference<>(null);

    protected AtomicReference<LoadingState> state = new AtomicReference<>(null); //might be get and set from different threads
    protected final String uuid;

    /**
     * @param ILoadingLayout loadingInterface must be ready to use it, otherwise use empty constructor and {@link #bind(ILoadingLayout)}
     */
    public RxLoading(ILoadingLayout ILoadingLayout) {
        this(String.valueOf(UUID.randomUUID()), ILoadingLayout);
    }


    /**
     * use this if you want to set your own id, so you can create a new rxLoading that will be considered the same as another instance
     * @param uuid and id used as operation id in {@link ILoadingLayout#setState(String, LoadingState)}
     * @param ILoadingLayout loadingInterface must be ready to use it, otherwise use empty constructor and {@link #bind(ILoadingLayout)}
     */
    public RxLoading(String uuid, ILoadingLayout ILoadingLayout) {
        this(uuid);
        bind(ILoadingLayout);
    }

    /**
     * use this in case {@link ILoadingLayout ILoading} is not ready yet, then use  {@link #bind(ILoadingLayout)} when it is
     */
    public RxLoading() {
        this(String.valueOf(UUID.randomUUID()));
    }

    /**
     * use this if you want to set your own id, so you can create a new rxLoading that will be considered the same as another instance
     * @param uuid and id used as operation id in {@link ILoadingLayout#setState(String, LoadingState)}
     */
    public RxLoading(String uuid) {
        this.uuid = uuid;
        initConfigurations();
    }

    //contains the DEFAULT configuration for each state
    protected Map<LoadingState, ILoadingStateConfiguration> configuration = new HashMap<>();

    protected void initConfigurations() {
        configuration.put(LoadingState.LOADING, new ILoadingLayout.LoadingILoadingStateConfiguration(uuid));
        configuration.put(LoadingState.DONE, new ILoadingLayout.DoneILoadingStateConfiguration(uuid));
        configuration.put(LoadingState.NO_DATA, new ILoadingLayout.NoDataILoadingStateConfiguration(uuid));
        configuration.put(LoadingState.LOADING_FAIL, new ILoadingLayout.FailILoadingStateConfiguration(uuid, null, null, onRetryListener));
    }



    /**
     * don't use loadingInterface directly, use this getter instead
     *
     * @return obvious isn't it?
     */
    @Nullable
    protected ILoadingLayout getLoadingInterface() {
        return loadingInterface.get();
    }


    //send one item and onComplete
    protected BehaviorSubject<WeakReference<ILoadingLayout>> bindSubject = BehaviorSubject.create();
    /**
     * this will save a reference to the layout, and will use it instead of any view previous initialized {@link ILoadingLayout}, it will also will {@link ILoadingLayout#setState(LoadingState)}
     * with the last state.
     * no need to unbind as it will save a weakReference to the object
     * @param ILoadingLayout the loadingObje
     */
    public RxLoading<T> bind(ILoadingLayout ILoadingLayout) {
        if ( ILoadingLayout == null)
            return this; //do nothing //TODO: allow unbind? we use weakReference for it
        this.loadingInterface = new WeakReference<>(ILoadingLayout); //TODO; try to get rid of it and be completely immutable
        bindSubject.onNext(this.loadingInterface); //this will set the last state emitted
        return this;
    }

    /**
     * this will make rxloading to consider onError and onCompleted/onUnsubscirbe while loading to be considered as DONE
     * note the {@link #setStateProvider(IStateProvider)} will still work
     * @return "this" so you can keep change it
     */
    public RxLoading<T> setAllResultsAsDone(){
        ILoadingStateConfiguration doneConfig = configuration.get(LoadingState.DONE);
        configuration.put(LoadingState.NO_DATA, doneConfig);
        configuration.put(LoadingState.LOADING_FAIL, doneConfig);
        return this;
    }

    /***
     * default will set to {@link LoadingState#NO_DATA}, <br/>
     * as the name suggest will set the given state if RxLoading is unsubscribe while it's in loading state
     * @param state to be set in the above condition, can be null, in that case do nothing
     * @return "this" so you can keep change it
     */
    public RxLoading<T> setStateWhenUnsubscribeWhileLoadingAs(@Nullable LoadingState state){
        this.unsubscribeState = state;
        return this;
    }

    /**
     * when true it mean the state of the connected {@link ILoadingLayout} will no longer be connected to this object
     * @param remove if true, {@link #setStateWhenUnsubscribeWhileLoadingAs(LoadingState)} will do nothing
     * @return
     */
    public RxLoading<T> removeLoadingLayoutStateOnUnsubscribe(boolean remove){
        isRemoveStateOnUnsubscribe = remove;
        return this;
    }

    protected void ifLoadingSetStateAs(LoadingState state) {
        if ( state != null && this.state == null || this.state.get().equals(LoadingState.LOADING) )  //we abuse the state of loading, should we?
            scheduleSetState(state);
    }

    protected BehaviorSubject<ILoadingLayout.ILoadingStateConfiguration> configurationSubject = null;
    @Override
    public Observable<T> call(Observable<T> observable) {
        if ( configurationSubject == null) {
            configurationSubject = BehaviorSubject.create(); //this will cause the last state once binded.
            Observable.combineLatest(configurationSubject.delaySubscription(bindSubject), bindSubject, Pair::new)
                    .delaySubscription(bindSubject) //it will only subscribe when we are first binded, this is not a must as combine will keep it working
                    .onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(p -> {
    //                    Log.e("DEBUG", "set configuration" + configuration);
                        final ILoadingLayout ILoadingLayout = p.second.get();
                        if ( ILoadingLayout != null)
                            p.first.set(ILoadingLayout);
                    });
        }

        return observable
                .doOnSubscribe(() -> {
                    //doOnSubscribe runs on the thread of subscription...  https://groups.google.com/forum/#!topic/rxjava/TCGBiT0gbyI
                    scheduleSetState(LoadingState.LOADING);
                })
                .doOnEach(new Subscriber<T>() {
                    @Override
                    public void onCompleted() {
                        ifLoadingSetStateAs(LoadingState.NO_DATA);
                    }

                    @Override
                    public void onError(Throwable e) {
                        scheduleSetState(getFailLoadingConfiguration(e));
                    }

                    @Override
                    public void onNext(T t) {
                        //will be called more then once but that's ok, IILoading must be idempotent
                        scheduleSetState(getOnNextLoadingConfiguration(t));
                    }
                })
                //in case we subscribe but no data arrived yet and we unsubscribe. this can be usuable in case the loading is blocking the ui
                .doOnUnsubscribe(() -> {
                    if (isRemoveStateOnUnsubscribe){
                        scheduleSetState(createClearStateConfiguration());
                    } else {
                        ifLoadingSetStateAs(unsubscribeState);
                    }
                }).retryWhen(observable1 -> observable1.observeOn(AndroidSchedulers.mainThread()) //we need this because we are accessing the loadingLayout
                        .delaySubscription(bindSubject)
                        .flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                //this will be called only when there is a bind so it can't be null
                                if (getLoadingInterface() != null && !getLoadingInterface().isRetryEnabled())
                                    return Observable.error(throwable); //if retry isn't enabled we want to propagate the error
                                return retrySubject.first();
                            }
                        })).repeatWhen(observable12 -> observable12.observeOn(AndroidSchedulers.mainThread()) //we need this because we are accessing the loadingLayout
                                .delaySubscription(bindSubject)
                                .flatMap((Func1<Void, Observable<?>>) none -> {
                                    //this will be called only when there is a bind so it can't be null
                                    //a state where we get an error in onNext as an Item and want to retry
                                    ILoadingLayout loadingLayout = getLoadingInterface();
                                    if (loadingLayout != null &&
                                            (loadingLayout.getState() != LoadingState.LOADING_FAIL || !loadingLayout.isRetryEnabled()))
                                        return Observable.empty(); //if retry isn't enabled we want to propagate the complete
                                    return retrySubject.first();
                                }));
                //TODO: I don't like the observeOn(AndroidSchedulers.mainThread()) in here, it might force sync with thread which may be slower
    }

    @NonNull
    private ILoadingStateConfiguration createClearStateConfiguration() {
        //we don't really need to create it every time
        return new ILoadingStateConfiguration() {
            @Override
            public void set(ILoadingLayout ILoadingLayout) {
                ILoadingLayout.removeState(uuid);
            }

            @Nullable
            @Override
            public LoadingState getState() {
                return null;
            }
        };
    }


    @NonNull
    protected ILoadingLayout.ILoadingStateConfiguration getFailLoadingConfiguration(Throwable e) {
        return configuration.get(LoadingState.LOADING_FAIL);
    }

    private IConfigurationProvider<T> configurationProvider;

    /**
     * in case you get a T object that states if the stream has failed,completed or even loading you can provide a configurationProvider
     * that will decide the Configuration of the LoadingLayout attached please also see {@link #setStateProvider(IStateProvider)} as it is simpler
     * @param configurationProvider  a simple object that gets T and return a configuration
     * @return this
     */
    public RxLoading<T> setConfigurationProvider(IConfigurationProvider<T> configurationProvider){
        this.configurationProvider = configurationProvider;
        return this;
    }

    /**
     * in case you get a T object that states if the stream has failed,completed or even loading you can provide a configurationProvider
     * that will decide the Configuration of the LoadingLayout attached,
     * please also see {@link #setConfigurationProvider(IConfigurationProvider)} as it give more control
     * @param stateProvider a simple object that gets T and return a State
     * @return this
     */
    public RxLoading<T> setStateProvider(final IStateProvider<T> stateProvider){
        this.configurationProvider = t -> {
            LoadingState state = stateProvider.nextState(t);
            switch (state){
                case LOADING_FAIL:
                    return new ILoadingLayout.FailILoadingStateConfiguration(uuid, stateProvider.getFailedMessage(t), stateProvider.isRetryEnabled(t), onRetryListener);
                default:
                    return configuration.get(state);
            }
        };
        return this;
    }

    @NonNull
    protected ILoadingStateConfiguration getOnNextLoadingConfiguration(T t) {
        //will be called more then once but that's ok, IILoading must be idempotent
        if ( configurationProvider != null)
            return configurationProvider.nextConfiguration(t);
        return configuration.get(ILoadingLayout.LoadingState.DONE); //default
    }


    protected void scheduleSetState(final LoadingState state) {
        scheduleSetState(configuration.get(state));
    }
    protected void scheduleSetState(final ILoadingStateConfiguration configuration) {
        final LoadingState state = configuration.getState();
        if ( state != null) //NOTE: null should not be set as it is only in initialization state, change it if it do
            this.state.set(state); //we must do it immediately as other might look at it
        configurationSubject.onNext(configuration);
    }



    private static boolean isOtherThread() {
        final boolean result = Looper.getMainLooper() != Looper.myLooper();
        if (result) {
            Log.e(TAG, "we are not on main thread!!! doing nothing");
        }
        return result;
    }

    //we use a subject because ILoadingLayout can be rebinded and we need to "resubscribe" to it.
    protected PublishSubject<Void> retrySubject = PublishSubject.create();
    protected final View.OnClickListener onRetryListener = v -> retrySubject.onNext(null);

}
