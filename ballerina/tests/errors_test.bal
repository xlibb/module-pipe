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
    groups: ["errors","close"]
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
