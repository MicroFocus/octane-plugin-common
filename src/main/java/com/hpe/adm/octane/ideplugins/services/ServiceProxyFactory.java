/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpe.adm.octane.ideplugins.services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BooleanSupplier;

/**
 * Simple class that can be used for creating a proxy for a service
 * You can add services of the same type to the factory with an attached boolean provider
 * The created proxy object will choose which implementation to use when proxying the method call based on the boolean provider result
 * Note that each call of the proxy object's methods will call the boolean providers until one of the returns true
 * @param <T> type of Object you are proxying
 */
public class ServiceProxyFactory<T> {

    private BiMap<BooleanSupplier, T> serviceMap = HashBiMap.create();
    private Class<T> clazz;

    public class ProxyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            //determine the target service by calling the boolean supplier
            T service = serviceMap.keySet()
                    .stream()
                    .filter(BooleanSupplier::getAsBoolean)
                    .map(booleanSupplier -> serviceMap.get(booleanSupplier))
                    .findFirst()
                    .get();


            try {
                return method.invoke(service, args);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }

    public ServiceProxyFactory(Class<T> clazz){
        this.clazz = clazz;
    }

    public void addService(BooleanSupplier booleanSupplier, T service){
        serviceMap.put(booleanSupplier, service);
    }

    public void removeService(T service){
        serviceMap.inverse().remove(service);
    }

    public T getServiceProxy(){
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new ProxyInvocationHandler());
    }


}
