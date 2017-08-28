package com.sberbank.pfm.test.concordion.extensions.exam.db.commands;

import com.sberbank.pfm.test.TableData;
import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.AbstractCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;

public class DBCommand extends AbstractCommand {
    protected final JdbcDatabaseTester dbTester;
    private final Map<String, String> remarks = new HashMap<>();
    protected ITable expectedTable;

    public DBCommand(JdbcDatabaseTester dbTester) {
        this.dbTester = dbTester;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html root = new Html(commandCall.getElement()).style("table table-condensed");
        try {
            remarks.clear();
            expectedTable = TableData.filled(
                    root.takeAwayAttr("table", eval), parseRows(root, eval), parseCols(root, eval));
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected TableData.Cols parseCols(Html el, Evaluator eval) {
        List<String> cols = new ArrayList<>();
        Map<String, Object> defaults = new HashMap<>();
        String attr = el.takeAwayAttr("cols");
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
        }
        return new TableData.Cols(defaults, cols.toArray(new String[cols.size()]));
    }


    protected List<List<Object>> parseRows(Html el, Evaluator evaluator) {
        List<List<Object>> result = new ArrayList<>();
        for (Html r : el.childs()) {
            if ("row".equals(r.localName())) {
                result.add(parseValues(evaluator, r.text()));
                el.remove(r);
            }
        }
        return result;
    }

    protected void renderTable(Html root, ITable t) {
        try {
            List<List<String>> rows = new ArrayList<>();
            final Column[] columns = t.getTableMetaData().getColumns();
            Column[] cols = Arrays.copyOf(columns, columns.length);

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

            String title = root.takeAwayAttr("caption");
            root.childs(caption(isNullOrEmpty(title) ? t.getTableMetaData().getTableName() : title));

            Html header = tr();
            for (Column col : cols) {
                header.childs(
                        th(col.getColumnName()).style(markedColumn(col))
                );
            }
            root.childs(header);

            for (List<String> row : rows) {
                Html tr = tr();
                for (int i = 0; i < row.size(); i++) {
                    tr.childs(
                            td(row.get(i)).style(markedColumn(cols[i]))
                    );
                }
                root.childs(tr);
            }
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    private String markedColumn(Column col) {
        return remarks.containsKey(col.getColumnName()) ? "bg-info" : "text-muted";
    }

    private List<Object> parseValues(Evaluator eval, String text) {
        List<Object> values = new ArrayList<>();
        String comaSeparatedValues = text;
        if (!isNullOrEmpty(comaSeparatedValues)) {
            for (String val : comaSeparatedValues.split(",")) {
                val = val.trim();
                if (val.startsWith("'") && val.endsWith("'")) {
                    val = val.substring(1, val.length() - 1);
                }
                values.add(PlaceholdersResolver.resolveToObj(val, eval));
            }
        }
        return values;
    }
}