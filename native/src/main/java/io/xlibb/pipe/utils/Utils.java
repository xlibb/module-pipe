package io.xlibb.pipe.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;

import java.util.concurrent.CompletableFuture;

import static io.xlibb.pipe.utils.ModuleUtils.getModule;

/**
 * This class contains utility methods for the Pipe module.
 */
public class Utils {
    public static final String ERROR_TYPE = "Error";
    public static final String NATIVE_PIPE = "nativePipe";
    public static final BString NATIVE_TIMER_OBJECT = StringUtils.fromString("nativeTimerObject");
    public static final String RESULT_ITERATOR = "ResultIterator";
    public static final String STREAM_GENERATOR = "StreamGenerator";
    public static final String TIME_OUT = "timeOut";
    public static final String TIMER = "Timer";

    private Utils() {
    }

    public static BError createError(String message) {
        return ErrorCreator.createError(getModule(), ERROR_TYPE, StringUtils.fromString(message), null, null);
    }

    public static BError createError(String message, BError cause) {
        return ErrorCreator.createError(getModule(), ERROR_TYPE, StringUtils.fromString(message), cause, null);
    }

    public static Object getResult(CompletableFuture<Object> balFuture) {
        try {
            return balFuture.get();
        } catch (Throwable throwable) {
            throw ErrorCreator.createError(throwable);
        }
    }
}
