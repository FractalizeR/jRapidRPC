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

package ru.fractalizer.jrapidrcdemo;

public class ServiceObject implements ServiceInterface {

    @Override
    public void voidMethod() {
        //Doing nothing
    }

    @Override
    public String mirror(String arg) {
        return arg;
    }

    @Override
    public void exceptionTest() throws Exception {
        throw new Exception("This is a test exception message!");
    }

    @Override
    public ComplexType complexTypeMethod(ComplexType arg) {
        arg.testField1++;
        arg.testField2 = !arg.testField2;
        return arg;
    }
}
