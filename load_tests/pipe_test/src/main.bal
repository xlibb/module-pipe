import ballerina/http;
import nuvindu/pipe;

service / on new http:Listener(9090) {
    pipe:Pipe pipe;
    pipe:Timer timer = new();
    function init() returns error? {
        self.pipe = new(20);
    }

    resource function get consume() returns string|error {
        string payload = check self.pipe.consume(120);
        return payload;
    }

    resource function post produce(@http:Payload string payload) returns string|error {
        check self.pipe.produce(payload, 120);
        return payload;
    }
}
