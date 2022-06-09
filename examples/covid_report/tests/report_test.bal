import nuvindu/pipe;
import ballerina/test;

@test:Config {
    groups: ["pipe", "covid_report_example"]
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
    groups: ["pipe", "covid_report_example"]
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
