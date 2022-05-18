package pipe;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;

/**
 * Abstract APIs of the Pipe class.
 */
public interface IPipe {
    public BError produce(Object data, BDecimal timeout) throws InterruptedException;
    public Object consumeData(BDecimal timeout) throws InterruptedException;
    public boolean isClosed();
    public void immediateClose();
    public void gracefulClose() throws InterruptedException;
}
