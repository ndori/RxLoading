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

import com.ndori.rxloading.stateProviders.IStateProvider;

import static com.ndori.rxloading.ILoadingLayout.ILoadingStateConfiguration;

/**
 * Created on 2017.
 * while similar to {@link IStateProvider} it is a lower level variant, means you can have more control. <br>
 * it allow you to decide the configuration of a {@link ILoadingLayout} by T. <br>
 * use case can be when T might represent error, empty and valid states let's say T is an object who <br>
 * has getResult() and getErrorMessage() methods, that means that it can represent a valid state but also <br>
 * an invalid state. <br>
 * in that case we can use this interface to check for getErrorMessage() and set the error state with this message in a complete <br>
 * decoupled way. <br>
 * <br>
 * however if your state screens are not dynamic but only the decision of the state you are better off with {@link IStateProvider}
 */
public interface IConfigurationProvider<T> {
    /**
     * is used to get a specific configuration for a specific item, e.g. show different error messages for different items
     * @param t the item on which the configuration should be decided
     * @return a configuration of {@link ILoadingLayout} to be set with
     */
    ILoadingStateConfiguration nextConfiguration(T t);

//    class DefaultConfigurationProvider<T> implements IConfigurationProvider<T> {
//
//        final String uuid = String.valueOf(UUID.randomUUID()); //TODO: do we need the one of rxloading?
//        private final ILoadingStateConfiguration defaultConfiguration = new DoneILoadingStateConfiguration(uuid);
//
//        @Override
//        public ILoadingStateConfiguration nextConfiguration(T t) {
//            return defaultConfiguration;
//        }
//    }
}
