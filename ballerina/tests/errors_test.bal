import ballerina/test;

@test:Config {
    groups: ["errors"]
}
function testPipeWithNullValues() returns error? {
    Pipe pipe = new(1);
    string expectedValue = "Nil values cannot be produced to a pipe.";
    Error? result = pipe.produce((), timeout = 5);
    test:assertTrue(result is Error);
    string actualValue = (<error>result).message();
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testTimeoutErrorsInProduce() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Operation has timed out.";
    check pipe.produce("data1", timeout = 1);
    Error? result = pipe.produce("data2", timeout = 1);
    test:assertTrue(result is Error);
    string actualValue = (<error>result).message();
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testTimeoutErrorsInConsume() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Operation has timed out.";
    string|Error result = pipe.consume(timeout = 1);
    test:assertTrue(result is Error);
    string actualValue = (<error>result).message();
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testImmediateClosingOfClosedPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Closing of a closed pipe is not allowed.";
    check pipe.immediateClose();
    Error? immediateCloseResult = pipe.immediateClose();
    test:assertTrue(immediateCloseResult is Error);
    string actualValueValue = (<error>immediateCloseResult).message();
    test:assertEquals(actualValueValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testGracefulClosingOfClosedPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Closing of a closed pipe is not allowed.";
    check pipe.gracefulClose();
    Error? gracefulCloseResult = pipe.gracefulClose();
    test:assertTrue(gracefulCloseResult is Error);
    string actualValueValue = (<error>gracefulCloseResult).message();
    test:assertEquals(actualValueValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testClosingOfClosedStreamInPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Failed to close the stream.";
    check pipe.produce("data", timeout = 1);
    stream<string, error?> resultStream = pipe.consumeStream(5);
    check resultStream.close();
    error? close = resultStream.close();
    test:assertTrue(close is error);
    string actualValue = (<error>close).message();
    test:assertEquals(actualValue, expectedValue);
}
