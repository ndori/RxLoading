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
 - **RxLoading** - an RxJava Transformer which take care of all the binding logics and gives a generic way to incoprate it to most of streams out there.
	 - 	Can be altred to be used with any progress component out there (e.g. SwipeRefreshLayout..)
	 - 	You can choose to set the "loading state" by action (i.e. subscribe,next,error...) or by the emitted item (e.g. if list is empty set "Empty" state).
	 - 	You can also customize the view by the emitted item (e.g. if has error show "Error" state with a specific getErrorMessage() ).
	 - Plays well with finite and infinite streams.
	 - Supports multiple streams with one layout, and also multiple layouts with one stream.
	 - You can use retry logic seamlessly, it will resubscribe to the stream in case of a retry button being hit.
	 - And even more fine grained options...
## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the software and how to install them

```
Give examples
```

### Installing

A step by step series of examples that tell you have to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

## Contributing

Please don't be shy, ask question, open issues, merge, I'll try to give support as I can

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc

