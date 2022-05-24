package pipe;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BObject;

import static io.ballerina.runtime.pipe.utils.Utils.createError;

/**
 * Java implementation for the APIs of the stream returned from the pipe.
 */
public class ResultIterator {

    public static Object nextValue(BObject streamGenerator) throws InterruptedException {
        Pipe pipe = (Pipe) streamGenerator.getNativeData(Constants.NATIVE_PIPE);
        if (pipe != null) {
            BDecimal timeOut = (BDecimal) streamGenerator.getNativeData(Constants.TIME_OUT);
            return pipe.consumeData(timeOut);
        }
        return createError("Data cannot be consumed after the stream is closed");
    }

    public static void close(BObject streamGenerator) throws InterruptedException {
        ((Pipe) streamGenerator.getNativeData(Constants.NATIVE_PIPE)).gracefulClose();
        streamGenerator.addNativeData(Constants.NATIVE_PIPE, null);
    }
}
