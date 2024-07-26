// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.xlibb.pipe;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BTypedesc;
import io.xlibb.pipe.observer.Callback;

import static io.xlibb.pipe.utils.Utils.NATIVE_PIPE;
import static io.xlibb.pipe.utils.Utils.TIME_OUT;
import static io.xlibb.pipe.utils.Utils.createError;

/**
 * Java implementation for the APIs of the stream returned from the pipe.
 */
public class ResultIterator {
    protected static final String CLOSED_PIPE_ERROR = "Attempting to close an already closed pipe";
    private static final String PRODUCE_TO_CLOSED_PIPE_ERROR = "Events cannot be consumed after the stream is closed";

    private ResultIterator() {
    }

    public static Object nextValue(Environment env, BObject streamGenerator, BTypedesc typeParam) {
        Pipe pipe = (Pipe) streamGenerator.getNativeData(NATIVE_PIPE);
        if (pipe != null) {
            Future future = env.markAsync();
            Callback observer = new Callback(future, pipe.getProducer(), pipe.getTimeKeeper(), pipe.getConsumer());
            long timeout = (long) streamGenerator.getNativeData(TIME_OUT);
            pipe.asyncConsume(observer, timeout, typeParam.getDescribingType());
            return null;
        }
        return createError(PRODUCE_TO_CLOSED_PIPE_ERROR);
    }

    public static BError close(Environment env, BObject streamGenerator) {
        long timeOut = (long) streamGenerator.getNativeData(TIME_OUT);
        Pipe pipe = ((Pipe) streamGenerator.getNativeData(NATIVE_PIPE));
        if (pipe == null) {
            return createError(CLOSED_PIPE_ERROR);
        }
        Future future = env.markAsync();
        Callback observer = new Callback(future, null, null, null);
        pipe.asyncClose(observer, timeOut);
        streamGenerator.addNativeData(NATIVE_PIPE, null);
        return null;
    }
}
