// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BTypedesc;
import org.nuvindu.pipe.observer.ClosureCallback;
import org.nuvindu.pipe.observer.ConsumerCallback;
import org.nuvindu.pipe.observer.Notifier;
import org.nuvindu.pipe.observer.Observable;
import org.nuvindu.pipe.observer.Observer;
import org.nuvindu.pipe.observer.ProducerCallback;
import org.nuvindu.pipe.observer.Timeout;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ConcurrentLinkedQueue<Object> queue;
    private final AtomicInteger queueSize;
    private final Long limit;
    private final AtomicBoolean isClosed;
    private final Observable producer;
    private final Observable consumer;
    private final Observable closure;
    private final Timeout timer;

    public Pipe(Long limit) {
        this.limit = limit;
        this.queue = new ConcurrentLinkedQueue<>();
        this.queueSize = new AtomicInteger(0);
        this.isClosed = new AtomicBoolean(false);
        this.consumer = new Observable();
        this.producer = new Observable();
        this.closure = new Observable();
        this.timer = new Timeout();
    }

    public BError asyncProduce(Environment env, Object events, BDecimal timeout) {
        if (events == null) {
            return createError("Nil values cannot be produced to a pipe.");
        } else if (this.isClosed.get()) {
            return createError("Events cannot be produced to a closed pipe.");
        }
        if (this.queueSize.get() == this.limit) {
            Future future = env.markAsync();
            Observer observer = new Observer(null);
            Callback callback = new ProducerCallback(future, this.producer, queueSize, queue, events, this.timer,
                                                     observer);
            observer.addCallback(callback);
            this.timer.registerObserver(observer);
            this.consumer.registerObserver(observer);
            Notifier notifier = new Notifier(this.timer, observer);
            this.timer.schedule(notifier, (long) timeout.floatValue() * 1000);
        } else {
            this.queue.add(events);
            this.queueSize.incrementAndGet();
            this.producer.notifyObservers(events);
        }
        return null;
    }

    public Object asyncConsume(Environment env, BDecimal timeout) {
        if (this.queue == null) {
            return createError("No any event is available in the closed pipe.");
        }
        if (this.queueSize.get() == 0) {
            this.closure.notifyObservers(null);
            Future future = env.markAsync();
            Observer observer = new Observer(null);
            Callback callback = new ConsumerCallback(future, this.consumer, queueSize, queue, this.timer,
                                                     observer);
            observer.addCallback(callback);
            this.producer.registerObserver(observer);
            this.timer.registerObserver(observer);

            Notifier notifier = new Notifier(this.timer, observer);
            this.timer.schedule(notifier, (long) timeout.floatValue() * 1000);
        } else {
            Object consumeObject = this.queue.remove();
            this.queueSize.decrementAndGet();
            this.consumer.notifyObservers(consumeObject);
            return consumeObject;
        }
        return null;
    }

    @Override
    public boolean isClosed() {
        return this.isClosed.get();
    }

    @Override
    public BError immediateClose() {
        if (this.isClosed.get()) {
            return createError("Closing of a closed pipe is not allowed.");
        }
        this.isClosed.compareAndSet(false, true);
        this.timer.cancel();
        this.queue = null;
        return null;
    }

    @Override
    public BError gracefulClose(BDecimal timeout) {
        if (this.isClosed.get()) {
            return createError("Closing of a closed pipe is not allowed.");
        }
        this.isClosed.compareAndSet(false, true);
        if (this.queueSize.get() != 0) {
            Observer observer = new Observer(null);
            Callback callback = new ClosureCallback(this.queue);
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    queue = null;
                }
            }, (long) timeout.floatValue() * 1000);
            observer.addCallback(callback);
            this.closure.registerObserver(observer);
        } else {
            this.timer.cancel();
            this.queue = null;
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

    public static Object consume(Environment env, BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        BHandle handle = (BHandle) pipe.get(JAVA_PIPE_OBJECT);
        Pipe javaPipe = (Pipe) handle.getValue();
        return javaPipe.asyncConsume(env, timeout);
    }

    public static BError produce(Environment env, BObject pipe, Object events, BDecimal timeout) {
        BHandle handle = (BHandle) pipe.get(JAVA_PIPE_OBJECT);
        Pipe javaPipe = (Pipe) handle.getValue();
        return javaPipe.asyncProduce(env, events, timeout);
    }
}
