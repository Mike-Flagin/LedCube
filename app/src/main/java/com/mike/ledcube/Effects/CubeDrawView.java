package com.mike.ledcube.Effects;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import com.mike.ledcube.R;
import com.mike.ledcube.databinding.CubeDrawLayoutBinding;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;

interface OnCellChangedListener {
    void onCellChanged(int layer, int row, int column, DrawField field);
}

interface OnFieldCleared {
    void onFieldCleared(int layer);
}

public class CubeDrawView extends ConstraintLayout implements View.OnClickListener, NumberPicker.OnValueChangeListener {
    private final Context context;
    private final AttributeSet attrs;
    private int defStyleAttr;
    private int defStyleRes;
    private CubeDrawLayoutBinding binding;
    private FragmentManager fragmentManager;

    public OnCellChangedListener cellChangedListener = null;
    public OnFieldCleared fieldClearedListener = null;
    private int currentColor;
    private int currentLayer;

    private int layers;
    private int rows;
    private int columns;

    private DrawField[] drawFields;

    public CubeDrawView(Context context) {
        super(context);
        this.context = context;
        attrs = null;
    }

    public CubeDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        defStyleRes = R.attr.drawFieldStyle;
        initAttributes();
    }

    public CubeDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        initAttributes();
    }

   /* public CubeDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        this.defStyleRes = defStyleRes;
        initAttributes();
    }*/

    private void initAttributes() {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.cube_draw_layout, this, true);
        binding = CubeDrawLayoutBinding.bind(this);
        int gridColor;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CubeDrawView, defStyleAttr, defStyleRes);
            gridColor = typedArray.getColor(R.styleable.CubeDrawView_cubeGridColor, Color.GRAY);
            typedArray.recycle();
        } else {
            gridColor = Color.GRAY;
        }
        binding.clearButton.setOnClickListener(this);
        binding.colorButton.setOnClickListener(this);
        binding.layerPicker.setOnValueChangedListener(this);
        binding.drawFieldView.setGridColor(gridColor);
        binding.drawFieldView.cellMoveListener = (row, column, field) -> {
            field.setCell(row, column, currentColor);
            if (cellChangedListener != null) {
                cellChangedListener.onCellChanged(currentLayer, row, column, field);
            }
        };
    }

    public void setDimensions(int layers, int rows, int columns, FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        this.layers = layers;
        this.rows = rows;
        this.columns = columns;
        currentLayer = 0;
        currentColor = Color.WHITE;
        drawFields = new DrawField[layers];
        for (int i = 0; i < layers; i++)
            drawFields[i] = new DrawField(rows, columns);
        binding.layerPicker.setMinValue(1);
        binding.layerPicker.setMaxValue(layers);
        binding.layerPicker.setValue(1);
        binding.colorButton.setBackgroundColor(currentColor);
        binding.drawFieldView.setDrawField(drawFields[0]);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.clearButton) {
            drawFields[currentLayer].clear();
            if (fieldClearedListener != null) {
                fieldClearedListener.onFieldCleared(currentLayer);
            }
        } else if (v == binding.colorButton) {
            int[] colorPallet = new int[16];
            for (int i = 0; i < colorPallet.length - 2; i++) {
                colorPallet[i] = Color.HSVToColor(new float[]{360f / (colorPallet.length - 2) * i, 1, 1});
            }
            colorPallet[colorPallet.length - 2] = Color.BLACK;
            colorPallet[colorPallet.length - 1] = Color.WHITE;
            new ColorPickerDialog()
                    .withAlphaEnabled(false)
                    .withPresets(colorPallet)
                    .withColor(currentColor)
                    .withCornerRadius(20)
                    .withListener((dialog, color) -> {
                        currentColor = color;
                        binding.colorButton.setBackgroundColor(color);
                    })
                    .show(fragmentManager, "drawColorPicker");
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        currentLayer = newVal - 1;
        binding.drawFieldView.setDrawField(drawFields[currentLayer]);
        invalidate();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.layers = layers;
        savedState.rows = rows;
        savedState.columns = columns;
        savedState.currentLayer = currentLayer;
        savedState.currentColor = currentColor;
        int[][][] colors = new int[layers][rows][columns];
        for (int i = 0; i < layers; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < columns; k++) {
                    colors[i][j][k] = drawFields[i].getCell(j, k);
                }
            }
        }
        savedState.colors = colors;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.layers = savedState.layers;
        this.rows = savedState.rows;
        this.columns = savedState.columns;
        this.currentLayer = savedState.currentLayer;
        this.currentColor = savedState.currentColor;
        drawFields = new DrawField[layers];
        for (int i = 0; i < layers; i++)
            drawFields[i] = new DrawField(rows, columns);
        for (int i = 0; i < layers; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < columns; k++) {
                    drawFields[i].setCell(j, k, savedState.colors[i][j][k], false);
                }
            }
        }
        binding.colorButton.setBackgroundColor(currentColor);
        binding.layerPicker.setValue(currentLayer + 1);
        binding.drawFieldView.setDrawField(drawFields[currentLayer]);
    }

    static class SavedState extends BaseSavedState {
        private int layers;
        private int rows;
        private int columns;
        private int currentLayer;
        private int currentColor;
        private int[][][] colors;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel parcel) {
            super(parcel);
            layers = parcel.readInt();
            rows = parcel.readInt();
            columns = parcel.readInt();
            currentLayer = parcel.readInt();
            currentColor = parcel.readInt();
            colors = new int[layers][rows][columns];
            for (int x = 0; x < layers; x++) {
                for (int y = 0; y < rows; y++) {
                    parcel.readIntArray(colors[x][y]);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(layers);
            out.writeInt(rows);
            out.writeInt(columns);
            out.writeInt(currentLayer);
            out.writeInt(currentColor);
            for (int x = 0; x < layers; x++) {
                for (int y = 0; y < rows; y++) {
                    out.writeIntArray(colors[x][y]);
                }
            }
        }

        public static final Creator<SavedState> CREATOR = new Creator<>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
