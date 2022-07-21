package org.nuvindu.pipe.observer;

/**
 * Abstract APIs for Observable class.
 */
public interface IObservable {
    public void registerObserver(Observer o);
    public void unregisterObserver(Observer o);
    public void notifyObservers(Object object);
}
