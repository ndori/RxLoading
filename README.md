# **RxLoading**

An RxJava library for showing a loading (i.e. progress bar) state while waiting for async data with minimal effort and advanced options.

**TL;DR;**
in your xml:

```xml
<com.ndori.loading.LoadingLayout
 android:id="@+id/loadingLayout"
 android:layout_width="match_parent"
 android:layout_height="match_parent">

<LinearLayout android:layout_width="match_parent"
              android:layout_height="match_parent">
    <!--....-->
</LinearLayout>
</com.ndori.loading.LoadingLayout>
```

in your java code:

`networkCall().
`**`compose(RxLoading.<>create(loadingLayout))`**`
    .subscribe(...);`

and you are done!

//TODO: add a gif here of loading

RxLoading will bind "loadingLayout" to your rx network call stream, when the stream is subscribed the layout will move to "loading" state, once an item arrives the loading is done.

This is the simplest and most frequent occurring flow, but RxLoading supports much more.

This library is made of 2 components:

 - **LoadingLayout** - a View which can be used to hide all or some of other views while it shows a progress bar.
	 - Error View- shows a view in case some error has occurred, can have a retry button and can be fully customized for you needs.
	 - 	 Empty  View - like error view in case you want to show something else when there is no data.
	 - 	Can wrap other views it wish to hide or can hide sibling views (white/black list) .
	 - 	Have some fine grained configuration for other needs (e.g. support multi users, show blank instead of progress bar...).
	 - Completely decoupled from RxJava, I might release it as a separate library if there will be request for it.
 - **RxLoading** - an RxJava Transformer which take care of all the binding logics and gives a generic way to incoprate it to most of streams out there.
	 - 	Can be altred to be used with any progress component out there (e.g. SwipeRefreshLayout..).
	 - 	You can choose to set the "loading state" by action (i.e. subscribe,next,error...) or by the emitted item (e.g. if list is empty set "Empty" state).
	 - 	You can also customize the view by the emitted item (e.g. if has error show "Error" state with a specific getErrorMessage() ).
	 - Plays well with finite and infinite streams.
	 - Supports multiple streams with one layout, and also multiple layouts with one stream.
	 - You can use retry logic seamlessly, it will resubscribe to the stream in case of a retry button being hit.
	 - And even more fine grained options...

## **LoadingLayout Usage**
Some of it's features are listed above, let's see how we use it.

it's basic functionality is the ability to hide and show other views, so while we load a page we can only show the relevant parts, hide the rest and when the data is ready we can show the views.

#### **wrap it:** 
```xml
<com.ndori.loading.LoadingLayout
 android:id="@+id/loadingLayout"
 android:layout_width="match_parent"
 android:layout_height="match_parent">

<LinearLayout android:layout_width="match_parent"
              android:layout_height="match_parent">
    <!--....-->
</LinearLayout>
</com.ndori.loading.LoadingLayout>
```
LoadingLayout is extending FrameLayout, so it can simply wrap the views you don't want to show yet.
This method is good enough for most cases but has some drawbacks, yet another view hierarchy, in android we try to flatten our layouts as much as possible. it show/hide all, sometimes we only want to hide certain views.

#### **Reference views:**
```xml
<android.support.constraint.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

    <com.ndori.loading.LoadingLayout
        app:layout_constraintRight_toRightOf="parent"
        app:referencedIds="description, link"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

    <TextView
        android:id="@+id/description"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_width="0dp"
        android:layout_height="wrap_content" />

    <TextView
        android:id="@+id/link"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/description"
        android:layout_width="0dp"
        android:layout_height="wrap_content" />

    <Button
        android:id="@+id/cancel"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/description"
        android:layout_width="0dp"
        android:layout_height="wrap_content" />
</android.support.constraint.ConstraintLayout>
```
Inspired by [Barriers](https://constraintlayout.com/basics/barriers.html) in ConstraintLayout, you can reference a list of view ids which will indicate which views to control.
In the above example while the screen is in loading state the 2 text views will be hidden but the button will be shown.
This behavior cannot be achieved with a simple wrap if "link", "description" and "cancel" need to constraint each other as they all need to be direct children of constraintLayout. 
related attributes you can use:

 - string **`referencedIds`** - list of ids to control, in the format of "id, id, ..."
 - boolean **`referenceSiblings`** - if set to true it will hide/show all it sibling, instead of using
        referenced_ids one by one.
 - boolean **`invertReferencedIds`** - used with referenceSiblings, if true referencedIds will act as white list of views not to control.

Using referenceSiblings you can get the same effect as wrap but without the overhead.

Other from that we got some more attributes to control the loading behavior: 

 - booelan **`progressBarVisibility`** - if set to false loading state will just be an invisible layout hiding other layouts.
 - int **`initSetStateDelayMilliseconds`** - default is 200, this is meant to prevent flickers, it will only set the initial state after this delay, in case of a very quick request we will not see the loading view that way.
 - boolean  **`forceDoneVisibility`** - by default when loading occurs it will save a snapshot of the visibility state of each view and will set it back upon completion. if set, it will set the visibility of all controlled views to Visible, one scenario could be in order to prevent flickering, you will set all the views to be hidden by default and it will make sure to make them visible when done.


**Error and Empty States**
loadingLayout can be set to 4 different states, we already covered "done" and "loading" states which are the main functionality. but what happen if we have some error in the process? or we got a special state that we want to show different view?
loadingLayout got 2 default views which will probably fit to most cases, they contain an image, a description and a button to take some action, it will usually be "retry" for error and some call to action in the empty state.
there are some attributes which let you modify this screens slightly:
 - drawable **`noDataImage`** - image for the empty state, you can also null it to remove it.
 - string **`noDataText`** - description for the empty state, you can remove it as well.
 - string **`noDataActionText`** - the text for the button in empty state, if not set no button will be shown.
 - drawable **`failImage`** - image for the error state, you can also null it to remove it. //TODO: add it.
 - string **`failText`** - description for the error state, you can remove it as well.
 - string **`failActionText`** - the text for the button in error state, if not set no button will be shown. //TODO: rename it.
 - booelan **`failActionEnabled`** - can be used to disable the button and not show it.

To have even more control you can also completely replace the layout for each of the states, keep in mind that unless you define the same ids for the views some functionality might not work (e.g. if it can't find the fail action id then it wouldn't be able to disable it).

 - layout **`customLoadingStateLayout`** - set it to replace the progress bar.
 - layout **`customNoDataStateLayout`** - for the empty state.
 - layout **`customLoadingFailedStateLayout`**- for the error state.

some code is needed in order to change it's state, add listeners etc.
although you can use it without `RxLoading` (and in some scenarios you might want to) it is advised to let `RxLoading` handle the heavy lifting, you can scroll down to find how to use on your own

## **RxLoading Usage**
Some of it's features are listed above, let's see how we use it.

`networkCall().
`**`compose(RxLoading.<>create(loadingLayout))`**`
    .subscribe(...);`

This class contains the logic for attaching the `loadingLayout` to a stream, it will take care of setting the states and even alter the views when needed, as such it needs to know `loadingLayout` internal, instead it only interacts with an interface called `ILoadingLayout` which `loadingLayout` implement, and by implementing this interface yourself you can make `RxLoading` interact with any progress component out there.

It does not have many options by itself, but it will alter it's behavior by the underlay `ILoadingLayout`  
The simplest usage as seen in the introduction. is to call `create(ILoadingLayout ILoadingLayout)`
and compose it with the rx stream which handles the layout data.

the basic behavior you will get is as follows:

 - `Loading` state is set upon `onSubscribe`
 - `LoadingFailed` state is set upon `onError`
 - `NoData` state is set upon `onCompleted` or `unsubscribe` only if was still in `loading` state (there was no item arrived or there was an error)
 - `LoadingDone` state is set each time an item is emitted, however only the first item will actually make an impact (unless you are changing the state of the `ILoadingLayout` manually)

RxLoading supports retry() of the entire stream and will retry if this conditions are met:

 1. ILoadingLayout has enabled the **`failActionEnabled`** attribute (enabled by default) and has an actual text (exists as "retry" by default).
 2. An error has happen an RxLoading has moved the state to LoadingFailed.
 3. The user has pressed the failAction/retry button.

But wait, there is more! clearly this simple scenario isn't enough, what if we got an error in the emitted item, or what if the emitted item is a list and we want to show a NoData state when it's empty?
and what if the emitted item can have several different errors and we want to show a different message for each?

enters `IStateProvider` and `IConfigurationProvider`

#### **`IStateProvider`**
this is the more simple provider, when an item is emitted the provider is being called and returns a state.
for example, let's say we emit an `Integer`, a positive number is valid, zero means special NoData state, and a negative number is...you guessed it an error.
all you need to do is add the following:
```java
class IntegerStateProvider implements IStateProvider<Integer>{
    @Override
    public LoadingState nextState(Integer integer) {
        return  integer > 0 ? LoadingState.DONE : 
                integer < 0 ? LoadingState.LOADING_FAIL:
                              LoadingState.NO_DATA;
    }
    @Override
    public String getFailedMessage(Integer integer) {
        return "you got an invalid number=" + integer; //different message for error
    }
    @Override
    public Boolean isRetryEnabled(Integer integer) {
        return integer < -10; //only integers below -10 is retryable
    }
}
```
and use it:

    compose(RxLoading.<>create(loadingLayout).setStateProvider(new IntegerStateProvider()))

Before implementing your own be sure to checkout `DefaultIStateProvider` , `CollectionStateProvider` and `MapStateProvider` 

#### **`IConfigurationProvider`**
for most cases `IStateProvider` will be enough, but if you need more control you can use the lower level `IConfigurationProvider`

instead of returning a `LoadingState`, you will now need to return a `ILoadingStateConfiguration` for each item emitted.
`ILoadingStateConfiguration` will be called getting the ILoadingLayout itself, so it can actually modify it for whatever it likes for each emitted item.
The reason for the indirection is that while the Configuration can be calculated in another thread the modification of the `IloadingLayout` must run on the main ui thread.

I recommend using `StateProvider` when possible and only use `ConfigurationProvider` when you must.

## **Advanced Usage**
#### **Subscribing before Views are created**
For most cases the simple usage will suffice, but what if we want to make a network call when a screen opens, a fragment has `onCreate` and `onCreateView` methods, we must attach the `ILoadingLayout` after it exists (which is only after `onCreateView` has been called) but we want to make the network call ASAP, before we render the screen so we want to call it in `onCreate` or even before.

We must compose `RxLoading` when the network is subscribed meaning in `onCreate`, but at that stage the views are not ready yet, so `loadingLayout` will still be null. more over there is a race here, between the rendering of the view and the return of the network call. 
RxLoading can take care of it, but we need a little more code for it.

```java
public class MyFragment extends Fragment {
    private RxLoading<Integer> rxLoading;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rxLoading = RxLoading.create();
        networkCall().compose(rxLoading).subscribe(...);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_layout, container);
        ILoadingLayout loadingLayout = (ILoadingLayout) root.findViewById(R.id.loadingLayout);
        rxLoading.bind(loadingLayout);
        ...
        return root;
    }
}
```
`Rxloading` will remember the state `ILoadingLayout` should be in and will set it once we bind it. 

Making sure the subscribe code of networkCall will run after `onCreateView` is out of this scope (hint [RxLifecycle](https://github.com/trello/RxLifecycle)) .


#### **2 Streams one LoadingLayout**
if we want several `loadingLayout` for to react to one stream it is easy, just create `RxLoading` instance for each `LoadingLayout` and compose each of them.
```java
networkCall().compose(RxLoading.<>create(loadingLayout1)).compose(RxLoading.<>create(loadingLayout2)).compose(RxLoading.<>create(loadingLayout3)).subscribe(...);
```

But what about the other direction? let's say we want one `loadingLayout` that covers the entire screen, but the data for that screen is actually composed out of 3 different network request?
Well this is a job for `loadingLayout`, it support multiple states, when you set a state for it you need to pass an id.
`loadingLayout` will then decide it state by combining all those state by priority:
`LoadingFailed` >> `Loading` >> `Done` >> `NoData`.
It's enough there is one failure to make `loadingLayout` show it has failed, all states must be `Done` in order for it to be in that state and so on... 
How you take care of it with `RxLoading`? it will take care of it for you, each `RxLoading` defines a different id so you can compose several `RxLoading` instances with a single `LoadingLayout`, cheers.
```java
networkCall1().compose(RxLoading.<>create(loadingLayout)).subscribe(...);
networkCall2().compose(RxLoading.<>create(loadingLayout)).subscribe(...);
networkCall3().compose(RxLoading.<>create(loadingLayout)).subscribe(...);
```

#### **ILoadingLayout logic side**
As I stated before, you can use LoadingLayout without RxLoading, for that ILoadingLayout exposes several methods to interact with (which RxLoading use), an example of usage can be a screen or logic which you don't use rxJava yet.

 - `setState` is the most important one, and as the name suggest you can
   change loadingLayout state. you should also consider it multi variant
   which accepts a unique id as well.
 - `addOnFailedActionButtonClickListener`/`removeOnFailedActionButtonClickListener`
   are pretty straight forward.
 - `setOnNoDataActionListener` as well.

For example:
```java
public class MyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_layout, container);
        ILoadingLayout loadingLayout = (ILoadingLayout) root.findViewById(R.id.loadingLayout);
        loadingLayout.setOnNoDataActionListener((view) -> closeScreen());
        askForData();
        ...
        return root;
    }

	public void askForData(){
		loadingLayout.setState(LoadingState.LOADING);
		...
	}

	public void onDataArrived(Integer integer){
	 if (integer < 0){
	     if ( integer < -10){
		     final OnClickListener listener = (view)->{
			  askForData();
			  loadingLayout.removeOnFailedActionButtonClickListener(listener);
			 }
			 loadingLayout.addOnFailedActionButtonClickListener(listener);
		 }
		 loadingLayout.setState(LoadingState.LOADING_FAIL);
		 }
		 return;
	 }
	 loadingLayout.setState(LoadingState.DONE);
	 ...
	}
}
```

**is something missing? got any questions? found a mistake? don't hesitate to contact me or open an issue.**
