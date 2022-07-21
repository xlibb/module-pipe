package org.nuvindu.pipe.observer;

import io.ballerina.runtime.api.values.BError;

/**
 * Abstract APIs for Observer class.
 */
public interface IObserver {
    public void update(Object o);
    public void update(BError bError);
}
