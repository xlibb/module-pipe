# Ballerina Pipe Library

[![Build](https://github.com/Nuvindu/module-pipe/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/Nuvindu/module-pipe/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/Nuvindu/module-pipe/branch/main/graph/badge.svg)](https://codecov.io/gh/Nuvindu/module-pipe)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/Nuvindu/module-pipe.svg)](https://github.com/Nuvindu/module-pipe/commits/main)
[![Github issues](https://img.shields.io/github/issues/Nuvindu/module-pipe/module/pipe.svg?label=Open%20Issues)](https://github.com/Nuvindu/module-pipe/labels/module%2Fpipe)

This library provides a medium to send and receive events simultaneously. And it includes APIs to produce, consume and return events via a stream.

**Note:** This library is originally developed by @Nuvindu and it is then moved to the xlibb organization.

## Pipe

The pipe allows you to send events from one place to another. The pipe can hold up to n number of data. In case the pipe is full, the `produce` method blocks until there is a free slot to produce data. On the other hand, in case the pipe is empty, the `consume` method blocks until there is some data to consume.

#### Create a `pipe:Pipe` instance

A `pipe:Pipe` instance can be created as follows. It will be used as a channel to produce and consume events. Each `pipe:Pipe` has a limit indicating the number of entries it can hold at one time.

```ballerina
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new('limit = 10);
}
```

### APIs associated with Pipe

- <b> produce </b>: Produces events into the pipe. If the pipe is full, it blocks further producing events.
- <b> consume </b>: Consumes events in the pipe. If the pipe is empty, it blocks until events are available in the pipe.
- <b> consumeStream </b>: Returns a stream. Events can be consumed by iterating the stream.
- <b> immediateClose </b>: Closes the pipe instantly. All the events in the pipe will be discarded.
- <b> gracefulClose </b>: Closes the pipe gracefully. A grace period is provided to consume available events in the pipe. After the period, all the events will be discarded.
- <b> isClosed </b>: Returns the closing status of the pipe.

#### Produce Events

Events can be produced to the pipe using the following method. It allows `any` type of event and they can be added up to the given limit of the pipe. When the pipe is full, it will block further producing events to the pipe. And nil values are not allowed to be produced to a pipe. If the event is successfully produced, the method will return `()`. Otherwise, it will return `pipe:Error`.

When the pipe is blocked, there is a waiting period to keep the event in the buffer. The waiting period has to be manually set using the `timeout` parameter and it is in `SECONDS`. After the timeout, the pipe will return a `pipe:Error` stating that the operation has timed out.

```ballerina
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new('limit = 10);
    check pipe.produce("event", timeout = 5);
}
```

Producing events to a closed pipe is not allowed. It will return a `pipe:Error`.

#### Consume Events

Events produced to the pipe can be consumed using this method. The type of the return value is inferred using the expected type from the function. If the return type cannot be cast into the expected type it will return a `TypeCast Error`.

If there is no event available in the pipe, it will wait until the `timeout` elapses (which has to be manually set in `SECONDS`). After the `timeout`, the pipe will return a `pipe:Error` stating that the operation has timed out.

```ballerina
import ballerina/io;
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new('limit = 10);
    string event = "event";
    check pipe.produce(event, timeout = 5);

    string consumedEvent = check pipe.consume(timeout = 10);
    io:println(consumedEvent);
}
```

#### Consume Events via a Stream

Using the following method, events in the pipe can be consumed via a stream. The stream type is inferred using the expected type from the function. If the return type cannot be cast into the expected type it will return a `TypeCast Error`.

The `consume` method is used here as an underlying method. Therefore a `timeout` needs to be set to
specify the maximum waiting period to consume events.

```ballerina
import ballerina/io;
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new('limit = 10);
    string event = "event";
    check pipe.produce(event, timeout = 5);

    stream<string, error?> eventStream = pipe.consumeStream(timeout = 5.12323);
    record {|string value;|}? nextEvent = check eventStream.next();
    if nextEvent != () {
        string consumedEvent = nextEvent.value;
        io:println(consumedEvent);
    }
}
```

### Closing Pipes

Closing a pipe can be complicated because there can be running APIs when the closing process starts. Therefore, when the closing method is invoked, the pipe is designed to allow no event to be produced. Closing of a closed pipe will return a `pipe:Error`. Even if the pipe is closed, both `consume` methods can be invoked.

#### Graceful Close

In the `gracefulClose` method, the remaining events in the pipe can be consumed for a specific period. The default timeout period is 30 seconds. But it can be manually set to a user's preferred time.

After that period, all the events are removed and the pipe instance is taken by the garbage collector. This graceful approach can reduce the damage that happened to the normal behavior of the pipe by suddenly closing it. If the pipe is successfully closed it will return `()`. Otherwise, it will return `pipe:Error`.

```ballerina
import ballerina/io;
import ballerina/lang.runtime;
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new(5);
    check pipe.produce("event", timeout = 5.00111);
    worker A {
        runtime:sleep(5);
        int|pipe:Error consumedEvent = pipe.consume(timeout = 5);
        io:println(consumedEvent);
    }
    @strand {
        thread: "any"
    }
    worker B {
        pipe:Error? close = pipe.gracefulClose(timeout = 10);
        pipe:Error? produce = pipe.produce("event", timeout = 5.00111); // This will produce an error
        io:println(produce);
    }
}
```

#### Immediate Close

This method will immediately close the pipe neglecting the graceful approach. If the pipe is successfully closed it will return `()`. Otherwise, it will return `pipe:Error`. Unexpected errors may occur.

```ballerina
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new('limit = 10);
    check pipe.immediateClose();

    check pipe.produce("event", timeout = 5); // This will produce an error
}
```

#### Check the Closing Status of the Pipe

This method will return a boolean value indicating whether the pipe is closed or not. If the pipe is closed, it will return `true`. Otherwise, it will return `false`.

```ballerina
import ballerina/io;
import nuvindu/pipe;

public function main() returns error? {
    pipe:Pipe pipe = new('limit = 10);
    boolean isClosed = pipe.isClosed();
    io:println(isClosed);

    check pipe.immediateClose();
    isClosed = pipe.isClosed();
    io:println(isClosed);
}
```

## Build from the source

### Set up the prerequisites

1.  Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).

    - [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

    - [OpenJDK](https://adoptopenjdk.net/)

      > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

2.  Export your Github Personal access token with the read package permissions as follows.

              export packageUser=<Username>
              export packagePAT=<Personal access token>

### Build the source

Execute the commands below to build from the source.

1. To build the library:

   ```
   ./gradlew clean build
   ```

2. To run the integration tests:
   ```
   ./gradlew clean test
   ```
3. To build the module without the tests:
   ```
   ./gradlew clean build -x test
   ```
4. To debug module implementation:
   ```
   ./gradlew clean build -Pdebug=<port>
   ./gradlew clean test -Pdebug=<port>
   ```
5. To debug the module with Ballerina language:
   ```
   ./gradlew clean build -PbalJavaDebug=<port>
   ./gradlew clean test -PbalJavaDebug=<port>
   ```
6. Publish ZIP artifact to the local `.m2` repository:
   ```
   ./gradlew clean build publishToMavenLocal
   ```
7. Publish the generated artifacts to the local Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToLocalCentral=true
   ```
8. Publish the generated artifacts to the Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToCentral=true
   ```
