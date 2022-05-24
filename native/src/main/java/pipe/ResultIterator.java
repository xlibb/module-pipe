package pipe;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

import static io.ballerina.runtime.pipe.utils.Utils.createError;

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
        return createError("Data cannot be consumed after the stream is closed");
    }

    public static BError close(BObject streamGenerator) {
        BError gracefulClose = ((Pipe) streamGenerator.getNativeData(Constants.NATIVE_PIPE)).gracefulClose();
        if (gracefulClose != null) {
            return createError("Failed to gracefully closed the pipe.");
        }
        streamGenerator.addNativeData(Constants.NATIVE_PIPE, null);
        return null;
    }
}
