package com.example.mover;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.Range;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChart extends BarChart {
    public RoundedBarChart(Context context) {
        super(context);
    }

    public RoundedBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        readRadiusAttr(context, attrs);
    }

    public RoundedBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readRadiusAttr(context, attrs);
    }

    private void readRadiusAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundedBarChart, 0, 0);
        try {
            setRadius(a.getDimensionPixelSize(R.styleable.RoundedBarChart_radius, 0));
        } finally {
            a.recycle();
        }
    }

    public void setRadius(int radius) {
        setRenderer(new RoundedBarChartRenderer(this, getAnimator(), getViewPortHandler(), radius));
    }

    private class RoundedBarChartRenderer extends BarChartRenderer {
        private int mRadius;
        private RectF mBarShadowRectBuffer = new RectF();

        RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int mRadius) {
            super(chart, animator, viewPortHandler);
            this.mRadius = mRadius;
        }

        @Override
        public void drawHighlighted(Canvas c, Highlight[] indices) {
            if (indices == null || indices.length == 0 || c == null) return;
            BarData barData = mChart.getBarData();
            if (barData == null) return;

            for (Highlight high : indices) {
                if (high == null) continue;

                IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());
                if (set == null || !set.isHighlightEnabled())
                    continue;
                BarEntry e = set.getEntryForXValue(high.getX(), high.getY());
                if (!isInBoundsX(e, set))
                    continue;

                Transformer trans = mChart.getTransformer(set.getAxisDependency());
                mHighlightPaint.setColor(set.getHighLightColor());
                mHighlightPaint.setAlpha(set.getHighLightAlpha());
                boolean isStack = high.getStackIndex() >= 0 && e.isStacked();

                final float y1;
                final float y2;

                if (isStack) {
                    if (mChart.isHighlightFullBarEnabled()) {
                        y1 = e.getPositiveSum();
                        y2 = -e.getNegativeSum();
                    } else {
                        Range range = e.getRanges()[high.getStackIndex()];
                        y1 = range.from;
                        y2 = range.to;
                    }
                } else {
                    y1 = e.getY();
                    y2 = 0.f;
                }

                prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2f, trans);
                setHighlightDrawPos(high, mBarRect);
                c.drawRoundRect(mBarRect, mRadius, mRadius, mHighlightPaint);
            }
        }

        @Override
        protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
            if (c == null || dataSet == null || mBarBuffers == null || index >= mBarBuffers.length) {
                return;
            }

            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mBarBorderPaint.setColor(dataSet.getBarBorderColor());
            mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

            final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

            float phaseX = mAnimator.getPhaseX();
            float phaseY = mAnimator.getPhaseY();

            BarBuffer buffer = mBarBuffers[index];
            buffer.setPhases(phaseX, phaseY);
            buffer.setDataSet(index);
            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
            buffer.setBarWidth(mChart.getBarData().getBarWidth());

            buffer.feed(dataSet);
            trans.pointValuesToPixel(buffer.buffer);

            final boolean isSingleColor = dataSet.getColors().size() == 1;

            if (isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor());
            }

            for (int j = 0; j < buffer.size(); j += 4) {
                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (!isSingleColor) {
                    mRenderPaint.setColor(dataSet.getColor(j / 4));
                }

                float left = buffer.buffer[j];
                float top = buffer.buffer[j + 1];
                float right = buffer.buffer[j + 2];
                float bottom = buffer.buffer[j + 3];

                Path path = new Path();
                path.addRoundRect(
                        left, top, right, bottom,
                        new float[]{mRadius, mRadius, mRadius, mRadius, 0f, 0f, 0f, 0f},
                        Path.Direction.CW
                );

                c.drawPath(path, mRenderPaint);

                if (drawBorder) {
                    c.drawPath(path, mBarBorderPaint);
                }
            }
        }
    }
}