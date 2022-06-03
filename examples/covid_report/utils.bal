import ballerina/io;

public function getReportData() returns Report[]|error {
    json inputs = check io:fileReadJson("modules/resources/data_source.json");
    Report[] reports = check inputs.cloneWithType();
    return reports.reverse();
}
