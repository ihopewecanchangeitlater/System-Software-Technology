package com.google.refine.model.changes;

import java.util.ArrayList;
import java.util.List;

import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnGroup;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class ColumnReorderHandler {

    private final List<String> columnNames;
    private List<Column> oldColumns;
    private List<Column> newColumns;
    private List<Column> removedColumns;
    private List<ColumnGroup> oldColumnGroups;
    private CellAtRowCellIndex[] oldCells;

    public ColumnReorderHandler(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public void reorderColumns(Project project) {
        synchronized (project) {
            if (newColumns == null) {
                initializeColumns(project);
            }

            if (removedColumns == null) {
                extractRemovedColumns(project);
            }

            if (oldCells == null) {
                backupOldCells(project);
            }

            clearRemovedCells(project);
            updateProjectColumns(project);
        }
    }

    public void revertColumnOrder(Project project) {
        synchronized (project) {
            project.columnModel.columns.clear();
            project.columnModel.columns.addAll(oldColumns);

            project.columnModel.columnGroups.clear();
            project.columnModel.columnGroups.addAll(oldColumnGroups);

            for (CellAtRowCellIndex cellInfo : oldCells) {
                Row row = project.rows.get(cellInfo.row);
                row.setCell(cellInfo.cellIndex, cellInfo.cell);
            }

            project.update();
        }
    }

    // Setter methods for assigning oldColumns and newColumns
    public void setOldColumns(List<Column> oldColumns) {
        this.oldColumns = oldColumns;
    }

    public void setNewColumns(List<Column> newColumns) {
        this.newColumns = newColumns;
    }

    // Additional setters if needed for other fields
    public void setRemovedColumns(List<Column> removedColumns) {
        this.removedColumns = removedColumns;
    }

    public void setOldColumnGroups(List<ColumnGroup> oldColumnGroups) {
        this.oldColumnGroups = oldColumnGroups;
    }

    public void setOldCells(CellAtRowCellIndex[] oldCells) {
        this.oldCells = oldCells;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Column> getOldColumns() {
        return oldColumns;
    }

    public List<Column> getNewColumns() {
        return newColumns;
    }

    public List<Column> getRemovedColumns() {
        return removedColumns;
    }

    public CellAtRowCellIndex[] getOldCells() {
        return oldCells;
    }

    public List<ColumnGroup> getOldColumnGroups() {
        return oldColumnGroups;
    }

    private void initializeColumns(Project project) {
        newColumns = new ArrayList<>();
        oldColumns = new ArrayList<>(project.columnModel.columns);

        for (String name : columnNames) {
            Column column = project.columnModel.getColumnByName(name);
            if (column != null) {
                newColumns.add(column);
            }
        }

        oldColumnGroups = new ArrayList<>(project.columnModel.columnGroups);
    }

    private void extractRemovedColumns(Project project) {
        removedColumns = new ArrayList<>();
        for (String name : project.columnModel.getColumnNames()) {
            Column oldColumn = project.columnModel.getColumnByName(name);
            if (!newColumns.contains(oldColumn)) {
                removedColumns.add(oldColumn);
            }
        }
    }

    private void backupOldCells(Project project) {
        oldCells = new CellAtRowCellIndex[project.rows.size() * removedColumns.size()];

        int count = 0;
        for (int i = 0; i < project.rows.size(); i++) {
            for (int j = 0; j < removedColumns.size(); j++) {
                int cellIndex = removedColumns.get(j).getCellIndex();
                Row row = project.rows.get(i);

                Cell oldCell = null;
                if (cellIndex < row.cells.size()) {
                    oldCell = row.cells.get(cellIndex);
                }
                oldCells[count++] = new CellAtRowCellIndex(i, cellIndex, oldCell);
            }
        }
    }

    private void clearRemovedCells(Project project) {
        for (int i = 0; i < project.rows.size(); i++) {
            for (int j = 0; j < removedColumns.size(); j++) {
                int cellIndex = removedColumns.get(j).getCellIndex();
                Row row = project.rows.get(i);
                row.setCell(cellIndex, null);
            }
        }
    }

    private void updateProjectColumns(Project project) {
        project.columnModel.columns.clear();
        project.columnModel.columns.addAll(newColumns);
        project.columnModel.columnGroups.clear();
        project.update();
    }
}
