package io.xlibb.pipe.observer;

import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract APIs for Callback class.
 */
public interface IObserver {

    void onTimeout(BError bError);

    void onConsume(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize, ReentrantLock lock);

    void onProduce(ConcurrentLinkedQueue<Object> queue, AtomicInteger queueSize, ReentrantLock lock);

    void onError(BError bError);

    void onSuccess(Object object);

    void onEmpty();
}
