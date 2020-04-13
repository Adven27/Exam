package specs.xj;

import org.joda.time.LocalDate;
import specs.Specs;

public class Xj extends Specs {
    private static final String NOW = LocalDate.now().toString("yyyy-MM-dd");

    public String getActualJson() {
        return "{\"date\" : \"" + NOW + "\"}";
    }

    public String getActualXml() {
        return "<date>" + NOW + "</date>";
    }

    public String getActualJsonWithFieldsToIgnore() {
        return "{ " +
            "\"param1\":\"value1\", " +
            "\"param2\":\"ignore\", " +
            "\"arr\": [" +
            "{\"param3\":\"value3\", \"param4\":\"ignore\"}, " +
            "{\"param3\":\"ignore\", \"param4\":\"value4\"}" +
            "]" +
            "}";
    }
}