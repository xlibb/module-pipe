package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Callback class for Closure Observers.
 */
public class ClosureCallback implements Callback {
    ConcurrentLinkedQueue<Object> queue;

    public ClosureCallback(ConcurrentLinkedQueue<Object> queue) {
        this.queue = queue;
    }

    @Override
    public void notifySuccess(Object o) {
        this.queue.clear();
        this.queue = null;
    }

    @Override
    public void notifyFailure(BError bError) {
        // never used
    }
}
