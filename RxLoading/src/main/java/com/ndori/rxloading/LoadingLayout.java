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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A layout that includes a progressbar, a fail and empty state. <br>
 * it has the ability to hide and show other views. <br>
 * it can be used in several manners: <br>
 *     - Wrap the views you wish to hide (it is a frameLayout) <br>
*      - be a sibling of the views you wish to hide and use attributes:  referenceSiblings /  referencedIds / invertReferencedIds
 */
//TODO: fail and no-data layouts can be the same...
public class LoadingLayout extends FrameLayout implements ILoadingLayout {
    private final static int initSetStateDelayMilliseconds = 200;
    private LoadingState state;
    private ViewStub loadingFailViewStub;
    private ViewStub loadingNoDataViewStub;
    private ViewStub loadingViewStub;
    private View inflatedLoadingFailView = null;
    private View inflatedLoadingNoDataView = null;
    private View inflatedLoadingView = null;
    private String failText;
    private int noDataImageId;
    private int failImageId;
    private String noDataText;
    private TextView loadingFailedMessage;
    private ImageView noDataImageView;
    private TextView noDataMessage;
    private String referencedIds;
    private String failRetryText;
    private Button loadingFailedRetryButton;
    private boolean referenceSiblings;
    private boolean invertReferencedIds;
    private boolean progressBarVisibility;
    private boolean isRetryEnabled = true;
    private int customLoadingFailedStateLayout = 0;
    private int customLoadingStateLayout = 0;
    private int customNoDataStateLayout = 0;
    private boolean isForceDoneVisibilityEnabled;
    private boolean intermediateBlank = true;
    private boolean isHideRootView;
    private Button noDataActionButton;
    private String noDataActionButtonText;
    private ImageView loadingFailImageView;
    private int progressBarStyleEnum;
    private int referencedViewsVisibility;
    private boolean hideImages;

    private Set<OnClickListener> onFailedRetryButtonClickListener = new LinkedHashSet<>();

    @Override
    public void addOnFailedActionButtonClickListener(OnClickListener onClickListener) {
        onFailedRetryButtonClickListener.add(onClickListener);
    }

    @Override
    public void removeOnFailedActionButtonClickListener(OnClickListener onClickListener) {
        onFailedRetryButtonClickListener.remove(onClickListener);
    }


    private void setOnFailedRetryButtonClickListener() {
        if ( loadingFailedRetryButton != null)
            loadingFailedRetryButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Set<OnClickListener> listeners = new LinkedHashSet<>(onFailedRetryButtonClickListener);
                    for (OnClickListener onClickListener : listeners){
                        if ( onClickListener != null)
                            onClickListener.onClick(v);
                    }
                }
            });
    }

    @Override
    public void setOnNoDataActionListener(OnClickListener onNoDataActionListener) {
        this.onNoDataActionListener = onNoDataActionListener;
    }

    private OnClickListener onNoDataActionListener;


    private void setOnNoDataActionButtonClickListener() {
        if ( noDataActionButton != null)
            noDataActionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( onNoDataActionListener != null)
                        onNoDataActionListener.onClick(v);
                }
            });
    }

    public void setNoDataActionButtonText(String text) {
        this.noDataActionButtonText = text;
        setNoDataActionButtonText();
    }
    private void setNoDataActionButtonText() {
        if ( noDataActionButton != null && !TextUtils.isEmpty(noDataActionButtonText)) { //view might not be inflated
            noDataActionButton.setText(noDataActionButtonText);
            noDataActionButton.setVisibility(VISIBLE);
            invalidate();
            requestLayout();
        }
    }

    @Override
    public void setIsRetryEnabled(boolean isRetryEnabled) {
        this.isRetryEnabled = isRetryEnabled;
        if ( loadingFailedRetryButton != null)
            loadingFailedRetryButton.setVisibility(isRetryEnabled ? VISIBLE : GONE);
    }

    public LoadingLayout(@NonNull Context context) {
        super(context);
        initAttrs(context, null);
    }

    public LoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public LoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        for(int i=0; i< getChildCount(); ++i) {
            View nextChild = getChildAt(i);
            if ( nextChild != null)
                referencedViews.add(nextChild);
        }


        isHideRootView = getChildCount() <= 0; //the loadingLayout itself should be hidden instead of just the loading in case of done
        int childCount = getChildCount();
        inflate(getContext(), R.layout.loading_layout, this); //no need to inflate if all is custom
        loadingFailViewStub = initView(isCustomLoadingFailedStateLayout(), customLoadingFailedStateLayout, R.id.stub_loading_fail);
        loadingNoDataViewStub = initView(customNoDataStateLayout != 0, customNoDataStateLayout, R.id.stub_loading_no_data);
        loadingViewStub = initView(customLoadingStateLayout != 0, customLoadingStateLayout, R.id.stub_loading);
    }
    public ViewStub initView(boolean isCustom, @LayoutRes int customViewId, @IdRes int viewStubId){
        ViewStub viewStub = (ViewStub) findViewById(viewStubId);
        //this will generate a new id, important for nested loadingLayout (findViewId will not work as expected, will get the first id of the inner loading layout)
        //TODO: we need to save instance state see http://trickyandroid.com/saving-android-view-state-correctly/, check that this doesn't break it...
        viewStub.setId(View.NO_ID);
        if (isCustom) {
            viewStub.setLayoutResource(customViewId);
        }
        return viewStub;
    }

    private boolean isCustomLoadingFailedStateLayout() {
        return customLoadingFailedStateLayout != 0;
    }


    private void initAttrs(Context context, @Nullable AttributeSet attrs) {
        if ( attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LoadingLayout,
                    0, 0);

            try {
                state = LoadingState.values()[a.getInt(R.styleable.LoadingLayout_initialState, 0)];
                referencedIds = a.getString(R.styleable.LoadingLayout_referencedIds);
                failText = a.getString(R.styleable.LoadingLayout_failText);
                noDataImageId = a.getResourceId(R.styleable.LoadingLayout_noDataImage, 0);
                failImageId = a.getResourceId(R.styleable.LoadingLayout_failImage, 0);
                hideImages = a.getBoolean(R.styleable.LoadingLayout_hideImages, false);
                noDataText = a.getString(R.styleable.LoadingLayout_noDataText);
                failRetryText = a.getString(R.styleable.LoadingLayout_failActionText);
                noDataActionButtonText = a.getString(R.styleable.LoadingLayout_noDataActionText);
                referenceSiblings = a.getBoolean(R.styleable.LoadingLayout_referenceSiblings, false);
                invertReferencedIds = a.getBoolean(R.styleable.LoadingLayout_invertReferencedIds, false);
                progressBarVisibility = a.getBoolean(R.styleable.LoadingLayout_progressBarVisibility, true);
                isRetryEnabled = a.getBoolean(R.styleable.LoadingLayout_failActionEnabled, true);
                isForceDoneVisibilityEnabled = a.getBoolean(R.styleable.LoadingLayout_forceDoneVisibility, false);
                customLoadingFailedStateLayout = a.getResourceId(R.styleable.LoadingLayout_customLoadingFailedStateLayout, 0);
                customLoadingStateLayout = a.getResourceId(R.styleable.LoadingLayout_customLoadingStateLayout, 0);
                customNoDataStateLayout = a.getResourceId(R.styleable.LoadingLayout_customNoDataStateLayout, 0);
                referencedViewsVisibility = a.getInt(R.styleable.LoadingLayout_referencedViewsVisibility, View.GONE);
                progressBarStyleEnum = a.getInt(R.styleable.LoadingLayout_progressBarSize, 0);
                if ( customLoadingStateLayout == 0 && progressBarStyleEnum == 1){ //make switch if more than 2 possibilities
                    customLoadingStateLayout = R.layout.loading_layout_loading_small;
                }
            } finally {
                a.recycle();
            }
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //TODO: better way?
        final View parent = (View) getParent();
        initReferencedIds(getContext(), parent);
        initReferenceSiblings(parent);
        setState(state);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                intermediateBlank = false;
                setState(state); //a refresh
            }
        }, initSetStateDelayMilliseconds); //to prevent "flicker" if data is returned quickly
    }

    private void initReferenceSiblings(View p) {
        if ( !(p instanceof ViewGroup) || (!referenceSiblings && !invertReferencedIds)){
            return;
        }
        ReferencedViews siblings = new ReferencedViews();
        ViewGroup parent = (ViewGroup) p;
        for(int i=0; i< parent.getChildCount(); ++i) {
            View nextChild = parent.getChildAt(i);
            if ( nextChild != null && nextChild != this)
                siblings.add(nextChild);
        }
        if ( invertReferencedIds) {
            siblings.removeAll(referencedViews);
            referencedViews = siblings;
        } else {
            referencedViews.addAll(siblings);
        }
    }

    private ReferencedViews referencedViews = new ReferencedViews();
    private void initReferencedIds(Context context, View parent) {
        if (TextUtils.isEmpty(referencedIds))
            return;
        String[] ids = referencedIds.split(",");
        for ( String id : ids){
            int intId = getResources().getIdentifier(id.trim(), "id", context.getPackageName());
            if ( intId == 0)
                continue;
            View view = parent.findViewById(intId);
            if ( view != null) //safety
                referencedViews.add(view);

        }
    }

    private class ReferencedViews extends LinkedHashSet<View> {


        public ReferencedViews(int initialCapacity) {
            super(initialCapacity);
        }

        ReferencedViews() {
        }


        boolean inHideState = false;
        private void hideViews(){
            if ( inHideState)
                return;
            inHideState = true;
            saveOriginalVisibility();
            for (View view : this){
                if ( view != null)
                    view.setVisibility(referencedViewsVisibility);
            }
        }

        private void saveOriginalVisibility(){
            for (View view : this) {
                if ( view != null)
                    view.setTag(R.id.VISIBILITY_KEY, view.getVisibility());
            }
        }
        private void restoreOriginalVisibility(){
            for (View view : this) {
                if ( view != null) {
                    if ( isForceDoneVisibilityEnabled) {
                        view.setVisibility(View.VISIBLE);
                    } else {
                        Integer visibility = (Integer) view.getTag(R.id.VISIBILITY_KEY); //if we put (Integer) with ? as if it will throw exception in case of null
                        if (visibility != null) {
                            view.setVisibility(visibility);
                        }
                    }
                }
            }
            inHideState = false;
        }
    }

    MultiStateLoadingLayout multiStateLoadingLayout = new MultiStateLoadingLayout(this);
    @Override
    public void setState(String id, LoadingState newState){
        multiStateLoadingLayout.setState(id, newState);
    }

    @Override
    public void setState(LoadingState state){
//        Log.e("DEBUG", "prev = " + this.state + " setState = " + state);
        this.state = state;
        if ( !ViewCompat.isAttachedToWindow(this))
            return;
        switch (state){
            case LOADING:
                setStateLoading();
                break;
            case LOADING_FAIL:
                setStateLoadingFail();
                break;
            case NO_DATA:
                setStateNoData();
                break;
            case DONE:
                setStateDone();
                break;
        }
        invalidate();
        requestLayout();
    }

    @Override
    public LoadingState getState() {
        return state;
    }

    @Override
    public LoadingState getState(String id) {
        return multiStateLoadingLayout.getState(id);
    }

    @Override
    public boolean isRetryEnabled(){
        return isRetryEnabled;
    }

    //TODO: we can refactor this...
    private void setStateDone() {
        referencedViews.restoreOriginalVisibility();
        loadingViewStub.setVisibility(GONE);
        loadingFailViewStub.setVisibility(GONE);
        loadingNoDataViewStub.setVisibility(GONE);
        setVisibility(isHideRootView ? GONE : VISIBLE);
    }

    private void setStateNoData() {
        referencedViews.hideViews();
        loadingViewStub.setVisibility(GONE);
        loadingFailViewStub.setVisibility(GONE);

        //one time lazy init
        if ( customNoDataStateLayout == 0 && inflatedLoadingNoDataView == null) {
            inflatedLoadingNoDataView = loadingNoDataViewStub.inflate();
            noDataImageView = (ImageView) inflatedLoadingNoDataView.findViewById(R.id.noDataImage);
            noDataMessage = (TextView) inflatedLoadingNoDataView.findViewById(R.id.noDataMessage);
            noDataActionButton = (Button) inflatedLoadingNoDataView.findViewById(R.id.noDataActionButton);

            setOnNoDataActionButtonClickListener();
            setNoDataActionButtonText();
            setNoImage();
            setNoDataText();
        }
        loadingNoDataViewStub.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
    }

    private void setStateLoadingFail() {
        referencedViews.hideViews();
        loadingViewStub.setVisibility(GONE);
        loadingNoDataViewStub.setVisibility(GONE);

        //one time lazy init
        if ( inflatedLoadingFailView == null) {
            inflatedLoadingFailView = loadingFailViewStub.inflate();
            if ( customLoadingStateLayout == 0) {
                loadingFailedMessage = (TextView) inflatedLoadingFailView.findViewById(R.id.loadingFailedMessage);
                setFailedText();
                loadingFailImageView = (ImageView) inflatedLoadingFailView.findViewById(R.id.loadingFailedImage);
                setFailImage();
            }
            //we assume that custom view has R.id.retryButton and it's a button if one's want a retry
            loadingFailedRetryButton = (Button) inflatedLoadingFailView.findViewById(R.id.retryButton);
            setIsRetryEnabled(isRetryEnabled);
            setFailedRetryButtonText();
            setOnFailedRetryButtonClickListener();
        }
        loadingFailViewStub.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
    }

    public void setFailedRetryButtonText(String failRetryText) {
        this.failRetryText = failRetryText;
        setFailedRetryButtonText();
    }
    private void setFailedRetryButtonText() {
        if ( loadingFailedRetryButton != null && !TextUtils.isEmpty(failRetryText)) { //view might not be inflated
            loadingFailedRetryButton.setText(failRetryText);
            invalidate();
            requestLayout();
        }
    }

    @Override
    public void setFailedText(String failText) {
        this.failText = failText;
        setFailedText();
    }
    private void setFailedText() {
        if (loadingFailedMessage != null && !TextUtils.isEmpty(failText)) { //view might not be inflated
            loadingFailedMessage.setText(failText);
            invalidate();
            requestLayout();
        }
    }

    private void setStateLoading() {
        referencedViews.hideViews();
        loadingFailViewStub.setVisibility(GONE);
        loadingNoDataViewStub.setVisibility(GONE);
        final int visibility = intermediateBlank ? GONE : progressBarVisibility ? VISIBLE : GONE;
        if ( inflatedLoadingView == null && visibility == VISIBLE){
            inflatedLoadingView = loadingViewStub.inflate(); //I'm aware setVisibility will inflate it but if you want to find a specific view in the future you must
            //use inflatedLoadingView.findViewById and not this.findViewById because of optional nested loadingLayouts
        }

        loadingViewStub.setVisibility(visibility);
        setVisibility(VISIBLE);
    }


    public void setNoDataText(String noDataText) {
        this.noDataText = noDataText;
        setNoDataText();
    }
    private void setNoDataText() {
        if ( noDataMessage != null && !TextUtils.isEmpty(noDataText)) {
            noDataMessage.setText(noDataText);
            invalidate();
            requestLayout();
        }
    }

    public void setNoDataImage(@DrawableRes int noDataImageId) {
        this.noDataImageId = noDataImageId;
        setNoImage();
    }

    private void setNoImage() {
        if ( noDataImageId != 0 && noDataImageView != null) {
            noDataImageView.setImageResource(noDataImageId);
            noDataImageView.setVisibility(VISIBLE);
            invalidate();
            requestLayout();
        }
        if ( hideImages && noDataImageView != null){
            noDataImageView.setVisibility(GONE);
        }
    }

    public void setFailImage(@DrawableRes int imageId) {
        this.failImageId = imageId;
        setFailImage();
    }

    private void setFailImage() {
        if ( failImageId != 0 && loadingFailImageView != null) {
            loadingFailImageView.setImageResource(failImageId);
            loadingFailImageView.setVisibility(VISIBLE);
            invalidate();
            requestLayout();
        }
        if ( hideImages && loadingFailImageView != null){
            loadingFailImageView.setVisibility(GONE);
        }
    }
    //**START OF FIX FOR SAVING VIEW PROPERLY*********************************************************************************************************
    //FIX FOR MULTIPLE COMPUND VIEWS IN A SINGLE LAYOUT
    //the problem is that upon saving and restoring the view it relys on the id of the view as a key in a single sparseArray, so multiple views will be saved
    //to the same key, and therefore only one of the views will be saved and will be restored to all of them.
    //the solution is to save a different sparseArray of each of this compound views.
    //see http://trickyandroid.com/saving-android-view-state-correctly/
    //TODO: still not perfect, will not resote the viewStubs correctly so any setFail text and stuff like that will not be remembered...
    @Override
    public Parcelable onSaveInstanceState() {
        //saves the children state in a different sparse array so the will be no collisions
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenState = new SparseArray<>(getChildCount()+1);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenState);
        }
        Bundle b = new Bundle();
        b.putString("LoadingState", getState().name());
        ss.childrenState.put(getId(), b);
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //restore the saved state from our inner sparseArray
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenState);
        }
        Parcelable p = ss.childrenState.get(getId());
        if ( p instanceof Bundle) {
            Bundle b = (Bundle) p;
            LoadingState loadingState = LoadingState.valueOf(b.getString("LoadingState"));
            setState(loadingState);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        //tells android that we handle the saving manually for the children
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        //tells android that we handle the restoring manually for the children
        dispatchThawSelfOnly(container);
    }

    /**
     * class for saving state of a custom view, look at BuySellSwitchAndSpinBox for example
     */
    public static class SavedState extends View.BaseSavedState {
        public SparseArray<Parcelable> childrenState;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenState = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray((SparseArray) childrenState);
        }

        @SuppressLint("NewApi")
        public static final Creator<SavedState> CREATOR
                = (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB_MR2) ? (new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }) : (new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
    }
    //**END OF FIX FOR SAVING VIEW PROPERLY**********************************************************************************************
}
