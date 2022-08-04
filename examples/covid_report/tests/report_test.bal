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
import nuvindu/pipe;

@test:Config {
    groups: ["examples", "covid_report"]
}
function testPipeConcurrently() returns error? {
    pipe:Pipe pipe = new(5);
    int expectedCount = 6;
    worker A {
        foreach int i in 1 ..< expectedCount {
            pipe:Error? produce = pipe.produce(i, timeout = 10.00111);
            test:assertTrue(produce !is pipe:Error);
        }
    }

    @strand {
        thread: "any"
    }
    worker B {
        stream<int, error?> intStream = pipe.consumeStream(timeout = 10.12323);
        IntRecord|error? 'record = intStream.next();
        int i = 0;
        while 'record is IntRecord {
            test:assertEquals('record, i);
            i += 1;
            'record = intStream.next();
        }
        test:assertEquals(expectedCount, i);
    }
}

@test:Config {
    groups: ["examples", "covid_report"]
}
function testPipeWithObjectsConcurrently() returns error? {
    pipe:Pipe pipe = new(5);
    Report report = {
        date: "20220514",
        positive: 663655,
        hospitalizedCurrently: 988,
        hospitalizedTotal: 553467,
        deaths: 16511
    };
    worker A {
        pipe:Error? produce = pipe.produce(report, timeout = 5.00111);
        test:assertTrue(produce !is pipe:Error);
    }

    @strand {
        thread: "any"
    }
    worker B {
        stream<Report, error?> covidReports = pipe.consumeStream(timeout = 10.12323);
        CovidRecord|error? covidRecord = covidReports.next();
        test:assertExactEquals(covidRecord, report);
    }
}
