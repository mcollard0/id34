package pro.michaelcollard.id34.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    private final int horizontalSpacing;
    private final int verticalSpacing;
    public FlowLayout(Context context) {
        super(context);
        horizontalSpacing = dp(8);
        verticalSpacing = dp(8);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        horizontalSpacing = dp(8);
        verticalSpacing = dp(8);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        horizontalSpacing = dp(8);
        verticalSpacing = dp(8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int maxLineWidth = 0;
        int lineWidth = 0;
        int totalHeight = getPaddingTop() + getPaddingBottom();
        int lineHeight = 0;
        int availableWidth = Math.max(0, widthSize - getPaddingLeft() - getPaddingRight());
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            MarginLayoutParams lp = asMarginLayoutParams(child.getLayoutParams());
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            int neededWidth = lineWidth == 0 ? childWidth : lineWidth + horizontalSpacing + childWidth;

            if (widthMode != MeasureSpec.UNSPECIFIED && neededWidth > availableWidth && lineWidth > 0) {
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                totalHeight += lineHeight + verticalSpacing;
                lineWidth = childWidth;
                lineHeight = childHeight;
            } else {
                lineWidth = neededWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
        }
        maxLineWidth = Math.max(maxLineWidth, lineWidth);
        totalHeight += lineHeight;

        int measuredWidth = widthMode == MeasureSpec.EXACTLY
                ? widthSize
                : maxLineWidth + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int lineHeight = 0;
        int availableRight = (r - l) - getPaddingRight();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            MarginLayoutParams lp = asMarginLayoutParams(child.getLayoutParams());
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int nextRight = x + lp.leftMargin + childWidth + lp.rightMargin;

            if (nextRight > availableRight && x > getPaddingLeft()) {
                x = getPaddingLeft();
                y += lineHeight + verticalSpacing;
                lineHeight = 0;
            }

            int left = x + lp.leftMargin;
            int top = y + lp.topMargin;
            child.layout(left, top, left + childWidth, top + childHeight);

            x = left + childWidth + lp.rightMargin + horizontalSpacing;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    private MarginLayoutParams asMarginLayoutParams(LayoutParams params) {
        if (params instanceof MarginLayoutParams) {
            return (MarginLayoutParams) params;
        }
        return new MarginLayoutParams(params);
    }
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
