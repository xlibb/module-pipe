import ballerina/jballerina.java;

public class StreamGenerator {
    ResultIterator resultIterator;

    public isolated function init(ResultIterator resultIterator) {
        self.resultIterator = resultIterator;
    }

    # Returns the next element in the stream wrapped in a record or () if the stream ends.
    # + return - If the stream has elements, return the element wrapped in a record with single field called `value`,
    #            otherwise returns ()
    public isolated function next() returns record {|any value;|}|error? {
        any|error streamValue = self.resultIterator.nextValue(self);
        if streamValue is any {
            return {value: streamValue};
        }
        return streamValue;
    }

    # Close the stream along with the pipe.
    # + return - Return (), if the stream is successfully closed. Otherwise returns error.
    #            The pipe related to the stream will be gracefully closed during the process.
    public isolated function close() returns error? {
        return self.resultIterator.close(self);
    }
}

public class ResultIterator {
    
    isolated function nextValue(StreamGenerator streamGenerator) returns any|error = @java:Method {
        'class: "pipe.ResultIterator"
    } external;

    isolated function close(StreamGenerator streamGenerator) returns error? = @java:Method {
        'class: "pipe.ResultIterator"
    } external;
}
