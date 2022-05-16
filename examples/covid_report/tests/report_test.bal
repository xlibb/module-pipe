import nuvindu_dias/pipe;
import ballerina/io;
import ballerina/test;

@test:Config {
    groups: ["pipe"]
}
function testPipe() returns error? {
    pipe:Pipe pipe = new (5);
    worker A {
        foreach int i in 1..<5 {
            error? produce = pipe.produce(i, timeout = 5.00111);
            if produce is error {
                io:println(produce);
            }            
        }
    }

    @strand {
        thread: "any"
    }
    worker B {
        stream<int, error?> intStream = pipe.consumeStream(timeout = 10.12323);
        IntRecord|error? 'record = intStream.next();
        int i = 1;
        while 'record is IntRecord {
            test:assertEquals('record, i);
            i+=1;
            'record = intStream.next();
        }
    }
}

@test:Config {
    groups: ["pipe"]
}
function testPipeWithObjects() returns error? {
    pipe:Pipe pipe = new (5);
    Report report = new("20220514", 663655, 988, 553467, 16511);
    worker A {
        error? produce = pipe.produce(report, timeout = 5.00111);
        if produce is error {
            io:println(produce);
        }
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
