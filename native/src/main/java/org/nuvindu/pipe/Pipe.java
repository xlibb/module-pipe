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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.UnionType;
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

import static org.nuvindu.pipe.utils.ModuleUtils.getModule;
import static org.nuvindu.pipe.utils.Utils.JAVA_PIPE_OBJECT;
import static org.nuvindu.pipe.utils.Utils.NATIVE_PIPE;
import static org.nuvindu.pipe.utils.Utils.RESULT_ITERATOR;
import static org.nuvindu.pipe.utils.Utils.STREAM_GENERATOR;
import static org.nuvindu.pipe.utils.Utils.TIME_OUT;
import static org.nuvindu.pipe.utils.Utils.createError;

/**
 * Provide APIs to exchange events concurrently.
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

    protected BError produceData(Object events, BDecimal timeout) {
        if (events == null) {
            return createError("Nil values cannot be produced to a pipe.");
        } else if (this.isClosed) {
            return createError("Events cannot be produced to a closed pipe.");
        }
        lock.lock();
        try {
            while (this.queue.size() == this.limit) {
                if (!notFull.await((long) timeout.floatValue(), TimeUnit.SECONDS)) {
                    return createError("Operation has timed out.");
                }
            }
            this.queue.add(events);
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
            return createError("No any event is available in the closed pipe.");
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
    public BError immediateClose() {
        if (this.isClosed) {
            return createError("Closing of a closed pipe is not allowed.");
        }
        this.isClosed = true;
        this.queue = null;
        return null;
    }

    @Override
    public BError gracefulClose(BDecimal timeout) {
        if (this.isClosed) {
            return createError("Closing of a closed pipe is not allowed.");
        }
        this.isClosed = true;
        lock.lock();
        try {
            while (this.queue.size() != 0) {
                if (!close.await((long) timeout.floatValue(), TimeUnit.SECONDS)) {
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

    public static BStream consumeStream(BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        UnionType typeUnion = TypeCreator.createUnionType(PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_ERROR);
        BObject resultIterator = ValueCreator.createObjectValue(getModule(), RESULT_ITERATOR);
        BObject streamGenerator = ValueCreator.createObjectValue(getModule(), STREAM_GENERATOR, resultIterator);
        BHandle handle = (BHandle) pipe.get(JAVA_PIPE_OBJECT);
        streamGenerator.addNativeData(NATIVE_PIPE, handle.getValue());
        streamGenerator.addNativeData(TIME_OUT, timeout);
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(typeParam.getDescribingType(), typeUnion),
                                              streamGenerator);
    }

    public static Object consume(BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        BHandle handle = (BHandle) pipe.get(JAVA_PIPE_OBJECT);
        Pipe javaPipe = (Pipe) handle.getValue();
        return javaPipe.consumeData(timeout);
    }

    public static BError produce(BObject pipe, Object events, BDecimal timeout) {
        BHandle handle = (BHandle) pipe.get(JAVA_PIPE_OBJECT);
        Pipe javaPipe = (Pipe) handle.getValue();
        return javaPipe.produceData(events, timeout);
    }
}
