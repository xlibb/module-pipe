package io.xlibb.pipe.observer;

import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract APIs for Observable class.
 */
public interface IObservable {
    public void registerObserver(Callback o);

    public void unregisterObserver(Callback o);

    public void notifyObservers(Object object, ReentrantLock lock);

    public void notifyObservers(ReentrantLock lock);

    public void notifyObservers(BError bError, Callback callback);

    public void notifyObservers(boolean isEmpty);
}
