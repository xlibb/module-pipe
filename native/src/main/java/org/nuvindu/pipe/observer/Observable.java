package org.nuvindu.pipe.observer;

import java.util.ArrayList;

/**
 * Observable class to notify observers when a change occurs.
 */
public class Observable implements IObservable {
    ArrayList<Observer> observerList;

    public Observable() {
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
        if (!observerList.isEmpty()) {
            observerList.remove(0).update(object);
        }
    }
}
