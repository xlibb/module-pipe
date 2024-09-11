// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

# Consists of APIs to exchange events concurrently.
public isolated class Pipe {

    # Creates a new `pipe:Pipe` instance.
    #
    # + 'limit - The maximum number of entries that are held in the pipe at once
    # + timer - The timer that used to keep track of time to notify the timeouts in APIs
    public isolated function init(int 'limit, Timer? timer = ()) {
        if timer is Timer {
            self.generatePipeWithTimer('limit, timer);
        } else {
            self.generatePipe('limit);
        }
    }

    public isolated function generatePipe(int 'limit) = @java:Method {
        'class: "io.xlibb.pipe.Pipe",
        paramTypes: ["java.lang.Long"]
    } external;

    public isolated function generatePipeWithTimer(int 'limit, Timer timer) = @java:Method {
        name: "generatePipe",
        'class: "io.xlibb.pipe.Pipe",
        paramTypes: ["java.lang.Long", "io.ballerina.runtime.api.values.BObject"]
    } external;

    # Produces an event into the pipe.
    #
    # + event - The event that needs to be produced to the pipe. This only supports `readonly|isolated object {}` types
    # + timeout - The maximum waiting period (in seconds) that holds the event. Set the timeout to `-1` to wait without a time limit.
    #             Any other negative value will return a `pipe:Error`
    # + return - Returns `()` if the event is successfully produced. Otherwise returns a `pipe:Error`
    public isolated function produce(Event event, decimal timeout) returns Error? = @java:Method {
        'class: "io.xlibb.pipe.Pipe"
    } external;

    # Consumes an event in the pipe.
    #
    # + timeout - The maximum waiting period (in seconds) to consume the event.
    #             Set the timeout to `-1` to wait without a time limit.
    #             Any other negative value will return a `pipe:Error`
    # + typeParam - The `type` of data that is needed to be consumed. When not provided, the type is inferred
    # using the expected type from the function
    # + return - Return type is inferred from the user specified type. That should be the same event type
    # produced to the pipe. Otherwise, returns a `pipe:Error`
    public isolated function consume(decimal timeout, typedesc<any> typeParam = <>)
        returns typeParam|Error = @java:Method {
        'class: "io.xlibb.pipe.Pipe"
    } external;

    # Consumes events in the pipe as a `stream`
    #
    # + timeout - The maximum waiting period (in seconds) to consume events. Set the timeout to `-1` to wait without a time limit.
    #             Any other negative value will return a `pipe:Error`
    # + typeParam - The `type` of data that is needed to be consumed. When not provided, the type is inferred
    # using the expected type from the function
    # + return - Returns a `stream`. The stream type is inferred from the user specified type
    public isolated function consumeStream(decimal timeout, typedesc<any> typeParam = <>)
        returns stream<typeParam, error?>|Error = @java:Method {
        'class: "io.xlibb.pipe.Pipe"
    } external;

    # Closes the pipe instantly.
    # + return - Return `()`, if the pipe is successfully closed. Otherwise returns a `pipe:Error`
    public isolated function immediateClose() returns Error? = @java:Method {
        'class: "io.xlibb.pipe.Pipe"
    } external;

    # Closes the pipe gracefully. Waits for some grace period until all the events in the pipe is consumed.
    #
    # + timeout - The maximum grace period (in seconds) to wait until the pipe is empty. Setting the timeout to a negative value will
    #             return a `pipe:Error`
    # + return - Return `()`, if the pipe is successfully closed. Otherwise returns a `pipe:Error`
    public isolated function gracefulClose(decimal timeout = 30) returns Error? = @java:Method {
        'class: "io.xlibb.pipe.Pipe"
    } external;

    # Checks whether the pipe is closed.
    #
    # + return - Returns `true`, if the pipe is closed. Otherwise returns `false`
    public isolated function isClosed() returns boolean = @java:Method {
        'class: "io.xlibb.pipe.Pipe"
    } external;
}
