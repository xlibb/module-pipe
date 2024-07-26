// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;
import ballerina/lang.runtime;
import ballerina/time;

@test:Config {
    groups: ["errors"]
}
function testPipeWithNullValues() returns error? {
    Pipe pipe = new(1);
    string expectedValue = "Nil values must not be produced to a pipe";
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
    string expectedValue = "Operation has timed out";
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
    string expectedValue = "Operation has timed out";
    string|Error result = pipe.consume(timeout = 1);
    test:assertTrue(result is Error);
    string actualValue = (<error>result).message();
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testPipeStreamWithErrors() returns error? {
    Pipe pipe = new(5);
    MovieRecord movieRecord = {name: "The Trial of the Chicago 7", director: "Aaron Sorkin"};
    check pipe.produce(movieRecord.cloneReadOnly(), timeout = 5);
    stream<MovieRecord, error?> 'stream = check pipe.consumeStream(5);
    record {|MovieRecord value;|}? 'record = check 'stream.next();
    MovieRecord actualValue = (<record {|MovieRecord value;|}>'record).value;
    MovieRecord expectedValue = movieRecord;
    test:assertEquals(actualValue, expectedValue);
    check pipe.produce("test", timeout = 5);
    record {|MovieRecord value;|}|error? next = 'stream.next();
    test:assertTrue(next is Error);
    test:assertEquals((<Error>next).message(), "{ballerina}ConversionError");
}

@test:Config {
    groups: ["errors", "close"]
}
function testImmediateClosingOfClosedPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Attempting to close an already closed pipe";
    check pipe.immediateClose();
    Error? immediateCloseResult = pipe.immediateClose();
    test:assertTrue(immediateCloseResult is Error);
    string actualValueValue = (<error>immediateCloseResult).message();
    test:assertEquals(actualValueValue, expectedValue);
}

@test:Config {
    groups: ["errors", "close"]
}
function testGracefulClosingOfClosedPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Attempting to close an already closed pipe";
    time:Utc currentUtc = time:utcNow();
    check pipe.gracefulClose();
    Error? gracefulCloseResult = pipe.gracefulClose();
    test:assertTrue(gracefulCloseResult is Error);
    string actualValueValue = (<error>gracefulCloseResult).message();
    test:assertEquals(actualValueValue, expectedValue);
    int val = time:utcNow()[0] - currentUtc[0];
    test:assertTrue(val < 30);
}

@test:Config {
    groups: ["errors", "close"]
}
function testClosingOfClosedStreamInPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Attempting to close an already closed pipe";
    check pipe.produce("data", timeout = 1);
    stream<string, error?> resultStream = check pipe.consumeStream(5);
    check resultStream.close();
    error? close = resultStream.close();
    test:assertTrue(close is error);
    string actualValue = (<error>close).message();
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {
    groups: ["errors"]
}
function testErrorsInPipesWithTimer() returns error? {
    Timer timeKeeper = new();
    Pipe timerPipe = new (1, timeKeeper);
    Pipe timerPipe2 = new (1, timeKeeper);
    Pipe timerPipe3 = new(5, timeKeeper);
    string expectedError = "Operation has timed out";

    worker A {
        Error? produce = timerPipe.produce("data1", timeout = 1);
        test:assertTrue(produce !is Error);
        Error? result1 = timerPipe.produce("data2", timeout = 1);
        test:assertTrue(result1 is Error);
        string actualValue1 = (<error>result1).message();
        test:assertEquals(actualValue1, expectedError);
    }

    worker B {
        string|Error result2 = timerPipe2.consume(timeout = 1);
        test:assertTrue(result2 is Error);
        string actualValue2 = (<error>result2).message();
        test:assertEquals(actualValue2, expectedError);
    }

    worker C {
        string expectedValue = "pipe_test";
        Error? produce = timerPipe3.produce(expectedValue, timeout = 2);
        test:assertTrue(produce !is Error);
        string|Error actualValue3 = timerPipe3.consume(5);
        test:assertTrue(actualValue3 is Error);
        test:assertEquals(actualValue3, expectedValue);
    }
}

@test:Config {
    groups: ["errors"]
}
isolated function testNegativeTimeout() returns error? {
    Pipe pipe = new(1);
    Error? produceResult = pipe.produce(1, -10);
    if produceResult is Error {
        string expectedMessage = "Invalid timeout value provided";
        string expectedCause = "Timeout must be -1 or greater. Provided: -10";
        validateTimeoutErrorCause(produceResult, expectedMessage, expectedCause);
    } else {
        test:assertFail("Expected an error");
    }
    int|error consumeResult = pipe.consume(-10);
    if consumeResult is Error {
        string expectedMessage = "Invalid timeout value provided";
        string expectedCause = "Timeout must be -1 or greater. Provided: -10";
        validateTimeoutErrorCause(consumeResult, expectedMessage, expectedCause);
    } else {
        test:assertFail("Expected an error");
    }
    stream<int, error?>|Error consumeStreamResult = pipe.consumeStream(-10);
    if consumeStreamResult is Error {
        string expectedMessage = "Invalid timeout value provided";
        string expectedCause = "Timeout must be -1 or greater. Provided: -10";
        validateTimeoutErrorCause(consumeStreamResult, expectedMessage, expectedCause);
    } else {
        test:assertFail("Expected an error");
    }
    Error? closeResult = pipe.gracefulClose(-10);
    if closeResult is Error {
        string expectedMessage = "Invalid timeout value provided";
        string expectedCause = "Timeout must be -1 or greater. Provided: -10";
        validateTimeoutErrorCause(closeResult, expectedMessage, expectedCause);
    } else {
        test:assertFail("Expected an error");
    }
    closeResult = pipe.gracefulClose(-1);
    if closeResult is Error {
        string expectedMessage = "Graceful close must provide a timeout of 0 or greater";
        test:assertEquals(closeResult.message(), expectedMessage);
    } else {
        test:assertFail("Expected an error");
    }
}

@test:Config {
    groups: ["errors"]
}
function testInfiniteWaiting() returns error? {
    Pipe pipe = new (1);

    worker A {
        foreach int i in 0..<3 {
            Error? result = pipe.produce(i, -1);
            if result is Error {
                test:assertFail("Unexpected error occurred");
            }
        }
    }

    worker B {
        foreach int i in 0..<3 {
            int|error result = pipe.consume(-1);
            if result is error {
                test:assertFail("Unexpected error occurred");
            }
            runtime:sleep(5);
        }
    }
}

isolated function validateTimeoutErrorCause(Error err, string expectedMessage, string expectedCause) {
    test:assertEquals(err.message(), expectedMessage);
    test:assertTrue(err.cause() is Error, "Expected to have a cause");
    Error cause = <Error> err.cause();
    test:assertEquals(cause.message(), expectedCause);
}
