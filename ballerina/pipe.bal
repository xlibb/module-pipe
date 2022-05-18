import ballerina/jballerina.java;

public class Pipe {
    private handle javaPipeObject;
    
    # Initiating a new Pipe instance.
    #
    # + 'limit - The maximum limit of data that holds in the pipe at once.
    public function init(int 'limit) {
        self.javaPipeObject = newPipe('limit);
    }

    # Add data into the pipe.
    #
    # + element - Data that needs to produce to the pipe. Can be any type. 
    # + timeout - The maximum waiting period that holds data in the buffer
    # + return - If data is successfully produced, return (). otherwise returns an error.
    public isolated function produce(any element, decimal timeout) returns error? {
        if element == () {
            return error("Null values cannot be produced to a pipe.");
        }
        check produce(self.javaPipeObject, element, timeout);
    }

    # Return data from the pipe.
    #
    # + timeout - The maximum waiting period to receive data.  
    # + typeParam - Parameter Description
    # + return - The same data type that has been produced to the pipe will return here. 
    #            If an error occurred in the process, return an error.
    public isolated function consume(decimal timeout, typedesc<any> typeParam = <>)
        returns typeParam|error = @java:Method {
        'class: "pipe.Pipe"
    } external;

    # Return data from the pipe in a stream.
    #
    # + timeout - The maximum waiting period to receive data.  
    # + typeParam - Default parameter used to infer the user specified type.
    # + return - Returns a stream. The stream type is inferred as user specified.
    public isolated function consumeStream(decimal timeout, typedesc<any> typeParam = <>) 
        returns stream<typeParam,error?> = @java:Method {
        'class: "pipe.Pipe"
    } external;

    # Close the pipe object immediately. Unexpected errors can occur.
    public isolated function immediateClose() {
        immediateClose(self.javaPipeObject);
    }

    # Close the pipe gracefully. Waits for some period until all the data in the pipe is consumed.
    # + return - Return (), if the pipe is successfully closed. Otherwise returns an error.
    public isolated function gracefulClose() returns error?{
        check gracefulClose(self.javaPipeObject);
    }

    # Check whether the pipe is closed.
    # + return - Return true, if the pipe is closed. Otherwise returns false.
    public isolated function isClosed() returns boolean {
        return isClosed(self.javaPipeObject);
    }
}

function newPipe(int 'limit) returns handle = @java:Constructor {
    'class: "pipe.Pipe"
} external;

isolated function produce(handle pipe, any data, decimal timeout) returns error? = @java:Method {
    'class: "pipe.Pipe"
} external;

isolated function immediateClose(handle pipe) = @java:Method {
    'class: "pipe.Pipe"
} external;

isolated function gracefulClose(handle pipe) returns error? = @java:Method {
    'class: "pipe.Pipe"
} external;

isolated function isClosed(handle pipe) returns boolean = @java:Method {
    'class: "pipe.Pipe"
} external;
