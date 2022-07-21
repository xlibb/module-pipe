package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Observer class to get updated when a change occurs in Observables.
 */
public class Observer implements IObserver {
    Callback callback;
    AtomicBoolean atomicUpdate;
    public Observer(Callback callback) {
        this.callback = callback;
        this.atomicUpdate = new AtomicBoolean(false);
    }

    @Override
    public void update(Object o) {
        if (atomicUpdate.compareAndSet(false, true)) {
            this.callback.notifySuccess(o);
        }
    }

    @Override
    public void update(BError bError) {
        if (atomicUpdate.compareAndSet(false, true)) {
            this.callback.notifyFailure(bError);
        }
    }

    public void addCallback(Callback callback) {
        this.callback = callback;
    }
}
