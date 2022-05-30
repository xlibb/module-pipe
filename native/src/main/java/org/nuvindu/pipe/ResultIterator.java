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
            return gracefulClose;
        }
        return createError("Failed to close the stream.", gracefulClose);
    }
}
