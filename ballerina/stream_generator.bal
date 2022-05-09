import ballerina/jballerina.java;

public class StreamGenerator {
    ResultIterator resultIterator;

    public isolated function init(ResultIterator resultIterator) {
        self.resultIterator = resultIterator;
    }

    public isolated function next() returns record {|any value;|}|error? {
        any|error streamValue = self.resultIterator.nextValue(self);
        if(streamValue is any){
            return {value: streamValue};
        }
        return streamValue;
    }

    public isolated function close() returns error? {
        return self.resultIterator.close(self);
    }
}

public class ResultIterator {
    
    public isolated function nextValue(StreamGenerator streamGenerator) returns any|error = @java:Method {
        'class: "pipe.ResultIterator"
    } external;

    public isolated function close(StreamGenerator streamGenerator) returns error? = @java:Method {
        'class: "pipe.ResultIterator"
    } external;
}
