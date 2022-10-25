package io.xlibb.pipe.observer;

import io.ballerina.runtime.api.values.BError;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Observable class to notify observers when a change occurs.
 */
public class Observable implements IObservable {
    ArrayList<Callback> callbackList;
    ConcurrentLinkedQueue<Object> queue;
    AtomicInteger queueSize;

    public Observable(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize) {
        this.callbackList = new ArrayList<>();
        this.queue = queue;
        this.queueSize = queueSize;
    }

    @Override
    public void registerObserver(Callback o) {
        callbackList.add(o);
    }

    @Override
    public void unregisterObserver(Callback o) {
        callbackList.remove(o);
    }

    @Override
    public void notifyObservers(Object object) {
        if (!callbackList.isEmpty()) {
            callbackList.remove(0).onProduce(this.queue, this.queueSize);
        }
    }

    @Override
    public void notifyObservers() {
        if (!callbackList.isEmpty()) {
            callbackList.remove(0).onConsume(this.queue, this.queueSize);
        }
    }

    @Override
    public void notifyObservers(BError bError) {
        if (!callbackList.isEmpty()) {
            callbackList.remove(0).onTimeout(bError);
        }
    }

    @Override
    public void notifyObservers(BError bError, Callback callback) {
        callback.onTimeout(bError);
    }

    @Override
    public void notifyObservers(boolean isEmpty) {
        for (Callback callback: callbackList) {
            callback.onEmpty();
        }
        callbackList.clear();
    }
}
