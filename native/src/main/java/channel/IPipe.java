package channel;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;

public interface IPipe<E> {
    public BError produce(Object data, BDecimal timeout) throws InterruptedException;
    public Object consumeData(BDecimal timeout) throws InterruptedException;
    public boolean isClosed();
    public void immediateStop();
    public void gracefulStop() throws InterruptedException;
}
