import nuvindu/pipe;
import ballerina/io;
import ballerina/log;
import ballerina/lang.runtime;

public function main() returns error? {
    pipe:Pipe pipe = new (5);
    Report[] reports = check getReportData();
    worker A {
        foreach Report report in reports {
            error? produce = pipe.produce(report, timeout = 5);
            if produce is error {
                log:printError("Error occurred while producing data to the pipe", produce);
            }
            runtime:sleep(1);
        }
        error? gracefulClose = pipe.gracefulClose();
        if gracefulClose is error {
            log:printError("Error occurred while closing the pipe gracefully", gracefulClose);
        }
    }

    @strand {
        thread: "any"
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
