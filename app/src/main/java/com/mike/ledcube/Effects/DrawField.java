package com.mike.ledcube.Effects;

import android.graphics.Color;

import java.util.HashSet;
import java.util.Set;

interface OnFieldChangedListener {
    void onFieldChange(DrawField field);
}

public class DrawField {
    private final int rows;
    private final int columns;
    private final int[][] cells;

    public DrawField(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        cells = new int[rows][];
        for (int i = 0; i < rows; i++) {
            cells[i] = new int[columns];
            for (int j = 0; j < columns; j++) {
                cells[i][j] = Color.BLACK;
            }
        }
    }

    public Set<OnFieldChangedListener> listeners = new HashSet<>();

    public int getCell(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) return 0;
        return cells[row][column];
    }

    public void setCell(int row, int column, int cell) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) return;
        if (cells[row][column] != cell) {
            cells[row][column] = cell;
            for (OnFieldChangedListener listener : listeners)
                listener.onFieldChange(this);
        }
    }

    public void setCell(int row, int column, int cell, boolean notify) {
        if (notify) setCell(row, column, cell);
        else {
            if (row < 0 || row >= rows || column < 0 || column >= columns) return;
            if (cells[row][column] != cell) {
                cells[row][column] = cell;
            }
        }
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public void clear() {
        for (int i = 0; i < rows; i++) {
            cells[i] = new int[columns];
            for (int j = 0; j < columns; j++) {
                cells[i][j] = Color.BLACK;
            }
        }
        for (OnFieldChangedListener listener : listeners)
            listener.onFieldChange(this);
    }
}
