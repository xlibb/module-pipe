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
    private handle nativePipeObject;

    # Creates a new `pipe:Pipe` instance.
    #
    # + 'limit - The maximum number of entries that are held in the pipe at once
    public isolated function init(int 'limit) {
        self.nativePipeObject = newPipe('limit);
    }

    # Produces events into the pipe.
    #
    # + events - Events that needs to be produced to the pipe. Can be `any` type
    # + timeout - The maximum waiting period that holds events
    # + return - Returns `()` if events is successfully produced. Otherwise returns a `pipe:Error`
    public isolated function produce(any events, decimal timeout) returns Error? = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;

    # Consumes events in the pipe.
    #
    # + timeout - The maximum waiting period to consume events
    # + typeParam - The `type` of data that is needed to be consumed. When not provided, the type is inferred 
    # using the expected type from the function
    # + return - Return type is inferred from the user specified type. That should be the same event type
    # produced to the pipe. Otherwise, returns a `pipe:Error`
    public isolated function consume(decimal timeout, typedesc<any> typeParam = <>)
        returns typeParam|Error = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;

    # Consumes events in the pipe as a `stream`
    #
    # + timeout - The maximum waiting period to consume events
    # + typeParam - The `type` of data that is needed to be consumed. When not provided, the type is inferred 
    # using the expected type from the function
    # + return - Returns a `stream`. The stream type is inferred from the user specified type
    public isolated function consumeStream(decimal timeout, typedesc<any> typeParam = <>)
        returns stream<typeParam, error?> = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;

    # Closes the pipe instantly.
    # + return - Return `()`, if the pipe is successfully closed. Otherwise returns a `pipe:Error`
    public isolated function immediateClose() returns Error? {
        lock {
            check immediateClose(self.nativePipeObject);
        }
    }

    # Closes the pipe gracefully. Waits for some grace period until all the events in the pipe is consumed.
    #
    # + timeout - The maximum grace period to wait until the pipe is empty. The default timeout is thirty seconds
    # + return - Return `()`, if the pipe is successfully closed. Otherwise returns a `pipe:Error`
    public isolated function gracefulClose(decimal timeout = 30) returns Error? = @java:Method {
        'class: "org.nuvindu.pipe.Pipe"
    } external;

    # Checks whether the pipe is closed.
    #
    # + return - Returns `true`, if the pipe is closed. Otherwise returns `false`
    public isolated function isClosed() returns boolean {
        lock {
            return isClosed(self.nativePipeObject);
        }
    }
}

isolated function newPipe(int 'limit) returns handle = @java:Constructor {
    'class: "org.nuvindu.pipe.Pipe"
} external;

isolated function immediateClose(handle pipe) returns Error? = @java:Method {
    'class: "org.nuvindu.pipe.Pipe"
} external;

isolated function isClosed(handle pipe) returns boolean = @java:Method {
    'class: "org.nuvindu.pipe.Pipe"
} external;
