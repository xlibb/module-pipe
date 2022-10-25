package io.xlibb.pipe.observer;

import io.ballerina.runtime.api.values.BError;

/**
 * Abstract APIs for Observable class.
 */
public interface IObservable {
    public void registerObserver(Callback o);
    public void unregisterObserver(Callback o);
    public void notifyObservers(Object object);
    public void notifyObservers();
    public void notifyObservers(BError bError);
    public void notifyObservers(BError bError, Callback callback);
    public void notifyObservers(boolean isEmpty);
}
