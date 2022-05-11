import ballerina/test;

@test:Config {}
function testPipe() returns error? {
    Pipe pipe = new(5);
    error? produce = pipe.produce("pipe_test", timeout = 5);
    string|error actualValue = pipe.consume(5);
    string expectedValue = "pipe_test";
    if actualValue !is error? {
        test:assertEquals(expectedValue, actualValue);
    } 
}

@test:Config {}
function testPipeStream() {
    Pipe pipe = new(5);
    error? produce = pipe.produce("1", timeout = 5);
    produce = pipe.produce("2", timeout = 5);
    stream<string, error?> 'stream = pipe.consumeStream(timeout = 5);
    foreach int i in 1 ..< 3 {
        string expectedValue = i.toString();
        record {| any value; |}|error? data = 'stream.next();
        if data !is error? {
            string actualValue = <string>data.value;
            test:assertEquals(expectedValue, actualValue);
        }
    }
}
