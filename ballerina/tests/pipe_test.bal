import ballerina/test;

@test:Config {
    groups: ["pipe"]
}
function testPipe() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("pipe_test", timeout = 2);
    string actualValue = check pipe.consume(5);
    string expectedValue = "pipe_test";
    test:assertEquals(expectedValue, actualValue);
}

@test:Config {
    groups: ["pipe"]
}
function testPipeWithRecords() returns error? {
    Pipe pipe = new(5);
    MovieRecord movieRecord = {name: "The Trial of the Chicago 7", director: "Aaron Sorkin"};
    check pipe.produce(movieRecord, timeout = 5);
    stream<MovieRecord, error?> 'stream = pipe.consumeStream(5);
    record {|MovieRecord value;|}|error? actualValue = 'stream.next();
    if actualValue is error? {
        return actualValue;
    }
    MovieRecord expectedValue = movieRecord;
    test:assertEquals(expectedValue, actualValue.value);
}

@test:Config {
    groups: ["pipe"]
}
function testPipeStream() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    check pipe.produce("2", timeout = 5);
    stream<string, error?> 'stream = pipe.consumeStream(timeout = 5);
    foreach int i in 1 ..< 3 {
        string expectedValue = i.toString();
        record {|string value;|}? data = check 'stream.next();
        if data != () {
            string actualValue = data.value;
            test:assertEquals(expectedValue, actualValue);
        }
    }
    check 'stream.close();
    string expectedValue = "Events cannot be produced to a closed pipe.";
    Error? actualValue = pipe.produce("1", timeout = 5);
    test:assertTrue(actualValue is Error, "Error was not produced while producing events to a closed pipe.");
    if actualValue is Error {
        test:assertEquals(actualValue.message(), expectedValue);
    }
    record {|string value;|}|error? nextValue = 'stream.next();
    test:assertTrue(nextValue is Error, "Error was not produced while consuming events from a closed and empty pipe.");
    expectedValue = "Events cannot be consumed after the stream is closed";
    if nextValue is Error {
        test:assertEquals(nextValue.message(), expectedValue);
    }

}

@test:Config {
    groups: ["pipe"]
}
function testImmediateClose() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    pipe.immediateClose();
    string expectedValue = "No any events is available in the closed pipe.";
    string|Error actualValue = pipe.consume(5);
    test:assertTrue(actualValue is Error,
                    "Error was not produced while consuming events from a closed and empty pipe.");
    if actualValue is Error {
        test:assertEquals(actualValue.message(), expectedValue);
    }
}

@test:Config {
    groups: ["pipe"]
}
function testGracefulClose() returns error? {
    Pipe pipe = new(5);
    check pipe.gracefulClose();
    string expectedValue = "Events cannot be produced to a closed pipe.";
    Error? actualValue = pipe.produce("1", timeout = 5);
    test:assertTrue(actualValue is Error, "Error was not produced while producing events to a closed pipe.");
    if actualValue is Error {
        test:assertEquals(actualValue.message(), expectedValue);
    }
}

@test:Config {
    groups: ["pipe"]
}
function testIsClosedInPipe() returns error? {
    Pipe pipe = new(5);
    string expectedValue = "Assertion failed for 'isClosed()' method";
    test:assertTrue(!pipe.isClosed(), expectedValue);
    check pipe.gracefulClose();
    test:assertTrue(pipe.isClosed(), expectedValue);
    Pipe newPipe = new(5);
    newPipe.immediateClose();
    test:assertTrue(pipe.isClosed(), expectedValue);
}
