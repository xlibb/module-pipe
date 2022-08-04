package org.nuvindu.pipe.observer;

import java.util.TimerTask;

import static org.nuvindu.pipe.utils.Utils.createError;

/**
 * Observable class for the observers waiting for the timeKeeper.
 */
public class Notifier extends TimerTask {
    TimeKeeper timer;
    Callback callback;

    public Notifier(TimeKeeper timer, Callback callback) {
        this.timer = timer;
        this.callback = callback;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        this.timer.notifyObservers(createError("Operation has timed out."), this.callback);
        this.cancel();
    }
}
