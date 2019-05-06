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
}