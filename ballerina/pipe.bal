import ballerina/jballerina.java;

# Consists of APIs to exchange events concurrently.
public isolated class Pipe {
    private handle javaPipeObject;

    # Creates a new `pipe:Pipe` instance.
    #
    # + 'limit - The maximum number of entries that are held in the pipe at once
    public isolated function init(int 'limit) {
        self.javaPipeObject = newPipe('limit);
    }

    # Produces events into the pipe.
    #
    # + events - Events that needs to be produced to the pipe. Can be `any` type
    # + timeout - The maximum waiting period that holds events
    # + return - Returns `()` if events is successfully produced. Otherwise returns a `pipe:Error`
    public isolated function produce(any events, decimal timeout) returns Error? = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;
    
    # Consumes events in the pipe.
    #
    # + timeout - The maximum waiting period to consume events
    # + typeParam - The `type` of data that is needed to be consumed. When not provided, the type is inferred 
    # using the expected type from the function
    # + return - Return type is inferred from the user specified type. That should be the same event type
    # produced to the pipe. Otherwise, returns a `pipe:Error`
    public isolated function consume(decimal timeout, typedesc<any> typeParam = <>)
        returns typeParam|Error = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;

    # Consumes events in the pipe as a `stream`
    #
    # + timeout - The maximum waiting period to consume events
    # + typeParam - The `type` of data that is needed to be consumed. When not provided, the type is inferred 
    # using the expected type from the function
    # + return - Returns a `stream`. The stream type is inferred from the user specified type
    public isolated function consumeStream(decimal timeout, typedesc<any> typeParam = <>)
        returns stream<typeParam, error?> = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;

    # Closes the pipe instantly.
    # + return - Return `()`, if the pipe is successfully closed. Otherwise returns a `pipe:Error`
    public isolated function immediateClose() returns Error? {
        lock {
            check immediateClose(self.javaPipeObject);
        }
    }

    # Closes the pipe gracefully. Waits for some grace period until all the events in the pipe is consumed.
    #
    # + timeout - The maximum grace period to wait until the pipe is empty. The default timeout is thirty seconds
    # + return - Return `()`, if the pipe is successfully closed. Otherwise returns a `pipe:Error`
    public isolated function gracefulClose(decimal timeout = 30) returns Error? {
        lock {
            check gracefulClose(self.javaPipeObject, timeout);
        }
    }

    # Checks whether the pipe is closed.
    #
    # + return - Returns `true`, if the pipe is closed. Otherwise returns `false`
    public isolated function isClosed() returns boolean {
        lock {
            return isClosed(self.javaPipeObject);
        }   
    }
}

isolated function newPipe(int 'limit) returns handle = @java:Constructor {
    'class: "org.nuvindu.pipe.Pipe"
} external;

isolated function immediateClose(handle pipe) returns Error? = @java:Method {
    'class: "org.nuvindu.pipe.Pipe"
} external;

isolated function gracefulClose(handle pipe, decimal timeout) returns Error? = @java:Method {
    'class: "org.nuvindu.pipe.Pipe"
} external;

isolated function isClosed(handle pipe) returns boolean = @java:Method {
    'class: "org.nuvindu.pipe.Pipe"
} external;
