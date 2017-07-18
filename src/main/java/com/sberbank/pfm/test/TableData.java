package com.sberbank.pfm.test;

import org.apache.commons.collections.map.HashedMap;
import org.dbunit.dataset.*;
import org.dbunit.dataset.builder.DataRowBuilder;
import org.dbunit.dataset.builder.DataSetBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.dbunit.dataset.datatype.DataType.UNKNOWN;

public class TableData {
    private final String table;
    private final Map<String, Object> defaults;
    private final List<String> columns;
    private DataSetBuilder dataSetBuilder = getDataSetBuilder();

    public TableData(String table, String... columns) {
        this(table, new HashedMap(), columns);
    }

    public TableData(String table, Map<String, Object> defaults, String... columns) {
        this.table = table;
        this.defaults = defaults;
        this.columns = asList(columns);
    }

    public TableData(String table, Cols columns) {
        this(table, columns.defaults(), columns.cols());
    }

    public TableData(String table, List<String> cols) {
        this(table, cols.toArray(new String[cols.size()]));
    }

    private static DataSetBuilder getDataSetBuilder() {
        try {
            return new DataSetBuilder();
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    public static ITable filled(String table, List<List<Object>> rows, Cols cols) throws DataSetException {
        return new TableData(table, cols).rows(rows).table();
    }

    public TableData row(Object... values) throws DataSetException {
        DataRowBuilder dataRowBuilder = dataSetBuilder.newRow(table);
        for (int i = 0; i < values.length; i++) {
            dataRowBuilder.with(columns.get(i), values[i]);
        }
        for (Map.Entry<String, Object> def : defaults.entrySet()) {
            dataRowBuilder.with(def.getKey(), def.getValue());
        }
        dataSetBuilder = dataRowBuilder.add();
        return this;
    }

    public TableData row(List<Object> values) throws DataSetException {
        return row(values.toArray(new Object[values.size()]));
    }

    public TableData rows(List<List<Object>> rows) throws DataSetException {
        for (List<Object> r : rows) {
            row(r);
        }
        return this;
    }

    public IDataSet build() throws DataSetException {
        return dataSetBuilder.build();
    }

    public ITable table() throws DataSetException {
        IDataSet dataSet = build();
        return dataSet.getTableNames().length == 0 ?
                new DefaultTable(table, columns(columns)) : dataSet.getTable(table);
    }

    private Column[] columns(List<String> c) {
        Column[] columns = new Column[c.size()];
        for (int i = 0; i < c.size(); i++) {
            columns[i] = new Column(c.get(i), UNKNOWN);
        }
        return columns;
    }

    public static class Cols {
        private final String[] cols;
        private final Map<String, Object> defaults;

        public Cols(Map<String, Object> defaults, String... cols) {
            this.cols = cols;
            this.defaults = defaults;
        }

        public String[] cols() {
            return cols.clone();
        }

        public Map<String, Object> defaults() {
            return new HashMap<>(defaults);
        }
    }
}