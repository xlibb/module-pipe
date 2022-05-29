import ballerina/jballerina.java;

class StreamGenerator {
    ResultIterator resultIterator;

    public isolated function init(ResultIterator resultIterator) {
        self.resultIterator = resultIterator;
    }

    public isolated function next() returns record {|any value;|}|Error? {
        any|Error streamValue = self.resultIterator.nextValue(self);
        if streamValue is any {
            return {value: streamValue};
        }
        return streamValue;
    }

    public isolated function close() returns Error? {
        return self.resultIterator.close(self);
    }
}

public class ResultIterator {

    isolated function nextValue(StreamGenerator streamGenerator) returns any|Error = @java:Method {
        'class: "org.nuvindu.pipe.ResultIterator"
    } external;

    isolated function close(StreamGenerator streamGenerator) returns Error? = @java:Method {
        'class: "org.nuvindu.pipe.ResultIterator"
    } external;
}
