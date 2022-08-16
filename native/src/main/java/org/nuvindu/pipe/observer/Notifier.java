package org.nuvindu.pipe.observer;

import java.util.TimerTask;

import static org.nuvindu.pipe.utils.Utils.createError;

/**
 * Observable class for the observers waiting for the timeKeeper.
 */
public class Notifier extends TimerTask {
    Observable timeKeeper;
    Callback callback;

    public Notifier(Observable timeKeeper, Callback callback) {
        this.timeKeeper = timeKeeper;
        this.callback = callback;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        this.timeKeeper.notifyObservers(createError("Operation has timed out."), this.callback);
    }
}
