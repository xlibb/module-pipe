import nuvindu_dias/pipe;
import ballerina/io;
import ballerina/lang.runtime;

public function main() returns error? {
    pipe:Pipe pipe = new (5);
    Report[] reports = check getReportData();
    worker A {
        foreach Report report in reports  {
            error? produce = pipe.produce(report, timeout = 5.00111);
            if produce is error {
                io:println(produce);
            }
            runtime:sleep(5);
        }
        error? gracefulClose = pipe.gracefulClose();
        io:print(gracefulClose);
    }

    @strand {
        thread: "any"
    }
    worker B {
        stream<Report, error?> covidReports = pipe.consumeStream(timeout = 10.12323);
        CovidRecord|error? covidRecord = covidReports.next();
        int i = 0;
        while covidRecord is CovidRecord {
            Report covidReport = covidRecord.value;
            io:println("Date: ",covidReport.date);
            io:println("Postives: ",covidReport.positive);
            io:println("Deaths: ",covidReport.deaths);
            io:println("..................");
            i+=1;
            covidRecord = covidReports.next();
        }
    }
}
