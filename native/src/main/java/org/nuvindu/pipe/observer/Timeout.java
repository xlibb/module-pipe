package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.values.BError;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Timer class implementation for Pipe package.
 */
public class Timeout extends Timer implements IObservable {
    ArrayList<Observer> observerList;

    /**
     * Creates a new timer.  The associated thread does <i>not</i>
     * {@linkplain Thread#setDaemon run as a daemon}.
     */
    public Timeout() {
        this.observerList = new ArrayList<>();
    }

    /**
     * Creates a new timer whose associated thread has the specified name,
     * and may be specified to
     * {@linkplain Thread#setDaemon run as a daemon}.
     *
     * @param name     the name of the associated thread
     * @param isDaemon true if the associated thread should run as a daemon
     * @throws NullPointerException if {@code name} is null
     * @since 1.5
     */
    public Timeout(String name, boolean isDaemon, ArrayList<Observer> observerList) {
        super(name, isDaemon);
        this.observerList = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        observerList.add(o);
    }

    @Override
    public void unregisterObserver(Observer o) {
        observerList.remove(o);
    }

    @Override
    public void notifyObservers(Object object) {
        //
    }

    public void notifyObservers(BError bError, Observer observer) {
        observer.update(bError);
        unregisterObserver(observer);
    }
}
