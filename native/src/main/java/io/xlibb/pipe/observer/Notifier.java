package io.xlibb.pipe.observer;

import io.xlibb.pipe.utils.Utils;

import java.util.TimerTask;

import static io.xlibb.pipe.utils.Utils.createError;

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
        this.timeKeeper.notifyObservers(Utils.createError("Operation has timed out."), this.callback);
    }
}
