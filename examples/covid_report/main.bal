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
            io:println("Date: ",covidReport.getDate());
            io:println("Postives: ",covidReport.getPositives());
            io:println("Deaths: ",covidReport.getDeaths());
            io:println("..................");
            i+=1;
            covidRecord = covidReports.next();
        }
    }
}
