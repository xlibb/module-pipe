package org.nuvindu.pipe.observer;

import java.util.TimerTask;

import static org.nuvindu.pipe.utils.Utils.createError;

/**
 * Observable class for the observers waiting for the timeout.
 */
public class Notifier extends TimerTask {
    Timeout timer;
    Observer observer;

    public Notifier(Timeout timer, Observer observer) {
        this.timer = timer;
        this.observer = observer;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        this.timer.notifyObservers(createError("Operation has timed out."), this.observer);
        this.cancel();
    }
}
