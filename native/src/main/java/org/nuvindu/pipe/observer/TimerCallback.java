package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

/**
 * Callback class to execute after the timeout.
 */
public class TimerCallback implements Callback {
    Future future;
    Observable observable;
    Observer observer;
    public TimerCallback(Future future, Observable observable, Observer observer) {
        this.future = future;
        this.observable = observable;
        this.observer = observer;
    }

    @Override
    public void notifySuccess(Object o) {
        // never used
    }

    @Override
    public void notifyFailure(BError bError) {
        observable.unregisterObserver(this.observer);
        future.complete(bError);
    }
}
