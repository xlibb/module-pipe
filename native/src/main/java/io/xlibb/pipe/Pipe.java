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

package io.xlibb.pipe;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BTypedesc;
import io.xlibb.pipe.observer.Callback;
import io.xlibb.pipe.observer.Notifier;
import io.xlibb.pipe.observer.Observable;
import io.xlibb.pipe.thread.WorkerThreadPool;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.xlibb.pipe.ResultIterator.CLOSED_PIPE_ERROR;
import static io.xlibb.pipe.thread.WorkerThreadPool.MAX_POOL_SIZE;
import static io.xlibb.pipe.utils.ModuleUtils.getModule;
import static io.xlibb.pipe.utils.Utils.NATIVE_PIPE;
import static io.xlibb.pipe.utils.Utils.NATIVE_TIMER_OBJECT;
import static io.xlibb.pipe.utils.Utils.RESULT_ITERATOR;
import static io.xlibb.pipe.utils.Utils.STREAM_GENERATOR;
import static io.xlibb.pipe.utils.Utils.TIMER;
import static io.xlibb.pipe.utils.Utils.TIME_OUT;
import static io.xlibb.pipe.utils.Utils.createError;

/**
 * Provide APIs to exchange events concurrently.
 */
public class Pipe {
    private static final BDecimal MILLISECONDS_FACTOR = ValueCreator.createDecimalValue(new BigDecimal(1000));
    private static final ExecutorService PIPE_EXECUTOR_SERVICE = new ThreadPoolExecutor(0, MAX_POOL_SIZE,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new WorkerThreadPool.PipeThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    private static final String PRODUCE_NIL_ERROR = "Nil values must not be produced to a pipe";
    private static final String PRODUCE_TO_CLOSED_PIPE = "Events must not be produced to a closed pipe";
    private static final String NEGATIVE_TIMEOUT_ERROR = "Graceful close must provide a timeout of 0 or greater";
    private static final String TIMEOUT_ERROR = "Timeout must be -1 or greater. Provided: %s";
    private static final String INVALID_TIMEOUT_ERROR = "Invalid timeout value provided";
    private static final long INFINITE_WAIT = -1;
    public static final String PIPE_OBJECT = "nativePipeObject";
    private final AtomicBoolean isClosed;
    private final AtomicInteger queueSize;
    private final Long limit;
    private final Object consumeLock = new Object();
    private final Object produceLock = new Object();
    private final Observable consumer;
    private final Observable emptyQueue;
    private final Observable producer;
    private final Observable timeKeeper;
    private final Timer timer;
    private ConcurrentLinkedQueue<Object> queue;

    public Pipe(Long limit) {
        this(limit, ValueCreator.createObjectValue(getModule(), TIMER));
    }

    public Pipe(Long limit, BObject timer) {
        this.limit = limit;
        this.timer = (Timer) ((BHandle) timer.get(NATIVE_TIMER_OBJECT)).getValue();
        this.queue = new ConcurrentLinkedQueue<>();
        this.queueSize = new AtomicInteger(0);
        this.isClosed = new AtomicBoolean(false);
        this.consumer = new Observable(this.queue, this.queueSize);
        this.producer = new Observable(this.queue, this.queueSize);
        this.emptyQueue = new Observable(null, null);
        this.timeKeeper = new Observable(null, null);
    }

    public static void generatePipeWithTimer(BObject pipe, Long limit, BObject timer) {
        pipe.addNativeData(PIPE_OBJECT, new Pipe(limit, timer));
    }

    public static void generatePipe(BObject pipe, Long limit) {
        pipe.addNativeData(PIPE_OBJECT, new Pipe(limit));
    }

    public static Object consumeStream(BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        long timeoutInMillis;
        try {
            timeoutInMillis = getTimeoutInMillis(timeout);
        } catch (BError bError) {
            return createError(INVALID_TIMEOUT_ERROR, bError);
        }
        UnionType typeUnion = TypeCreator.createUnionType(PredefinedTypes.TYPE_NULL, PredefinedTypes.TYPE_ERROR);
        BObject resultIterator = ValueCreator.createObjectValue(getModule(), RESULT_ITERATOR, typeParam);
        BObject streamGenerator = ValueCreator.createObjectValue(getModule(), STREAM_GENERATOR, resultIterator);
        streamGenerator.addNativeData(NATIVE_PIPE, pipe.getNativeData(PIPE_OBJECT));
        streamGenerator.addNativeData(TIME_OUT, timeoutInMillis);
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(typeParam.getDescribingType(), typeUnion),
                streamGenerator);
    }

    public static BError produce(Environment env, BObject pipe, Object event, BDecimal timeout) {
        Future future = env.markAsync();
        try {
            long timeoutInMillis = getTimeoutInMillis(timeout);
            Pipe nativePipe = (Pipe) pipe.getNativeData(PIPE_OBJECT);
            Callback observer = new Callback(future, nativePipe.getConsumer(), nativePipe.getTimeKeeper(),
                    nativePipe.getProducer());
            PIPE_EXECUTOR_SERVICE.execute(() -> nativePipe.asyncProduce(observer, event, timeoutInMillis));
        } catch (BError bError) {
            future.complete(createError(INVALID_TIMEOUT_ERROR, bError));
        } catch (Exception e) {
            future.complete(createError(e.getMessage()));
        }
        return null;
    }

    public static Object consume(Environment env, BObject pipe, BDecimal timeout, BTypedesc typeParam) {
        Future future = env.markAsync();
        try {
            long timeoutInMillis = getTimeoutInMillis(timeout);
            Pipe nativePipe = (Pipe) pipe.getNativeData(PIPE_OBJECT);
            Callback observer = new Callback(future, nativePipe.getProducer(), nativePipe.getTimeKeeper(),
                    nativePipe.getConsumer());
            PIPE_EXECUTOR_SERVICE.execute(() -> {
                nativePipe.asyncConsume(observer, timeoutInMillis, typeParam.getDescribingType());
            });
        } catch (BError bError) {
            future.complete(createError(INVALID_TIMEOUT_ERROR, bError));
        } catch (Exception e) {
            future.complete(createError(e.getMessage()));
        }
        return null;
    }

    public static BError gracefulClose(Environment env, BObject pipe, BDecimal timeout) {
        Future future = env.markAsync();
        try {
            long timeoutInMillis = getTimeoutInMillis(timeout);
            Pipe nativePipe = (Pipe) pipe.getNativeData(PIPE_OBJECT);
            Callback observer = new Callback(future, null, null, null);
            PIPE_EXECUTOR_SERVICE.execute(() -> nativePipe.asyncClose(observer, timeoutInMillis));
        } catch (BError bError) {
            future.complete(createError(INVALID_TIMEOUT_ERROR, bError));
        } catch (Exception e) {
            future.complete(createError(e.getMessage()));
        }
        return null;
    }

    private static long getTimeoutInMillis(BDecimal timeout) {
        if (timeout.floatValue() == -1) {
            return INFINITE_WAIT;
        } else if (timeout.floatValue() < 0) {
            throw createError(String.format(TIMEOUT_ERROR, timeout));
        }
        BDecimal timeoutInMillis = timeout.multiply(MILLISECONDS_FACTOR);
        return Double.valueOf(timeoutInMillis.floatValue()).longValue();
    }

    protected void asyncProduce(Callback callback, Object event, long timeout) {
        if (event == null) {
            callback.onError(createError(PRODUCE_NIL_ERROR));
            return;
        }
        if (this.isClosed.get()) {
            callback.onError(createError(PRODUCE_TO_CLOSED_PIPE));
            return;
        }
        synchronized (this.produceLock) {
            if (this.queueSize.get() == this.limit) {
                callback.setEvent(event);
                this.consumer.registerObserver(callback);
                this.scheduleAction(callback, timeout);
            } else {
                queue.add(event);
                queueSize.incrementAndGet();
                this.producer.notifyObservers(event);
                callback.onSuccess(null);
            }
        }
    }

    protected void asyncConsume(Callback callback, long timeout, Type type) {
        if (this.queue == null) {
            callback.onSuccess(null);
            return;
        }
        synchronized (this.consumeLock) {
            if (this.queueSize.get() == 0) {
                this.emptyQueue.notifyObservers(true);
                this.producer.registerObserver(callback);
                this.scheduleAction(callback, timeout);
            } else {
                try {
                    queueSize.decrementAndGet();
                    this.consumer.notifyObservers();
                    Object value = ValueUtils.convert(queue.remove(), type);
                    callback.onSuccess(value);
                } catch (Exception e) {
                    callback.onError(createError(e.getMessage()));
                }
            }
        }
    }

    public static boolean isClosed(BObject pipe) {
        Pipe nativePipe = (Pipe) pipe.getNativeData(PIPE_OBJECT);
        return nativePipe.getIsClosed().get();
    }

    public static BError immediateClose(BObject pipe) {
        Pipe nativePipe = (Pipe) pipe.getNativeData(PIPE_OBJECT);
        if (nativePipe.getIsClosed().get()) {
            return createError(CLOSED_PIPE_ERROR);
        }
        nativePipe.getIsClosed().compareAndSet(false, true);
        nativePipe.nullifyQueue();
        return null;
    }

    protected void asyncClose(Callback callback, long timeout) {
        if (this.isClosed.get()) {
            callback.onError(createError(CLOSED_PIPE_ERROR));
        } else if (timeout == -1) {
            callback.onError(createError(NEGATIVE_TIMEOUT_ERROR));
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
                    }
                }, timeout);
            } else {
                this.queue = null;
                callback.onSuccess(null);
            }
        }
    }

    private void scheduleAction(Callback callback, long timeout) {
        if (timeout == INFINITE_WAIT) {
            return;
        }
        this.timeKeeper.registerObserver(callback);
        Notifier notifier = new Notifier(this.timeKeeper, callback);
        this.timer.schedule(notifier, timeout);
    }

    protected Observable getProducer() {
        return producer;
    }

    protected Observable getConsumer() {
        return consumer;
    }

    protected Observable getTimeKeeper() {
        return timeKeeper;
    }

    protected AtomicBoolean getIsClosed() {
        return this.isClosed;
    }

    protected void nullifyQueue() {
        this.queue = null;
    }
}
