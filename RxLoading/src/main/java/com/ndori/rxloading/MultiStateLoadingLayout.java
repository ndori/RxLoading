package com.ndori.rxloading;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created on 2017.
 */

public class MultiStateLoadingLayout {

    private final ILoadingLayout loadingLayout;

    public MultiStateLoadingLayout(ILoadingLayout loadingLayout) {
        this.loadingLayout = loadingLayout;
    }

    private Map<String, ILoadingLayout.LoadingState> multiState = new LinkedHashMap<>();

    public ILoadingLayout.LoadingState getState(String id) {
        final ILoadingLayout.LoadingState state = multiState.get(id);
        return state != null ? state : loadingLayout.getState(); //is that a good behaviour?
    }

    public void setState(String id, ILoadingLayout.LoadingState newState){
        //I could make it faster then o(N) but I assume N is relatively small
        ILoadingLayout.LoadingState prevState = multiState.put(id, newState);
        loadingLayout.setState(calcGlobalState());
    }

    private ILoadingLayout.LoadingState calcGlobalState() {
        if (multiState.containsValue(ILoadingLayout.LoadingState.LOADING_FAIL))
            return ILoadingLayout.LoadingState.LOADING_FAIL;
        if (multiState.containsValue(ILoadingLayout.LoadingState.LOADING))
            return ILoadingLayout.LoadingState.LOADING;
        if ( multiState.containsValue(ILoadingLayout.LoadingState.NO_DATA))
            return ILoadingLayout.LoadingState.NO_DATA;
        if (multiState.containsValue(ILoadingLayout.LoadingState.DONE))
            return ILoadingLayout.LoadingState.DONE;
        return loadingLayout.getState();
    }
}
