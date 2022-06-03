public type CovidRecord record {|
    Report value;
|};

public type IntRecord record {|
    int value;
|};

public type Report record {|
    string date;
    int positive;
    int hospitalizedCurrently;
    int hospitalizedTotal;
    int deaths;
|};
