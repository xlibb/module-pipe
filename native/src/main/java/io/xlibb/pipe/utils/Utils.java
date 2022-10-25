package io.xlibb.pipe.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;

import static io.xlibb.pipe.utils.ModuleUtils.getModule;

/**
 *  This class contains utility methods for the Pipe module.
 */
public class Utils {

    private Utils() {
    }

    // Internal type names
    public static final String ERROR_TYPE = "Error";
    public static final BString NATIVE_PIPE_OBJECT = StringUtils.fromString("nativePipeObject");
    public static final BString NATIVE_TIMER_OBJECT = StringUtils.fromString("nativeTimerObject");
    public static final String NATIVE_PIPE = "nativePipe";
    public static final String TIMER = "Timer";
    public static final String RESULT_ITERATOR = "ResultIterator";
    public static final String STREAM_GENERATOR = "StreamGenerator";
    public static final String TIME_OUT = "timeOut";
    
    public static BError createError(String message) {
        return ErrorCreator.createError(getModule(), ERROR_TYPE, StringUtils.fromString(message), null, null);
    }

    public static BError createError(String message, BError cause) {
        return ErrorCreator.createError(getModule(), ERROR_TYPE, StringUtils.fromString(message), cause, null);
    }
}
