## Overview

This module provides APIs to simultaneously send and receive data. And it includes an API to return data as a stream.

### Pipe

The Pipe allows you to send data from one place to another. Following are the APIs of the Pipe.

#### Create a `pipe:Pipe` instance

A `pipe:Pipe` instance can be created as follows. It will be used as a channel to produce and consume data. Each `pipe:Pipe` has a limit indicating the number of entries it can hold at one time.

```ballerina
import nuvindu/pipe;

pipe:Pipe pipe = new(limit = 10);
```

#### Produce Data

Events can be produced to the pipe using the following method. It allows `any` type of events and they can be added up to the given limit of the pipe. When the pipe is full, it will block further producing events to the pipe. And nil values are not allowed to be produced to a pipe. If data is successfully produced, the method will return `()`. Otherwise, it will return `pipe:Error`.

When the pipe is blocked, there is a waiting period to keep the event in the buffer. The waiting period has to be manually set using the `timeout` parameter and it is in `SECONDS`. After the timeout, the pipe will return a `pipe:Error` stating that the operation has timed out.

```ballerina
pipe:Error? produce = pipe.produce(event, timeout = 5);
```

Producing data to a closed pipe is not allowed. It will return a `pipe:Error`.

#### Consume Data

Events produced to the pipe can be consumed using this method. The type of the return value is inferred using the expected type from the function. If the return type cannot be cast into the expected type it will return a `TypeCast Error`.

If there is no data available in the pipe, it will wait until the `timeout` elapses (which has to be manually set in `SECONDS`). After the `timeout`, the pipe will return a `pipe:Error` stating that the operation has timed out.

```ballerina
string|pipe:Error event = pipe.consume(timeout = 10);
```

#### Consume Data as a Stream

Using the following method, events in the pipe can be consumed via a stream. The stream type is inferred using the expected type from the function. If the return type cannot be cast into the expected type it will return a `TypeCast Error`.

The `consume` method is used here as an underlying method. Therefore a `timeout` needs to be set to
specify the maximum waiting period to consume data.

```ballerina
stream<string, error?> eventStream = pipe.consumeStream(timeout = 5.12323);

record {|string value;|}|error? nextEvent = dataStream.next();
```

If the 'nextEvent' is neither `error` nor `null`, the event produced to the pipe can be received as a `record`.

```ballerina
string event = nextEvent.value
```

### Closing Pipes

Closing a pipe can be complicated because there can be running APIs when the closing process starts. Therefore, when the closing method is invoked, the pipe is designed to allow no data to be produced.

Any pipe can be checked whether it is closed using this method. Closing of a closed pipe will return a `pipe:Error`.

```ballerina
boolean isClosed = pipe.isClosed();
```

Even if the pipe is closed, both `consume` methods can be invoked.

#### Graceful Close

In the `gracefulClose` method, the remaining data in the pipe can be consumed for a specific period. The default timeout period is 30 seconds. But it can be manually set to a user's preferred time.

After that period, all the data is removed and the pipe instance is taken by the garbage collector. This graceful approach can reduce the damage that happened to the normal behavior of the pipe by suddenly closing it. If the pipe is successfully closed it will return `()`. Otherwise, it will return `pipe:Error`.

```ballerina
Error? gracefulClose = pipe.gracefulClose(timeout = 30);
```

#### Immediate Close

This method will immediately close the pipe neglecting the graceful approach. If the pipe is successfully closed it will return `()`. Otherwise, it will return `pipe:Error`. Unexpected errors may occur.

```ballerina
Error? immediateClose = pipe.immediateClose();
```

### Errors

Any error related to the `Pipe` module can be represented by `pipe:Error`.
