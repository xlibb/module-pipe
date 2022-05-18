# Real-time Covid Stats

## Overview

This application notifies real-time statistics of Covid-19.

## Implementation

There is a mock data set added as the source for Covid-19 statistics. The Pipe is used as the mediator here. Each data retrieved from the data source will be added to the pipe within a time gap. </br>
Assume there is a user who needs to view these data. The pipe can return data as a stream to the user. Whenever new data is added to the pipe, the user can view it through the stream.</br>
After data has been produced to the pipe, it can be gracefully closed.

## Run the Example

```ballerina 
cd .\examples\covid_report
bal run
```
