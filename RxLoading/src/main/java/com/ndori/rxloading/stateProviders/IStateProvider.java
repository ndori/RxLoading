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

package com.ndori.rxloading.stateProviders;

import com.ndori.rxloading.IConfigurationProvider;
import com.ndori.rxloading.ILoadingLayout;

import static com.ndori.rxloading.ILoadingLayout.LoadingState;

/**
 * Created on 2017.
 * while similar to {@link IConfigurationProvider} it is a simpler variant. <br>
 * it allow you to decide the state by T, however it does not allow a more precise configuration. <br>
 * use case can be when T might represent error, empty and valid states let's say T is an object who <br>
 * has getResult() and isError() methods, that means that it can represent a valid state but also <br>
 * an invalid state. <br>
 * in that case we can use this interface to check for isError() and return an error state in a complete <br>
 * decoupled way. <br>
 * <br>
 * however if we have a method such as String getErrorMessage() and we want to show it we probably need <br>
 * to use {@link IConfigurationProvider} as it will allow to use {@link ILoadingLayout#setFailedText(String)} <br>
 */
public interface IStateProvider<T> {
    LoadingState nextState(T t);

    String getFailedMessage(T t);

    Boolean isRetryEnabled(T t);


}
