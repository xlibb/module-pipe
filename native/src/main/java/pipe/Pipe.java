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

import static io.ballerina.runtime.pipe.utils.ModuleUtils.getModule;
import static io.ballerina.runtime.pipe.utils.Utils.createError;

/**
 * Provide APIs to exchange data concurrently.
 */
public class Pipe implements IPipe {
    final Lock lock = new ReentrantLock(true);
    final Condition notFull  = lock.newCondition();
    final Condition notEmpty = lock.newCondition();
    final Condition close = lock.newCondition();

    private List<Object> queue = new LinkedList<>();
    private final Long limit;
    private boolean isClosed = false;
    public Pipe(Long limit) {
        this.limit = limit;
    }

    @Override
    public BError produce(Object data, BDecimal timeout) {
        if (this.isClosed) {
            return createError("Data cannot be produced to a closed pipe.");
        }
        lock.lock();
        try {
            while (this.queue.size() == this.limit) {
                if (!notFull.await((long) timeout.floatValue(), TimeUnit.SECONDS)) {
                    return createError("Operation has timed out.");
                }
            }
            this.queue.add(data);
            notEmpty.signal();
        } catch (InterruptedException e) {
            return createError("Operation has been interrupted.");
        } finally {
            lock.unlock();
        }
        return null;
    }

    protected Object consumeData(BDecimal timeout) {
        if (this.queue == null) {
            return createError("No any data is available in the closed pipe.");
        }
        lock.lock();
        try {
            while (this.queue.size() == 0) {
                if (this.isClosed) {
                    close.signal();
                }
                if (!notEmpty.await((long) timeout.floatValue(), TimeUnit.SECONDS)) {
                    return createError("Operation has timed out.");
                }
            }
            notFull.signal();
            return this.queue.remove(0);
        } catch (InterruptedException e) {
            return createError("Operation has been interrupted.");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public void immediateClose() {
        this.isClosed = true;
        this.queue = null;
    }

    @Override
    public BError gracefulClose() {
        this.isClosed = true;
        lock.lock();
        try {
            while (this.queue.size() != 0) {
                if (!close.await(30, TimeUnit.SECONDS)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            return createError("Operation has been interrupted.");
        } finally {
            this.queue = null;
            lock.unlock();
        }
        return null;
    }

    public static BStream consumeStream(Environment env, BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        UnionType typeUnion = TypeCreator.createUnionType(PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_ERROR);
        BObject resultIterator = ValueCreator.createObjectValue(getModule(), Constants.RESULT_ITERATOR);
        BObject streamGenerator = ValueCreator.createObjectValue(getModule(),
                                                                 Constants.STREAM_GENERATOR, resultIterator);
        BHandle handle = (BHandle) pipe.get(StringUtils.fromString(Constants.JAVA_PIPE_OBJECT));
        streamGenerator.addNativeData(Constants.NATIVE_PIPE, handle.getValue());
        streamGenerator.addNativeData(Constants.TIME_OUT, timeout);
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(typeParam.getDescribingType(), typeUnion),
                                              streamGenerator);
    }

    public static Object consume(BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        BHandle handle = (BHandle) pipe.get(StringUtils.fromString(Constants.JAVA_PIPE_OBJECT));
        Pipe javaPipe = (Pipe) handle.getValue();
        return (Object) javaPipe.consumeData(timeout);
    }
}
