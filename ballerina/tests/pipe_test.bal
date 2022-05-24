import ballerina/test;

@test:Config {
    groups: ["pipe"]
}
function testPipe() returns error? {
    Pipe pipe = new(5);
    error? produce = pipe.produce("pipe_test", timeout = 2);
    if produce !is error {
        string|error actualValue = pipe.consume(5);
        string expectedValue = "pipe_test";
        if actualValue !is error? {
            test:assertEquals(expectedValue, actualValue);
        }
    }
}

@test:Config {
    groups: ["pipe"]
}
function testPipeWithRecords() returns error? {
    Pipe pipe = new(5);
    MovieRecord movieRecord = {name: "The Trial of the Chicago 7", director: "Aaron Sorkin"};
    error? produce = pipe.produce(movieRecord, timeout = 5);
    if produce !is error {
        stream<MovieRecord, error?> 'stream = pipe.consumeStream(5);
        record{|MovieRecord value;|}|error? actualValue = 'stream.next();
        if actualValue is error? {
            return actualValue;
        }
        MovieRecord expectedValue = movieRecord;
        test:assertEquals(expectedValue, actualValue.value);
    }
}

@test:Config {
    groups: ["pipe"]
}
function testPipeStream() returns error? {
    Pipe pipe = new(5);
    error? produce = pipe.produce("1", timeout = 5);
    produce = pipe.produce("2", timeout = 5);
    if produce !is error {
        stream<string, error?> 'stream = pipe.consumeStream(timeout = 5);
        foreach int i in 1 ..< 3 {
            string expectedValue = i.toString();
            record {| any value; |}|error? data = 'stream.next();
            if data !is error? {
                string actualValue = <string>data.value;
                test:assertEquals(expectedValue, actualValue);
            }
        }
    }
}
