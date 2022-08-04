package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.values.BError;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Timer class implementation for Pipe package.
 */
public class TimeKeeper extends Timer implements IObservable {
    ArrayList<Callback> callbackList;

    /**
     * Creates a new timer.  The associated thread does <i>not</i>
     * {@linkplain Thread#setDaemon run as a daemon}.
     */
    public TimeKeeper() {
        this.callbackList = new ArrayList<>();
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
    public TimeKeeper(String name, boolean isDaemon, ArrayList<Callback> callbackList) {
        super(name, isDaemon);
        this.callbackList = new ArrayList<>();
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
        //
    }

    @Override
    public void notifyObservers() {
        //
    }

    @Override
    public void notifyObservers(BError bError) {
        //
    }

    public void notifyObservers(BError bError, Callback callback) {
        callback.onTimeout(bError);
    }

    @Override
    public void notifyObservers(boolean isEmpty) {
        //
    }
}
