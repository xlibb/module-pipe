import ballerina/jballerina.java;

isolated function init() {
    setModule();
}

isolated function setModule() = @java:Method {
    'class: "org.nuvindu.pipe.utils.ModuleUtils"
} external;
