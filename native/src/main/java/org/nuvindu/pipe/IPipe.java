package org.nuvindu.pipe;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;

/**
 * Abstract APIs of the Pipe class.
 */
public interface IPipe {
    public boolean isClosed();
    public BError immediateClose();
    public BError gracefulClose(BDecimal timeou);
}
