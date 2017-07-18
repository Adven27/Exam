package com.sberbank.pfm.test.concordion.extensions.exam.db.commands;

import com.sberbank.pfm.test.TableData;
import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import org.concordion.api.*;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

public class DBCommand extends AbstractCommand {
    protected final JdbcDatabaseTester dbTester;
    protected ITable expectedTable;
    private final Map<String, String> remarks = new HashMap<>();

    public DBCommand(JdbcDatabaseTester dbTester) {
        this.dbTester = dbTester;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element el = commandCall.getElement();
        el.addStyleClass("table table-condensed");
        try {
            expectedTable = TableData.filled(parseTableName(el), parseRows(el, evaluator), parseCols(el, evaluator));
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected TableData.Cols parseCols(Element el, Evaluator eval) {
        List<String> cols = new ArrayList<>();
        Map<String, Object> defaults = new HashMap<>();
        String attr = el.getAttributeValue("cols");
        if (attr != null) {
            for (String col : attr.split(",")) {
                String c = col.trim();
                String remark = null;
                if (c.startsWith("*")) {
                    int endRemark = c.lastIndexOf("*");
                    remark = c.substring(0, endRemark + 1);
                    c = c.substring(endRemark + 1);
                }
                if (c.contains("=")) {
                    String[] colDefault = c.split("=");
                    defaults.put(colDefault[0], PlaceholdersResolver.resolveToObj(colDefault[1], eval));
                    if (remark != null) {
                        remarks.put(colDefault[0], remark);
                    }
                } else {
                    cols.add(c);
                    if (remark != null) {
                        remarks.put(c, remark);
                    }
                }
            }
            el.removeAttribute("cols");
        }
        return new TableData.Cols(defaults, cols.toArray(new String[cols.size()]));
    }


    protected List<List<Object>> parseRows(Element el, Evaluator evaluator) {
        List<List<Object>> result = new ArrayList<>();
        for (Element r : el.getChildElements()) {
            if ("row".equals(r.getLocalName())) {
                result.add(parseValues(r, evaluator));
                el.removeChild(r);
            }
        }
        return result;
    }

    protected String parseTableName(Element el) {
        final String table = el.getAttributeValue("table");
        el.removeAttribute("table");
        return table;
    }

    protected void renderTable(Element element, ITable t) {
        try {
            List<List<String>> rows = new ArrayList<>();
            Column[] cols = t.getTableMetaData().getColumns();

            Arrays.sort(cols, new Comparator<Column>() {
                @Override
                public int compare(Column o1, Column o2) {
                    return -1 * Integer.compare(length(o1), length(o2));
                }

                private int length(Column c) {
                    return remarks.containsKey(c.getColumnName()) ? remarks.get(c.getColumnName()).length() : 0;
                }
            });


            for (int i = 0; i < t.getRowCount(); i++) {
                List<String> row = new ArrayList<>();
                for (Column col : cols) {
                    try {
                        Object value = t.getValue(i, col.getColumnName());
                        row.add(value == null ? "(null)" : value.toString());
                    } catch (DataSetException e) {
                        throw new RuntimeException(e);
                    }
                }
                rows.add(row);
            }

            String title = element.getAttributeValue("caption");
            if (!isNullOrEmpty(title)) {
                element.appendChild(new Element("caption").appendText(title));
            }

            Element headerRow = new Element("tr");
            for (Column col : cols) {
                headerRow.appendChild(new Element("th").appendText(col.getColumnName()).
                        addStyleClass(markedColumn(col)));
            }
            element.appendChild(headerRow);

            for (List<String> row : rows) {
                Element tr = new Element("tr");
                for (int i = 0; i < row.size(); i++) {
                    tr.appendChild(new Element("td").appendText(row.get(i)).
                            addStyleClass(markedColumn(cols[i])));
                }
                element.appendChild(tr);
            }
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    private String markedColumn(Column col) {
        return remarks.containsKey(col.getColumnName()) ? "bg-info" : "text-muted";
    }

    private List<Object> parseValues(Element r, Evaluator eval) {
        List<Object> values = new ArrayList<>();
        String comaSeparatedValues = r.getText();
        if (!isNullOrEmpty(comaSeparatedValues)) {
            for (String val : comaSeparatedValues.split(",")) {
                values.add(PlaceholdersResolver.resolveToObj(val, eval));
            }
        }
        return values;
    }
}