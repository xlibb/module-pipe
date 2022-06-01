import ballerina/jballerina.java;

isolated function init() {
    setModule();
}

isolated function setModule() = @java:Method {
    'class: "org.nuvindu.pipe.utils.ModuleUtils"
} external;

public isolated function getModule() returns handle = @java:Method {
    'class: "org.nuvindu.pipe.utils.ModuleUtils"
} external;
