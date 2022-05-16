public class Report {

    private string date;
    private int positive;
    private int hospitalizedCurrently;
    private int hospitalizedTotal;
    private int deaths;

    function init(string date, int positive, int hospitalizedCurrently, int hospitalizedTotal, int deaths) {
        self.date = date;
        self.positive = positive;
        self.hospitalizedCurrently = hospitalizedCurrently;
        self.hospitalizedTotal = hospitalizedTotal;
        self.deaths = deaths;
    }

    public function getDeaths() returns int {
        return self.deaths;
    }

    public function getDate() returns string {
        return self.date;
    }

    public function getPositives() returns int {
        return self.positive;
    }
}
