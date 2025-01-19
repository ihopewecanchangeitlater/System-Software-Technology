package com.google.refine.model.changes;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.refine.history.Change;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnGroup;
import com.google.refine.util.Pool;

public class ColumnReorderSerializer {

    public void save(Writer writer, ColumnReorderHandler handler) throws IOException {
        writer.write("columnNameCount=");
        writer.write(Integer.toString(handler.getColumnNames().size()));
        writer.write('\n');
        for (String columnName : handler.getColumnNames()) {
            writer.write(columnName);
            writer.write('\n');
        }

        writer.write("oldColumnCount=");
        writer.write(Integer.toString(handler.getOldColumns().size()));
        writer.write('\n');
        for (Column column : handler.getOldColumns()) {
            column.save(writer);
            writer.write('\n');
        }

        writer.write("newColumnCount=");
        writer.write(Integer.toString(handler.getNewColumns().size()));
        writer.write('\n');
        for (Column column : handler.getNewColumns()) {
            column.save(writer);
            writer.write('\n');
        }

        writer.write("removedColumnCount=");
        writer.write(Integer.toString(handler.getRemovedColumns().size()));
        writer.write('\n');
        for (Column column : handler.getRemovedColumns()) {
            column.save(writer);
            writer.write('\n');
        }
    }

    public static Change load(LineNumberReader reader, Pool pool) throws Exception {
        List<String> columnNames = new ArrayList<>();
        List<Column> oldColumns = new ArrayList<>();
        List<Column> newColumns = new ArrayList<>();
        List<Column> removedColumns = new ArrayList<>();
        CellAtRowCellIndex[] oldCells = new CellAtRowCellIndex[0];
        List<ColumnGroup> oldColumnGroups = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null && !"/ec/".equals(line)) {
            int equal = line.indexOf('=');
            String field = line.substring(0, equal);

            if ("columnNameCount".equals(field)) {
                int count = Integer.parseInt(line.substring(equal + 1));
                for (int i = 0; i < count; i++) {
                    columnNames.add(reader.readLine());
                }
            } else if ("oldColumnCount".equals(field)) {
                int count = Integer.parseInt(line.substring(equal + 1));
                for (int i = 0; i < count; i++) {
                    oldColumns.add(Column.load(reader.readLine()));
                }
            } else if ("newColumnCount".equals(field)) {
                int count = Integer.parseInt(line.substring(equal + 1));
                for (int i = 0; i < count; i++) {
                    newColumns.add(Column.load(reader.readLine()));
                }
            } else if ("removedColumnCount".equals(field)) {
                int count = Integer.parseInt(line.substring(equal + 1));
                for (int i = 0; i < count; i++) {
                    removedColumns.add(Column.load(reader.readLine()));
                }
            }
        }

        ColumnReorderHandler handler = new ColumnReorderHandler(columnNames);
        handler.setOldColumns(oldColumns);
        handler.setNewColumns(newColumns);
        handler.setRemovedColumns(removedColumns);
        handler.setOldCells(oldCells);

        return new ColumnReorderChange(columnNames);
    }
}
