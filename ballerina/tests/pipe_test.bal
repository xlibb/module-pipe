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
    record {|MovieRecord value;|}? 'record = check 'stream.next();
    MovieRecord actualValue = (<record {|MovieRecord value;|}>'record).value;
    MovieRecord expectedValue = movieRecord;
    test:assertEquals(expectedValue, actualValue);
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
        string actualValue = (<record {|string value;|}>data).value;
        test:assertEquals(expectedValue, actualValue);
    }
    check 'stream.close();
    string expectedValue = "Events cannot be produced to a closed pipe.";
    Error? actualValue = pipe.produce("1", timeout = 5);
    test:assertTrue(actualValue is Error);
    test:assertEquals((<Error>actualValue).message(), expectedValue);
    record {|string value;|}|error? nextValue = 'stream.next();
    test:assertTrue(nextValue is Error);
    expectedValue = "Events cannot be consumed after the stream is closed";
    test:assertEquals((<Error>nextValue).message(), expectedValue);
}

@test:Config {
    groups: ["pipe"]
}
function testImmediateClose() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    check pipe.immediateClose();
    string expectedValue = "No any event is available in the closed pipe.";
    string|Error actualValue = pipe.consume(5);
    test:assertTrue(actualValue is Error);
    test:assertEquals((<Error>actualValue).message(), expectedValue);
}

@test:Config {
    groups: ["pipe"]
}
function testGracefulClose() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    check pipe.gracefulClose();
    string expectedValue = "Events cannot be produced to a closed pipe.";
    Error? actualValue = pipe.produce("1", timeout = 5);
    test:assertTrue(actualValue is Error);
    test:assertEquals((<Error>actualValue).message(), expectedValue);
}

@test:Config {
    groups: ["pipe"]
}
function testIsClosedInPipe() returns error? {
    Pipe pipe = new(5);
    test:assertTrue(!pipe.isClosed());
    check pipe.gracefulClose();
    test:assertTrue(pipe.isClosed());
    Pipe newPipe = new(5);
    check newPipe.immediateClose();
    test:assertTrue(pipe.isClosed());
}

@test:Config {
    groups: ["pipe"]
}
function testWaitingInGracefulClose() returns error? {
    Pipe pipe = new(5);
    int expectedValue = 1;
    check pipe.produce(expectedValue, timeout = 5.00111);
    worker A {
        runtime:sleep(5);
        int|Error actualValue = pipe.consume(timeout = 5);
        int|Error actualError = pipe.consume(timeout = 5);

        test:assertTrue(actualValue !is Error);
        test:assertEquals(actualValue, expectedValue);

        string expectedErrorMessage = "Operation has timed out.";
        test:assertTrue(actualError is Error);
        string actualErrorMessage = (<error>actualError).message();
        test:assertEquals(actualErrorMessage, expectedErrorMessage);
    }
    @strand {
        thread: "any"
    }
    worker B {
        Error? close = pipe.gracefulClose();
        test:assertTrue(close !is Error);
    }
}
