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

import ballerina/lang.runtime;
import ballerina/test;
import ballerina/time;

@test:Config {}
function testPipe() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("pipe_test", timeout = 2);
    string actualValue = check pipe.consume(5);
    string expectedValue = "pipe_test";
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {}
function testPipeWithRecords() returns error? {
    Pipe pipe = new(5);
    MovieRecord movieRecord = {name: "The Trial of the Chicago 7", director: "Aaron Sorkin"};
    check pipe.produce(movieRecord.cloneReadOnly(), timeout = 2);
    MovieRecord actualValue = check pipe.consume(-1);
    check pipe.produce(movieRecord.cloneReadOnly(), timeout = 2);
    test:assertEquals(actualValue, movieRecord);
}

@test:Config {}
function testPipeStreamWithRecords() returns error? {
    Pipe pipe = new(5);
    MovieRecord movieRecord = {name: "The Trial of the Chicago 7", director: "Aaron Sorkin"};
    check pipe.produce(movieRecord.cloneReadOnly(), timeout = 5);
    stream<MovieRecord, error?> 'stream = check pipe.consumeStream(5);
    record {|MovieRecord value;|}? 'record = check 'stream.next();
    MovieRecord actualValue = (<record {|MovieRecord value;|}>'record).value;
    MovieRecord expectedValue = movieRecord;
    test:assertEquals(actualValue, expectedValue);
}

@test:Config {}
function testPipeStream() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    check pipe.produce("2", timeout = 5);
    stream<string, error?> 'stream = check pipe.consumeStream(timeout = 5);
    foreach int i in 1 ..< 3 {
        string expectedValue = i.toString();
        record {|string value;|}? data = check 'stream.next();
        string actualValue = (<record {|string value;|}>data).value;
        test:assertEquals(actualValue, expectedValue);
    }
    check 'stream.close();
    string expectedValue = "Events must not be produced to a closed pipe";
    Error? actualValue = pipe.produce("1", timeout = 5);
    test:assertTrue(actualValue is Error);
    test:assertEquals((<Error>actualValue).message(), expectedValue);
    record {|string value;|}|error? nextValue = 'stream.next();
    test:assertTrue(nextValue is Error);
    expectedValue = "Events cannot be consumed after the stream is closed";
    test:assertEquals((<Error>nextValue).message(), expectedValue);
}

@test:Config {
    groups: ["close"]
}
function testImmediateClose() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    check pipe.immediateClose();
    Error? actualValue = pipe.consume(5);
    test:assertTrue(actualValue is Error);
}

@test:Config {
    groups: ["close"]
}
function testConsumeStreamAfterClose() returns error? {
    Pipe pipe = new(5);
    foreach int i in 0..<5 {
        check pipe.produce(i, 5);
    }
    stream<int, error?> result = check pipe.consumeStream(5);
    check pipe.immediateClose();
    record {|int value;|}|error? actualValue = result.next();
    test:assertTrue(actualValue is error);
}

@test:Config {
    groups: ["close"]
}
function testGracefulClose() returns error? {
    Pipe pipe = new(5);
    check pipe.produce("1", timeout = 5);
    check pipe.gracefulClose(timeout = 5);
    string expectedValue = "Events must not be produced to a closed pipe";
    Error? actualValue = pipe.produce("1", timeout = 5);
    test:assertTrue(actualValue is Error);
    test:assertEquals((<Error>actualValue).message(), expectedValue);
    Error? consumeValue = pipe.consume(5);
    test:assertTrue(consumeValue is Error);
}

@test:Config {
    groups: ["close"]
}
function testGracefulCloseWithConsume() returns error? {
    Pipe pipe = new(5);

    worker A {
        runtime:sleep(5);
        Error? close = pipe.gracefulClose(timeout = 5);
        test:assertTrue(close is ());
    }
    worker B {
        Error? consumedValue = pipe.consume(20);
        test:assertTrue(consumedValue is Error);
        test:assertEquals((<Error>consumedValue).message(), "Pipe has been closed");

        consumedValue = pipe.consume(20);
        test:assertTrue(consumedValue is Error);
        test:assertEquals((<Error>consumedValue).message(), "Events must not be consumed from a closed pipe");
    }
    wait B;
}

@test:Config {
    groups: ["close"]
}
function testImmediateCloseWithConsume() returns error? {
    Pipe pipe = new(5);

    worker A {
        runtime:sleep(5);
        Error? close = pipe.immediateClose();
        test:assertTrue(close is ());
    }
    worker B {
        Error? consumedValue = pipe.consume(20);
        test:assertTrue(consumedValue is Error);
        test:assertEquals((<Error>consumedValue).message(), "Pipe has been closed");

        consumedValue = pipe.consume(20);
        test:assertTrue(consumedValue is Error);
        test:assertEquals((<Error>consumedValue).message(), "Events must not be consumed from a closed pipe");
    }
    wait B;
}

@test:Config {
    groups: ["close"]
}
function testGracefulCloseWithProduce() returns error? {
    Pipe pipe = new(1);

    worker A {
        runtime:sleep(5);
        Error? close = pipe.gracefulClose(timeout = 5);
        test:assertTrue(close is ());
    }
    worker B {
        Error? produceData = pipe.produce("value", 20);
        produceData = pipe.produce("value", 20);
        test:assertTrue(produceData is Error);
        test:assertEquals((<Error>produceData).message(), "Pipe has been closed");

        produceData = pipe.produce("value", 20);
        test:assertTrue(produceData is Error);
        test:assertEquals((<Error>produceData).message(), "Events must not be produced to a closed pipe");
    }
    wait B;
}

@test:Config {
    groups: ["close"]
}
function testImmediateCloseWithProduce() returns error? {
    Pipe pipe = new(1);

    worker A {
        runtime:sleep(5);
        Error? close = pipe.immediateClose();
        test:assertTrue(close is ());
    }
    worker B {
        Error? produceData = pipe.produce("value", 20);
        produceData = pipe.produce("value", 20);
        test:assertTrue(produceData is Error);
        test:assertEquals((<Error>produceData).message(), "Pipe has been closed");

        produceData = pipe.produce("value", 20);
        test:assertTrue(produceData is Error);
        test:assertEquals((<Error>produceData).message(), "Events must not be produced to a closed pipe");
    }
    wait B;
}

@test:Config {
    groups: ["close"]
}
function testIsClosedInPipe() returns error? {
    Pipe pipe = new(5);
    test:assertTrue(!pipe.isClosed());
    time:Utc currentUtc = time:utcNow();
    check pipe.gracefulClose();
    test:assertTrue(pipe.isClosed());
    Pipe newPipe = new(5);
    check newPipe.immediateClose();
    test:assertTrue(pipe.isClosed());
    int val = time:utcNow()[0] - currentUtc[0];
    test:assertTrue(val < 30);
}

@test:Config {
    groups: ["close"]
}
function testWaitingInGracefulClose() returns error? {
    Pipe pipe = new(5);
    int expectedValue = 1;
    time:Utc currentUtc = time:utcNow();
    check pipe.produce(expectedValue, timeout = 5.00111);
    worker A {
        runtime:sleep(5);
        int|Error actualValue = pipe.consume(timeout = 5);
        int|Error actualError = pipe.consume(timeout = 5);

        test:assertTrue(actualValue !is Error);
        test:assertEquals(actualValue, expectedValue);

        string expectedErrorMessage = "Operation has timed out";
        test:assertTrue(actualError is Error);
        string actualErrorMessage = (<error>actualError).message();
        test:assertEquals(actualErrorMessage, expectedErrorMessage);
    }

    worker B {
        Error? close = pipe.gracefulClose();
        test:assertTrue(close !is Error);
        int val = time:utcNow()[0] - currentUtc[0];
        test:assertTrue(val < 30);
    }
}

@test:Config {}
function testWaitingInConsume() returns error? {
    Pipe pipe = new(1);
    int expectedValue = 3;
    worker A {
        runtime:sleep(5);
        Error? produce = pipe.produce(expectedValue, 30);
        test:assertTrue(produce !is Error);
    }

    worker B {
        int|Error actualValue = pipe.consume(30);
        test:assertTrue(actualValue !is Error);
        test:assertEquals(actualValue, expectedValue);
    }
}

@test:Config {}
function testWaitingInProduce() returns error? {
    Pipe pipe = new(1);
    int expectedValue = 10;
    int expectedNextValue = 11;
    worker A {
        Error? produce = pipe.produce(expectedValue, 30);
        test:assertTrue(produce !is Error);

        produce = pipe.produce(expectedNextValue, 30);
        test:assertTrue(produce !is Error);

        int|Error actualValue = pipe.consume(30);
        test:assertTrue(actualValue !is Error);
        test:assertEquals(actualValue, expectedNextValue);
    }

    worker B {
        runtime:sleep(5);
        int|Error actualValue = pipe.consume(30);
        test:assertTrue(actualValue !is Error);
        test:assertEquals(actualValue, expectedValue);
    }
}

@test:Config {}
function testConcurrencyInPipe() returns error? {
    Pipe pipe = new(1);
    int expectedValue = 3;
    int workerCount = 0;
    worker A {
        runtime:sleep(1);
        Error? produce = pipe.produce(expectedValue, 5);
        test:assertTrue(produce !is Error);
        workerCount+=1;
    }

    worker B {
        int|Error actualValue = pipe.consume(6);
        test:assertTrue(actualValue !is Error);
        test:assertEquals(actualValue, expectedValue);
        workerCount+=1;
    }

    worker C {
        runtime:sleep(1);
        int|Error actualValue = pipe.consume(2);
        test:assertTrue(actualValue is Error);
        workerCount+=1;
    }

    worker D {
        runtime:sleep(1);
        int|Error actualValue = pipe.consume(2);
        test:assertTrue(actualValue is Error);
        workerCount+=1;
    }

    worker E {
        runtime:sleep(8);
        test:assertEquals(workerCount, 4);
    }
}

@test:Config {}
function testPipesWithTimer() returns error? {
    Timer timeKeeper = new();

    Pipe timerPipe = new(5, timeKeeper);
    Pipe timerPipe2 = new(5, timeKeeper);
    Pipe timerPipe3 = new(5, timeKeeper);
    string expectedValue = "pipe_test";

    worker A {
        runtime:sleep(1);
        Error? produce = timerPipe.produce("pipe_test", timeout = 2);
        test:assertTrue(produce !is Error);
        string|Error actualValue1 = timerPipe.consume(5);
        test:assertTrue(actualValue1 is Error);
        test:assertEquals(actualValue1, expectedValue);
    }

    worker B {
        Error? produce = timerPipe2.produce("pipe_test", timeout = 2);
        test:assertTrue(produce !is Error);
        string|Error actualValue2 = timerPipe2.consume(5);
        test:assertTrue(actualValue2 is Error);
        test:assertEquals(actualValue2, expectedValue);
    }

    worker C {
        runtime:sleep(1);
        Error? produce = timerPipe3.produce("pipe_test", timeout = 2);
        test:assertTrue(produce !is Error);
        string|Error actualValue3 = timerPipe3.consume(5);
        test:assertTrue(actualValue3 is Error);
        test:assertEquals(actualValue3, expectedValue);
    }
}
