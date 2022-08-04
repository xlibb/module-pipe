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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BTypedesc;
import org.nuvindu.pipe.observer.Callback;
import org.nuvindu.pipe.observer.Notifier;
import org.nuvindu.pipe.observer.Observable;
import org.nuvindu.pipe.observer.TimeKeeper;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.nuvindu.pipe.utils.ModuleUtils.getModule;
import static org.nuvindu.pipe.utils.Utils.NATIVE_PIPE;
import static org.nuvindu.pipe.utils.Utils.NATIVE_PIPE_OBJECT;
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
    private final Observable emptyQueue;
    private final TimeKeeper timer;

    public Pipe(Long limit) {
        this.limit = limit;
        this.queue = new ConcurrentLinkedQueue<>();
        this.queueSize = new AtomicInteger(0);
        this.isClosed = new AtomicBoolean(false);
        this.consumer = new Observable(this.queue, this.queueSize);
        this.producer = new Observable(this.queue, this.queueSize);
        this.emptyQueue = new Observable(null, null);
        this.timer = new TimeKeeper();
    }

    public Pipe(Long limit, TimeKeeper timeKeeper) {
        this.limit = limit;
        this.queue = new ConcurrentLinkedQueue<>();
        this.queueSize = new AtomicInteger(0);
        this.isClosed = new AtomicBoolean(false);
        this.consumer = new Observable(this.queue, this.queueSize);
        this.producer = new Observable(this.queue, this.queueSize);
        this.emptyQueue = new Observable(null, null);
        this.timer = timeKeeper;
    }

    protected void asyncProduce(Callback callback, Object events, BDecimal timeout) {
        if (events == null) {
            callback.onError(createError("Nil values cannot be produced to a pipe."));
        } else if (this.isClosed.get()) {
            callback.onError(createError("Events cannot be produced to a closed pipe."));
        } else if (this.queueSize.get() == this.limit) {
            callback.setEvents(events);
            this.timer.registerObserver(callback);
            this.consumer.registerObserver(callback);
            Notifier notifier = new Notifier(this.timer, callback);
            this.timer.schedule(notifier, (long) timeout.floatValue() * 1000);
        } else {
            queue.add(events);
            queueSize.incrementAndGet();
            this.producer.notifyObservers(events);
            callback.onSuccess(null);
        }
    }

    protected void asyncConsume(Callback callback, BDecimal timeout) {
        if (this.queue == null) {
            callback.onError(createError("No any event is available in the closed pipe."));
        } else if (this.queueSize.get() == 0) {
            this.emptyQueue.notifyObservers(true);
            this.producer.registerObserver(callback);
            this.timer.registerObserver(callback);
            Notifier notifier = new Notifier(this.timer, callback);
            this.timer.schedule(notifier, (long) timeout.floatValue() * 1000);
        } else {
            queueSize.decrementAndGet();
            callback.onSuccess(queue.remove());
            this.consumer.notifyObservers();
        }
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

    protected void asyncClose(Callback callback, BDecimal timeout) {
        if (this.isClosed.get()) {
            callback.onError(createError("Closing of a closed pipe is not allowed."));
        } else {
            this.isClosed.compareAndSet(false, true);
            if (this.queueSize.get() != 0) {
                emptyQueue.registerObserver(callback);
                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        queue = null;
                        emptyQueue.unregisterObserver(callback);
                        callback.onSuccess(null);
                        this.cancel();
                    }
                }, (long) timeout.floatValue() * 1000);
            } else {
                this.queue = null;
                callback.onSuccess(null);
            }
        }
    }

    public static BStream consumeStream(BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        UnionType typeUnion = TypeCreator.createUnionType(PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_ERROR);
        BObject resultIterator = ValueCreator.createObjectValue(getModule(), RESULT_ITERATOR);
        BObject streamGenerator = ValueCreator.createObjectValue(getModule(), STREAM_GENERATOR, resultIterator);
        BHandle handle = (BHandle) pipe.get(NATIVE_PIPE_OBJECT);
        streamGenerator.addNativeData(NATIVE_PIPE, handle.getValue());
        streamGenerator.addNativeData(TIME_OUT, timeout);
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(typeParam.getDescribingType(), typeUnion),
                                              streamGenerator);
    }

    public static BError produce(Environment env, BObject pipe, Object events, BDecimal timeout) {
        BHandle handle = (BHandle) pipe.get(NATIVE_PIPE_OBJECT);
        Pipe nativePipe = (Pipe) handle.getValue();
        Future future = env.markAsync();
        Callback observer = new Callback(future, nativePipe.getConsumer(), nativePipe.getTimer(),
                nativePipe.getProducer());
        nativePipe.asyncProduce(observer, events, timeout);
        return null;
    }

    public static Object consume(Environment env, BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        BHandle handle = (BHandle) pipe.get(NATIVE_PIPE_OBJECT);
        Pipe nativePipe = (Pipe) handle.getValue();
        Future future = env.markAsync();
        Callback observer = new Callback(future, nativePipe.getProducer(), nativePipe.getTimer(),
                                         nativePipe.getConsumer());
        nativePipe.asyncConsume(observer, timeout);
        return null;
    }

    public static BError gracefulClose(Environment env, BObject pipe, BDecimal timeout) {
        BHandle handle = (BHandle) pipe.get(NATIVE_PIPE_OBJECT);
        Pipe nativePipe = (Pipe) handle.getValue();
        Future future = env.markAsync();
        Callback observer = new Callback(future, null, null, null);
        nativePipe.asyncClose(observer, timeout);
        return null;
    }

    protected Observable getProducer() {
        return producer;
    }

    protected Observable getConsumer() {
        return consumer;
    }

    protected TimeKeeper getTimer() {
        return timer;
    }
}
