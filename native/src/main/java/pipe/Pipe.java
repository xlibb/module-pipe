package pipe;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.LinkedList;
import java.util.List;

/**
 * Provide APIs to exchange data concurrently.
 */
public class Pipe implements IPipe {

    private List<Object> queue = new LinkedList<Object>();
    private final Long limit;
    private boolean isClosed = false;
    public Pipe(Long limit) {
        this.limit = limit;
    }

    @Override
    public synchronized BError produce(Object data, BDecimal timeout) throws InterruptedException {
        if (this.isClosed) {
            return ErrorCreator.createError(StringUtils.fromString("Data cannot be produced to a closed pipe."));
        }
        while (this.queue.size() == this.limit) {
            wait((long) (timeout.floatValue() * 1000));
        }
        if (this.queue.size() == 0) {
            notifyAll();
        }
        this.queue.add(data);
        return null;
    }

    @Override
    public synchronized Object consumeData(BDecimal timeout) throws InterruptedException {
        if (this.queue == null) {
            return ErrorCreator.createError(StringUtils.fromString("No any data is available in the closed pipe."));
        }
        while (this.queue.size() == 0) {
            wait((long) (timeout.floatValue() * 1000));
        }
        if (this.queue.size() == this.limit) {
            notifyAll();
        }
        return this.queue.remove(0);
    }

    @Override
    public synchronized boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public synchronized void immediateClose() {
        this.isClosed = true;
        this.queue = null;
    }

    @Override
    public synchronized void gracefulClose() throws InterruptedException {
        this.isClosed = true;
        while (this.queue.size() != 0) {
            wait(30000);
        }
        this.queue = null;
    }

    public static BStream consumeStream(Environment env, BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        UnionType typeUnion = TypeCreator.createUnionType(PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_ERROR);
        BObject resultIterator = ValueCreator.createObjectValue(env.getCurrentModule(), Constants.RESULT_ITERATOR);
        BObject streamGenerator = ValueCreator.createObjectValue(env.getCurrentModule(),
                                                                 Constants.STREAM_GENERATOR, resultIterator);
        BHandle handle = (BHandle) pipe.get(StringUtils.fromString(Constants.JAVA_PIPE_OBJECT));
        streamGenerator.addNativeData(Constants.NATIVE_PIPE, handle.getValue());
        streamGenerator.addNativeData(Constants.TIME_OUT, timeout);
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(typeParam.getDescribingType(), typeUnion),
                                              streamGenerator);
    }

    public static Object consume(BObject pipe, BDecimal timeout, BTypedesc typeParam) throws InterruptedException {
        BHandle handle = (BHandle) pipe.get(StringUtils.fromString(Constants.JAVA_PIPE_OBJECT));
        Pipe javaPipe = (Pipe) handle.getValue();
        return (Object) javaPipe.consumeData(timeout);
    }
}
