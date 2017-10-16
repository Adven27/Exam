package com.adven.concordion.extensions.exam.db.commands;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.db.TableData;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.html.RowParser;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.oracle.OracleDataTypeFactory;

import java.util.*;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY;

public class DBCommand extends ExamCommand {
    protected final IDatabaseTester dbTester;
    private final Map<String, String> remarks = new HashMap<>();
    protected ITable expectedTable;

    public DBCommand(String name, String tag, IDatabaseTester dbTester) {
        super(name, tag);
        this.dbTester = dbTester;
    }

    //Fix for warning "Potential problem found: The configured data type factory 'class org.dbunit.dataset.datatype.DefaultDataTypeFactory'"
    private void getRidOfDbUnitWarning() {
        try {
            IDatabaseConnection connection = dbTester.getConnection();
            final String dbName = connection.getConnection().getMetaData().getDatabaseProductName();
            DatabaseConfig dbConfig = connection.getConfig();
            switch (dbName) {
                case "HSQL Database Engine":
                    dbConfig.setProperty(PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
                    break;
                case "H2":
                    dbConfig.setProperty(PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
                    break;
                case "Oracle":
                    dbConfig.setProperty(PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());
                    break;
                default:
                    System.err.println("No matching database product found " + dbName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        getRidOfDbUnitWarning();
        Html root = tableSlim(new Html(commandCall.getElement()));
        try {
            remarks.clear();
            expectedTable = TableData.filled(
                    root.takeAwayAttr("table", eval),
                    new RowParser(root, "row", eval).parse(),
                    parseCols(root, eval)
            );
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
            root.childs(dbCaption(t, title));

            Html header = thead();
            Html trh = tr();
            for (Column col : cols) {
                trh.childs(
                        th(col.getColumnName()).css(markedColumn(col))
                );
            }
            root.childs(header.childs(trh));
            Html tbody = tbody();
            for (List<String> row : rows) {
                Html tr = tr();
                for (int i = 0; i < row.size(); i++) {
                    tr.childs(
                            td(row.get(i)).css(markedColumn(cols[i]))
                    );
                }
                tbody.childs(tr);
            }
            root.childs(tbody);
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected Html dbCaption(ITable t, String title) {
        return Html.caption().childs(
                italic("").css("fa fa-database fa-pull-left fa-border")
        ).text(isNullOrEmpty(title) ? t.getTableMetaData().getTableName() : title);
    }

    private String markedColumn(Column col) {
        return remarks.containsKey(col.getColumnName()) ? "table-info" : "";
    }
}