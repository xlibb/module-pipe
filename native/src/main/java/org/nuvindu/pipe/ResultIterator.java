// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.nuvindu.pipe;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

import static org.nuvindu.pipe.utils.Utils.createError;

/**
 * Java implementation for the APIs of the stream returned from the pipe.
 */
public class ResultIterator {

    public static Object nextValue(BObject streamGenerator) {
        Pipe pipe = (Pipe) streamGenerator.getNativeData(Constants.NATIVE_PIPE);
        if (pipe != null) {
            BDecimal timeOut = (BDecimal) streamGenerator.getNativeData(Constants.TIME_OUT);
            return pipe.consumeData(timeOut);
        }
        return createError("Events cannot be consumed after the stream is closed");
    }

    public static BError close(BObject streamGenerator) {
        BDecimal timeOut = (BDecimal) streamGenerator.getNativeData(Constants.TIME_OUT);
        Pipe pipe = ((Pipe) streamGenerator.getNativeData(Constants.NATIVE_PIPE));
        if (pipe == null) {
            return createError("Failed to close the stream.", 
                               createError("Closing of a closed pipe is not allowed."));
        }
        BError gracefulClose = pipe.gracefulClose(timeOut);
        if (gracefulClose == null) {
            streamGenerator.addNativeData(Constants.NATIVE_PIPE, null);
            return null;
        }
        return createError("Failed to close the stream.", gracefulClose);
    }
}
