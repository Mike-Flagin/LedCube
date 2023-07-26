package com.mike.ledcube.Effects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.mike.ledcube.R;

interface OnCellMoveListener {
    void onCellMove(int row, int column, DrawField field);
}

public class DrawFieldView extends View {
    private final Context context;
    private final AttributeSet attrs;
    private int defStyleAttr;
    private int defStyleRes;

    private int rows;
    private int columns;
    private int gridColor;

    private DrawField drawField;
    private float cellSize;
    private final RectF fieldRect = new RectF(0, 0, 0, 0);

    public OnCellMoveListener cellMoveListener = null;

    public DrawFieldView(Context context) {
        super(context);
        this.context = context;
        attrs = null;
    }

    public DrawFieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        defStyleRes = R.attr.drawFieldStyle;
        initAttributes();
    }

    public DrawFieldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        initAttributes();
    }

/*    public DrawFieldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        this.defStyleRes = defStyleRes;
        initAttributes();
    }*/

    @SuppressLint("ClickableViewAccessibility")
    private void initAttributes() {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawFieldView, defStyleAttr, defStyleRes);
            gridColor = typedArray.getColor(R.styleable.DrawFieldView_gridColor, Color.GRAY);
            typedArray.recycle();
        } else {
            gridColor = Color.GRAY;
        }
        setFocusable(true);
        setClickable(true);
    }


    /**
     * Use this method to set draw field
     *
     * @param drawfield draw field to draw
     */
    public void setDrawField(DrawField drawfield) {
        if (drawField != null) drawField.listeners.remove(listener);
        this.drawField = drawfield;
        drawField.listeners.add(listener);
        rows = drawfield.getRows();
        columns = drawfield.getColumns();
        updateViewSizes();
        invalidate();
    }

    public void setGridColor(int color) {
        gridColor = color;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (drawField != null) drawField.listeners.add(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (drawField != null) drawField.listeners.remove(listener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateViewSizes();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = widthMeasureSpec;
        int height = heightMeasureSpec;
        if ((MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) &&
                (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)) {
            if (MeasureSpec.getSize(widthMeasureSpec) > MeasureSpec.getSize(heightMeasureSpec)) {
                width = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY);
                setMeasuredDimension(width, heightMeasureSpec);
            } else {
                height = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY);
                setMeasuredDimension(widthMeasureSpec, height);
            }
        }
        super.onMeasure(width, height);
    }

    private void updateViewSizes() {
        if (drawField == null) return;
        int safeWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int safeHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        float cellWidth = (float) safeWidth / drawField.getColumns();
        float cellHeight = (float) safeHeight / drawField.getRows();
        cellSize = Math.min(cellWidth, cellHeight);

        float fieldWidth = cellSize * drawField.getColumns();
        float fieldHeight = cellSize * drawField.getRows();

        fieldRect.left = getPaddingLeft();
        fieldRect.top = getPaddingTop();
        fieldRect.right = fieldRect.left + fieldWidth;
        fieldRect.bottom = fieldRect.top + fieldHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawField == null) return;
        drawCells(canvas);
        drawGrid(canvas);
    }

    private void drawCells(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(gridColor);
        paint.setStyle(Paint.Style.FILL);

        float xStart = fieldRect.left;
        float yStart = fieldRect.top;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                paint.setColor(drawField.getCell(i, j));
                canvas.drawRect(xStart + j * cellSize, yStart + i * cellSize, xStart + (j + 1) * cellSize, yStart + (i + 1) * cellSize, paint);
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(gridColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics()));

        float xStart = fieldRect.left;
        float xEnd = fieldRect.right;
        for (int i = 0; i <= drawField.getRows(); i++) {
            float y = fieldRect.top + cellSize * i;
            canvas.drawLine(xStart, y, xEnd, y, paint);
        }

        float yStart = fieldRect.top;
        float yEnd = fieldRect.bottom;
        for (int i = 0; i <= drawField.getColumns(); i++) {
            float x = fieldRect.left + cellSize * i;
            canvas.drawLine(x, yStart, x, yEnd, paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawField == null) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
            case MotionEvent.ACTION_MOVE:
                int row = getRow(event);
                int column = getColumn(event);
                if (row >= 0 && column >= 0 && row < drawField.getRows() && column < drawField.getColumns()) {
                    if (cellMoveListener != null) {
                        cellMoveListener.onCellMove(row, column, drawField);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    private int getRow(MotionEvent event) {
        return (int) ((event.getY() - fieldRect.top) / cellSize);
    }

    private int getColumn(MotionEvent event) {
        return (int) ((event.getX() - fieldRect.left) / cellSize);
    }

    private final OnFieldChangedListener listener = (view) -> invalidate();
}
