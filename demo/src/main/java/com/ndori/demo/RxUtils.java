package com.ndori.demo;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Nitzan on 25/11/17.
 */

public class RxUtils {

    @SafeVarargs
    public static <T> Observable<T> choose(final Observable<T>... observables) {
        return Observable.just(null).flatMap(new Func1<Object, Observable<T>>() {
            int i = 0;
            @Override
            public Observable<T> call(Object o) {
                return observables[i++];
            }
        });
    }
}
