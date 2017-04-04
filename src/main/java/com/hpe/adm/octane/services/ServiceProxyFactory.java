package com.hpe.adm.octane.services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.InvocationHandler;
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

            System.out.println("Using service: " + service.getClass().getCanonicalName());

            return method.invoke(service, args);
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
