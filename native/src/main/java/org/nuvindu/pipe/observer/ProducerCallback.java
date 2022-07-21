package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Callback class for Producer Observers.
 */
public class ProducerCallback implements Callback {
    Future future;
    Observable observable;
    AtomicInteger queueSize;
    ConcurrentLinkedQueue<Object> queue;
    Object events;
    Timeout timeout;
    Observer timeoutObserver;

    public ProducerCallback(Future future, Observable observable, AtomicInteger size,
                            ConcurrentLinkedQueue<Object> queue, Object events, Timeout timeout,
                            Observer timeoutObserver) {
        this.future = future;
        this.observable = observable;
        this.queueSize = size;
        this.queue = queue;
        this.events = events;
        this.timeout = timeout;
        this.timeoutObserver = timeoutObserver;
    }

    @Override
    public void notifySuccess(Object o) {
        this.queue.add(events);
        this.queueSize.incrementAndGet();
        this.observable.notifyObservers(o);
        this.timeout.unregisterObserver(timeoutObserver);
        this.future.complete(null);
    }

    @Override
    public void notifyFailure(BError bError) {
        this.future.complete(bError);
    }
}
