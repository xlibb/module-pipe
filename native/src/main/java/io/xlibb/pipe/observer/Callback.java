package io.xlibb.pipe.observer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BError;
import io.xlibb.pipe.utils.Utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Callback class to get updated when a change occurs in Observables.
 */
public class Callback implements IObserver {
    private final Future future;
    private final Observable timeKeeper;
    private final Observable observable;
    private final Observable notifyObservable;
    private final AtomicBoolean atomicUpdate;
    private Object event;
    private Type type;

    public Callback(Future future, Observable observable, Observable timeKeeper, Observable notifyObservable) {
        this.future = future;
        this.timeKeeper = timeKeeper;
        this.notifyObservable = notifyObservable;
        this.observable = observable;
        this.atomicUpdate = new AtomicBoolean(false);
    }

    public void setEvent(Object event) {
        this.event = event;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void onTimeout(BError bError) {
        if (atomicUpdate.compareAndSet(false, true)) {
            observable.unregisterObserver(this);
            timeKeeper.unregisterObserver(this);
            onError(bError);
        }
    }

    @Override
    public void onConsume(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize, ReentrantLock lock) {
        if (atomicUpdate.compareAndSet(false, true)) {
            try {
                incrementQueue(queue, event, queueSize, lock);
                this.notifyObservable.notifyObservers(event, lock);
                this.timeKeeper.unregisterObserver(this);
                this.observable.unregisterObserver(this);
                onSuccess(null);
            } catch (Throwable throwable) {
                onError(Utils.createError(throwable.getMessage()));
            }
        }
    }

    @Override
    public void onProduce(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize, ReentrantLock lock) {
        if (atomicUpdate.compareAndSet(false, true)) {
            try {
                Object value = decrementQueue(queue, queueSize, lock);
                this.notifyObservable.notifyObservers(lock);
                this.observable.unregisterObserver(this);
                this.timeKeeper.unregisterObserver(this);
                onSuccess(ValueUtils.convert(value, type));
            } catch (Throwable throwable) {
                onError(Utils.createError(throwable.getMessage()));
            }
        }
    }

    @Override
    public void onError(BError bError) {
        this.future.complete(bError);
    }

    @Override
    public void onSuccess(Object object) {
        this.future.complete(object);
    }

    @Override
    public void onEmpty() {
        this.future.complete(null);
    }

    public static void incrementQueue(ConcurrentLinkedQueue<Object> queue,
                                      Object event, AtomicInteger size, ReentrantLock lock) {
        lock.lock();
        try {
            size.incrementAndGet();
            queue.add(event);
        } finally {
            lock.unlock();
        }
    }

    public static Object decrementQueue(ConcurrentLinkedQueue<Object> queue, AtomicInteger size, ReentrantLock lock) {
        lock.lock();
        try {
            size.decrementAndGet();
            return queue.remove();
        } finally {
            lock.unlock();
        }
    }
}
