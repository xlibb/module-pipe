import ballerina/jballerina.java;

isolated function init() {
    setModule();
}

isolated function setModule() = @java:Method {
    'class: "org.nuvindu.pipe.runtime.utils.ModuleUtils"
} external;
