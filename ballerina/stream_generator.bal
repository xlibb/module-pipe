import ballerina/jballerina.java;

class StreamGenerator {
    ResultIterator resultIterator;

    isolated function init(ResultIterator resultIterator) {
        self.resultIterator = resultIterator;
    }

    public isolated function next() returns record {|any value;|}|Error? {
        any streamValue = check self.resultIterator.nextValue(self);
        return {value: streamValue};
    }

    public isolated function close() returns Error? {
        return self.resultIterator.close(self);
    }
}

class ResultIterator {

    isolated function nextValue(StreamGenerator streamGenerator) returns any|Error = @java:Method {
        'class: "org.nuvindu.pipe.ResultIterator"
    } external;

    isolated function close(StreamGenerator streamGenerator) returns Error? = @java:Method {
        'class: "org.nuvindu.pipe.ResultIterator"
    } external;
}
