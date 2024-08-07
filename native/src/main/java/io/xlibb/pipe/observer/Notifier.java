package io.xlibb.pipe.observer;

import io.xlibb.pipe.utils.Utils;

import java.util.TimerTask;

/**
 * Observable class for the observers waiting for the timeKeeper.
 */
public class Notifier extends TimerTask {
    private static final String OPERATION_TIMEOUT_ERROR = "Operation has timed out";
    private final Observable timeKeeper;
    private final Callback callback;

    public Notifier(Observable timeKeeper, Callback callback) {
        this.timeKeeper = timeKeeper;
        this.callback = callback;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        this.timeKeeper.notifyObservers(Utils.createError(OPERATION_TIMEOUT_ERROR), this.callback);
    }
}
