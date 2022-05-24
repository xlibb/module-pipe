import ballerina/test;
@test:Config {
    groups: ["pipe", "errors"]
}
function testPipeWithNullValues() returns error? {
    Pipe pipe = new(1);
    string expectedValue = "Nil values cannot be produced to a pipe.";
    string actual = (<error> pipe.produce((), timeout = 5)).message();
    test:assertEquals(actual, expectedValue);
}
