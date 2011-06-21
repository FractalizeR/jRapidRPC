/*
 * ========================================================================
 * Copyright (c) 2011 Vladislav "FractalizeR" Rastrusny
 * Website: http://www.fractalizer.ru
 * Email: FractalizeR@yandex.ru
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

package ru.fractalizer.jrapidrpc.tools;

import com.dyuproject.protostuff.runtime.RuntimeSchema;
import ru.fractalizer.jrapidrpc.api.RpcAfterConnect;
import ru.fractalizer.jrapidrpc.api.RpcMethod;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Class for caching object methods data
 */
public class ReflectionCache {

    private HashMap<Short, Method> methodIdToMethod;
    private HashMap<Short, String> methodIdToName;
    private HashMap<String, Short> methodNameToId;

    private Method afterConnectMethod;

    /**
     * Constructor
     *
     * @param serviceInterface Interface to cache methods from
     */
    public ReflectionCache(Class<?> serviceInterface) {
        if (!serviceInterface.isInterface()) {
            throw new ClassFormatError(String.format("Class '%s' is not an interface!", serviceInterface.getName()));
        }
        Method[] methods = serviceInterface.getMethods();

        methodIdToName = new HashMap<Short, String>(methods.length);
        methodIdToMethod = new HashMap<Short, Method>(methods.length);
        methodNameToId = new HashMap<String, Short>(methods.length);

        for (Method method : methods) {

            //Checking for prelogin annotation
            RpcAfterConnect afterConnectAnnotation = method.getAnnotation(RpcAfterConnect.class);
            if (afterConnectAnnotation != null) {
                this.afterConnectMethod = method;
            }

            //Getting methodId
            RpcMethod methodIdAnnotation = method.getAnnotation(RpcMethod.class);
            if (methodIdAnnotation != null) {
                Short methodId = methodIdAnnotation.methodId();

                //Reading parameter types and their schemas
                Class<?>[] parameterTypes = method.getParameterTypes();
                for (Class<?> parameterType : parameterTypes) {
                    //Caching schemas. No need to store them
                    RuntimeSchema.getSchema(parameterType.getClass());
                }

                //Putting everything to collections
                methodIdToMethod.put(methodId, method);
                methodIdToName.put(methodId, method.getName());
                methodNameToId.put(method.getName(), methodId);
            }
        }
    }


    /**
     * Returns reflection method info for the given method name
     *
     * @param methodName The name of the method
     * @return Reflection data for a given method
     */
    public Method getMethodMethod(String methodName) {
        return methodIdToMethod.get(methodNameToId.get(methodName));
    }

    /**
     * Returns method ID by its name
     *
     * @param methodName The name of the method
     * @return Method it
     */
    public short getMethodId(String methodName) {
        return methodNameToId.get(methodName);
    }

    /**
     * Returns method name by its ID
     *
     * @param methodId Id of the method
     * @return Method name
     */
    public String getMethodName(short methodId) {
        return methodIdToName.get(methodId);
    }

    public Method getAfterConnectMethod() {
        return afterConnectMethod;
    }
}
