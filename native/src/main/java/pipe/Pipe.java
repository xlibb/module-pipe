package pipe;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.ballerina.runtime.pipe.utils.Utils.ERROR_TYPE;
import static io.ballerina.runtime.pipe.utils.Utils.createError;

/**
 * Provide APIs to exchange data concurrently.
 */
public class Pipe implements IPipe {
    final Lock lock = new ReentrantLock();
    final Condition notFull  = lock.newCondition();
    final Condition notEmpty = lock.newCondition();
    final Condition close = lock.newCondition();

    private List<Object> queue = new LinkedList<Object>();
    private final Long limit;
    private boolean isClosed = false;
    public Pipe(Long limit) {
        this.limit = limit;
    }

    @Override
    public BError produce(Object data, BDecimal timeout) throws InterruptedException {
        lock.lock();
        try {
            if (this.isClosed) {
                throw createError("Data cannot be produced to a closed pipe.", ERROR_TYPE);
            }
            while (this.queue.size() == this.limit) {
                if (!notFull.await((long) timeout.floatValue(), TimeUnit.SECONDS)) {
                    throw createError("Operation has timed out.", ERROR_TYPE);
                }
            }
            this.queue.add(data);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public Object consumeData(BDecimal timeout) throws InterruptedException {
        lock.lock();
        try {
            if (this.queue == null) {
                throw createError("No any data is available in the closed pipe.", ERROR_TYPE);
            }
            while (this.queue.size() == 0) {
                if (!notEmpty.await((long) timeout.floatValue(), TimeUnit.SECONDS)) {
                    throw createError("Operation has timed out.", ERROR_TYPE);
                }
            }
            notFull.signal();
            return this.queue.remove(0);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isClosed() {
        lock.lock();
        try {
            return this.isClosed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void immediateClose() {
        this.isClosed = true;
        this.queue = null;
    }

    @Override
    public void gracefulClose() throws InterruptedException {
        lock.lock();
        try {
            this.isClosed = true;
            while (this.queue.size() != 0) {
                if (!close.await(30, TimeUnit.SECONDS)) {
                    break;
                }
            }
        } finally {
            this.queue = null;
            lock.unlock();
        }
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
