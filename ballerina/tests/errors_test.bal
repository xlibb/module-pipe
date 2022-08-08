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
import ballerina/time;

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
    groups: ["errors", "close"]
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
    groups: ["errors", "close"]
}
function testGracefulClosingOfClosedPipe() returns error? {
    Pipe pipe = new (1);
    string expectedValue = "Closing of a closed pipe is not allowed.";
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
    string expectedValue = "Failed to close the stream.";
    check pipe.produce("data", timeout = 1);
    stream<string, error?> resultStream = pipe.consumeStream(5);
    check resultStream.close();
    error? close = resultStream.close();
    test:assertTrue(close is error);
    string actualValue = (<error>close).message();
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {
    groups: ["main_apis"]
}
function testErrorsInPipesWithTimer() returns error? {
    handle timeKeeper = newTimer();
    Pipe timerPipe = new (1, timeKeeper);
    Pipe timerPipe2 = new (1, timeKeeper);
    Pipe timerPipe3 = new(5, timeKeeper);
    string expectedError = "Operation has timed out.";

    worker A {       
        Error? produce = timerPipe.produce("data1", timeout = 1);
        test:assertTrue(produce !is Error);
        Error? result1 = timerPipe.produce("data2", timeout = 1);
        test:assertTrue(result1 is Error);
        string actualValue1 = (<error>result1).message();
        test:assertEquals(actualValue1, expectedError);
    }

    @strand {
        thread: "any"
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
