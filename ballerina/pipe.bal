import ballerina/jballerina.java;

public class Pipe {
    private handle javaPipeObject;
    
    public function init(int 'limit) {
        self.javaPipeObject = newPipe('limit);
    }

    public isolated function produce(any element, decimal timeout) returns error? {
        if element == () {
            return error("Null values cannot be produced to a pipe.");
        }
        check produce(self.javaPipeObject, element, timeout);
    }

    public isolated function consume(decimal timeout, typedesc<any> typeParam = <>) returns typeParam|error = @java:Method {
        'class: "pipe.Pipe"
    } external;

    public isolated function consumeStream(decimal timeout, typedesc<any> typeParam = <>) returns stream<typeParam,error?> = @java:Method {
        'class: "pipe.Pipe"
    } external;

    public isolated function immediateStop() {
        immediateStop(self.javaPipeObject);
    }

    public isolated function gracefulStop() returns error?{
        check gracefulStop(self.javaPipeObject);
    }

    public isolated function isClosed() returns boolean {
        return isClosed(self.javaPipeObject);
    }
}

function newPipe(int 'limit) returns handle = @java:Constructor {
    'class: "pipe.Pipe"
} external;

isolated function produce(handle pipe,any data, decimal timeout) returns error? = @java:Method {
    'class: "pipe.Pipe"
} external;

isolated function immediateStop(handle pipe) = @java:Method {
    'class: "pipe.Pipe"
} external;

isolated function gracefulStop(handle pipe) returns error? = @java:Method {
    'class: "pipe.Pipe"
} external;

isolated function isClosed(handle pipe) returns boolean = @java:Method {
    'class: "pipe.Pipe"
} external;
