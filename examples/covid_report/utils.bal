import ballerina/io;
public function getReportData() returns Report[]|error {
    json inputs = check io:fileReadJson("modules/resources/data_source.json");
    Report[] reports = [];
    foreach json data in <json[]>inputs {
        string date = (check data.date).toString();
        int positive = check data.positive;
        int hospitalizedCurrently = check data.hospitalizedCurrently;
        int hospitalizedTotal = check data.hospitalizedCumulative;
        int deaths = check data.death;
        reports.push({date: date, positive: positive, hospitalizedCurrently: hospitalizedCurrently,
                     hospitalizedTotal: hospitalizedTotal, deaths: deaths});
    }
    return reports.reverse();
}
