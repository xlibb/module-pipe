package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Callback class for Consumer Observers.
 */
public class ConsumerCallback implements Callback {
    Future future;
    Observable observable;
    AtomicInteger queueSize;
    ConcurrentLinkedQueue<Object> queue;
    Timeout timeout;
    Observer timeoutObserver;
    public ConsumerCallback(Future future, Observable observable, AtomicInteger size,
                            ConcurrentLinkedQueue<Object> queue, Timeout timeout, Observer timeoutObserver) {
        this.future = future;
        this.observable = observable;
        this.queueSize = size;
        this.queue = queue;
        this.timeout = timeout;
        this.timeoutObserver = timeoutObserver;
    }

    @Override
    public void notifySuccess(Object o) {
        this.future.complete(queue.remove());
        this.queueSize.decrementAndGet();
        this.observable.notifyObservers(o);
        this.timeout.unregisterObserver(timeoutObserver);
    }

    @Override
    public void notifyFailure(BError bError) {
        this.future.complete(bError);
    }
}
