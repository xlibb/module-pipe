import ballerina/jballerina.java;

class StreamGenerator {
    ResultIterator resultIterator;

    public isolated function init(ResultIterator resultIterator) {
        self.resultIterator = resultIterator;
    }

    # Returns the next element in the stream wrapped in a record or () if the stream ends.
    # 
    # + return - If the stream has elements, return the element wrapped in a record with single field called `value`,
    #            otherwise returns `()`
    public isolated function next() returns record {|any value;|}|Error? {
        any|Error streamValue = self.resultIterator.nextValue(self);
        if streamValue is any {
            return {value: streamValue};
        }
        return streamValue;
    }

    # Closes the stream along with the pipe.
    # 
    # + return - Returns `()`, if the stream is successfully closed. Otherwise returns an error.
    #            The pipe related to the stream will be gracefully closed during the process
    public isolated function close() returns Error? {
        return self.resultIterator.close(self);
    }
}

public class ResultIterator {

    isolated function nextValue(StreamGenerator streamGenerator) returns any|Error = @java:Method {
        'class: "pipe.ResultIterator"
    } external;

    isolated function close(StreamGenerator streamGenerator) returns Error? = @java:Method {
        'class: "pipe.ResultIterator"
    } external;
}
