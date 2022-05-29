import ballerina/test;

@test:Config {
    groups: ["pipe", "errors"]
}
function testPipeWithNullValues() returns error? {
    Pipe pipe = new(1);
    string expectedValue = "Nil values cannot be produced to a pipe.";
    Error? result = pipe.produce((), timeout = 5);
    test:assertTrue(result is Error);
    string actual = (<error>result).message();
    test:assertEquals(actual, expectedValue);
}
