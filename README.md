# RxLoading

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

`networkCall().`**`compose(RxLoading.<>create(loadingLayout))`**`
    .subscribe(...);`

and you are done!

//TODO: add a gif here of loading

RxLoading will bind "loadingLayout" to your rx network call stream, when the stream is subscribed the layout will move to "loading" state, once an item arrives the loading is done.

This is the simplest and most often occurring flow, but RxLoading supports much more.

This library is made of 2 components:

 - **LoadingLayout** - a View which can be used to hide all or some of other views while it shows a progress bar
	 - Error View- shows a view in case some error has occurred, can have a retry button and can be fully customized for you needs.
	 - 	 Empty  View - like error view in case you want to show something else when there is no data.
	 - 	Can wrap other views it wish to hide or can hide sibling views (white/black list) 
	 - 	Have some fine grained configuration for other needs (e.g. support multi users, show blank instead of progress bar...)
	 - Completely decoupled from RxJava, I might release it as a separate library if there will be request for it
 - **RxLoading** - an RxJava Transformer which take care of all the binding logics and gives a generic way to incoprate it to most of streams out there.
	 - 	Can be altred to be used with any progress component out there (e.g. SwipeRefreshLayout..)
	 - 	You can choose to set the "loading state" by action (i.e. subscribe,next,error...) or by the emitted item (e.g. if list is empty set "Empty" state).
	 - 	You can also customize the view by the emitted item (e.g. if has error show "Error" state with a specific getErrorMessage() ).
	 - Plays well with finite and infinite streams.
	 - Supports multiple streams with one layout, and also multiple layouts with one stream.
	 - You can use retry logic seamlessly, it will resubscribe to the stream in case of a retry button being hit.
	 - And even more fine grained options...

#LoadingLayout Usage
Some of it's features are listed above, let's see how we use it.

it's basic functionality is the ability to hide and show other views, so while we load a page we can only show the relevant parts and when the views are ready we can show them.

**wrap it:** 
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
This method is good enough for most cases but has some drawbacks, 

 - yet another view hierarchy, in android we try to flatten our layouts 
   as much as possible.
 - show/hide all, sometimes we only want to hide certain views.

**Reference views:**
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
        android:id="@+id/גescription"
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
        referenced_ids one by one
 - boolean **`invertReferencedIds`** - used with referenceSiblings, if true referencedIds will act as white list of views not to control

using  referenceSiblings you can get the same effect as wrap but without overhead.

other from that we got some more attributes to control the loading behavior: 

 - booelan **`progressBarVisibility`** - if set to false loading state will just be an invisible layout hiding other layouts
 - int **`initSetStateDelayMilliseconds`** - default is 200, this is meant to prevent flickers, it will only set the initial state after this delay, in case of a very quick request we will not see the loading view that way
 - boolean  **`forceDoneVisibility`** - by default when loading occurs it will save a snapshot of the visibility state of each view and will set it back upon completion. if set, it will set the visibility of all controlled views to Visible, one scenario could be in order to prevent flickering, you will set all the views to be hidden by default and it will make sure to make them visible when done.


**Error and Empty States**
loadingLayout can be set to 4 different states, we already covered "done" and "loading" states which are the main functionality. but what happen if we have some error in the process? or we got a special state that we want to show different view?
loadingLayout got 2 default views which will probably fit to most cases, they contain an image, a description and a button to take some action, it will usually be "retry" for error and some call to action in the empty state.
there are some attributes which let you modify this screens slightly:
 - drawable **`noDataImage`** - image for the empty state, you can also null it to remove it.
 - string **`noDataText`** - description for the empty state, you can remove it as well.
 - string **`noDataActionText`** - the text for the button in empty state, if not set no button will be shown.
 - drawable **`failImage`** - image for the error state, you can also null it to remove it. //TODO: add it
 - string **`failText`** - description for the error state, you can remove it as well.
 - string **`failActionText`** - the text for the button in error state, if not set no button will be shown. //TODO: rename it
 - booelan **`failActionEnabled`** - can be used to disable the button and not show it

To have even more control you can also completely replace the layout for each of the states, keep in mind that unless you define the same ids for the views some functionality might not work (e.g. if it can't find the fail action id then it wouldn't be able to disable it)   

 - layout **`customLoadingStateLayout`** - set it to replace the progress bar
 - layout **`customNoDataStateLayout`** - for the empty state
 - layout **`customLoadingFailedStateLayout`**- for the error state

