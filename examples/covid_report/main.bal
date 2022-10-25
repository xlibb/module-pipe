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

import ballerina/io;
import ballerina/log;
import ballerina/lang.runtime;
import xlibb/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new (5);
    Report[] reports = check getReportData();
    worker A {
        foreach Report report in reports {
            pipe:Error? produce = pipe.produce(report, timeout = 5);
            if produce is pipe:Error {
                log:printError("Error occurred while producing data to the pipe", produce);
            }
            runtime:sleep(1);
        }
        pipe:Error? gracefulClose = pipe.gracefulClose();
        if gracefulClose is pipe:Error {
            log:printError("Error occurred while closing the pipe gracefully", gracefulClose);
        }
    }

    worker B {
        stream<Report, error?> covidReports = pipe.consumeStream(timeout = 5.12323);
        CovidRecord|error? covidRecord = covidReports.next();
        int i = 0;
        while covidRecord is CovidRecord {
            Report covidReport = covidRecord.value;
            io:println("Date: ", covidReport.date);
            io:println("Postives: ", covidReport.positive);
            io:println("Deaths: ", covidReport.deaths);
            io:println("..................");
            i += 1;
            covidRecord = covidReports.next();
        }
    }
}
