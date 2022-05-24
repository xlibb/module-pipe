package pipe;

import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;

/**
 * Abstract APIs of the Pipe class.
 */
public interface IPipe {
    public BError produce(Object data, BDecimal timeout);
    public Object consumeData(BDecimal timeout);
    public boolean isClosed();
    public void immediateClose();
    public BError gracefulClose();
}
