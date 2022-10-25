package io.xlibb.pipe.observer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Callback class to get updated when a change occurs in Observables.
 */
public class Callback implements IObserver {
    Future future;
    Observable timeKeeper;
    Object events;
    Observable observable;
    Observable notifyObservable;
    AtomicBoolean atomicUpdate;

    public Callback(Future future, Observable observable, Observable timeKeeper, Observable notifyObservable) {
        this.future = future;
        this.timeKeeper = timeKeeper;
        this.notifyObservable = notifyObservable;
        this.observable = observable;
        this.atomicUpdate = new AtomicBoolean(false);
    }

    public void setEvents(Object events) {
        this.events = events;
    }

    @Override
    public void onTimeout(BError bError) {
        if (atomicUpdate.compareAndSet(false, true)) {
            observable.unregisterObserver(this);
            timeKeeper.unregisterObserver(this);
            future.complete(bError);
        }
    }

    @Override
    public void onConsume(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize) {
        if (atomicUpdate.compareAndSet(false, true)) {
            queue.add(events);
            queueSize.incrementAndGet();
            this.notifyObservable.notifyObservers(events);
            this.timeKeeper.unregisterObserver(this);
            this.observable.unregisterObserver(this);
            onSuccess(null);
        }
    }

    @Override
    public void onProduce(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize) {
        if (atomicUpdate.compareAndSet(false, true)) {
            onSuccess(queue.remove());
            queueSize.decrementAndGet();
            this.notifyObservable.notifyObservers();
            this.observable.unregisterObserver(this);
            this.timeKeeper.unregisterObserver(this);
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
}
